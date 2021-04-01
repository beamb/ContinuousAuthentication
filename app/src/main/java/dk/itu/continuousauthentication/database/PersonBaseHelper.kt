package dk.itu.continuousauthentication.database

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
            "create table " + PersonsDBSchema.PersonTable.NAME.toString() + "(" +
                    PersonsDBSchema.PersonTable.Cols.NAME.toString() + ", " + PersonsDBSchema.PersonTable.Cols.EMBEDDINGS.toString() + ", " + PersonsDBSchema.PersonTable.Cols.MOVEMENTS.toString() + ")"
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