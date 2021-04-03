package dk.itu.continuousauthentication.model.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PersonBaseHelper(context: Context?) :
    SQLiteOpenHelper(
        context,
        DATABASE_NAME,
        null,
        VERSION
    ) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table " + PersonsDBSchema.PersonTable.NAME + "(" +
                    PersonsDBSchema.PersonTable.Cols.NAME + ", " + PersonsDBSchema.PersonTable.Cols.EMBEDDINGS + ", " + PersonsDBSchema.PersonTable.Cols.MOVEMENTS + ")"
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }

    companion object {
        private const val VERSION = 1
        const val DATABASE_NAME = "persons.db"
    }
}