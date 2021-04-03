package dk.itu.continuousauthentication.model.database

import android.database.Cursor
import android.database.CursorWrapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dk.itu.continuousauthentication.model.Person


class PersonCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {
    fun getPerson(): Person {
        val name = getString(getColumnIndex(PersonsDBSchema.PersonTable.Cols.NAME))
        val embeddings = getString(getColumnIndex(PersonsDBSchema.PersonTable.Cols.EMBEDDINGS))
        val movements = getString(getColumnIndex(PersonsDBSchema.PersonTable.Cols.MOVEMENTS))
        val person = Person(name)
        val type = object : TypeToken<ArrayList<FloatArray>>() {}.type
        val embeddingsList: ArrayList<FloatArray> = Gson().fromJson(embeddings, type)
        embeddingsList.forEach {
            person.addEmbeddings(it)
        }
        person.addMovement(movements)
        return person
    }
}