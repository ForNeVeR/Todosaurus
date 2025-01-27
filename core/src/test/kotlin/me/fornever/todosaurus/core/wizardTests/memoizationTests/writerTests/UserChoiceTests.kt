// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.wizardTests.memoizationTests.writerTests

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceWriter
import me.fornever.todosaurus.core.wizardTests.memoizationTests.FakeIssuePlacementDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserChoiceTests {
    companion object {
        private const val CREDENTIALS_IDENTIFIER = "Identifier"

        private const val ISSUE_TRACKER_ID = "GitHub"
    }

    @Test
    fun `Should write primary user choice fields properly`() {
        // Arrange
        val placementDetails = FakeIssuePlacementDetails()
        val actual = UserChoice(ISSUE_TRACKER_ID, CREDENTIALS_IDENTIFIER, placementDetails)
        val sut = UserChoiceWriter()

        // Act
        sut.visit(actual)

        // Assert
        assertEquals(sut.json, JsonObject(
            mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive(ISSUE_TRACKER_ID),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(emptyMap())
            )
        ))
    }
}

