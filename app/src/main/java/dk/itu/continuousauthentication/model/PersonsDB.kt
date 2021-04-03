package dk.itu.continuousauthentication.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.gson.Gson
import dk.itu.continuousauthentication.database.PersonBaseHelper
import dk.itu.continuousauthentication.database.PersonCursorWrapper
import dk.itu.continuousauthentication.database.PersonsDBSchema

class PersonsDB private constructor(context: Context) {

    fun getPersonsDB(): ArrayList<Person> {
        val persons = ArrayList<Person>()
        val cursor = queryItems(null, null)
        cursor?.moveToFirst()
        if (cursor != null) {
            while (!cursor.isAfterLast) {
                persons.add(cursor.getPerson())
                cursor.moveToNext()
            }
        }
        cursor?.close()
        return persons
    }

    fun getPerson(name: String): Person {
        getPersonsDB().forEach {
            if (it.name == name) {
                return it
            }
        }
        return Person("unknown")
    }

    fun add(person: Person) {
        val values = getContentValues(person)
        if (!getPersonsDB().contains(person)) {
            mDatabase.insert(PersonsDBSchema.PersonTable.NAME, null, values)
            Log.i("PersonsDB", "Adding ${person.name} with embeddings of size ${person.embeddings.size} and the movements ${person.movements}")
        } else {
            mDatabase.update(PersonsDBSchema.PersonTable.NAME, values, null, null)
            Log.i("PersonsDB", "Updating ${person.name} with embeddings of size ${person.embeddings.size} and the movements ${person.movements}")
        }
    }

    fun contains(person: Person): Boolean {
        return getPersonsDB().contains(person)
    }

    fun isNotEmpty(): Boolean {
        return getPersonsDB().isNotEmpty()
    }

    // Database helper methods to convert between Items and database rows
    private fun getContentValues(person: Person): ContentValues {
        val values = ContentValues()
        values.put(PersonsDBSchema.PersonTable.Cols.NAME, person.name)
        values.put(PersonsDBSchema.PersonTable.Cols.EMBEDDINGS, Gson().toJson(person.embeddings))
        values.put(PersonsDBSchema.PersonTable.Cols.MOVEMENTS, person.movements.toString())
        return values
    }

    private fun queryItems(
        whereClause: String?,
        whereArgs: Array<String>?
    ): PersonCursorWrapper? {
        val cursor: Cursor = mDatabase.query(
            PersonsDBSchema.PersonTable.NAME,
            null,  // Columns - null selects all columns
            whereClause, whereArgs,
            null,  // groupBy
            null,  // having
            null // orderBy
        )
        return PersonCursorWrapper(cursor)
    }

    companion object {
        private lateinit var sPersonsDB: PersonsDB
        private lateinit var mDatabase: SQLiteDatabase

        operator fun get(context: Context): PersonsDB {
            if (!::sPersonsDB.isInitialized) {
                mDatabase = PersonBaseHelper(context.applicationContext).writableDatabase
                sPersonsDB = PersonsDB(context)
            }
            return sPersonsDB
        }
    }
}


