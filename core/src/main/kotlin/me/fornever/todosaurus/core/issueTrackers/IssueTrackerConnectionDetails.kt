// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

class IssueTrackerConnectionDetails {
    var issueTracker: IssueTracker? = null

    var serverHost: String? = null

    var credentials: IssueTrackerCredentials? = null

    fun isComplete(): Boolean
        = issueTracker != null && serverHost != null && credentials != null
}
