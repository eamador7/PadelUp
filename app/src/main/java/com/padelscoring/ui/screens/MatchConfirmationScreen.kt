package com.padelscoring.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.components.*
import com.padelscoring.ui.theme.*
import com.padelscoring.viewmodel.MatchConfirmationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchConfirmationScreen(
    viewModel: MatchConfirmationViewModel? = null,
    courtNumber: Int = 1,
    teamANames: Pair<String, String> = Pair("Ale Galán", "Arturo Coello"),
    teamBNames: Pair<String, String> = Pair("Juan Lebrón", "Agustín Tapia"),
    finalScoreA: Int = 18,
    finalScoreB: Int = 14,
    onConfirmed: () -> Unit = {},
    onDisputeSubmitted: (reason: String) -> Unit = {}
) {
    val haptics = rememberPadelHaptics()
    val vmState = viewModel?.uiState?.collectAsState()?.value

    var localIsDisputed by remember { mutableStateOf(false) }
    var selectedDisputeReason by remember { mutableStateOf<String?>(null) }
    val disputeReasons = listOf(
        "Incorrect score recorded",
        "Wrong players in pairing",
        "Match prematurely ended / injury",
        "Rule interpretation conflict"
    )

    val isDisputed = vmState?.isDisputed ?: localIsDisputed
    val displayCourtNumber = vmState?.courtNumber ?: courtNumber
    val displayRoundNumber = vmState?.roundNumber ?: 3
    val displayTeamA = vmState?.teamANames ?: teamANames
    val displayTeamB = vmState?.teamBNames ?: teamBNames
    val displayFinalScoreA = vmState?.finalScoreA ?: finalScoreA
    val displayFinalScoreB = vmState?.finalScoreB ?: finalScoreB

    Scaffold(
        containerColor = PadelObsidianDarkest,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MATCH VERIFICATION",
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pending Verification Status Badge
            Box(
                modifier = Modifier
                    .clip(PadelPillShape)
                    .background(if (isDisputed) PadelRedSubtle else PadelAmberSubtle)
                    .border(
                        1.dp,
                        if (isDisputed) PadelRedFault.copy(alpha = 0.5f) else PadelAmberTiebreak.copy(alpha = 0.5f),
                        PadelPillShape
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isDisputed) PadelRedFault else PadelAmberTiebreak)
                    )
                    Text(
                        text = if (isDisputed) "DISPUTED MATCH" else "PENDING CONFIRMATION",
                        style = PadelTypography.labelSmall.copy(
                            color = if (isDisputed) PadelRedFault else PadelAmberTiebreak,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
            }

            // Hero Match Score Summary Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PadelCourtCardShape)
                    .background(PadelSurface)
                    .border(1.dp, PadelBorderHairline, PadelCourtCardShape)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "COURT $displayCourtNumber • ROUND $displayRoundNumber",
                    style = PadelTypography.labelSmall.copy(
                        color = PadelTextMuted,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Big Scoreboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team A
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$displayFinalScoreA",
                            style = PadelTypography.displayLarge.copy(
                                color = if (displayFinalScoreA > displayFinalScoreB) PadelVolt else PadelTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 56.sp
                            )
                        )
                        Text(
                            text = "${displayTeamA.first}\n${displayTeamA.second}",
                            style = PadelTypography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PadelTextSecondary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "—",
                        style = PadelTypography.displaySmall.copy(color = PadelTextMuted)
                    )

                    // Team B
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$displayFinalScoreB",
                            style = PadelTypography.displayLarge.copy(
                                color = if (displayFinalScoreB > displayFinalScoreA) PadelVolt else PadelTextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 56.sp
                            )
                        )
                        Text(
                            text = "${displayTeamB.first}\n${displayTeamB.second}",
                            style = PadelTypography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PadelTextSecondary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Slide To Confirm Component (Tactile Verification)
            if (!isDisputed) {
                Text(
                    text = "Both pairs must verify the final result before advancing.",
                    style = PadelTypography.bodyMedium.copy(
                        color = PadelTextSecondary,
                        textAlign = TextAlign.Center
                    )
                )

                PadelSlideToConfirm(
                    title = "SLIDE TO CONFIRM RESULT",
                    onConfirmed = {
                        haptics.heavyConfirm()
                        viewModel?.onSlideToConfirm()
                        onConfirmed()
                    },
                    haptics = haptics
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dispute Link
                Text(
                    text = "Incorrect score? Dispute this match",
                    style = PadelTypography.labelMedium.copy(
                        color = PadelAmberTiebreak,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.clickable {
                        haptics.warning()
                        localIsDisputed = true
                    }
                )
            } else {
                // Dispute Selection Sheet
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PadelCourtCardShape)
                        .background(PadelSurfaceVariant)
                        .border(1.dp, PadelRedSubtle, PadelCourtCardShape)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "SELECT DISPUTE REASON",
                        style = PadelTypography.labelLarge.copy(
                            color = PadelRedFault,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    disputeReasons.forEach { reason ->
                        val isSelected = selectedDisputeReason == reason
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(PadelShapes.medium)
                                .background(if (isSelected) PadelRedSubtle else PadelSurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) PadelRedFault else PadelBorderHairline,
                                    PadelShapes.medium
                                )
                                .clickable {
                                    haptics.tick()
                                    selectedDisputeReason = reason
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reason,
                                style = PadelTypography.bodyMedium.copy(
                                    color = if (isSelected) PadelTextPrimary else PadelTextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                            if (isSelected) {
                                Text(
                                    text = "✓",
                                    color = PadelRedFault,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PadelPrimaryButton(
                        text = "SUBMIT DISPUTE TO COORDINATOR",
                        onClick = {
                            selectedDisputeReason?.let { reason ->
                                viewModel?.onDispute(reason)
                                onDisputeSubmitted(reason)
                            }
                        },
                        enabled = selectedDisputeReason != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PadelSecondaryButton(
                        text = "Cancel",
                        onClick = { localIsDisputed = false },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
