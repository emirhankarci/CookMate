package com.emirhankarci.cookmate.presentation.countries

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.emirhankarci.cookmate.R
import com.emirhankarci.cookmate.data.model.Country
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CountryListScreen(
    viewModel: CountryListViewModel,
    onFavouriteClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCountriesIfNeeded()
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CountryCardRow()
        }

        FloatingActionButton(
            onClick = { viewModel.onEvent(CountryListEvent.ToggleViewMode) },
            containerColor = Color(0xFFFF69B4),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (state.isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                contentDescription = if (state.isGridView) "List View" else "Grid View"
            )
        }
    }
}


@Composable
fun FilterChip(
) {}

@Composable
fun CountryCardRow() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Popular recipes in France",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(10) {
                RecipeCard()
            }
        }
    }
}

@Composable
fun RecipeCard() {
    Column(
    ) {
        // Recipe Image Card
        Card(
            modifier = Modifier
                .size(180.dp)
                .aspectRatio(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ratatouille_food_img),
                    contentDescription = "Recipe Image",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Favorite icon
                IconButton(
                    onClick = { /* TODO: Add recipe ID */ },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Add to favourites",
                                tint = Color(0xFFFF69B4),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Recipe Info
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ratatouille",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2C3E50),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CompactCountryCard(

) {}
