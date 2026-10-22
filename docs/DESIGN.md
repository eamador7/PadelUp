# PADEL UP — DESIGN SYSTEM & UI SPECIFICATION
**Visual World:** "Padel Court Athletic"  
**Platform:** Android Jetpack Compose (Material 3 Native)  
**Design Philosophy:** Impeccable Design System × Emil Kowalski Motion & Tactile Craft  
**Target Surface:** Pro Padel Courts, Sunlight & Floodlight Glare Environments, Tournament Command Centers  

---

## 1. Executive Vision & Design Direction

Padel is the fastest-growing racket sport on earth. Unlike traditional tennis, padel is loud, intensely social, electric, and physical—enclosed within 10mm tempered glass walls, steel mesh fences, and vivid blue synthetic turf courts.

Most racket-sport scoring apps fail on the court because they are designed like desktop spreadsheets: tiny text, gray-on-gray low-contrast controls, and fragile button targets that are impossible to tap with sweaty hands under blinding Mexican sun or vibrating floodlights.

**Padel Up** establishes a new design benchmark: **"Padel Court Athletic"**. It pairs the raw electric energy of Premier Padel and the World Padel Tour with the meticulous interaction craft of Emil Kowalski—every point addition yields a crisp haptic tick and rolling odometer number bounce; every match confirmation requires an intentional, satisfying tactile slide; and every tournament standing change springs dynamically across the screen.

---

## 2. Visual World & Brand Identity: "Padel Court Athletic"

### 2.1 The Environmental Scene & Atmosphere
- **The Scene:** Players on Court 1 and Court 2 resting between games, holding their rackets with one hand and their Android devices with the other. Direct overhead floodlights or midday sunlight create harsh glare.
- **The Counter-Rut:** We categorically refuse generic muted tennis green, bland white corporate dashboards, and unstyled grey lists.
- **The Aesthetic Tone:** Obsidian midnight court chassis (`#0F141C`), charged by high-visibility **Electric Court Lime (`#CCFF00`)**, deep blue turf accents (`#0E253F`), and frosted tempered-glass container panels.

### 2.2 Color Architecture & Semantic Tokens

| Token | Hex Value | Opacity / Role | Environmental Purpose |
|---|---|---|---|
| `PadelVolt` | `#CCFF00` | 100% Core Accent | Signature high-vis padel ball felt; primary CTAs, active highlights |
| `PadelVoltHover` | `#D8FF33` | 100% Highlight | Interactive touch highlight |
| `PadelVoltPressed` | `#B5E600` | 100% Pressed | Tactile button press feedback |
| `PadelVoltSubtle` | `#1FCCFF00` | 12% Tint | Glows, rank #1 highlight, badge pill backgrounds |
| `PadelVoltBorder` | `#66CCFF00` | 40% Alpha | Active court borders, focus outlines |
| `PadelObsidianDarkest` | `#080B10` | 100% Background | Pure void background minimizing OLED power draw |
| `PadelObsidian` | `#0F141C` | 100% Surface | App bar, command center header, persistent bottom bars |
| `PadelSurface` | `#161D26` | 100% Card Surface | Court visualizers, score pads, player rows |
| `PadelSurfaceElevated` | `#1E2734` | 100% Modal / Sheet | Dialogs, bottom sheets, secondary button fill |
| `PadelSurfaceGlass` | `#CC1A2330` | 80% Frosted Glass | Glass panel enclosures simulating court walls |
| `PadelTurfBlue` | `#0E253F` | 100% Court Turf | Authentic synthetic court turf for court schematics |
| `PadelTurfCyan` | `#00E5FF` | 100% Secondary Accent | Live court indicator, Revés badge, secondary stats |
| `PadelBorderHairline` | `#283546` | 100% Hairline Border | 1dp clean boundary for cards and inputs |
| `PadelTextPrimary` | `#FFFFFF` | 100% High-Contrast | Pure white for outdoor readability (7:1+ contrast ratio) |
| `PadelTextSecondary` | `#94A3B8` | Slate 400 | Player team descriptions, secondary metadata |
| `PadelTextMuted` | `#64748B` | Slate 500 | Labels, timestamps, subtle headers |
| `PadelGreenWin` | `#10B981` | Emerald Green | Win indicators, positive delta pills |
| `PadelRedFault` | `#EF4444` | Coral Red | Faults, losses, negative delta chips, disputes |
| `PadelAmberTiebreak` | `#F59E0B` | Amber Orange | Tiebreak indicator, pending confirmation warning |
| `PadelSkyOffline` | `#38BDF8` | Sky Blue | 100% Offline / Local Privacy Shield badge |

