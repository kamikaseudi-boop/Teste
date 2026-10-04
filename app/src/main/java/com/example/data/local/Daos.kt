package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FoodItemEntity
import com.example.data.model.MealEntryEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WaterEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoods(query: String): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: String): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFoods(foods: List<FoodItemEntity>)

    @Delete
    suspend fun deleteFood(food: FoodItemEntity)

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE dateString = :dateString AND userId = :userId ORDER BY timestamp ASC")
    fun getMealsForDate(dateString: String, userId: Long = 1): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meals ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY timestamp DESC LIMIT 50")
    fun getRecentMeals(userId: Long): Flow<List<MealEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntryEntity): Long

    @Delete
    suspend fun deleteMeal(meal: MealEntryEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("SELECT COUNT(*) FROM meals WHERE dateString = :dateString")
    suspend fun getMealCountForDate(dateString: String): Int
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_logs WHERE dateString = :dateString AND userId = :userId ORDER BY timestamp ASC")
    fun getWaterLogsForDate(dateString: String, userId: Long = 1): Flow<List<WaterEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: WaterEntryEntity): Long

    @Query("DELETE FROM water_logs WHERE dateString = :dateString AND userId = :userId")
    suspend fun clearWaterForDate(dateString: String, userId: Long = 1)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<UserProfileEntity>)

    @Update
    suspend fun updateUser(user: UserProfileEntity)

    @Delete
    suspend fun deleteUser(user: UserProfileEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
