package com.padelscoring.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.theme.*

/**
 * High-visibility, tactile modal sheet allowing users to launch either a
 * 2v2 Single Match, a Club Group Americano, or a Guest Offline Americano.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartActivityModal(
    onDismissRequest: () -> Unit,
    onSelectSingleMatch: () -> Unit,
    onSelectGroupAmericano: () -> Unit,
    onSelectGuestAmericano: () -> Unit
) {
    val haptics = rememberPadelHaptics()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = PadelSurfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(PadelPillShape)
                    .background(PadelBorderHairline)
            )
        },
        shape = PadelSheetTopShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PadelRacketIcon(size = 18.dp, tint = PadelBrandBlue)
                    Text(
                        text = "START PADEL ACTIVITY",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelBrandBlue,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select Game Mode",
                    style = PadelTypography.headlineMedium.copy(
                        color = PadelTextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = "Pick a format to begin tracking court action instantly.",
                    style = PadelTypography.bodyMedium.copy(
                        color = PadelTextSecondary
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 1. Single 2v2 Match Card
            ActivityModeOptionCard(
                emoji = "🎾",
                badgeText = "2V2 REGULATION",
                badgeColor = PadelTurfCyan,
                badgeBg = PadelTurfCyanSubtle,
                title = "Single Match (2v2)",
                subtitle = "Traditional padel match with Sets, Games, Golden Point or Advantage tracking.",
                onClick = {
                    haptics.heavyConfirm()
                    onDismissRequest()
                    onSelectSingleMatch()
                }
            )

            // 2. Club Group Americano Card
            ActivityModeOptionCard(
                emoji = "🏆",
                badgeText = "RANKED SQUAD",
                badgeColor = PadelBrandBlue,
                badgeBg = PadelBrandBlueSubtle,
                title = "Club Americano Tournament",
                subtitle = "Multi-player tournament synced with your club roster & group Elo ratings.",
                onClick = {
                    haptics.heavyConfirm()
                    onDismissRequest()
                    onSelectGroupAmericano()
                }
            )

            // 3. Guest Offline Americano Card
            ActivityModeOptionCard(
                emoji = "⚡",
                badgeText = "INSTANT PLAY",
                badgeColor = PadelSkyOffline,
                badgeBg = PadelSkySubtle,
                title = "Guest Americano (Quick Launch)",
                subtitle = "Fast offline rotation for 4 to 32 players with local names and auto-courts.",
                onClick = {
                    haptics.heavyConfirm()
                    onDismissRequest()
                    onSelectGuestAmericano()
                }
            )
        }
    }
}

@Composable
private fun ActivityModeOptionCard(
    emoji: String,
    badgeText: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    badgeBg: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PadelCourtCardShape)
            .background(PadelSurface)
            .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(PadelSurfaceElevated)
                    .border(1.dp, PadelBorderHairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = PadelTypography.titleMedium.copy(
                            color = PadelTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(badgeBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = PadelTypography.labelSmall.copy(
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = PadelTypography.bodySmall.copy(
                        color = PadelTextSecondary,
                        lineHeight = 16.sp
                    )
                )
            }
        }
    }
}
