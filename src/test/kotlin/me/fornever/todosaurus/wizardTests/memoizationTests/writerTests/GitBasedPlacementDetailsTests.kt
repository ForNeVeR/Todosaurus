// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.wizardTests.memoizationTests.writerTests

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.issueTrackers.IssueTrackerType
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.issues.IssuePlacementDetailsType
import me.fornever.todosaurus.ui.wizard.memoization.UserChoice
import me.fornever.todosaurus.ui.wizard.memoization.UserChoiceWriter
import me.fornever.todosaurus.vcs.git.GitBasedPlacementDetails
import me.fornever.todosaurus.vcs.git.GitHostingRemote
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

        val actual = UserChoice(IssueTrackerType.GitHub, CREDENTIALS_IDENTIFIER, placementDetails)
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
