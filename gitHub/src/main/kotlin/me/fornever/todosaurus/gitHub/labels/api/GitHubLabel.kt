// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.labels.api

data class GitHubLabel(
    val id: Long,
    val nodeId: String,
    val url: String,
    val name: String,
    val description: String,
    val color: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other !is GitHubLabel)
            return false

        return id == other.id
    }

    override fun hashCode(): Int
        = id.hashCode()
}
