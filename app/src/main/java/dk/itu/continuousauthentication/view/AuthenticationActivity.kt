package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.controller.FaceDetector
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.utils.Frame
import dk.itu.continuousauthentication.utils.LensFacing
import java.util.*

class AuthenticationActivity : AppCompatActivity(), Observer {
    private val EXTRA_NAME =
        "dk.itu.continuousauthentication.view.name"
    private val EXTRA_LOCK =
        "dk.itu.continuousauthentication.view.lock"

    // GUI variables
    private lateinit var viewfinder: CameraView
    private lateinit var authBtn: Button

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    //Controller: Face Detector
    private lateinit var faceDetector: FaceDetector

    private lateinit var name: String
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        personsDB = PersonsDB[this]
        faceDetector = FaceDetector()
        faceDetector.addObserver(this)
        if (intent.hasExtra(EXTRA_NAME)) name = intent.getStringExtra(EXTRA_NAME).toString()
        val lensFacing =
            savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.FRONT
        viewfinder = findViewById(R.id.auth_camera_view)
        faceDetector.setIsAuthenticating(true)
        faceDetector.setStartAuthentication(true)
        setupCamera(lensFacing)

        authBtn = findViewById(R.id.btn_authenticate)
        authBtn.setOnClickListener {
            Log.i("FaceRecognition", "Input: ${faceDetector.displayInput()}")
            faceDetector.setStartAuthentication(false)
            val result = faceDetector.checkInput()
            faceDetector.resetMovementClassifier()
            if (result) {
                faceDetector.setIsAuthenticating(false)
                if (::name.isInitialized) {
                    if (faceDetector.getIdentifiedPerson().name == name) {
                        faceDetector.close()
                        viewfinder.destroy()
                        val intent = Intent(this, EnrollmentActivity::class.java)
                        intent.putExtra(EXTRA_NAME, name)
                        finish()
                        startActivity(intent)
                    } else {
                        val toast = Toast.makeText(
                            this,
                            "Sorry, you don't seem to be $name",
                            Toast.LENGTH_SHORT
                        )
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        Log.i("Recognize", "Sorry, you don't seem to be $name")
                        faceDetector.close()
                        viewfinder.destroy()
                        val intent = Intent(this, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                    }
                } else {
                    Log.i("Recognize", "Gained access to the app")
                    faceDetector.close()
                    viewfinder.destroy()
                    val intent = Intent(this, AppEntryActivity::class.java)
                    finish()
                    startActivity(intent)
                }
            } else {
                counter++
                if (counter < 3) {
                    val toast = Toast.makeText(
                        this,
                        resources.getString(R.string.msg_incorrect) + " Attempt $counter/3",
                        Toast.LENGTH_SHORT
                    )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    faceDetector.setIsAuthenticating(true)
                    faceDetector.setStartAuthentication(true)
                } else {
                    faceDetector.setIsAuthenticating(false)
                    faceDetector.setStartAuthentication(false)
                    faceDetector.setUnknownFaceStatus(false)
                    val toast = Toast.makeText(
                        this,
                        resources.getString(R.string.msg_timeout) + " Attempt $counter/3",
                        Toast.LENGTH_SHORT
                    )
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    Log.i("Recgonize", "Too many failed attempts")
                    faceDetector.close()
                    viewfinder.destroy()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(EXTRA_LOCK, true)
                    finish()
                    startActivity(intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewfinder.start()
    }

    override fun onPause() {
        super.onPause()
        viewfinder.stop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(KEY_LENS_FACING, viewfinder.facing)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewfinder.destroy()
    }

    override fun update(observable: Observable?, data: Any?) {
        if (faceDetector.getUnknownFaceStatus()) {
            Log.i("Recognize", "Update was called")
            faceDetector.setIsAuthenticating(false)
            faceDetector.setStartAuthentication(false)
            faceDetector.setUnknownFaceStatus(false)
            faceDetector.close()
            viewfinder.destroy()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_LOCK, true)
            finish()
            startActivity(intent)
        }
    }

    private fun setupCamera(lensFacing: Facing) {
        viewfinder.facing = lensFacing
        viewfinder.addFrameProcessor {
            faceDetector.process(
                Frame(
                    data = it.data,
                    rotation = it.rotation,
                    size = Size(it.size.width, it.size.height),
                    format = it.format,
                    lensFacing = if (viewfinder.facing == Facing.BACK) LensFacing.BACK else LensFacing.FRONT
                ), this, "unknown"
            )
        }
    }

    companion object {
        private const val KEY_LENS_FACING = "key-lens-facing"
    }
}