package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AiFoodRecognitionResult
import com.example.data.model.DailyMacroSummary
import com.example.data.model.FoodItemEntity
import com.example.data.model.MealEntryEntity
import com.example.data.model.MealType
import com.example.data.model.UserProfileEntity
import com.example.data.remote.GeminiVisionService
import com.example.data.repository.NutriRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface AiScanUiState {
    object Idle : AiScanUiState
    object Scanning : AiScanUiState
    data class Success(
        val result: AiFoodRecognitionResult,
        val portionMultiplier: Float = 1.0f,
        val selectedMealType: MealType = MealType.LUNCH,
        val scannedBitmap: Bitmap? = null
    ) : AiScanUiState
    data class Error(val message: String) : AiScanUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = NutriRepository(
        database.foodDao(),
        database.mealDao(),
        database.waterDao(),
        database.userDao()
    )
    private val visionService = GeminiVisionService()

    // Current User
    private val _currentUserId = MutableStateFlow<Long>(1)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserProfileEntity?> = _currentUserId.flatMapLatest { id ->
        repository.getUser(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Selected Date
    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Daily Meals
    val dailyMeals: StateFlow<List<MealEntryEntity>> = kotlinx.coroutines.flow.combine(
        _selectedDate,
        _currentUserId
    ) { date, uid -> Pair(date, uid) }.flatMapLatest { (date, uid) ->
        repository.getMealsForDate(date, uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Summary
    val dailySummary: StateFlow<DailyMacroSummary> = kotlinx.coroutines.flow.combine(
        _selectedDate,
        _currentUserId
    ) { date, uid -> Pair(date, uid) }.flatMapLatest { (date, uid) ->
        repository.getDailySummary(date, uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyMacroSummary())

    // Water Intake
    val currentWaterMl: StateFlow<Int> = kotlinx.coroutines.flow.combine(
        _selectedDate,
        _currentUserId
    ) { date, uid -> Pair(date, uid) }.flatMapLatest { (date, uid) ->
        repository.getTotalWaterForDate(date, uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Food Catalog & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val foodSearchResults: StateFlow<List<FoodItemEntity>> = _searchQuery.flatMapLatest { q ->
        repository.searchFoods(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Recognition State
    private val _aiScanState = MutableStateFlow<AiScanUiState>(AiScanUiState.Idle)
    val aiScanState: StateFlow<AiScanUiState> = _aiScanState.asStateFlow()

    // Admin Mode & Users
    val allUsers: StateFlow<List<UserProfileEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Notification Reminder toggles
    private val _mealRemindersEnabled = MutableStateFlow(true)
    val mealRemindersEnabled: StateFlow<Boolean> = _mealRemindersEnabled.asStateFlow()

    private val _waterRemindersEnabled = MutableStateFlow(true)
    val waterRemindersEnabled: StateFlow<Boolean> = _waterRemindersEnabled.asStateFlow()

    // Dark Mode Override (null means system default)
    private val _darkThemeOverride = MutableStateFlow<Boolean?>(null)
    val darkThemeOverride: StateFlow<Boolean?> = _darkThemeOverride.asStateFlow()

    init {
        NotificationHelper.initNotificationChannels(application)
    }

    fun toggleDarkTheme() {
        _darkThemeOverride.value = when (_darkThemeOverride.value) {
            true -> false
            false -> null
            null -> true
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    fun previousDay() {
        val current = LocalDate.parse(_selectedDate.value)
        _selectedDate.value = current.minusDays(1).toString()
    }

    fun nextDay() {
        val current = LocalDate.parse(_selectedDate.value)
        _selectedDate.value = current.plusDays(1).toString()
    }

    fun setToday() {
        _selectedDate.value = LocalDate.now().toString()
    }

    fun switchUser(userId: Long) {
        _currentUserId.value = userId
    }

    fun setAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(amountMl, _selectedDate.value, _currentUserId.value)
        }
    }

    fun resetWater() {
        viewModelScope.launch {
            repository.resetWater(_selectedDate.value, _currentUserId.value)
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMealById(mealId)
        }
    }

    fun addMeal(
        foodName: String,
        mealType: MealType,
        grams: Int,
        calories: Int,
        carbs: Float,
        protein: Float,
        fat: Float,
        fiber: Float = 0f,
        isAi: Boolean = false,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val entry = MealEntryEntity(
                userId = _currentUserId.value,
                foodName = foodName,
                mealType = mealType,
                portionGrams = grams,
                calories = calories,
                carbs = carbs,
                protein = protein,
                fat = fat,
                fiber = fiber,
                dateString = _selectedDate.value,
                isAiDetected = isAi,
                notes = notes
            )
            repository.insertMeal(entry)
        }
    }

    // AI Food Recognition Methods
    fun analyzeFoodImage(bitmap: Bitmap, customHint: String? = null) {
        _aiScanState.value = AiScanUiState.Scanning
        viewModelScope.launch {
            val result = visionService.analyzeFoodImage(bitmap, customHint)
            result.onSuccess { data ->
                _aiScanState.value = AiScanUiState.Success(
                    result = data,
                    portionMultiplier = 1.0f,
                    selectedMealType = MealType.LUNCH,
                    scannedBitmap = bitmap
                )
            }.onFailure { err ->
                _aiScanState.value = AiScanUiState.Error(
                    err.message ?: "Erro ao analisar o alimento com IA."
                )
            }
        }
    }

    fun updateAiPortionMultiplier(multiplier: Float) {
        val current = _aiScanState.value
        if (current is AiScanUiState.Success) {
            _aiScanState.value = current.copy(portionMultiplier = multiplier)
        }
    }

    fun updateAiSelectedMealType(mealType: MealType) {
        val current = _aiScanState.value
        if (current is AiScanUiState.Success) {
            _aiScanState.value = current.copy(selectedMealType = mealType)
        }
    }

    fun saveAiDetectedMealToDiary() {
        val current = _aiScanState.value
        if (current is AiScanUiState.Success) {
            val r = current.result
            val m = current.portionMultiplier
            addMeal(
                foodName = r.dishTitle,
                mealType = current.selectedMealType,
                grams = (r.estimatedWeightGrams * m).toInt(),
                calories = (r.calories * m).toInt(),
                carbs = r.carbs * m,
                protein = r.protein * m,
                fat = r.fat * m,
                fiber = r.fiber * m,
                isAi = true,
                notes = "Reconhecimento IA (${(r.confidenceScore * 100).toInt()}% precisão)"
            )
            _aiScanState.value = AiScanUiState.Idle
        }
    }

    fun resetAiScanState() {
        _aiScanState.value = AiScanUiState.Idle
    }

    // Food Database actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createCustomFood(
        name: String,
        category: String,
        servingGrams: Int,
        calories: Int,
        carbs: Float,
        protein: Float,
        fat: Float,
        fiber: Float
    ) {
        viewModelScope.launch {
            val newFood = FoodItemEntity(
                name = name,
                category = category,
                servingSizeGrams = servingGrams,
                calories = calories,
                carbs = carbs,
                protein = protein,
                fat = fat,
                fiber = fiber,
                isCustom = true
            )
            repository.insertFood(newFood)
        }
    }

    // Admin User Management
    fun createUserProfile(user: UserProfileEntity) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun updateUserProfile(user: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    fun deleteUserProfile(user: UserProfileEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    // Reminders
    fun toggleMealReminders(enabled: Boolean) {
        _mealRemindersEnabled.value = enabled
    }

    fun toggleWaterReminders(enabled: Boolean) {
        _waterRemindersEnabled.value = enabled
    }

    fun sendTestNotification(type: String) {
        if (type == "water") {
            NotificationHelper.sendWaterReminderNotification(getApplication())
        } else {
            NotificationHelper.sendMealReminderNotification(getApplication(), "Almoço")
        }
    }
}
