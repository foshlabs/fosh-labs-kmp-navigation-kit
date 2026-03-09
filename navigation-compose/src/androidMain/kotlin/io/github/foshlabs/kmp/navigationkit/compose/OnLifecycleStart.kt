package io.github.foshlabs.kmp.navigationkit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.LifecycleResumeEffect

/**
 * Executes the given action when the lifecycle reaches the RESUMED state.
 *
 * Inside a NavHost, the NavBackStackEntry lifecycle reaches RESUMED only
 * after the enter transition animation completes, so data loads won't
 * interrupt the animation.
 *
 * @param action The action to execute when RESUMED is reached
 */
@Deprecated("Use OnLifecycleStart(onAppear = { ... }) instead")
@Composable
fun OnLifecycleStart(action: () -> Unit) {
    LifecycleResumeEffect(Unit) {
        action()
        onPauseOrDispose { }
    }
}

/**
 * Lifecycle-aware effect with optional onFirstAppear, onAppear, and onDisappear handlers.
 *
 * Inside a NavHost, the NavBackStackEntry lifecycle reaches RESUMED only
 * after the enter transition animation completes, so handlers won't run
 * during the enter animation.
 *
 * - [onFirstAppear]: Called only the first time the screen becomes RESUMED (e.g. initial load).
 * - [onAppear]: Called every time the screen becomes RESUMED (including returning from background).
 * - [onDisappear]: Called when the lifecycle leaves RESUMED (paused or disposed).
 *
 * @param onFirstAppear Optional. Called once on first appearance.
 * @param onAppear Optional. Called on every resume.
 * @param onDisappear Optional. Called when the screen is paused or disposed.
 * @param key Optional. Keys used to restart the effect (e.g. when screen args change).
 */
@Composable
fun OnLifecycleStart(
    onFirstAppear: (() -> Unit)? = null,
    onAppear: (() -> Unit)? = null,
    onDisappear: (() -> Unit)? = null,
    key: Any? = Unit
) {
    val hasAppearedOnce = rememberSaveable(key) { mutableStateOf(false) }
    LifecycleResumeEffect(key) {
        if (onFirstAppear != null && !hasAppearedOnce.value) {
            hasAppearedOnce.value = true
            onFirstAppear()
        }
        onAppear?.invoke()

        onPauseOrDispose {
            onDisappear?.invoke()
        }
    }
}
