package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.Facing
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.controller.FaceDetector
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.utils.Frame
import dk.itu.continuousauthentication.utils.LensFacing
import java.util.*


class EnrollmentActivity : AppCompatActivity(), Observer {

    private val EXTRA_NAME =
        "dk.itu.continuousauthentication.view.name"

    private lateinit var name: String
    private lateinit var builder: AlertDialog.Builder

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
        faceDetector = FaceDetector()
        faceDetector.addObserver(this)
        name = intent.getStringExtra(EXTRA_NAME).toString()
        builder = AlertDialog.Builder(this)
        val lensFacing =
            savedInstanceState?.getSerializable(KEY_LENS_FACING) as Facing? ?: Facing.FRONT
        viewfinder = findViewById(R.id.viewfinder)
        faceDetector.setIsEnrolling(true)
        setupCamera(lensFacing)

        doneBtn = findViewById(R.id.btn_done)
        doneBtn.setOnClickListener {
            if (faceDetector.getFinishedEnrollmentStatus()) {
                faceDetector.setIsEnrolling(false)
                faceDetector.close()
                viewfinder.destroy()
                val intent = Intent(this, MainActivity::class.java)
                finish()
                startActivity(intent)
            }
        }

        resetBtn = findViewById(R.id.btn_reset)
        resetBtn.setOnClickListener {
            val person = personsDB.getPerson(name)
            person.resetMovements()
            personsDB.add(person)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_enrollment, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_hint -> {
                if (personsDB.getPerson(name).movements.isNotEmpty()) {
                    builder.setMessage(
                        "Your movements are: \n " +
                                personsDB.getPerson(name).listMovements().map {
                                    "\n" + it
                                }.reduce { acc, string -> acc + string }
                    )
                        .setCancelable(false)
                        .setPositiveButton(
                            "Close"
                        ) { dialog, _ ->
                            dialog.cancel()
                        }
                } else {
                    builder.setMessage(
                        "You have not yet enrolled any movements!"
                    )
                        .setCancelable(false)
                        .setPositiveButton(
                            "Close"
                        ) { dialog, _ ->
                            dialog.cancel()
                        }
                }
                val alert = builder.create()
                alert.setTitle("$name's movements")
                alert.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun update(observable: Observable?, data: Any?) {
        if (faceDetector.getUnknownFaceStatus()) {
            Log.i("Recognize", "Update was called")
            faceDetector.close()
            viewfinder.destroy()
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupCamera(lensFacing: Facing) {
        viewfinder.facing = lensFacing
        viewfinder.addFrameProcessor {
            if (it != null) {
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
    }

    companion object {
        private const val KEY_LENS_FACING = "key-lens-facing"
    }
}