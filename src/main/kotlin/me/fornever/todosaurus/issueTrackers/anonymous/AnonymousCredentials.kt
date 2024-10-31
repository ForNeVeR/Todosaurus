// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.anonymous

import com.intellij.collaboration.api.ServerPath
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials

class AnonymousCredentials(override val serverPath: ServerPath) : IssueTrackerCredentials {
    override val username: String
        get() = "Anonymous"
}
