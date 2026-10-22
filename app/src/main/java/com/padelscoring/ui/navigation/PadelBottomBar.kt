package com.padelscoring.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.padelscoring.ui.components.PadelHapticFeedback
import com.padelscoring.ui.components.rememberPadelHaptics
import com.padelscoring.ui.theme.*

/**
 * Athletic navigation bar with electric volt accent indicator bar, tactile feedback,
 * and high-contrast padel aesthetic.
 */
@Composable
fun PadelBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    items: List<PadelNavigationItem> = PadelNavigationItem.items
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    PadelBottomBar(
        currentRoute = currentRoute,
        onNavigateToDestination = { item ->
            val startDest = try {
                navController.graph.findStartDestination()
            } catch (_: Throwable) {
                null
            }
            val startDestId = startDest?.id ?: navController.graph.startDestinationId

            navController.navigate(item.route) {
                if (startDestId != 0) {
                    popUpTo(startDestId) {
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier,
        items = items
    )
}

/**
 * Stateless Athletic Padel Bottom Navigation Bar for easy previews and headless testing.
 */
@Composable
fun PadelBottomBar(
    currentRoute: String?,
    onNavigateToDestination: (PadelNavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    items: List<PadelNavigationItem> = PadelNavigationItem.items,
    haptics: PadelHapticFeedback = rememberPadelHaptics()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PadelObsidian)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = PadelBorderHairline
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute != null && (
                    currentRoute == item.route ||
                    currentRoute.startsWith("${item.route}?") ||
                    currentRoute.startsWith(item.route)
                )

                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) PadelVolt else PadelTextSecondary,
                    label = "tabLabelColor"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag(item.testTag)
                        .semantics {
                            role = Role.Tab
                            selected = isSelected
                            contentDescription = item.contentDescription
                        }
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            haptics.tick()
                            onNavigateToDestination(item)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Electric Volt Active Indicator Accent Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .height(3.dp)
                            .clip(PadelPillShape)
                            .background(if (isSelected) PadelVolt else Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.iconEmoji,
                        fontSize = 20.sp
                    )

                    Text(
                        text = item.title,
                        style = PadelTypography.labelSmall.copy(
                            color = labelColor,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
