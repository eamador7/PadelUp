package com.padelscoring.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// PADEL UP DESIGN SYSTEM - "PADEL COURT ATHLETIC"
// High-visibility, outdoor sunlight & floodlight-optimized sports palette
// ============================================================================

// Electric Court Volt / Lime (The signature accent, high-vis padel ball felt)
val PadelVolt = Color(0xFFCCFF00)
val PadelVoltHover = Color(0xFFD8FF33)
val PadelVoltPressed = Color(0xFFB5E600)
val PadelVoltSubtle = Color(0x1FCCFF00) // 12% alpha for glows and pill backgrounds
val PadelVoltBorder = Color(0x66CCFF00) // 40% alpha for active state borders

// Court Deep Obsidian / Charcoal Void (Ground & Surfaces)
val PadelObsidianDarkest = Color(0xFF080B10) // Pure background
val PadelObsidian = Color(0xFF0F141C)        // Main surface
val PadelSurface = Color(0xFF161D26)         // Card surface
val PadelSurfaceElevated = Color(0xFF1E2734) // Modals, bottom sheets, elevated cards
val PadelSurfaceVariant = PadelSurfaceElevated
val PadelSurfaceGlass = Color(0xCC1A2330)    // Frosted glass overlay (80% opacity)

// Court Turf Accents (Premier Padel Classic Blue Turf)
val PadelTurfBlue = Color(0xFF0E253F)
val PadelTurfBlueAccent = Color(0xFF0091FF)
val PadelTurfCyan = Color(0xFF00E5FF)

// Court Lines & Metal Mesh Glass Accents
val PadelBorderHairline = Color(0xFF283546)
val PadelBorderGlass = Color(0x40FFFFFF)
val PadelMeshDivider = Color(0xFF232D3B)

// High-Contrast Outdoor Readability Typography (WCAG AA & AAA compliant on PadelObsidian)
val PadelTextPrimary = Color(0xFFFFFFFF)
val PadelTextSecondary = Color(0xFFCBD5E1) // High-contrast Slate 300 (9.8:1 contrast)
val PadelTextMuted = Color(0xFF94A3B8)     // High-visibility Slate 400 (5.6:1 contrast)
val PadelTextInverse = Color(0xFF080B10)

// Tactical Status Indicators
val PadelGreenWin = Color(0xFF10B981)
val PadelGreenSubtle = Color(0x2610B981)
val PadelRedFault = Color(0xFFEF4444)
val PadelRedSubtle = Color(0x26EF4444)
val PadelAmberTiebreak = Color(0xFFF59E0B)
val PadelAmberSubtle = Color(0x26F59E0B)
val PadelSkyOffline = Color(0xFF38BDF8)
val PadelSkySubtle = Color(0x2638BDF8)
