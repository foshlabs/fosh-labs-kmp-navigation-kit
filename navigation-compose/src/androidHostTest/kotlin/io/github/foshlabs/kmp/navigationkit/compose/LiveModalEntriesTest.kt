package io.github.foshlabs.kmp.navigationkit.compose

import kotlin.test.Test
import kotlin.test.assertEquals

class LiveModalEntriesTest {

    @Test
    fun `an open modal keeps its entry`() {
        val live = liveModalEntries(
            modalEntries = listOf("home"),
            backStackIds = listOf("home", "sheet"),
            currentId = "sheet",
        )

        assertEquals(listOf("home"), live)
    }

    @Test
    fun `a sheet closed by system back over another sheet drops only its own entry`() {
        // Home presented the action sheet, which presented the contact sheet; back closed the latter.
        val live = liveModalEntries(
            modalEntries = listOf("home", "actionSheet"),
            backStackIds = listOf("home", "actionSheet"),
            currentId = "actionSheet",
        )

        assertEquals(listOf("home"), live)
    }

    @Test
    fun `screens pushed inside a modal keep the modal's entry`() {
        val live = liveModalEntries(
            modalEntries = listOf("home"),
            backStackIds = listOf("home", "sheet", "pushed"),
            currentId = "pushed",
        )

        assertEquals(listOf("home"), live)
    }

    @Test
    fun `entries gone from the back stack are dropped`() {
        val live = liveModalEntries(
            modalEntries = listOf("oldRoot", "home"),
            backStackIds = listOf("home", "sheet"),
            currentId = "sheet",
        )

        assertEquals(listOf("home"), live)
    }

    @Test
    fun `nested modals all closed by back leave nothing behind`() {
        val live = liveModalEntries(
            modalEntries = listOf("home", "actionSheet"),
            backStackIds = listOf("home"),
            currentId = "home",
        )

        assertEquals(emptyList(), live)
    }
}
