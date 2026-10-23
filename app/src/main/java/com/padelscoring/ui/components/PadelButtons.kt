package com.padelscoring.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.padelscoring.ui.theme.*
import kotlin.math.roundToInt

/**
 * Emil Kowalski Tactile Button:
 * Uses spring physics scale-on-press (0.96) so the button feels physically responsive to touch.
 */
@Composable
fun PadelPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    haptics: PadelHapticFeedback? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "ButtonPressSpring"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .clip(PadelScoreButtonShape)
            .background(if (enabled) PadelVolt else PadelSurfaceElevated)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                haptics?.heavyConfirm()
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = PadelTypography.labelLarge.copy(
                color = if (enabled) PadelOnPrimary else PadelTextMuted,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Secondary ghost button with frosted court outline.
 */
@Composable
fun PadelSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "SecondaryButtonSpring"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .clip(PadelScoreButtonShape)
            .background(PadelSurfaceVariant)
            .border(1.dp, PadelBorderHairline, PadelScoreButtonShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = PadelTypography.labelLarge.copy(
                color = if (enabled) PadelTextPrimary else PadelTextMuted,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * Emil Kowalski Slide-To-Confirm Gesture Component:
 * Prevents accidental taps by sweaty players on court.
 * Smooth dragging with spring resistance and instant snap on threshold.
 */
@Composable
fun PadelSlideToConfirm(
    title: String = "SLIDE TO CONFIRM RESULT",
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    haptics: PadelHapticFeedback = rememberPadelHaptics()
) {
    var componentWidth by remember { mutableFloatStateOf(0f) }
    val thumbWidth = 56.dp
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }

    val maxDrag = (componentWidth - 64).coerceAtLeast(1f)
    val progress = (dragOffset / maxDrag).coerceIn(0f, 1f)

    val animatedOffset by animateFloatAsState(
        targetValue = if (isConfirmed) maxDrag else dragOffset,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f),
        label = "SlideOffsetSpring"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .onSizeChanged { componentWidth = it.width.toFloat() }
            .clip(PadelPillShape)
            .background(PadelSurface)
            .border(1.dp, if (progress > 0.8f) PadelVoltBorder else PadelBorderHairline, PadelPillShape)
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Progress Fill
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .clip(PadelPillShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PadelVoltSubtle, PadelVoltBorder)
                    )
                )
        )

        // Center Hint Text
        Text(
            text = if (isConfirmed) "CONFIRMED ✓" else title,
            style = PadelTypography.labelLarge.copy(
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
                color = if (isConfirmed) PadelVolt else PadelTextSecondary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Draggable Tactile Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .size(thumbWidth)
                .clip(CircleShape)
                .background(PadelVolt)
                .pointerInput(isConfirmed) {
                    if (isConfirmed) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragOffset > maxDrag * 0.85f) {
                                dragOffset = maxDrag
                                isConfirmed = true
                                haptics.heavyConfirm()
                                onConfirmed()
                            } else {
                                dragOffset = 0f
                                haptics.tick()
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragOffset = (dragOffset + dragAmount).coerceIn(0f, maxDrag)
                            if (dragOffset >= maxDrag * 0.85f && !isConfirmed) {
                                haptics.milestone()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                style = PadelTypography.titleLarge.copy(
                    color = PadelTextInverse,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )
        }
    }
}
