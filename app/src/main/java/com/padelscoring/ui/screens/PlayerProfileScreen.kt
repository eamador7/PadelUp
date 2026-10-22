package com.padelscoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*
import com.padelscoring.viewmodel.PlayerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    viewModel: PlayerProfileViewModel? = null,
    playerName: String = "Alejandro Galán",
    level: Double = 4.85,
    delta: Double = +0.12,
    winRate: Int = 74,
    matchesPlayed: Int = 58,
    bestStreak: Int = 9,
    preferredSide: String = "REVÉS", // "REVÉS" (Left/Backhand) or "DRIVE" (Right/Forehand)
    recentForm: List<Boolean> = listOf(true, true, true, false, true, true, true, false, true, true)
) {
    val vmState = viewModel?.uiState?.collectAsState()?.value

    val displayPlayerName = vmState?.playerName ?: playerName
    val displayLevel = vmState?.level ?: level
    val displayDelta = vmState?.delta ?: delta
    val displayWinRate = vmState?.winRate ?: winRate
    val displayMatchesPlayed = vmState?.matchesPlayed ?: matchesPlayed
    val displayBestStreak = vmState?.bestStreak ?: bestStreak
    val displayCurrentStreak = vmState?.currentStreak ?: 2
    val displayPreferredSide = vmState?.preferredSide ?: preferredSide
    val displayRecentForm = vmState?.recentForm?.takeIf { it.isNotEmpty() } ?: recentForm
    val displayConfidenceProgress = vmState?.confidenceProgress ?: 0.92f

    val playerInitials = displayPlayerName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelObsidian)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PadelRacketIcon(size = 18.dp)
                            Text(
                                text = "ATHLETE PASSPORT",
                                style = PadelTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = PadelVolt
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PLAYER STATS & PROFILE",
                            style = PadelTypography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = PadelTextPrimary
                            )
                        )
                    }

                    // Level Tier Pill
                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelVoltSubtle)
                            .border(1.dp, PadelVoltBorder, PadelPillShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "PRO DIVISION",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelVolt,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Player Card with Dynamic Level & Confidence Ring
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PadelCourtCardShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1B2433), PadelSurface)
                        )
                    )
                    .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Avatar & Dynamic Level Ring
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Initials Badge
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PadelSurfaceElevated)
                            .border(2.dp, PadelVolt, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = playerInitials,
                            style = PadelTypography.headlineLarge.copy(
                                color = PadelVolt,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp
                            )
                        )
                    }

                    // Circular Level Badge with Animated Arc & Delta
                    DynamicLevelBadge(
                        level = displayLevel,
                        delta = displayDelta,
                        confidenceProgress = displayConfidenceProgress
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = displayPlayerName,
                    style = PadelTypography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PadelTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tactical Court Side Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourtSideBadge(side = displayPreferredSide)
                    Text(
                        text = if (displayPreferredSide == "REVÉS") "Left Side • Aggressive Finisher" else "Right Side • Tactical Builder",
                        style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                    )
                }
            }

            // Recent 10-Match Rolling Form Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PadelCourtCardShape)
                    .background(PadelSurface)
                    .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LAST 10 MATCHES FORM",
                        style = PadelTypography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            color = PadelTextMuted
                        )
                    )
                    Text(
                        text = "🔥 ${displayCurrentStreak}W STREAK",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelVolt,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    displayRecentForm.forEach { isWin ->
                        FormDot(isWin = isWin)
                    }
                }
            }

            // Key Performance Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Win Rate Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PadelShapes.medium)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "WIN RATE",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$displayWinRate%",
                        style = PadelTypography.displaySmall.copy(
                            color = PadelVolt,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "$displayMatchesPlayed Total Matches",
                        style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                    )
                }

                // Best Streak Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PadelShapes.medium)
                        .background(PadelSurface)
                        .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "BEST STREAK",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            letterSpacing = 0.8.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${displayBestStreak}W",
                        style = PadelTypography.displaySmall.copy(
                            color = PadelTurfCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "Active Streak: ${displayCurrentStreak}W",
                        style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                    )
                }
            }

            // Americano Trophy Cabinet Preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PadelCourtCardShape)
                    .background(PadelSurface)
                    .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AMERICANO TROPHIES & BADGES",
                        style = PadelTypography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            color = PadelTextMuted
                        )
                    )
                    Text(
                        text = "3 UNLOCKED",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelGoldCrown,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Trophy 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PadelShapes.small)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelGoldCrown.copy(alpha = 0.3f), PadelShapes.small)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "4x Champion",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelGoldCrown,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Trophy 2
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PadelShapes.small)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelTurfCyan.copy(alpha = 0.3f), PadelShapes.small)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "⚡", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Smash King",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelTurfCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Trophy 3
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PadelShapes.small)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelBorderHairline, PadelShapes.small)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Wall Defense",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
