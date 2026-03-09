package io.github.foshlabs.kmp.navigationkit.compose

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

private const val DEFAULT_ANIMATION_DURATION = 350

/**
 * A reusable navigation host that sets up [NavHost] with [CompositionLocalProvider]
 * for [LocalNavigationManager] and default transition animations based on
 * [NavigationTransitionType].
 *
 * This encapsulates the boilerplate of creating a [NavController], [NavigationManager],
 * providing them via CompositionLocal, and configuring push/modal/replace/none animations.
 *
 * @param startDestination The start destination route object.
 * @param animationDuration Duration in milliseconds for transition animations.
 * @param builder Lambda to register composable destinations. Receives the [NavController]
 *   so screens can use it for navigation.
 */
@Composable
fun NavigationHost(
    startDestination: Any,
    animationDuration: Int = DEFAULT_ANIMATION_DURATION,
    builder: NavGraphBuilder.(NavController) -> Unit
) {
    val navController = rememberNavController()
    val navigationManager = remember { NavigationManager() }

    CompositionLocalProvider(LocalNavigationManager provides navigationManager) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                when (navigationManager.currentTransitionType) {
                    NavigationTransitionType.Push -> slideInHorizontally(
                        animationSpec = tween(animationDuration),
                        initialOffsetX = { fullWidth -> fullWidth }
                    )
                    NavigationTransitionType.Modal -> slideInVertically(
                        animationSpec = tween(animationDuration),
                        initialOffsetY = { fullHeight -> fullHeight }
                    )
                    NavigationTransitionType.Replace -> fadeIn(
                        animationSpec = tween(animationDuration)
                    )
                    NavigationTransitionType.None -> EnterTransition.None
                }
            },
            exitTransition = {
                when (navigationManager.currentTransitionType) {
                    NavigationTransitionType.Push -> slideOutHorizontally(
                        animationSpec = tween(animationDuration),
                        targetOffsetX = { fullWidth -> -fullWidth / 3 }
                    )
                    NavigationTransitionType.Modal -> fadeOut(
                        animationSpec = tween(animationDuration)
                    )
                    NavigationTransitionType.Replace -> fadeOut(
                        animationSpec = tween(animationDuration)
                    )
                    NavigationTransitionType.None -> ExitTransition.None
                }
            },
            popEnterTransition = {
                when (navigationManager.currentTransitionType) {
                    NavigationTransitionType.Push -> slideInHorizontally(
                        animationSpec = tween(animationDuration),
                        initialOffsetX = { fullWidth -> -fullWidth / 3 }
                    )
                    NavigationTransitionType.Modal -> fadeIn(
                        animationSpec = tween(animationDuration)
                    )
                    NavigationTransitionType.Replace -> fadeIn(
                        animationSpec = tween(animationDuration)
                    )
                    NavigationTransitionType.None -> EnterTransition.None
                }
            },
            popExitTransition = {
                when (navigationManager.currentTransitionType) {
                    NavigationTransitionType.Push -> slideOutHorizontally(
                        animationSpec = tween(animationDuration),
                        targetOffsetX = { fullWidth -> fullWidth }
                    )
                    NavigationTransitionType.Modal -> slideOutVertically(
                        animationSpec = tween(animationDuration),
                        targetOffsetY = { fullHeight -> fullHeight }
                    )
                    NavigationTransitionType.Replace -> fadeOut(
                        animationSpec = tween(animationDuration)
                    )
                    NavigationTransitionType.None -> ExitTransition.None
                }
            }
        ) {
            builder(navController)
        }
    }
}
