// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

import com.intellij.tasks.TaskRepositoryType

fun TaskRepositoryType<*>.isGitHub(): Boolean
    = name == IssueTrackerType.GitHub.name
