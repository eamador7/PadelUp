package com.padelscoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    playerName: String = "Alejandro Galán",
    level: Double = 4.85,
    delta: Double = +0.12,
    winRate: Int = 74,
    matchesPlayed: Int = 58,
    bestStreak: Int = 9,
    preferredSide: String = "REVÉS", // "REVÉS" (Left/Backhand) or "DRIVE" (Right/Forehand)
    recentForm: List<Boolean> = listOf(true, true, true, false, true, true, true, false, true, true)
) {
    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PLAYER STATS & PROFILE",
                        style = PadelTypography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = PadelTextPrimary
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PadelObsidian
                )
            )
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
                    .background(PadelSurface)
                    .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Level Badge
                DynamicLevelBadge(
                    level = level,
                    delta = delta,
                    confidenceProgress = 0.92f
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = playerName,
                    style = PadelTypography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PadelTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tactical Court Side Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CourtSideBadge(side = preferredSide)
                    Text(
                        text = if (preferredSide == "REVÉS") "Left Side • Aggressive Finisher" else "Right Side • Tactical Builder",
                        style = PadelTypography.bodySmall.copy(color = PadelTextMuted)
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
                Text(
                    text = "LAST 10 MATCHES FORM",
                    style = PadelTypography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        color = PadelTextMuted
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    recentForm.forEach { isWin ->
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
                        style = PadelTypography.labelSmall.copy(color = PadelTextMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$winRate%",
                        style = PadelTypography.displaySmall.copy(
                            color = PadelVolt,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "$matchesPlayed Total Matches",
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
                        style = PadelTypography.labelSmall.copy(color = PadelTextMuted)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${bestStreak}W",
                        style = PadelTypography.displaySmall.copy(
                            color = PadelTurfCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "Active Streak: 2W",
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
                Text(
                    text = "AMERICANO TROPHIES & BADGES",
                    style = PadelTypography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        color = PadelTextMuted
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Trophy 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PadelShapes.small)
                            .background(PadelSurfaceElevated)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🏆", fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "4x Champion",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelVolt,
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
