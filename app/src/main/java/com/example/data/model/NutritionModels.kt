package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val ptName: String, val defaultHour: String) {
    BREAKFAST("Café da Manhã", "08:00"),
    LUNCH("Almoço", "12:30"),
    SNACK("Lanche", "16:30"),
    DINNER("Jantar", "20:00")
}

@Entity(tableName = "foods")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Proteínas", "Carboidratos", "Frutas", "Verduras", "Laticínios", "Pratos Prontos", "Doces/Outros"
    val servingSizeGrams: Int = 100,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val isCustom: Boolean = false
)

@Entity(tableName = "meals")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val foodName: String,
    val mealType: MealType,
    val portionGrams: Int,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val dateString: String, // "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis(),
    val isAiDetected: Boolean = false,
    val notes: String? = null
)

@Entity(tableName = "water_logs")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 1,
    val dateString: String,
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String = "",
    val isAdmin: Boolean = false,
    val calorieGoal: Int = 2000,
    val proteinGoalGrams: Int = 140,
    val carbGoalGrams: Int = 220,
    val fatGoalGrams: Int = 60,
    val waterGoalMl: Int = 2500,
    val currentWeightKg: Float = 72.5f,
    val targetWeightKg: Float = 68.0f,
    val streakDays: Int = 5,
    val isActive: Boolean = true,
    val registrationDate: String = "2026-09-01"
)

data class AiFoodIngredient(
    val name: String,
    val grams: Int,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float
)

data class AiFoodRecognitionResult(
    val dishTitle: String,
    val confidenceScore: Float,
    val estimatedWeightGrams: Int,
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float,
    val healthTip: String,
    val ingredients: List<AiFoodIngredient> = emptyList()
)

data class DailyMacroSummary(
    val totalCalories: Int = 0,
    val totalCarbs: Float = 0f,
    val totalProtein: Float = 0f,
    val totalFat: Float = 0f,
    val totalFiber: Float = 0f,
    val totalWaterMl: Int = 0,
    val mealsCount: Int = 0
)
