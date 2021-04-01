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
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.controller.FaceDetector
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.utils.Frame
import dk.itu.continuousauthentication.utils.LensFacing

class EnrollmentActivity : AppCompatActivity() {

    private val EXTRA_NAME =
        "dk.itu.continuousauthentication.view.name"

    private lateinit var name: String

    // GUI variables
    private lateinit var viewfinder: CameraView
    private lateinit var resetBtn: Button
    private lateinit var doneBtn: Button

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    //Controller: Face Detector
    private lateinit var faceDetector: FaceDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enrollment)
        personsDB = PersonsDB[this]
        faceDetector = FaceDetector[this]
        name = intent.getStringExtra(EXTRA_NAME).toString()
        val lensFacing =
            savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.FRONT
        viewfinder = findViewById(R.id.viewfinder)
        faceDetector.setIsEnrolling(true)
        setupCamera(lensFacing)

        doneBtn = findViewById(R.id.btn_done)
        doneBtn.setOnClickListener {
            Log.i("Roman", "Person: ${personsDB.getPerson(name)} and ArrayList of FloatArray: ${personsDB.getPerson(name).embeddings.toString()}")
            if (faceDetector.getFinishedEnrollmentStatus()) {
                faceDetector.setIsEnrolling(false)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        resetBtn = findViewById(R.id.btn_reset)
        resetBtn.setOnClickListener {
            personsDB.getPerson(name).resetMovements()
            val toast = Toast.makeText(
                this,
                resources.getString(R.string.msg_reset),
                Toast.LENGTH_SHORT
            )
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
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
                ), this, name
            )
        }
    }

    companion object {
        private const val KEY_LENS_FACING = "key-lens-facing"
    }

    //TODO: If Person already exists, and they have movements - Bitmaps should only be triggered by the stored movements and not any movement
}