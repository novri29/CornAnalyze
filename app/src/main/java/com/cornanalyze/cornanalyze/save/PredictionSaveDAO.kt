package com.cornanalyze.cornanalyze.save

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PredictionSaveDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionSave)

    @Query("SELECT * FROM save_prediction ORDER BY id DESC")
    suspend fun getALLPrediction(): List<PredictionSave>

    @Delete
    suspend fun deletePrediction(prediction: PredictionSave)

    @Query("SELECT * FROM save_prediction WHERE id = :id LIMIT 1")
    suspend fun getPredictionById(id: Long): PredictionSave?
}