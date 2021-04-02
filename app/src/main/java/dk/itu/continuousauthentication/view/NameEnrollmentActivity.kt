package dk.itu.continuousauthentication.view

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dk.itu.continuousauthentication.R
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.model.PersonsDB

class NameEnrollmentActivity : AppCompatActivity() {
    private val EXTRA_NAME =
        "dk.itu.continuousauthentication.view.name"

    // GUI variables
    private lateinit var registerBtn: Button
    private lateinit var etName: EditText

    // Model: Database of persons
    private lateinit var personsDB: PersonsDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_enroll)
        personsDB = PersonsDB[this]
        etName = findViewById(R.id.et_name)
        registerBtn = findViewById(R.id.btn_register)
        registerBtn.setOnClickListener(View.OnClickListener {
            val name = etName.text.toString().trim()
            if (name != "unknown" && name != "") {
                if (!personsDB.contains(Person(name))) {
                    val person = Person(name)
                    personsDB.add(person)
                    val intent = Intent(this, EnrollmentActivity::class.java)
                    intent.putExtra(EXTRA_NAME, name)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, AuthenticationActivity::class.java)
                    intent.putExtra(EXTRA_NAME, name)
                    startActivity(intent)
                }
            } else {
                val toast = Toast.makeText(
                    this,
                    resources.getString(R.string.msg_input_name),
                    Toast.LENGTH_SHORT
                )
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
            }
        })
    }
}