package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.*
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodCatalogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FoodCatalogEntity): Long

    @Update
    suspend fun update(entity: FoodCatalogEntity)

    @Delete
    suspend fun delete(entity: FoodCatalogEntity)

    @Query("SELECT * FROM food_catalog ORDER BY name ASC")
    fun getAll(): Flow<List<FoodCatalogEntity>>

    /** Búsqueda parcial por nombre (case-insensitive). */
    @Query("SELECT * FROM food_catalog WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<FoodCatalogEntity>>

    @Query("SELECT * FROM food_catalog WHERE foodType = :foodType ORDER BY name ASC")
    fun getByType(foodType: String): Flow<List<FoodCatalogEntity>>
}

