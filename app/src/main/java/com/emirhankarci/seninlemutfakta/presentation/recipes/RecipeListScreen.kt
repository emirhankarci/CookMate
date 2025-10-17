package com.emirhankarci.seninlemutfakta.presentation.recipes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emirhankarci.seninlemutfakta.data.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel,
    onBack: () -> Unit,
    onRecipeClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${state.countryName} Tarifleri")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Geri")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()

                state.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Hata: ${state.error}")
                        Button(onClick = { viewModel.onEvent(RecipeListEvent.Retry) }) {
                            Text("Tekrar Dene")
                        }
                    }
                }

                state.recipes.isEmpty() -> {
                    Text("Henüz tarif eklenmemiş")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.getSortedRecipes()) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                isCompleted = state.isRecipeCompleted(recipe.recipeId),
                                onClick = {
                                    onRecipeClick(recipe.recipeId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.titleTurkish.ifEmpty { recipe.title },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "⏱️ ${recipe.estimatedTime} dk",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "🍽️ ${recipe.servings} kişilik",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "⭐ ${recipe.difficulty}/5",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (isCompleted) {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
