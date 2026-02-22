package com.foshlabs.navigation

/**
 * Navigation states for handling different types of navigation in the app.
 *
 * Usage examples:
 * - Push: navigate(NavigationState.Push(AppScene.Onboarding))
 * - Present Sheet: navigate(NavigationState.PresentSheet(AppScene.Settings))
 * - Replace Root: navigate(NavigationState.ReplaceRoot(AppScene.TabBar))
 */
sealed interface NavigationState {
    data object None : NavigationState
    data class ReplaceRoot(val destination: Scene) : NavigationState

    data class PresentSheet(val destination: Scene) : NavigationState
    data class PresentFullScreen(val destination: Scene) : NavigationState
    data object Dismiss : NavigationState

    data class Push(val destination: Scene) : NavigationState
    data class PopTo(val destination: Scene?, val inclusive: Boolean = false) : NavigationState
    data object Pop : NavigationState
    data object PopToRoot : NavigationState
}
