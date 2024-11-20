// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub

fun TaskRepositoryType<*>.isGitHub(): Boolean
    = name == GitHub::class.simpleName
