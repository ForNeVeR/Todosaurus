// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui.wizard.memoization

import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerProvider
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.issues.IssuePlacementDetailsType
import java.net.URI
import kotlin.io.path.Path

class UserChoiceReader(private val json: JsonObject) : UserChoiceVisitor {
    override fun visit(userChoice: UserChoice) {
        val issueTrackerId = json[UserChoice::issueTrackerId.name]
            ?.takeIf { it !is JsonNull }
            ?.jsonPrimitive
            ?.content

        userChoice.issueTrackerId = issueTrackerId
                ?: error("Issue tracker type field has invalid value")

        userChoice.credentialsId = json[UserChoice::credentialsId.name]
            ?.takeIf { it !is JsonNull }
            ?.jsonPrimitive
            ?.content
                ?: error("Credentials identifier field has invalid value")

        val placementDetailsJson = json[UserChoice::placementDetails.name]?.jsonObject
            ?: error("Placement details field has invalid value")

        val placementDetailsType = placementDetailsJson[IssuePlacementDetails::type.name]
            ?.takeIf { it !is JsonNull }
            ?.jsonPrimitive
            ?.content
            ?.let { IssuePlacementDetailsType.valueOf(it) }

        val placementDetails = when (placementDetailsType) {
            IssuePlacementDetailsType.GitBased -> GitBasedPlacementDetails()
            else -> error("Placement details type $placementDetailsType is unsupported")
        }

        placementDetails.accept(UserChoiceReader(placementDetailsJson))

        userChoice.placementDetails = placementDetails
    }

    override fun visit(placementDetails: GitBasedPlacementDetails) {
        val url = json[GitHostingRemote::url.name]
            ?.takeIf { it !is JsonNull }
            ?.jsonPrimitive
            ?.content
                ?: error("Git hosting remote url field has invalid value")

        val rootPath = json[GitHostingRemote::rootPath.name]
            ?.takeIf { it !is JsonNull }
            ?.jsonPrimitive
            ?.content
                ?: error("Git hosting remote root path field has invalid value")

        placementDetails.remote = GitHostingRemote(URI(url), Path(rootPath)) // TODO[#139]: Add tests which verify this conversions to URI and Path
    }
}