### 2.3 Aerodynamic Court Geometry & Shapes
Pro padel rackets feature aerodynamic curves with circular perforation grids; courts feature 10mm glass sheets bounded by steel posts.
- **Button Radius:** `16dp` (`PadelScoreButtonShape`) — large, comfortable touch surface.
- **Card Radius:** `18dp` (`PadelCourtCardShape`) — framed by subtle `1dp` glass-cut borders (`#283546`).
- **Capsule Pills:** `RoundedCornerShape(50)` (`PadelPillShape`) — rank deltas, side badges, offline shields.
- **Bottom Sheets:** `24dp` top radius (`PadelSheetTopShape`).

### 2.4 Typography System: "Athletic Display & Humanist Precision"

```
Score Display:    54sp ExtraBold  Tabular Athletic Digits
Match Headers:    26sp Bold       Clean Humanist Sans
Player Names:     17sp SemiBold   High Legibility
Stat Numbers:     20sp ExtraBold  Electric Volt Accent
Badges & Deltas:  11sp Bold       All-Caps Tracked (+0.8sp)
```

- **Athletic Display Numerals (`displayLarge` 52-56sp, `displayMedium` 36sp):** ExtraBold weight, tabular lining figures to prevent score jitter during point increments.
- **Humanist Sans (`titleLarge`, `titleMedium`, `bodyLarge`):** Open apertures for instant glanceability from 2 feet away when the phone rests on the court bench.
- **All-Caps Tracking (`labelSmall` 11sp, letter-spacing `0.8sp`):** Used on status tags (`PENDING`, `DRIVE`, `REVÉS`, `FINAL`).

### 2.5 Distinctive Iconography System
Custom Compose Canvas & Vector components avoiding generic stock tennis assets:
1. **The Perforated Teardrop Racket (`PadelRacketIcon`):** Aerodynamic padel racket outline with the signature 6-hole ventilation pattern drill-grid.
2. **The High-Vis Padel Ball (`PadelBallIcon`):** Electric volt felt sphere with dark contrast curved seam path.
3. **The Court Matrix (`PadelCourtGridIcon`):** Enclosed glass perimeter with center net line and dual service boxes.
4. **The Verified Handshake:** Crossed rackets marking verified results.

---

## 3. Emil Kowalski Motion & Tactile Craft

Every interaction in Padel Up follows the four rules of Emil Kowalski design engineering:

| Rule | Implementation in Padel Up |
|---|---|
| **1. Purpose over decoration** | Motion only exists to confirm physical touch, indicate point additions, or show standing shifts. |
| **2. Fast UI durations (<250ms)** | Button press animations settle in 140ms; score numbers roll in 180ms. |
| **3. Strong custom curves & springs** | Uses `spring(dampingRatio = 0.7f, stiffness = 400f)` instead of weak CSS linear/ease-in. |
| **4. Responsive press feedback** | Buttons scale down to `0.96` on touch down; release snaps back instantly with tactile haptics. |

### 3.1 The Animated Digit Odometer (Score Updates)
When a player scores a point:
- **Upward Increment:** The incoming number slides in from the bottom while the outgoing number slides out toward the top (`slideInVertically { height } + fadeIn()`).
- **Downward Decrement:** Reverses seamlessly from the top.
- **Spring Tuning:** `spring(dampingRatio = 0.70f, stiffness = 400f)`.
- **Haptic Trigger:** Generates an immediate `VibrationEffect.EFFECT_TICK` (or 12ms impulse).

### 3.2 Slide-To-Confirm Gesture Physics
Court scores must not be accidentally finalized by sweaty thumbs. 
- The user drags a circular Electric Volt thumb along a frosted obsidian track.
- **Velocity & Drag Threshold:** Snaps to completion once dragged past 85% width.
- **Release without completion:** Springs back to origin (`spring(dampingRatio = 0.75f, stiffness = 350f)`) with a light tick.
- **Successful confirmation:** Triggers `VibrationEffect.EFFECT_HEAVY_CLICK` (40ms solid thud) and transitions state to `"CONFIRMED ✓"`.

### 3.3 Dynamic Leaderboard Spring Placement
As Americano match scores are entered:
- `Modifier.animateItemPlacement(animationSpec = spring(dampingRatio = 0.78f, stiffness = 380f))` reorders rows smoothly without flickering.
- Players climbing the ranks show an animated `RankDeltaChip` (`▲ +2`) pulsing with subtle scale entry (`0.92f` to `1.0f`).

### 3.4 Tactile Haptic Matrix

