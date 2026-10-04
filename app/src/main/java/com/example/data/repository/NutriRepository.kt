package com.example.data.repository

import com.example.data.local.FoodDao
import com.example.data.local.MealDao
import com.example.data.local.UserDao
import com.example.data.local.WaterDao
import com.example.data.model.DailyMacroSummary
import com.example.data.model.FoodItemEntity
import com.example.data.model.MealEntryEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.WaterEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class NutriRepository(
    private val foodDao: FoodDao,
    private val mealDao: MealDao,
    private val waterDao: WaterDao,
    private val userDao: UserDao
) {
    // Food Catalog
    val allFoods: Flow<List<FoodItemEntity>> = foodDao.getAllFoods()

    fun searchFoods(query: String): Flow<List<FoodItemEntity>> {
        return if (query.isBlank()) {
            foodDao.getAllFoods()
        } else {
            foodDao.searchFoods(query)
        }
    }

    suspend fun insertFood(food: FoodItemEntity): Long = foodDao.insertFood(food)
    suspend fun deleteFood(food: FoodItemEntity) = foodDao.deleteFood(food)

    // Meals
    fun getMealsForDate(dateString: String, userId: Long): Flow<List<MealEntryEntity>> =
        mealDao.getMealsForDate(dateString, userId)

    fun getAllMeals(): Flow<List<MealEntryEntity>> = mealDao.getAllMeals()

    suspend fun insertMeal(meal: MealEntryEntity): Long = mealDao.insertMeal(meal)
    suspend fun deleteMealById(id: Long) = mealDao.deleteMealById(id)

    // Water
    fun getWaterLogsForDate(dateString: String, userId: Long): Flow<List<WaterEntryEntity>> =
        waterDao.getWaterLogsForDate(dateString, userId)

    fun getTotalWaterForDate(dateString: String, userId: Long): Flow<Int> =
        waterDao.getWaterLogsForDate(dateString, userId).map { logs ->
            logs.sumOf { it.amountMl }
        }

    suspend fun addWater(amountMl: Int, dateString: String, userId: Long): Long =
        waterDao.insertWater(
            WaterEntryEntity(
                userId = userId,
                dateString = dateString,
                amountMl = amountMl
            )
        )

    suspend fun resetWater(dateString: String, userId: Long) =
        waterDao.clearWaterForDate(dateString, userId)

    // Daily Summary
    fun getDailySummary(dateString: String, userId: Long): Flow<DailyMacroSummary> {
        val mealsFlow = mealDao.getMealsForDate(dateString, userId)
        val waterFlow = getTotalWaterForDate(dateString, userId)

        return combine(mealsFlow, waterFlow) { meals, waterMl ->
            var cal = 0
            var carb = 0f
            var prot = 0f
            var fat = 0f
            var fiber = 0f

            for (m in meals) {
                cal += m.calories
                carb += m.carbs
                prot += m.protein
                fat += m.fat
                fiber += m.fiber
            }

            DailyMacroSummary(
                totalCalories = cal,
                totalCarbs = carb,
                totalProtein = prot,
                totalFat = fat,
                totalFiber = fiber,
                totalWaterMl = waterMl,
                mealsCount = meals.size
            )
        }
    }

    // Users & Admin
    val allUsers: Flow<List<UserProfileEntity>> = userDao.getAllUsers()

    fun getUser(userId: Long): Flow<UserProfileEntity?> = userDao.getUserById(userId)

    suspend fun getUserOnce(userId: Long): UserProfileEntity? = userDao.getUserByIdOnce(userId)

    suspend fun insertUser(user: UserProfileEntity): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserProfileEntity) = userDao.updateUser(user)

    suspend fun deleteUser(user: UserProfileEntity) = userDao.deleteUser(user)
}
