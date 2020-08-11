package com.example.imageencoder.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.imageencoder.dao.ImageDao
import com.example.imageencoder.entity.Image

@Database(entities = [Image::class], version = 1)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        private var instance: ImageDatabase? = null
        fun getImageDatabase(context: Context): ImageDatabase? {
            if (instance == null) {
                synchronized(ImageDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ImageDatabase::class.java,
                        "ImageDB"
                    ).build()
                }
            }
            return instance
        }
    }
}