```
Score Tap (+1 / -1)       ───► Light Tick          (EFFECT_TICK / 12ms)
Target 32 Pts Reached     ───► Milestone Double    (EFFECT_DOUBLE_CLICK / 20ms-40ms-20ms)
Slide to Confirm Locked   ───► Heavy Solid Thud    (EFFECT_HEAVY_CLICK / 40ms)
Match Dispute / Warning   ───► Dual Warning Pulse  (35ms-60ms-35ms)
```

---

## 4. Screen Specifications & Flows

```
                   ┌─────────────────────────────────────────┐
                   │            GROUPS & CLUBS HUB           │
                   │  • Squad Elo Leaderboard                │
                   │  • Americano Trophy Cabinet             │
                   └────────────────────┬────────────────────┘
                                        │
                         [Start Americano CTA]
                                        │
                 ┌──────────────────────┴──────────────────────┐
                 ▼                                             ▼
  ┌─────────────────────────────┐               ┌─────────────────────────────┐
  │     ONLINE GROUP MODE       │               │   OFFLINE QUICK LAUNCHER    │
  │ • Live Sync Across Phones   │               │ • 100% Local Privacy Shield │
  │ • Official Group Elo Update │               │ • 4/8/12/16 Player Presets  │
  └──────────────┬──────────────┘               └──────────────┬──────────────┘
                 │                                             │
                 └──────────────────────┬──────────────────────┘
                                        ▼
                   ┌─────────────────────────────────────────┐
                   │    AMERICANO COMMAND CENTER (HERO)      │
                   │  • Court Visualizer (Court 1, 2, ...)   │
                   │  • Tactile Quick Score Pad              │
                   │  • Live Standings & Dynamic Shifts      │
                   │  • Next Round Generator Banner          │
                   └────────────────────┬────────────────────┘
                                        │
                                [Tap Court Match]
                                        ▼
                   ┌─────────────────────────────────────────┐
                   │        MATCH CONFIRMATION FLOW          │
                   │  • Pending Verification Badge           │
                   │  • Tactile Slide-To-Confirm Bar         │
                   │  • Disputed Match Reason Selector       │
                   └─────────────────────────────────────────┘
```

### 4.1 Americano Tournament Command Center (THE HERO FEATURE)
- **Top Tournament Header:**
  - Padel racket icon + `"AMERICANO COMMAND"`.
  - Tournament Title: e.g. "Friday Night Masters".
  - Round Badge: `"ROUND 3 OF 7"` in `PadelVoltSubtle`.
  - Progress Bar: Linear progress showing tournament completion percentage.
- **Mode Switcher (Pill Segmented Control):**
  - `[ ACTIVE COURTS ]` vs `[ LIVE STANDINGS ]`.
- **Active Courts View:**
  - **Court Visualizer Card (`PadelCourtVisualizerCard`):** Top-down padel court visualizer rendered with blue synthetic turf (`#0E253F`), crisp white service lines and net line, active pairs ("Ale Galán & Arturo Coello vs Juan Lebrón & Agustín Tapia"), live score badge, and tap-to-expand.
  - **Tactile Quick Score Pad (`PadelQuickScorePad`):** Dual team score counters with big 54sp digits, tactile `+1` / `-1` buttons, and auto-balancing fixed target points (e.g., 18 + 14 = 32 points).
  - **Next Round Generation Banner (`PadelNextRoundBanner`):** Appears when all court matches of the round reach target points. Features an Electric Volt glowing border and instant CTA `"GENERATE ROUND 4"`.
- **Live Standings View:**
  - Rows from `#1` to `#8` displaying Rank, Initials, Name, Played, Won, Point Difference, and Total Points.
  - Rank #1 highlighted with Volt subtle glow and crown/trophy accent.
  - Animated rank delta chips (`▲ +2`, `▼ -1`).

### 4.2 Match Confirmation Flow
- **Verification Header:** Pending confirmation banner in Amber (`#F59E0B`).
- **Match Score Card:** Prominent scoreboard displaying Team A score vs Team B score with player names.
- **Tactile Slide-to-Confirm:** Prevents inadvertent clicks. Dragging the thumb past 85% triggers a heavy haptic click and submits the score.
- **Disputed State:**
  - Tapping "Incorrect score? Dispute this match" activates the dispute reason selector:
    1. Incorrect score recorded
    2. Wrong players in pairing
    3. Match prematurely ended / injury
    4. Rule interpretation conflict
  - Submitting dispute flags the match on the tournament director's screen.

### 4.3 Player Profile & Trends (Playtomic-Inspired)
- **Dynamic Level Badge (`DynamicLevelBadge`):**
  - Center numeral (e.g. `3.42` or `4.85`) bounded by a circular confidence ring in Electric Volt representing rating certainty.
  - Progression Delta Pill: `▲ +0.08` (Volt/Green) or `▼ -0.04` (Red/Coral).
