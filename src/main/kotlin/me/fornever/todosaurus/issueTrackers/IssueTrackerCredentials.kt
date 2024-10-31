// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.collaboration.api.ServerPath

interface IssueTrackerCredentials {
    val id: String

    val username: String

    val serverPath: ServerPath
}
