package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.FoodItemEntity
import com.example.data.model.MealEntryEntity
import com.example.data.model.MealType
import com.example.data.model.UserProfileEntity
import com.example.data.model.WaterEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromMealType(value: MealType): String = value.name

    @TypeConverter
    fun toMealType(value: String): MealType = try {
        MealType.valueOf(value)
    } catch (e: Exception) {
        MealType.LUNCH
    }
}

@Database(
    entities = [
        FoodItemEntity::class,
        MealEntryEntity::class,
        WaterEntryEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun mealDao(): MealDao
    abstract fun waterDao(): WaterDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nutri_ia_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val foodDao = database.foodDao()
            val userDao = database.userDao()
            val mealDao = database.mealDao()
            val waterDao = database.waterDao()

            if (foodDao.getFoodCount() == 0) {
                val initialFoods = listOf(
                    FoodItemEntity(name = "Arroz Branco Cozido", category = "Carboidratos", servingSizeGrams = 100, calories = 130, carbs = 28.2f, protein = 2.7f, fat = 0.3f, fiber = 0.4f),
                    FoodItemEntity(name = "Arroz Integral Cozido", category = "Carboidratos", servingSizeGrams = 100, calories = 124, carbs = 25.8f, protein = 2.6f, fat = 1.0f, fiber = 2.7f),
                    FoodItemEntity(name = "Feijão Carioca Cozido", category = "Proteínas", servingSizeGrams = 100, calories = 76, carbs = 13.6f, protein = 4.8f, fat = 0.5f, fiber = 8.5f),
                    FoodItemEntity(name = "Feijão Preto Cozido", category = "Proteínas", servingSizeGrams = 100, calories = 77, carbs = 14.0f, protein = 4.5f, fat = 0.5f, fiber = 8.4f),
                    FoodItemEntity(name = "Peito de Frango Grelhado", category = "Proteínas", servingSizeGrams = 100, calories = 159, carbs = 0.0f, protein = 32.0f, fat = 3.2f, fiber = 0.0f),
                    FoodItemEntity(name = "Patinho Bovino Grelhado", category = "Proteínas", servingSizeGrams = 100, calories = 219, carbs = 0.0f, protein = 35.9f, fat = 7.3f, fiber = 0.0f),
                    FoodItemEntity(name = "Ovo Cozido Inteiro", category = "Proteínas", servingSizeGrams = 50, calories = 78, carbs = 0.6f, protein = 6.3f, fat = 5.3f, fiber = 0.0f),
                    FoodItemEntity(name = "Ovo Mexido com Azeite", category = "Proteínas", servingSizeGrams = 100, calories = 160, carbs = 1.2f, protein = 13.0f, fat = 11.5f, fiber = 0.0f),
                    FoodItemEntity(name = "Salmão Grelhado", category = "Proteínas", servingSizeGrams = 100, calories = 206, carbs = 0.0f, protein = 22.0f, fat = 12.0f, fiber = 0.0f),
                    FoodItemEntity(name = "Pão Francês", category = "Carboidratos", servingSizeGrams = 50, calories = 150, carbs = 29.3f, protein = 4.0f, fat = 1.5f, fiber = 1.1f),
                    FoodItemEntity(name = "Tapioca (Goma)", category = "Carboidratos", servingSizeGrams = 60, calories = 135, carbs = 33.0f, protein = 0.2f, fat = 0.0f, fiber = 0.3f),
                    FoodItemEntity(name = "Aveia em Flocos", category = "Carboidratos", servingSizeGrams = 30, calories = 106, carbs = 17.0f, protein = 4.3f, fat = 2.2f, fiber = 2.9f),
                    FoodItemEntity(name = "Batata Doce Cozida", category = "Carboidratos", servingSizeGrams = 100, calories = 86, carbs = 20.1f, protein = 1.6f, fat = 0.1f, fiber = 3.0f),
                    FoodItemEntity(name = "Banana Prata", category = "Frutas", servingSizeGrams = 100, calories = 89, carbs = 23.0f, protein = 1.1f, fat = 0.3f, fiber = 2.6f),
                    FoodItemEntity(name = "Maçã Fuji", category = "Frutas", servingSizeGrams = 130, calories = 72, carbs = 19.0f, protein = 0.4f, fat = 0.2f, fiber = 3.1f),
                    FoodItemEntity(name = "Açaí Puro (sem xarope)", category = "Frutas", servingSizeGrams = 100, calories = 60, carbs = 6.2f, protein = 1.0f, fat = 3.8f, fiber = 3.2f),
                    FoodItemEntity(name = "Abacate", category = "Frutas", servingSizeGrams = 100, calories = 160, carbs = 8.5f, protein = 2.0f, fat = 14.7f, fiber = 6.7f),
                    FoodItemEntity(name = "Alface Americana", category = "Verduras", servingSizeGrams = 100, calories = 15, carbs = 2.8f, protein = 1.4f, fat = 0.2f, fiber = 1.3f),
                    FoodItemEntity(name = "Tomate Salada", category = "Verduras", servingSizeGrams = 100, calories = 18, carbs = 3.9f, protein = 0.9f, fat = 0.2f, fiber = 1.2f),
                    FoodItemEntity(name = "Brócolis Cozido", category = "Verduras", servingSizeGrams = 100, calories = 35, carbs = 7.0f, protein = 2.4f, fat = 0.4f, fiber = 3.3f),
                    FoodItemEntity(name = "Queijo Minas Frescal", category = "Laticínios", servingSizeGrams = 30, calories = 75, carbs = 0.9f, protein = 5.2f, fat = 5.7f, fiber = 0.0f),
                    FoodItemEntity(name = "Iogurte Natural Desnatado", category = "Laticínios", servingSizeGrams = 170, calories = 70, carbs = 9.0f, protein = 6.8f, fat = 0.0f, fiber = 0.0f),
                    FoodItemEntity(name = "Whey Protein 80%", category = "Proteínas", servingSizeGrams = 30, calories = 120, carbs = 2.0f, protein = 24.0f, fat = 1.5f, fiber = 0.0f),
                    FoodItemEntity(name = "Azeite de Oliva Extra Virgem", category = "Doces/Outros", servingSizeGrams = 13, calories = 119, carbs = 0.0f, protein = 0.0f, fat = 13.5f, fiber = 0.0f),
                    FoodItemEntity(name = "Castanha do Pará", category = "Doces/Outros", servingSizeGrams = 20, calories = 131, carbs = 2.4f, protein = 2.9f, fat = 13.3f, fiber = 1.5f),
                    FoodItemEntity(name = "Prato Feito Brasileiro (Misto)", category = "Pratos Prontos", servingSizeGrams = 400, calories = 580, carbs = 65.0f, protein = 38.0f, fat = 16.0f, fiber = 11.0f)
                )
                foodDao.insertAllFoods(initialFoods)
            }

            if (userDao.getUserCount() == 0) {
                val defaultUsers = listOf(
                    UserProfileEntity(
                        id = 1,
                        name = "Carlos Oliveira",
                        email = "carlos.fit@email.com",
                        phone = "+55 11 98765-4321",
                        isAdmin = false,
                        calorieGoal = 2100,
                        proteinGoalGrams = 150,
                        carbGoalGrams = 220,
                        fatGoalGrams = 65,
                        waterGoalMl = 2800,
                        currentWeightKg = 76.2f,
                        targetWeightKg = 72.0f,
                        streakDays = 7,
                        isActive = true,
                        registrationDate = "2026-09-10"
                    ),
                    UserProfileEntity(
                        id = 2,
                        name = "Dra. Mariana Costa (Admin)",
                        email = "nutri.mariana@clinica.com",
                        phone = "+55 11 99988-7766",
                        isAdmin = true,
                        calorieGoal = 1800,
                        proteinGoalGrams = 120,
                        carbGoalGrams = 180,
                        fatGoalGrams = 55,
                        waterGoalMl = 2200,
                        currentWeightKg = 61.0f,
                        targetWeightKg = 60.0f,
                        streakDays = 14,
                        isActive = true,
                        registrationDate = "2026-08-15"
                    ),
                    UserProfileEntity(
                        id = 3,
                        name = "Beatriz Santos",
                        email = "beatriz.santos@email.com",
                        phone = "+55 21 97654-3210",
                        isAdmin = false,
                        calorieGoal = 1650,
                        proteinGoalGrams = 110,
                        carbGoalGrams = 170,
                        fatGoalGrams = 50,
                        waterGoalMl = 2000,
                        currentWeightKg = 64.5f,
                        targetWeightKg = 59.0f,
                        streakDays = 4,
                        isActive = true,
                        registrationDate = "2026-09-18"
                    ),
                    UserProfileEntity(
                        id = 4,
                        name = "Lucas Mendes",
                        email = "lucas.mendes@email.com",
                        phone = "+55 31 98877-6655",
                        isAdmin = false,
                        calorieGoal = 2500,
                        proteinGoalGrams = 180,
                        carbGoalGrams = 280,
                        fatGoalGrams = 75,
                        waterGoalMl = 3200,
                        currentWeightKg = 83.0f,
                        targetWeightKg = 85.0f,
                        streakDays = 12,
                        isActive = true,
                        registrationDate = "2026-09-02"
                    )
                )
                userDao.insertAllUsers(defaultUsers)

                // Add sample meals for today so Carlos has a realistic dashboard immediately
                val todayStr = java.time.LocalDate.now().toString()
                mealDao.insertMeal(
                    MealEntryEntity(
                        userId = 1,
                        foodName = "Ovos Mexidos (2 un) com Pão Francês",
                        mealType = MealType.BREAKFAST,
                        portionGrams = 150,
                        calories = 310,
                        carbs = 30.5f,
                        protein = 17.0f,
                        fat = 13.0f,
                        fiber = 1.2f,
                        dateString = todayStr,
                        isAiDetected = false
                    )
                )
                mealDao.insertMeal(
                    MealEntryEntity(
                        userId = 1,
                        foodName = "Prato Feito: Frango Grelhado, Arroz e Feijão",
                        mealType = MealType.LUNCH,
                        portionGrams = 380,
                        calories = 540,
                        carbs = 58.0f,
                        protein = 44.0f,
                        fat = 12.0f,
                        fiber = 9.5f,
                        dateString = todayStr,
                        isAiDetected = true,
                        notes = "Reconhecido pela IA NutriIA"
                    )
                )

                // Add initial water
                waterDao.insertWater(
                    WaterEntryEntity(
                        userId = 1,
                        dateString = todayStr,
                        amountMl = 1250
                    )
                )
            }
        }
    }
}
