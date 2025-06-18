package com.cornanalyze.cornanalyze.save

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_prediction")
data class PredictionSave(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val image: String,
    val result: String,
    val description: String,
    val cause: String,
    val advice: String,
    val source: String,
    val date: String
)