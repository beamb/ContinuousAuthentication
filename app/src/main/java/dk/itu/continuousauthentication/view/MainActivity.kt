package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.model.PersonsDB

class MainActivity : AppCompatActivity() {
    private val EXTRA_LOCK =
        "dk.itu.continuousauthentication.view.lock"

    // GUI variables
    private lateinit var enrollButton: Button
    private lateinit var authButton: Button

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        personsDB = PersonsDB[this]

        enrollButton = findViewById(R.id.btn_enrollment)
        enrollButton.setOnClickListener {
            val intent = Intent(this, NameEnrollmentActivity::class.java)
            startActivity(intent)
        }

        authButton = findViewById(R.id.btn_authentication)
        authButton.setOnClickListener {
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
        }
        if (intent.hasExtra(EXTRA_LOCK) && intent.getBooleanExtra(EXTRA_LOCK, false)) {
            enrollButton.isEnabled = false
            authButton.isEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({ // This method will be executed once the timer is over
                enrollButton.isEnabled = true
                authButton.isEnabled = true
                Log.d("Delay", "resend1")
            }, 60000) // set time as per your requirement
        }
    }
}