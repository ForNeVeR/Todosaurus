// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import com.intellij.openapi.project.Project
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import javax.swing.Icon

interface IssueTracker {
    val type: IssueTrackerType

    val icon: Icon

    val title: String

    suspend fun checkConnection(credentials: IssueTrackerCredentials): TestConnectionResult

    fun createClient(project: Project, credentials: IssueTrackerCredentials, placementDetails: IssuePlacementDetails): IssueTrackerClient
}
