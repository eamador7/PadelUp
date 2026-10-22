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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*

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
    groupName: String = "Friday Night Padel Club",
    memberCount: Int = 16,
    onStartAmericanoOnline: () -> Unit = {},
    onStartAmericanoOffline: () -> Unit = {}
) {
    var showStartModal by remember { mutableStateOf(false) }

    val members = listOf(
        GroupMemberRanking(1, "Alejandro Galán", 1845, 6, 78),
        GroupMemberRanking(2, "Arturo Coello", 1812, 5, 75),
        GroupMemberRanking(3, "Juan Lebrón", 1760, 4, 69),
        GroupMemberRanking(4, "Agustín Tapia", 1735, 3, 67),
        GroupMemberRanking(5, "Paquito Navarro", 1690, 2, 62),
        GroupMemberRanking(6, "Franco Stupaczuk", 1640, 2, 58),
        GroupMemberRanking(7, "Martín Di Nenno", 1595, 1, 54),
        GroupMemberRanking(8, "Fernando Belasteguín", 1580, 1, 50)
    )

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GROUPS & LEADERBOARD",
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
        },
        bottomBar = {
            // Master Action Bar: Start Americano
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PadelObsidian)
                    .padding(20.dp)
            ) {
                PadelPrimaryButton(
                    text = "START AMERICANO TOURNAMENT",
                    onClick = { showStartModal = true },
                    modifier = Modifier.fillMaxWidth()
                )
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
            // Group Hero Overview Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelCourtCardShape)
                        .background(PadelSurface)
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
                                text = groupName,
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
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$memberCount MEMBERS",
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

            // Group Members Leaderboard
            item {
                Text(
                    text = "GROUP RANKINGS",
                    style = PadelTypography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        color = PadelTextMuted
                    )
                )
            }

            items(members) { member ->
                val isPodium = member.rank <= 3
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelShapes.medium)
                        .background(if (member.rank == 1) PadelVoltSubtle else PadelSurface)
                        .border(
                            1.dp,
                            if (member.rank == 1) PadelVoltBorder else PadelBorderHairline,
                            PadelShapes.medium
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${member.rank}",
                        style = PadelTypography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (member.rank == 1) PadelVolt else PadelTextSecondary,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.width(32.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = member.name,
                            style = PadelTypography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PadelTextPrimary
                            )
                        )
                        Text(
                            text = "${member.trophiesWon} 🏆 Americano Titles • ${member.winRate}% Win",
                            style = PadelTypography.bodySmall.copy(color = PadelTextMuted)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${member.elo}",
                            style = PadelTypography.headlineSmall.copy(
                                color = if (isPodium) PadelVolt else PadelTextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Text(
                            text = "ELO",
                            style = PadelTypography.labelSmall.copy(
                                color = PadelTextMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet for choosing Mode: Online Group Sync vs Quick Offline Guest Mode
    if (showStartModal) {
        AlertDialog(
            onDismissRequest = { showStartModal = false },
            containerColor = PadelSurfaceElevated,
            title = {
                Text(
                    text = "SELECT TOURNAMENT MODE",
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

                    // Option 1: Online Group Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PadelShapes.medium)
                            .background(PadelSurface)
                            .border(1.dp, PadelVoltBorder, PadelShapes.medium)
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
                                text = "ONLINE GROUP MODE",
                                style = PadelTypography.labelLarge.copy(
                                    color = PadelVolt,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Live sync across phones. Updates group Elo and stats automatically.",
                            style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                        )
                    }

                    // Option 2: Quick Offline Guest Mode
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PadelShapes.medium)
                            .background(PadelSurface)
                            .border(1.dp, PadelSkyOffline.copy(alpha = 0.4f), PadelShapes.medium)
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
                            Text(text = "🛡️", fontSize = 18.sp)
                            Text(
                                text = "QUICK OFFLINE GUEST MODE",
                                style = PadelTypography.labelLarge.copy(
                                    color = PadelSkyOffline,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero account needed. Add local player names and run instantly from 1 device.",
                            style = PadelTypography.bodySmall.copy(color = PadelTextSecondary)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStartModal = false }) {
                    Text("Cancel", color = PadelTextMuted)
                }
            }
        )
    }
}
