package com.example.happytravels.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.happytravels.models.TravelDestinationModel


class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

            companion object{
                private const val DATABASE_VERSION = 1
                private const val DATABASE_NAME ="HappyTravelsDatabase"
                private const val  TABLE_TRAVEL_DESTINATION = "TravelDestination"


                //All the Columns names
                private const val KEY_ID = "_id"
                private const val KEY_TITLE = "title"
                private const val KEY_LOCATION = "location"
                private const val KEY_LATITUDE = "latitude"
                private const val KEY_LONGITUDE = "longitude"
            }

     override fun onCreate(db: SQLiteDatabase?) {
         //creating table with fields
         val CREATE_TRAVEL_DESTINATION_TABLE = ("CREATE TABLE " + TABLE_TRAVEL_DESTINATION + "("
                 + KEY_ID + " INTEGER PRIMARY KEY,"
                 + KEY_TITLE + " TEXT,"
                 + KEY_LOCATION + " TEXT,"
                 + KEY_LATITUDE + " TEXT,"
                 + KEY_LONGITUDE + " TEXT)")
         db?.execSQL(CREATE_TRAVEL_DESTINATION_TABLE)
     }

     override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
         db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TRAVEL_DESTINATION")
         onCreate(db)
     }
     fun addDestination(destinationPlace: TravelDestinationModel): Long{
         val db = this.writableDatabase


         val contentValues = ContentValues()
         contentValues.put(KEY_TITLE, destinationPlace.title)
         contentValues.put(KEY_LOCATION, destinationPlace.location)
         contentValues.put(KEY_LATITUDE, destinationPlace.latitude)
         contentValues.put(KEY_LONGITUDE, destinationPlace.longitude)

         // Inserting Row
         val result = db.insert(TABLE_TRAVEL_DESTINATION, null, contentValues)
         //2nd argument is String containing nullColumnHack

         db.close() // Closing database connection
         return result
     }

    fun updateDestination(destinationPlace: TravelDestinationModel): Int{
        val db = this.writableDatabase


        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, destinationPlace.title)
        contentValues.put(KEY_LOCATION, destinationPlace.location)
        contentValues.put(KEY_LATITUDE, destinationPlace.latitude)
        contentValues.put(KEY_LONGITUDE, destinationPlace.longitude)

        val success = db.update(
            TABLE_TRAVEL_DESTINATION,
            contentValues,
            KEY_ID + "=" + destinationPlace.id, null)
        //2nd argument is String containing nullColumnHack

        db.close() // Closing database connection
        return success
    }

    fun deleteDestinationPlace(destinationPlace: TravelDestinationModel): Int{
        val db = this.writableDatabase
        val success = db.delete(
            TABLE_TRAVEL_DESTINATION,
            KEY_ID + "=" + destinationPlace.id,null)
        db.close()
        return success
    }

    fun getDestinationPlacesList():ArrayList<TravelDestinationModel>{
        val destinationList = ArrayList<TravelDestinationModel>()
        val selectQuery = "SELECT * FROM $TABLE_TRAVEL_DESTINATION"
        val db = this.readableDatabase

        try{
            val cursor: Cursor = db.rawQuery(selectQuery, null)

            if(cursor.moveToFirst()){
                do {
                    val place = TravelDestinationModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LONGITUDE))
                    )
                    destinationList.add(place)

                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e: SQLiteException){
            db.execSQL(selectQuery)
            return ArrayList()
        }

        return destinationList
    }
 }