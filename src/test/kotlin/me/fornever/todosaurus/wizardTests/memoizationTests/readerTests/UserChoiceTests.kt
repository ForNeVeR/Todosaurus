// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.wizardTests.memoizationTests.readerTests

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.issueTrackers.IssueTrackerType
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceReader
import me.fornever.todosaurus.vcs.git.GitHostingRemote
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class UserChoiceTests(private val issueTrackerType: IssueTrackerType) {
    companion object {
        private const val CREDENTIALS_IDENTIFIER = "Identifier"

        private val placementDetails: Pair<String, JsonElement>
            = UserChoice::placementDetails.name to JsonObject(mapOf(
                IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
                GitHostingRemote::url.name to JsonPrimitive("https://example.com"),
                GitHostingRemote::rootPath.name to JsonPrimitive("\\home")
            ))

        @JvmStatic
        @Parameters
        fun issueTrackerTypes()
            = arrayOf(IssueTrackerType.GitHub)
    }

    @Test(expected = Exception::class)
    fun `Should throws error if issue tracker type has invalid value`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(
                mapOf(
                    UserChoice::issueTrackerType.name to JsonPrimitive("IssueTracker"),
                    UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                    placementDetails
                )
            )
        )

        // Act & Assert
        sut.visit(actual)
    }

    @Test(expected = Exception::class)
    fun `Should throws error if issue tracker type is not primitive`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(
                mapOf(
                    UserChoice::issueTrackerType.name to JsonObject(emptyMap()),
                    UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                    placementDetails
                )
            )
        )

        // Act & Assert
        sut.visit(actual)
    }

    @Test(expected = Exception::class)
    fun `Should throws error if credentials identifier has invalid value`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(
                mapOf(
                    UserChoice::issueTrackerType.name to JsonPrimitive(issueTrackerType.name),
                    UserChoice::credentialsId.name to JsonNull,
                    placementDetails
                )
            )
        )

        // Act & Assert
        sut.visit(actual)
    }

    @Test(expected = Exception::class)
    fun `Should throws error if credentials identifier is not primitive`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(
                mapOf(
                    UserChoice::issueTrackerType.name to JsonPrimitive(issueTrackerType.name),
                    UserChoice::credentialsId.name to JsonObject(emptyMap()),
                    placementDetails
                )
            )
        )

        // Act & Assert
        sut.visit(actual)
    }

    @Test
    fun `Should read primary user choice fields properly`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
			JsonObject(
				mapOf(
					UserChoice::issueTrackerType.name to JsonPrimitive(issueTrackerType.name),
					UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
					placementDetails
				)
			)
		)

        // Act
        sut.visit(actual)

        // Assert
		Assert.assertEquals(issueTrackerType.name, actual.issueTrackerType?.name)
		Assert.assertEquals(CREDENTIALS_IDENTIFIER, actual.credentialsId)
    }
}
