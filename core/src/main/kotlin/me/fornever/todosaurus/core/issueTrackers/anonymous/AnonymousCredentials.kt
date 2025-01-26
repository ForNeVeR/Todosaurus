// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers.anonymous

import com.intellij.collaboration.api.ServerPath
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials

class AnonymousCredentials(override val serverPath: ServerPath) : IssueTrackerCredentials() {
    companion object {
        const val ID: String = "Anonymous"
    }

    override val id: String
        get() = ID

    override val username: String
        get() = ID
}
