package io.github.foshlabs.kmp.navigationkit.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController

/**
 * Describes how a navigation transition should be animated.
 */
enum class NavigationTransitionType {
    /** Horizontal slide (push from right, pop to right) — used for Push/Pop */
    Push,
    /** Vertical slide (slide up from bottom, dismiss downward) — used for PresentSheet/PresentFullScreen/Dismiss */
    Modal,
    /** Crossfade — used for ReplaceRoot */
    Replace,
    /** No animation */
    None
}

/**
 * Navigation manager that tracks modal navigation stacks and transition animations.
 *
 * This mirrors the iOS ModalNavigationManager behavior where:
 * - PresentFullScreen/PresentSheet creates a new modal context
 * - Push/Pop within a modal operates within that modal's stack
 * - Dismiss pops the entire modal stack back to the entry point
 *
 * It also tracks transition types so that NavHost can apply the correct animation
 * based on how the navigation was triggered (push, modal, replace) without
 * per-screen hardcoding.
 */
class NavigationManager {

    // MARK: - Properties

    /**
     * Stack of back stack entry IDs that mark the start of modal presentations.
     * When a modal is presented, the current back stack entry ID is pushed here.
     * When Dismiss is called, we pop back to the entry at the top of this stack.
     */
    private val modalEntryStack = mutableListOf<String>()

    /**
     * The current transition type. Set before each navigation call so that
     * NavHost transition lambdas can read it to determine the animation.
     */
    var currentTransitionType: NavigationTransitionType by mutableStateOf(NavigationTransitionType.None)

    /**
     * History of transition types matching the navigation back stack.
     * When a screen is navigated to, its transition type is pushed here.
     * When popping/dismissing, the type is popped so the exit animation
     * matches the enter animation.
     */
    private val transitionHistory = mutableListOf<NavigationTransitionType>()

    /**
     * Whether we're currently inside a modal context.
     */
    val isInModalContext: Boolean
        get() = modalEntryStack.isNotEmpty()

    // MARK: - Transition Tracking

    /**
     * Records a transition type for a forward navigation (Push, PresentSheet, etc.).
     */
    fun pushTransition(type: NavigationTransitionType) {
        currentTransitionType = type
        transitionHistory.add(type)
    }

    /**
     * Pops the most recent transition type and sets it as current.
     * Used for Pop/Dismiss so the exit animation matches the enter animation.
     */
    fun popTransition() {
        currentTransitionType = if (transitionHistory.isNotEmpty()) {
            transitionHistory.removeAt(transitionHistory.lastIndex)
        } else {
            NavigationTransitionType.None
        }
    }

    /**
     * Pops transition history entries until the stack size matches [targetSize].
     * Used when dismissing a modal that may have multiple screens pushed on top.
     */
    fun popTransitionsTo(targetSize: Int) {
        val toPop = transitionHistory.size - targetSize
        if (toPop > 0) {
            // The first popped entry determines the animation (the modal presentation itself)
            val firstPopped = transitionHistory.removeAt(transitionHistory.lastIndex)
            repeat(toPop - 1) {
                if (transitionHistory.isNotEmpty()) {
                    transitionHistory.removeAt(transitionHistory.lastIndex)
                }
            }
            currentTransitionType = firstPopped
        }
    }

    /**
     * Clears all transition history. Called when replacing root.
     */
    fun clearTransitionHistory() {
        transitionHistory.clear()
    }

    /**
     * Current size of the transition history stack.
     */
    val transitionHistorySize: Int
        get() = transitionHistory.size

    // MARK: - Modal Tracking

    /**
     * Called when presenting a modal (PresentFullScreen or PresentSheet).
     * Saves the current back stack entry so we can dismiss back to it later.
     */
    fun onModalPresented(navController: NavController) {
        val currentEntry = navController.currentBackStackEntry
        val entryId = currentEntry?.id
        if (entryId != null) {
            modalEntryStack.add(entryId)
        }
    }

    /**
     * Called when dismissing a modal.
     * Pops the entire modal stack back to the entry point.
     *
     * @return true if a modal was dismissed, false if there was no modal to dismiss
     */
    fun onModalDismissed(navController: NavController): Boolean {
        if (modalEntryStack.isEmpty()) {
            return false
        }

        val modalEntryId = modalEntryStack.removeAt(modalEntryStack.lastIndex)

        val backQueue = navController.currentBackStack.value
        val targetEntry = backQueue.find { it.id == modalEntryId }

        if (targetEntry != null) {
            val route = targetEntry.destination.route
            if (route != null) {
                navController.popBackStack(route, inclusive = false)
                return true
            }
        }

        navController.popBackStack()
        return true
    }

    /**
     * Clears all modal tracking. Should be called when replacing root.
     */
    fun clearModalStack() {
        modalEntryStack.clear()
    }
}

/**
 * CompositionLocal for providing NavigationManager throughout the app.
 */
val LocalNavigationManager = compositionLocalOf { NavigationManager() }
