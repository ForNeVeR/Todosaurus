// SPDX-FileCopyrightText: 2024-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub

import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskManager
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.impl.BaseRepository
import com.intellij.util.containers.toArray
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentialsProvider
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.authentication.GHAccountsUtil

class GitHubCredentialsProvider(private val project: Project) : IssueTrackerCredentialsProvider {
    override suspend fun provideAll(): Array<IssueTrackerCredentials>
        = fromPlugin()
            .plus(fromTasks())
            .toArray(emptyArray())

    override suspend fun provide(credentialsId: String): IssueTrackerCredentials?
        = provideAll().firstOrNull { credentialsId == it.id }

    private fun fromPlugin(): List<IssueTrackerCredentials>
        = GHAccountsUtil
            .accounts
            .map { GitHubCredentials(it.name, apiToken = null, it.server, account = it) }

    private fun fromTasks(): List<IssueTrackerCredentials>
        = TaskManager
            .getManager(project)
            .allRepositories
            .filter { it.repositoryType.isGitHub() }
            .mapIndexed(::mapToGitHubAccount)

    private fun mapToGitHubAccount(index: Int, server: TaskRepository): GitHubCredentials {
        val details = server as BaseRepository
        val serverPath = GithubServerPath.from(details.url)

        var username = details
            .presentableName
            .removePrefix(serverPath.schema)
            .removePrefix("://")
            .removePrefix(serverPath.host)
            .removePrefix("/")

        if (username.isEmpty())
            username = "Unnamed #${index + 1}"

        return GitHubCredentials(username, details.password, serverPath)
    }
}
