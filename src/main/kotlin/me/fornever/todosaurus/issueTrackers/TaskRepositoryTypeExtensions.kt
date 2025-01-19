// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.tasks.TaskRepositoryType

fun TaskRepositoryType<*>.isGitHub(): Boolean
    = name == IssueTrackerType.GitHub.name
