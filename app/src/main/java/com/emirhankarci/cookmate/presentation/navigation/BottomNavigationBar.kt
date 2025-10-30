package com.emirhankarci.cookmate.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emirhankarci.cookmate.R
import com.emirhankarci.cookmate.ui.theme.*

/**
 * Bottom Navigation Item model
 */
data class BottomNavItem(
    val route: Screen,
    val icon: Int,
    val label: String
)

/**
 * Get bottom navigation items
 */
fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            route = Screen.CountryList,
            icon = R.drawable.ic_globe, // We'll create these icons
            label = "Ülkeler"
        ),
        BottomNavItem(
            route = Screen.Profile,
            icon = R.drawable.ic_profile,
            label = "Profil"
        )
    )
}

/**
 * Modern, elegant bottom navigation bar with smooth animations
 */
@Composable
fun BottomNavigationBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = getBottomNavItems()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                clip = false
            )
            .background(
                color = NavBarBackground,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BottomNavItemView(
                    item = item,
                    isSelected = currentScreen == item.route,
                    onClick = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual bottom navigation item with animations
 */
@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated colors
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) NavBarActiveIcon else NavBarInactiveIcon,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) NavBarActiveText else NavBarInactiveText,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "text_color"
    )

    // Animated scale for spring effect
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Animated pill background
    val pillHeight by animateDpAsState(
        targetValue = if (isSelected) 64.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pill_height"
    )

    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 110.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "pill_width"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    radius = 40.dp,
                    color = NavBarActiveIcon
                ),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background pill for active state
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(pillWidth)
                    .height(pillHeight)
                    .background(
                        color = NavBarPillBackground,
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        // Icon and label
        Column(
            modifier = Modifier
                .scale(scale)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = item.icon),
                contentDescription = item.label,
                modifier = Modifier.size(28.dp),
                tint = iconColor
            )

            Text(
                text = item.label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                letterSpacing = 0.3.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Alternative: Top Line Indicator Style
 */
@Composable
fun BottomNavigationBarTopLine(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = getBottomNavItems()
    val selectedIndex = items.indexOfFirst { it.route == currentScreen }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                clip = false
            )
            .background(
                color = NavBarBackground,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)) {
            // Top indicator line
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .padding(horizontal = 16.dp)
            ) {
                items.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                brush = if (index == selectedIndex) {
                                    Brush.horizontalGradient(
                                        colors = listOf(NavBarIndicatorStart, NavBarIndicatorEnd)
                                    )
                                } else {
                                    Brush.horizontalGradient(colors = listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                    )
                }
            }

            // Navigation items
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    BottomNavItemSimple(
                        item = item,
                        isSelected = currentScreen == item.route,
                        onClick = { onNavigate(item.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Simple bottom nav item (for top line variant)
 */
@Composable
private fun BottomNavItemSimple(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) NavBarActiveIcon else NavBarInactiveIcon,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "icon_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) NavBarActiveText else NavBarInactiveText,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "text_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    radius = 40.dp,
                    color = NavBarActiveIcon
                ),
                onClick = onClick
            )
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = item.icon),
            contentDescription = item.label,
            modifier = Modifier.size(28.dp),
            tint = iconColor
        )

        Text(
            text = item.label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            letterSpacing = 0.3.sp,
            maxLines = 1
        )
    }
}
