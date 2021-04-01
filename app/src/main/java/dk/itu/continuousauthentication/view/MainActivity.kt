package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dk.itu.continuousauthentication.model.PersonsDB
import dk.itu.continuousauthentication.R

class MainActivity : AppCompatActivity() {
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
        enrollButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, NameEnrollmentActivity::class.java)
            startActivity(intent)})
        authButton = findViewById(R.id.btn_authentication)
        authButton.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, AuthenticationActivity::class.java)
            startActivity(intent)
        })
    }
}