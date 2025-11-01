package com.emirhankarci.cookmate.presentation.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.cookmate.data.model.Recipe

@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit,
    selectedFilter: RecipeFilter,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F5))
    ) {

        // Content
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    color = Color(0xFFFF69B4)
                )

                state.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Error: ${state.error}", color = Color.DarkGray) // Beyaz arka plan için renk güncellendi
                        Button(
                            onClick = { viewModel.onEvent(RecipeListEvent.Retry) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF69B4)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                state.recipes.isEmpty() -> {
                    EmptyRecipeState()
                }

                else -> {
                    val filteredRecipes = state.getFilteredRecipes(selectedFilter)

                    if (filteredRecipes.isEmpty()) {
                        EmptyFilterState(filter = selectedFilter)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredRecipes) { recipe ->
                                RecipeCard(
                                    recipe = recipe,
                                    status = state.getRecipeStatus(recipe.recipeId),
                                    isLocked = state.isRecipeLocked(recipe),
                                    difficultyText = state.getDifficultyText(recipe.difficulty),
                                    onClick = {
                                        if (!state.isRecipeLocked(recipe)) {
                                            onRecipeClick(recipe.recipeId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeListHeader(
    countryName: String,
    countryFlag: String,
    totalRecipes: Int,
    completedRecipes: Int,
    progressPercentage: Int,
    selectedFilter: RecipeFilter,
    onFilterChange: (RecipeFilter) -> Unit,
    onBack: () -> Unit
) {
    val gradientColors = listOf(
        Color(0xFFFFB6C1),
        Color(0xFFFF69B4),
        Color(0xFFFF6B6B)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
            .statusBarsPadding()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Country info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (countryFlag.isNotEmpty()) {
                Text(
                    text = countryFlag,
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "$countryName Recipes",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$totalRecipes recipes • $completedRecipes completed",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your Progress",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$progressPercentage%",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            LinearProgressIndicator(
                progress = { progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Filter chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            RecipeFilterChip(
                label = "All",
                selected = selectedFilter == RecipeFilter.ALL,
                onClick = { onFilterChange(RecipeFilter.ALL) }
            )
            RecipeFilterChip(
                label = "Not Started",
                selected = selectedFilter == RecipeFilter.NOT_STARTED,
                onClick = { onFilterChange(RecipeFilter.NOT_STARTED) }
            )
            RecipeFilterChip(
                label = "Completed",
                selected = selectedFilter == RecipeFilter.COMPLETED,
                onClick = { onFilterChange(RecipeFilter.COMPLETED) }
            )
        }
    }
}

@Composable
fun RecipeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.3f),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color(0xFFFF69B4) else Color.White
            )
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    status: RecipeStatus,
    isLocked: Boolean,
    difficultyText: String,
    onClick: () -> Unit
) {
    val cardColor = when {
        isLocked -> Color(0xFFE0E0E0)
        status == RecipeStatus.COMPLETED -> Color(0xFFF1F8F4) // Light green tint
        else -> Color.White
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 2.dp else 6.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isLocked) Modifier.alpha(0.6f) else Modifier
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top row: Status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Recipe emoji or image placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFE8F0),
                                        Color(0xFFFFD0E0)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLocked) "🔒" else "🍳",
                            fontSize = 20.sp
                        )
                    }

                    // Right: Status badge
                    when {
                        isLocked -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF757575)
                            ) {
                                Text(
                                    text = "🔒 Locked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        status == RecipeStatus.COMPLETED -> {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "✓",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFF69B4)
                            ) {
                                Text(
                                    text = "New",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                // Recipe name
                Text(
                    text = recipe.titleTurkish.ifEmpty { recipe.title },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) Color(0xFF757575) else Color(0xFF2C3E50),
                    maxLines = 1
                )

                // English name (if different)
                if (recipe.titleTurkish.isNotEmpty() && recipe.title != recipe.titleTurkish) {
                    Text(
                        text = recipe.title,
                        fontSize = 12.sp,
                        color = if (isLocked) Color(0xFF9E9E9E) else Color(0xFF95A5A6),
                        maxLines = 1
                    )
                }

                // Description
                Text(
                    text = recipe.description,
                    fontSize = 12.sp,
                    color = if (isLocked) Color(0xFF9E9E9E) else Color(0xFF95A5A6),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.weight(1f))

                if (!isLocked) {
                    // Info badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Time badge
                        InfoBadge(
                            icon = "⏱️",
                            text = "${recipe.estimatedTime} min"
                        )
                        // Servings badge
                        InfoBadge(
                            icon = "🍽️",
                            text = "${recipe.servings} servings"
                        )
                        // Difficulty badge
                        DifficultyBadge(
                            difficulty = recipe.difficulty,
                            text = difficultyText
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // CTA Button
                    Button(
                        onClick = onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (status) {
                                RecipeStatus.COMPLETED -> Color(0xFF4CAF50)
                                else -> Color(0xFFFF69B4)
                            }
                        )
                    ) {
                        Text(
                            text = when (status) {
                                RecipeStatus.COMPLETED -> "Cook Again →"
                                RecipeStatus.IN_PROGRESS -> "Continue →"
                                RecipeStatus.NOT_STARTED -> "Start Cooking →"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Locked state
                    Text(
                        text = "Complete previous recipes to unlock",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBadge(
    icon: String,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 11.sp)
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color(0xFF95A5A6)
        )
    }
}

@Composable
fun DifficultyBadge(
    difficulty: Int,
    text: String
) {
    val color = when (difficulty) {
        1, 2 -> Color(0xFF4CAF50) // Green
        3 -> Color(0xFFFFA726) // Orange
        4, 5 -> Color(0xFFFF6B6B) // Red
        else -> Color(0xFFFFA726)
    }

    Surface(
        shape = RoundedCornerShape(7.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stars
            repeat(difficulty.coerceIn(1, 5)) {
                Text(text = "⭐", fontSize = 9.sp)
            }
            Text(
                text = text,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyRecipeState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📝",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Recipes Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Recipes for this country will be added soon!",
            fontSize = 14.sp,
            color = Color(0xFF95A5A6),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyFilterState(filter: RecipeFilter) {
    val message = when (filter) {
        RecipeFilter.NOT_STARTED -> "No recipes to start.\nAll recipes are either in progress or completed!"
        RecipeFilter.IN_PROGRESS -> "No recipes in progress.\nStart a new recipe to see it here!"
        RecipeFilter.COMPLETED -> "No completed recipes yet.\nStart cooking to earn completions!"
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Results",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color(0xFF95A5A6),
            textAlign = TextAlign.Center
        )
    }
}
