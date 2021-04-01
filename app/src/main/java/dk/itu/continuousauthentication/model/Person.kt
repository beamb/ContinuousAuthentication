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

}