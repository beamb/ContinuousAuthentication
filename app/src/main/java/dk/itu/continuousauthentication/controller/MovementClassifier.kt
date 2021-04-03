package dk.itu.continuousauthentication.controller

import android.content.Context
import android.util.Log
import dk.itu.continuousauthentication.model.Person
import dk.itu.continuousauthentication.model.PersonsDB
import java.lang.StringBuilder

class MovementClassifier private constructor(val person: Person) {

    private val inputs: StringBuilder = StringBuilder()

    fun addInput(movement: String) {
        inputs.append(movement)
        Log.i("MovementClassifier", "Input size: ${inputs.length} and person: ${person.name}")
        Log.i(
            "MovementClassifier",
            "Input: $inputs and person movements length: ${personsDB.getPerson(person.name).movements.length}"
        )
        Log.i(
            "MovementClassifier",
            "Input: $inputs and person movements: ${personsDB.getPerson(person.name).movements}"
        )
    }

    fun checkInput(): Boolean {
        val secret = personsDB.getPerson(person.name).movements.toString()
        val input = inputs.toString()
        return input == secret
    }

    fun resetInput() {
        inputs.clear()
    }

    fun getInput(): String {
        return inputs.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MovementClassifier

        if (person != other.person) return false
        if (inputs != other.inputs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = person.hashCode()
        result = 31 * result + inputs.hashCode()
        return result
    }

    companion object {
        private lateinit var sMovementClassifier: MovementClassifier
        private lateinit var personsDB: PersonsDB

        operator fun get(context: Context, person: Person): MovementClassifier {
            if (!::sMovementClassifier.isInitialized || sMovementClassifier.person.name != person.name) {
                personsDB = PersonsDB[context]
                sMovementClassifier = MovementClassifier(person)
            }
            return sMovementClassifier
        }
    }
}