package dk.itu.continuousauthentication.controller

import android.util.Log
import dk.itu.continuousauthentication.model.Person
import java.lang.StringBuilder

class MovementClassifier private constructor(val person: Person) {

    private val inputs: StringBuilder = StringBuilder()

    fun addInput(movement: String) {
        inputs.append(movement)
        Log.i("MovementClassifier", "Input size: ${inputs.length} and person: ${person.name}")
        Log.i("MovementClassifier", "Input: $inputs and person movements length: ${person.movements.length}")
        Log.i("MovementClassifier", "Input: $inputs and person movements: ${person.movements}")
    }

    fun checkInput(): Boolean {
        val secret = person.movements.toString()
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
        operator fun get(person: Person): MovementClassifier {
            if (!::sMovementClassifier.isInitialized) sMovementClassifier = MovementClassifier(person)
            return sMovementClassifier
        }
    }
}