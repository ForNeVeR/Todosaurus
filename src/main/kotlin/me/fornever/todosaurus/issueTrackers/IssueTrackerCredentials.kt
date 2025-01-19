// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.collaboration.api.ServerPath
import java.util.*

abstract class IssueTrackerCredentials {
    open val id: String
        get() {
            val identifier = username + "_" + serverPath.toString()
            return Base64.getEncoder().encodeToString(identifier.toByteArray())
        }

    abstract val username: String

    abstract val serverPath: ServerPath
}
