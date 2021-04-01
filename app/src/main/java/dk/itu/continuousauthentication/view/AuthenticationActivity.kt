package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.controller.FaceDetector
import dk.itu.continuousauthentication.controller.MovementClassifier
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.utils.Frame
import dk.itu.continuousauthentication.utils.LensFacing

class AuthenticationActivity: AppCompatActivity() {
    // GUI variables
    private lateinit var viewfinder: CameraView
    private lateinit var authBtn: Button

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    //Controller: Face Detector
    private lateinit var faceDetector: FaceDetector
    private lateinit var movementClassifier: MovementClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        personsDB = PersonsDB[this]
        faceDetector = FaceDetector[this]
        val lensFacing =
            savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.FRONT
        viewfinder = findViewById(R.id.auth_camera_view)
        faceDetector.setIsAuthenticating(true)
        faceDetector.setStartAuthentication(true)
        //viewfinder.isVisible = false
        setupCamera(lensFacing)

        authBtn = findViewById(R.id.btn_authenticate)
        authBtn.setOnClickListener {
            Log.i("FaceRecognition", "Input: ${faceDetector.displayInput()}")
            faceDetector.setStartAuthentication(false)
            var result = faceDetector.checkInput()
            faceDetector.resetMovementClassifier()
            if (result) {
                faceDetector.setIsAuthenticating(false)
                val intent = Intent(this, AppEntryActivity::class.java)
                startActivity(intent)
            } else {
                val toast = Toast.makeText(
                    this,
                    resources.getString(R.string.msg_incorrect),
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                faceDetector.setIsAuthenticating(true)
                faceDetector.setStartAuthentication(true)
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
            if (faceDetector.getUnknownFaceStatus()) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val KEY_LENS_FACING = "key-lens-facing"
    }

    //TODO: Lock out user after too many failed attempts - Pass being locked out as an extra
    //TODO: Block unknown user from authenticating
    //TODO: Find a way to hide the camera view (maybe)
}