// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.gitLab.api

import com.fasterxml.jackson.annotation.JsonProperty

data class GitLabIssue(
	@JsonProperty("iid") val issueNumber: Long,
	@JsonProperty("web_url") val url: String
)
