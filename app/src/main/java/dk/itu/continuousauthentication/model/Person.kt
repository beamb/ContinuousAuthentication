package dk.itu.continuousauthentication.model

import java.lang.StringBuilder

class Person(val name: String) { // person could have e FloatBuffer instead and then use .equals()

    val embeddings: ArrayList<FloatArray> = ArrayList()
    val movements: StringBuilder = StringBuilder()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Person

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + embeddings.hashCode()
        result = 31 * result + movements.hashCode()
        return result
    }

    fun addEmbeddings(embedding: FloatArray) {
        embeddings.add(embedding)
    }

    fun addMovement(movement: String) {
        movements.append(movement)
    }

    fun resetMovements() {
        movements.clear()
    }

    fun listMovements(): ArrayList<String> {
        val movementStrings = ArrayList<String>()
        var i = 0
        while (i < movements.length) {
            when {
                movements.substring(i, i + 1) == "0" -> {
                    movementStrings.add("Facing down")
                }
                movements.substring(i, i + 1) == "1" -> {
                    movementStrings.add("Facing left")
                }
                movements.substring(i, i + 1) == "2" -> {
                    movementStrings.add("Facing right")
                }
                movements.substring(i, i + 1) == "3" -> {
                    movementStrings.add("Facing up")
                }
                movements.substring(i, i + 1) == "4" -> {
                    movementStrings.add("Smile")
                }
                movements.substring(i, i + 1) == "5" -> {
                    movementStrings.add("Left wink")
                }
                movements.substring(i, i + 1) == "6" -> {
                    movementStrings.add("Right wink")
                }
                movements.substring(i, i + 1) == "7" -> {
                    movementStrings.add("Both eyes closed")
                }
                movements.substring(i, i + 1) == "8" -> {
                    movementStrings.add("Left tilt")
                }
                movements.substring(i, i + 1) == "9" -> {
                    movementStrings.add("Right tilt")
                }
            }
            i++
        }
        return movementStrings
    }

}