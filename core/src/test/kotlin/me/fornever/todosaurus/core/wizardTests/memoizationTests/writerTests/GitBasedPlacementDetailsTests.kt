// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.wizardTests.memoizationTests.writerTests

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.core.ui.wizard.memoization.UserChoiceWriter
import org.junit.Assert
import org.junit.Test
import java.net.URI
import java.nio.file.Path

class GitBasedPlacementDetailsTests {
    companion object {
        private const val CREDENTIALS_IDENTIFIER = "Identifier"
    }

    @Test
    fun `Should write git based placement details field properly`() {
        // Arrange
        val url = "https://example.com"
        val rootPath = "\\home"
        val placementDetails = GitBasedPlacementDetails()
        placementDetails.remote = GitHostingRemote(URI(url), Path.of(rootPath))

        val actual = UserChoice("GitHub", CREDENTIALS_IDENTIFIER, placementDetails)
        val sut = UserChoiceWriter()

        // Act
        sut.visit(actual)

        // Assert
		Assert.assertEquals(
			sut.json[UserChoice::placementDetails.name], JsonObject(
				mapOf(
					IssuePlacementDetails::type.name to JsonPrimitive(IssuePlacementDetailsType.GitBased.name),
					GitHostingRemote::url.name to JsonPrimitive(url),
					GitHostingRemote::rootPath.name to JsonPrimitive(rootPath)
				)
			)
		)
    }
}