- **Tactical Preferred Side Badge (`CourtSideBadge`):**
  - `"REVÉS"` (Left Side / Backhand): Aggressive smasher / finisher role.
  - `"DRIVE"` (Right Side / Forehand): Tactical builder / control role.
- **10-Match Rolling Form:**
  - 10 circular dots: `[ W ][ W ][ W ][ L ][ W ][ W ][ W ][ L ][ W ][ W ]`.
  - Wins rendered in Electric Volt; losses in Dark Slate.
- **Performance Grid:**
  - Win Rate Card: `74%` in big bold athletic numerals.
  - Best Streak Card: `9W` in Court Cyan.
- **Americano Trophy Cabinet:**
  - Cards celebrating club tournament victories: "4x Champion 🏆", "Smash King ⚡", "Wall Defense 🛡️".

### 4.4 Groups & Group Ranking Hub
- **Group Hero Header:** e.g., "Friday Night Padel Club" (16 Members).
- **Group Leaderboard:** Ranked by Group Elo (e.g., 1845 Elo) and Americano Titles Won.
- **Master CTA:** `"START AMERICANO TOURNAMENT"`.
- **Mode Selection Modal:**
  - Option 1: **Online Group Mode** (Live multi-phone sync, updates group Elo).
  - Option 2: **Quick Offline Guest Mode** (Zero login, run from 1 device).

### 4.5 Offline Tournament Quick-Launcher
- **Privacy Shield:** `"100% OFFLINE • LOCAL ONLY"` blue shield badge.
- **Roster Size Selector:** `[ 4 Players ] [ 8 Players ] [ 12 Players ] [ 16 Players ]`.
- **Target Points Selector:** `[ 21 PTS ] [ 24 PTS ] [ 32 PTS ]`.
- **Instant Guest Roster:** Pre-populated player slots with fast inline editing and history from previous tournaments.
- **Kickoff Button:** `"KICKOFF AMERICANO (8 PLAYERS)"`.

---

## 5. Jetpack Compose Architecture

All files are structured cleanly under `com.padelscoring`:

```
app/src/main/java/com/padelscoring/
├── MainActivity.kt                          // Root Navigation & PadelTheme wrapping
├── ui/
│   ├── theme/
│   │   ├── Color.kt                         // Padel Court Athletic palette & semantic tokens
│   │   ├── Type.kt                          // Athletic display numerals & humanist typography
│   │   ├── Shape.kt                         // Aerodynamic court & button geometries
│   │   └── Theme.kt                         // Dark-mode first high-contrast theme
│   ├── components/
│   │   ├── Haptics.kt                       // Tactile haptic feedback engine
│   │   ├── PadelButtons.kt                  // Tactile scale-press buttons & SlideToConfirm
│   │   ├── PadelCards.kt                    // Court visualizer, Leaderboard row, Next Round banner
│   │   ├── PadelScorePad.kt                 // Dual score counter with rolling digit odometer
│   │   ├── PadelBadge.kt                    // Dynamic level badge, form dots, delta chips
│   │   └── PadelIcons.kt                    // Perforated racket, ball, court grid canvas icons
│   └── screens/
│       ├── AmericanoCommandCenterScreen.kt  // The Hero Feature: Courts, Quick Score, Standings
│       ├── MatchConfirmationScreen.kt       // Slide-to-confirm verification & dispute flow
│       ├── PlayerProfileScreen.kt           // Playtomic-style level, stats, form & side badges
│       ├── GroupsHubScreen.kt               // Group ranking, Elo leaderboard, mode selector
│       └── OfflineQuickLauncherScreen.kt    // Instant local tournament setup with privacy badge
└── docs/
    └── DESIGN.md                            // This comprehensive design specification
```

---

## 6. Verification & Quality Bar Checklist

- [x] **High-Contrast Glare Legibility:** All critical scores are white (`#FFFFFF`) or Electric Volt (`#CCFF00`) on deep obsidian (`#0F141C`) exceeding WCAG AAA contrast ratio (7:1+).
- [x] **One-Handed Court Reachability:** Score tap targets are minimum `52dp`-`64dp` in height and span thumb-accessible bottom zones.
- [x] **Zero Accidental Match Submissions:** Slide-to-confirm requires sustained 85% gesture drag with physical snap feedback.
- [x] **Frame Budget (60 FPS Performance):** All animations run on GPU transforms (`scale`, `translateY`) and `opacity` without expensive layout recalculations.
- [x] **No Hard-Coded Glitches:** Tested against Impeccable UI anti-pattern detector (`impeccable detect`) with 0 issues reported.
