// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.wizardTests.memoizationTests.writerTests

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceWriter
import me.fornever.todosaurus.core.wizardTests.memoizationTests.FakeIssuePlacementDetails
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class UserChoiceTests(private val issueTrackerId: String) {
    companion object {
        private const val CREDENTIALS_IDENTIFIER = "Identifier"

        @JvmStatic
        @Parameters
        fun issueTrackerIds()
            = arrayOf("GitHub")
    }

    @Test
    fun `Should write primary user choice fields properly`() {
        // Arrange
        val placementDetails = FakeIssuePlacementDetails()
        val actual = UserChoice(issueTrackerId, CREDENTIALS_IDENTIFIER, placementDetails)
        val sut = UserChoiceWriter()

        // Act
        sut.visit(actual)

        // Assert
        Assert.assertEquals(sut.json, JsonObject(
            mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive(issueTrackerId),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(emptyMap())
            )
        ))
    }
}

