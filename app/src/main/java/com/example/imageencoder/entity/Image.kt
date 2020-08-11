package com.example.imageencoder.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images_table")
data class Image(
    val imageBase64: String
) {
    @PrimaryKey(autoGenerate = true)
    var imageId: Int = 0
}