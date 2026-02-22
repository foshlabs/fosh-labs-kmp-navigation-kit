package com.foshlabs.navigation

/**
 * Marker interface for navigation destinations.
 *
 * Consuming projects should define their own sealed interface extending this:
 * ```
 * sealed interface MyProjectScene : AppScene {
 *     data object Home : MyProjectScene
 *     data object Settings : MyProjectScene
 * }
 * ```
 */
interface AppScene
