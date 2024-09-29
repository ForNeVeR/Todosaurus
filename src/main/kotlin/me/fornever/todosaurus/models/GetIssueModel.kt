// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.models

import org.jetbrains.plugins.github.authentication.accounts.GithubAccount

data class GetIssueModel(
	val repository: RepositoryModel?,
    val account: GithubAccount?,
	val issueNumber: Long
)
