package com.example.imageencoder.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.imageencoder.entity.Image

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(image: Image)

    @Insert
    suspend fun insertAllImages(images: List<Image>)

    @Query("SELECT * FROM images_table")
    suspend fun getImages(): List<Image>

    @Query("DELETE FROM images_table")
    suspend fun deleteAll()
}