// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.wizardTests.memoizationTests.readerTests

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceReader
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GitBasedPlacementDetailsTests {
    companion object {
        private const val CREDENTIALS_IDENTIFIER = "Identifier"
    }

    @Test
    fun `Should throws error if placement details has invalid value`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonArray(emptyList())
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should throws error if placement details type has invalid value`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonPrimitive("PlacementDetails"),
                    GitHostingRemote::url.name to JsonPrimitive("https://example.com"),
                    GitHostingRemote::rootPath.name to JsonPrimitive("\\home")
                ))
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should throws error if placement details type is not primitive`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonArray(emptyList()),
                    GitHostingRemote::url.name to JsonPrimitive("https://example.com"),
                    GitHostingRemote::rootPath.name to JsonPrimitive("\\home")
                ))
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should throws error if url has invalid value`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
                    GitHostingRemote::url.name to JsonNull,
                    GitHostingRemote::rootPath.name to JsonPrimitive("\\home")
                ))
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should throws error if url is not primitive`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
                    GitHostingRemote::url.name to JsonObject(mapOf()),
                    GitHostingRemote::rootPath.name to JsonPrimitive("\\home")
                ))
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should throws error if root path has invalid value`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
                    GitHostingRemote::url.name to JsonPrimitive("https://example.com"),
                    GitHostingRemote::rootPath.name to JsonNull
                ))
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should throws error if root path is not primitive`() {
        // Arrange
        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
                    GitHostingRemote::url.name to JsonPrimitive("https://example.com"),
                    GitHostingRemote::rootPath.name to JsonObject(mapOf())
                ))
            )))

        // Act & Assert
        assertThrows<Exception> {
            sut.visit(actual)
        }
    }

    @Test
    fun `Should read git based placement details field properly`() {
        // Arrange
        val url = "https://example.com"
        val rootPath = "\\home"

        val actual = UserChoice()
        val sut = UserChoiceReader(
            JsonObject(mapOf(
                UserChoice::issueTrackerId.name to JsonPrimitive("GitHub"),
                UserChoice::credentialsId.name to JsonPrimitive(CREDENTIALS_IDENTIFIER),
                UserChoice::placementDetails.name to JsonObject(mapOf(
                    IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
                    GitHostingRemote::url.name to JsonPrimitive(url),
                    GitHostingRemote::rootPath.name to JsonPrimitive(rootPath)
                ))
            )))

        // Act
        sut.visit(actual)

        // Assert
        val gitBasedPlacementDetails = actual.placementDetails as? GitBasedPlacementDetails
            ?: return fail()

        assertEquals(IssuePlacementDetailsType.GitBased, gitBasedPlacementDetails.type)
        assertEquals(url, gitBasedPlacementDetails.remote?.url.toString())
        assertEquals(rootPath, gitBasedPlacementDetails.remote?.rootPath.toString())
    }
}
