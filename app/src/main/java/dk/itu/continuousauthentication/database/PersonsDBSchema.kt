package dk.itu.continuousauthentication.database

class PersonsDBSchema {
    object PersonTable {
        const val NAME = "Persons"

        object Cols {
            const val NAME = "name"
            const val EMBEDDINGS = "embeddings"
            const val MOVEMENTS = "movements"
        }
    }
}