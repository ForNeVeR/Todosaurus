// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.labels

data class IssueLabel(
    val id: Long,
    val name: String,
    val description: String,
    val color: String
)
