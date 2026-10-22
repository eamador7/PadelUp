package com.padelscoring.ui.navigation

/**
 * Navigation items representing core athletic hub destinations in Padel Up.
 */
sealed class PadelNavigationItem(
    val route: String,
    val title: String,
    val iconEmoji: String,
    val contentDescription: String,
    val testTag: String
) {
    data object Americano : PadelNavigationItem(
        route = "americano",
        title = "Americano",
        iconEmoji = "🎾",
        contentDescription = "Americano tournament command center",
        testTag = "nav_americano"
    )

    data object QuickLaunch : PadelNavigationItem(
        route = "quick_launcher",
        title = "Quick Launch",
        iconEmoji = "⚡",
        contentDescription = "Offline guest tournament launcher",
        testTag = "nav_quick_launcher"
    )

    data object Groups : PadelNavigationItem(
        route = "groups",
        title = "Groups",
        iconEmoji = "👥",
        contentDescription = "Squad leaderboards & rivalries",
        testTag = "nav_groups"
    )

    data object Profile : PadelNavigationItem(
        route = "profile",
        title = "Profile",
        iconEmoji = "👤",
        contentDescription = "Dynamic Level, form, and stats",
        testTag = "nav_profile"
    )

    companion object {
        val items: List<PadelNavigationItem> = listOf(Americano, QuickLaunch, Groups, Profile)
    }
}
