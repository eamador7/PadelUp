package com.padelscoring.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.padelscoring.viewmodel.GroupsHubViewModel

data class GroupMemberRanking(
    val rank: Int,
    val name: String,
    val elo: Int,
    val trophiesWon: Int,
    val winRate: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsHubScreen(
    viewModel: GroupsHubViewModel? = null,
    groupName: String = "Friday Night Padel Club",
    memberCount: Int = 16,
    onStartAmericanoOnline: () -> Unit = {},
    onStartAmericanoOffline: () -> Unit = {}
) {
    val vmState = viewModel?.uiState?.collectAsState()?.value
    var showStartModal by remember { mutableStateOf(false) }

    val defaultMembers = listOf(
        GroupMemberRanking(1, "Alejandro Galán", 1845, 6, 78),
        GroupMemberRanking(2, "Arturo Coello", 1812, 5, 75),
        GroupMemberRanking(3, "Juan Lebrón", 1760, 4, 69),
        GroupMemberRanking(4, "Agustín Tapia", 1735, 3, 67),
        GroupMemberRanking(5, "Paquito Navarro", 1690, 2, 62),
        GroupMemberRanking(6, "Franco Stupaczuk", 1640, 2, 58),
        GroupMemberRanking(7, "Martín Di Nenno", 1595, 1, 54),
        GroupMemberRanking(8, "Fernando Belasteguín", 1580, 1, 50)
    )

    val displayGroupName = vmState?.groupName ?: groupName
    val displayMemberCount = vmState?.memberCount ?: memberCount
    val displayMembers = vmState?.members?.takeIf { it.isNotEmpty() } ?: defaultMembers

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
                                text = "COMMUNITY HUB",
                                style = PadelTypography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = PadelVolt
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "GROUPS & LEADERBOARD",
                            style = PadelTypography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = PadelTextPrimary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(PadelPillShape)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelBorderHairline, PadelPillShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "🎾 ACTIVE CLUB",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelVolt,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Master Action Button inside scroll view
            item {
                PadelPrimaryButton(
                    text = "⚡ START AMERICANO TOURNAMENT",
                    onClick = { showStartModal = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Group Hero Overview Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelCourtCardShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF192332), PadelSurface)
                            )
                        )
                        .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE SQUAD",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelVolt,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = displayGroupName,
                                style = PadelTypography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PadelTextPrimary
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(PadelPillShape)
                                .background(PadelSurfaceElevated)
                                .border(1.dp, PadelBorderHairline, PadelPillShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$displayMemberCount MEMBERS",
                                style = PadelTypography.labelSmall.copy(
                                    color = PadelTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Leaderboard ranks players based on cumulative Group Elo and total Americano tournament titles won.",
                        style = PadelTypography.bodyMedium.copy(color = PadelTextSecondary)
                    )
                }
            }

            // Group Members Leaderboard Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GROUP RANKINGS",
                        style = PadelTypography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            color = PadelTextMuted
                        )
                    )
                    Text(
                        text = "ELO RATING",
                        style = PadelTypography.labelSmall.copy(
                            color = PadelTextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            items(displayMembers) { member ->
                val isPodium = member.rank <= 3
                val rankColor = when (member.rank) {
                    1 -> PadelGoldCrown
                    2 -> PadelSilverMedal
                    3 -> PadelBronzeMedal
                    else -> PadelTextSecondary
                }

                val cardBg = when (member.rank) {
                    1 -> PadelVoltSubtle
                    2 -> PadelSilverSubtle
                    3 -> PadelBronzeSubtle
                    else -> PadelSurface
                }

                val cardBorder = when (member.rank) {
                    1 -> PadelVoltBorder
                    2 -> PadelSilverMedal.copy(alpha = 0.3f)
                    3 -> PadelBronzeMedal.copy(alpha = 0.3f)
                    else -> PadelBorderHairline
                }

                val memberInitials = member.name.split(" ")
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelShapes.medium)
                        .background(cardBg)
                        .border(1.dp, cardBorder, PadelShapes.medium)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${member.rank}",
                        style = PadelTypography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = rankColor,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.width(36.dp)
                    )

                    // Initials Circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (member.rank == 1) PadelVolt else PadelSurfaceElevated)
                            .border(
                                1.dp,
                                if (member.rank == 1) PadelVolt else PadelBorderHairline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = memberInitials,
                            style = PadelTypography.labelMedium.copy(
                                color = if (member.rank == 1) PadelTextInverse else PadelTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.name,
                            style = PadelTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PadelTextPrimary
                            )
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏆 ${member.trophiesWon} Titles",
                                style = PadelTypography.bodySmall.copy(
                                    color = if (member.trophiesWon > 3) PadelGoldCrown else PadelTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                            Text(
                                text = "• ${member.winRate}% Win",
                                style = PadelTypography.bodySmall.copy(
                                    color = PadelTextMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    // Elo Rating Badge
                    Box(
                        modifier = Modifier
                            .clip(PadelShapes.small)
                            .background(if (isPodium) PadelSurfaceElevated else PadelSurface)
                            .border(
                                1.dp,
                                if (isPodium) rankColor.copy(alpha = 0.4f) else PadelBorderHairline,
                                PadelShapes.small
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${member.elo}",
                            style = PadelTypography.titleSmall.copy(
                                color = if (isPodium) rankColor else PadelTextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal to choose Americano Tournament Mode
    if (showStartModal) {
        AlertDialog(
            onDismissRequest = { showStartModal = false },
            title = {
                Text(
                    text = "Launch Americano Tournament",
                    style = PadelTypography.headlineSmall.copy(
                        color = PadelTextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Choose how players will join and record scores for this Americano.",
                        style = PadelTypography.bodyMedium.copy(color = PadelTextSecondary)
                    )

                    // Option 1: Fast Offline Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PadelShapes.medium)
                            .background(PadelSurfaceElevated)
                            .border(1.5.dp, PadelVoltBorder, PadelShapes.medium)
                            .clickable {
                                showStartModal = false
                                onStartAmericanoOffline()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "⚡", fontSize = 18.sp)
                            Text(
                                text = "Single-Device Quick Kickoff",
                                style = PadelTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PadelVolt
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero login needed. Quick guest roster (4, 8, 12, 16 players), instant kickoff.",
                            style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                        )
                    }

                    // Option 2: Online Group Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PadelShapes.medium)
                            .background(PadelSurfaceElevated)
                            .border(1.dp, PadelBorderHairline, PadelShapes.medium)
                            .clickable {
                                showStartModal = false
                                onStartAmericanoOnline()
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🌐", fontSize = 18.sp)
                            Text(
                                text = "Club Squad Live Sync",
                                style = PadelTypography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PadelTurfCyan
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Players log scores simultaneously from their own devices and update club rankings.",
                            style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStartModal = false }) {
                    Text(text = "Dismiss", color = PadelTextMuted)
                }
            },
            containerColor = PadelSurface
        )
    }
}
