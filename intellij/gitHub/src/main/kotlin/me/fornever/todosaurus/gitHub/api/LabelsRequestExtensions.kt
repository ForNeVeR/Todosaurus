// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub.api

import org.jetbrains.plugins.github.api.GithubApiRequest
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubApiRequests.Repos.Labels
import org.jetbrains.plugins.github.api.GithubApiRequests.getUrl
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.data.GithubResponsePage
import org.jetbrains.plugins.github.api.data.request.GithubRequestPagination
import org.jetbrains.plugins.github.api.util.GithubApiUrlQueryBuilder.Companion.urlQuery

@Suppress("UnusedReceiverParameter")
fun Labels.paginate(url: String)
    = GithubApiRequest.Get.jsonPage<GitHubLabel>(url)
        .withOperationName("get labels")

fun Labels.paginate(server: GithubServerPath, username: String, repoName: String, pagination: GithubRequestPagination? = null) : GithubApiRequest<GithubResponsePage<GitHubLabel>>
    = paginate(getUrl(server, GithubApiRequests.Repos.urlSuffix, "/$username/$repoName", urlSuffix, urlQuery {
        param(pagination)
    }))
