// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

class IssueTrackerConnectionDetails {
    var issueTracker: IssueTracker? = null

    var serverHost: String? = null

    var credentials: IssueTrackerCredentials? = null

    fun isComplete(): Boolean
        = issueTracker != null && serverHost != null && credentials != null
}
