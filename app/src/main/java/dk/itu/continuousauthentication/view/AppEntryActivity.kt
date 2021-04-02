package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.util.Size
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.controller.FaceDetector
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.utils.Frame
import dk.itu.continuousauthentication.utils.LensFacing
import java.util.*

class AppEntryActivity: AppCompatActivity(), Observer {
    // GUI variables
    private lateinit var viewfinder: CameraView
    private lateinit var homeBtn: Button
    private lateinit var decisionBtn: Button

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    //Controller: Face Detector
    private lateinit var faceDetector: FaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appentry)
        personsDB = PersonsDB[this]
        faceDetector = FaceDetector()
        faceDetector.addObserver(this)

        val lensFacing =
            savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.FRONT
        viewfinder = findViewById(R.id.app_camera_view)
        setupCamera(lensFacing)

        homeBtn = findViewById(R.id.btn_home)
        homeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        decisionBtn = findViewById(R.id.btn_decision)
        decisionBtn.setOnClickListener {
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
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
            faceDetector.setUnknownFaceStatus(false)
            faceDetector.close()
            viewfinder.destroy()
            val intent = Intent(this, MainActivity::class.java)
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
                ), this, "unknown")
            if (faceDetector.getUnknownFaceStatus()) {
                faceDetector.setUnknownFaceStatus(false)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val KEY_LENS_FACING = "key-lens-facing"
    }
    // TODO: Don't allow user to leave screen
}