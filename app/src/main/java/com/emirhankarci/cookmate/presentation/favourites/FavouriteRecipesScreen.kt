package com.emirhankarci.cookmate.presentation.favourites

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.cookmate.R

@Composable
fun FavouriteRecipesScreen(
    viewModel: FavouriteRecipesViewModel,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavouriteRecipes()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFFF69B4)
                )
            }
            
            state.favouriteRecipes.isEmpty() -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "No favourites",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFE0E0E0)
                    )
                    Text(
                        text = "No favourite recipes yet",
                        fontSize = 18.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.favouriteRecipes) { recipe ->
                        FavouriteRecipeCard(
                            recipe = recipe,
                            onRecipeClick = { onRecipeClick(recipe.recipeId) },
                            onRemoveFavourite = { 
                                viewModel.onEvent(FavouriteRecipesEvent.RemoveFavourite(recipe.recipeId))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavouriteRecipeCard(
    recipe: com.emirhankarci.cookmate.data.model.Recipe,
    onRecipeClick: () -> Unit,
    onRemoveFavourite: () -> Unit
) {
    Card(
        onClick = onRecipeClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Recipe Image
            Image(
                painter = painterResource(id = R.drawable.ratatouille_food_img),
                contentDescription = "Recipe Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            )
            
            // Recipe Info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recipe.titleTurkish ?: recipe.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50),
                    maxLines = 2
                )
                
                Text(
                    text = "${recipe.ingredients.size} ingredients",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
            }
            
            // Remove Favourite Button
            IconButton(
                onClick = onRemoveFavourite,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove from favourites",
                    tint = Color(0xFFFF69B4)
                )
            }
        }
    }
}
