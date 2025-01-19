// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issues.IssuePlacementDetails
import javax.swing.Icon

interface IssueTracker {
    val type: IssueTrackerType

    val icon: Icon

    val title: String

    suspend fun checkConnection(credentials: IssueTrackerCredentials): TestConnectionResult

    fun createClient(project: Project, credentials: IssueTrackerCredentials, placementDetails: IssuePlacementDetails): IssueTrackerClient
}

