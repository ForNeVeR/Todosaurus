// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard.memoization

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.vcs.git.GitBasedPlacementDetails
import me.fornever.todosaurus.vcs.git.GitHostingRemote

class UserChoiceWriter : UserChoiceVisitor {
    private val elements: MutableMap<String, JsonElement> = mutableMapOf()

    val json: JsonObject
        get() = JsonObject(elements)

    override fun visit(userChoice: UserChoice) {
        val issueTrackerType = userChoice.issueTrackerType
            ?: error("Issue tracker type must be specified")

        val credentialsId = userChoice.credentialsId
            ?: error("Credentials identifier must be specified")

        val placementDetails = userChoice.placementDetails
            ?: error("Placement details must be specified")

        elements[UserChoice::issueTrackerType.name] = JsonPrimitive(issueTrackerType.name)
        elements[UserChoice::credentialsId.name] = JsonPrimitive(credentialsId)

        val placementDetailsWriter = UserChoiceWriter()
        placementDetails.accept(placementDetailsWriter)

        elements[UserChoice::placementDetails.name] = placementDetailsWriter.json
    }

    override fun visit(placementDetails: GitBasedPlacementDetails) {
        val remote = placementDetails.remote
            ?: error("Git hosting remote must be specified")

        elements[IssuePlacementDetails::type.name] = JsonPrimitive(placementDetails.type.name)
        elements[GitHostingRemote::url.name] = JsonPrimitive(remote.url.toString())
        elements[GitHostingRemote::rootPath.name] = JsonPrimitive(remote.rootPath.toString())
    }
}
