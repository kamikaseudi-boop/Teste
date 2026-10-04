package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItemEntity
import com.example.data.model.MealType
import com.example.ui.NutriViewModel
import com.example.ui.theme.MacroCarbColor
import com.example.ui.theme.MacroFatColor
import com.example.ui.theme.MacroProteinColor
import com.example.ui.theme.PrimaryGreen

@Composable
fun FoodDatabaseScreen(
    viewModel: NutriViewModel,
    onFoodAdded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val foods by viewModel.foodSearchResults.collectAsState()

    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedFoodForAdd by remember { mutableStateOf<FoodItemEntity?>(null) }
    var showCreateFoodDialog by remember { mutableStateOf(false) }

    val categories = listOf("Todos", "Proteínas", "Carboidratos", "Frutas", "Verduras", "Laticínios", "Pratos Prontos", "Doces/Outros")

    val filteredFoods = if (selectedCategory == "Todos") {
        foods
    } else {
        foods.filter { it.category == selectedCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateFoodDialog = true },
                containerColor = PrimaryGreen,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("fab_create_food")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Criar alimento")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("food_database_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_search_input"),
                    placeholder = { Text("Buscar alimento (ex: frango, feijão, arroz...)") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            leadingIcon = if (selectedCategory == cat) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }

            // Count label
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alimentos Cadastrados (${filteredFoods.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Toque para adicionar",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Foods List
            items(filteredFoods) { food ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedFoodForAdd = food }
                        .testTag("food_item_${food.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (food.isCustom) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "Personalizado",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${food.category} • Porção de ${food.servingSizeGrams}g",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MacroPill("P: ${food.protein}g", MacroProteinColor)
                                MacroPill("C: ${food.carbs}g", MacroCarbColor)
                                MacroPill("G: ${food.fat}g", MacroFatColor)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${food.calories} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            IconButton(onClick = { selectedFoodForAdd = food }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Adicionar ao diário",
                                    tint = PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Add to Meal Dialog
    selectedFoodForAdd?.let { food ->
        var gramsText by remember { mutableStateOf(food.servingSizeGrams.toString()) }
        var chosenMealType by remember { mutableStateOf(MealType.LUNCH) }

        val grams = gramsText.toIntOrNull() ?: food.servingSizeGrams
        val ratio = grams.toFloat() / food.servingSizeGrams.toFloat()
        val calculatedKcal = (food.calories * ratio).toInt()
        val calculatedProt = food.protein * ratio
        val calculatedCarb = food.carbs * ratio
        val calculatedFat = food.fat * ratio

        AlertDialog(
            onDismissRequest = { selectedFoodForAdd = null },
            title = { Text(food.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Escolha a refeição e a quantidade em gramas:")

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MealType.entries) { mType ->
                            FilterChip(
                                selected = chosenMealType == mType,
                                onClick = { chosenMealType = mType },
                                label = { Text(mType.ptName, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = gramsText,
                        onValueChange = { gramsText = it },
                        label = { Text("Quantidade (gramas)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_food_grams_input"),
                        singleLine = true
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Total: $calculatedKcal kcal",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Proteínas: ${calculatedProt.toInt()}g | Carbos: ${calculatedCarb.toInt()}g | Gorduras: ${calculatedFat.toInt()}g",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addMeal(
                            foodName = food.name,
                            mealType = chosenMealType,
                            grams = grams,
                            calories = calculatedKcal,
                            carbs = calculatedCarb,
                            protein = calculatedProt,
                            fat = calculatedFat,
                            fiber = food.fiber * ratio,
                            isAi = false
                        )
                        selectedFoodForAdd = null
                        onFoodAdded()
                    },
                    modifier = Modifier.testTag("confirm_add_food_to_diary")
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFoodForAdd = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Create New Food Dialog
    if (showCreateFoodDialog) {
        var newName by remember { mutableStateOf("") }
        var newCat by remember { mutableStateOf("Proteínas") }
        var newGrams by remember { mutableStateOf("100") }
        var newKcal by remember { mutableStateOf("") }
        var newCarb by remember { mutableStateOf("") }
        var newProt by remember { mutableStateOf("") }
        var newFat by remember { mutableStateOf("") }
        var newFiber by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateFoodDialog = false },
            title = { Text("Cadastrar Novo Alimento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nome do Alimento") },
                        modifier = Modifier.fillMaxWidth().testTag("create_food_name_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newKcal,
                            onValueChange = { newKcal = it },
                            label = { Text("Calorias") },
                            modifier = Modifier.weight(1f).testTag("create_food_kcal_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newGrams,
                            onValueChange = { newGrams = it },
                            label = { Text("Porção (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newProt,
                            onValueChange = { newProt = it },
                            label = { Text("Proteína (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newCarb,
                            onValueChange = { newCarb = it },
                            label = { Text("Carboidrato (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = newFat,
                            onValueChange = { newFat = it },
                            label = { Text("Gordura (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newFiber,
                            onValueChange = { newFiber = it },
                            label = { Text("Fibras (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val kcal = newKcal.toIntOrNull() ?: 100
                        val grams = newGrams.toIntOrNull() ?: 100
                        val carb = newCarb.toFloatOrNull() ?: 0f
                        val prot = newProt.toFloatOrNull() ?: 0f
                        val fat = newFat.toFloatOrNull() ?: 0f
                        val fiber = newFiber.toFloatOrNull() ?: 0f

                        if (newName.isNotBlank()) {
                            viewModel.createCustomFood(
                                name = newName,
                                category = newCat,
                                servingGrams = grams,
                                calories = kcal,
                                carbs = carb,
                                protein = prot,
                                fat = fat,
                                fiber = fiber
                            )
                            showCreateFoodDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_create_food_button")
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFoodDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun MacroPill(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
