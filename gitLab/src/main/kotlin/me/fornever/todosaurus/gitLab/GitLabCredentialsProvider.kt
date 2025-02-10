// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitLab

import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskManager
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.impl.BaseRepository
import com.intellij.util.containers.toArray
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentialsProvider
import org.jetbrains.plugins.gitlab.api.GitLabServerPath
import org.jetbrains.plugins.gitlab.authentication.accounts.PersistentGitLabAccountManager

class GitLabCredentialsProvider(private val project: Project) : IssueTrackerCredentialsProvider {
    override suspend fun provide(credentialsId: String): IssueTrackerCredentials?
        = provideAll().firstOrNull { credentialsId == it.id }

    override suspend fun provideAll(): Array<IssueTrackerCredentials>
        = fromPlugin()
            .plus(fromTasks())
            .toArray(emptyArray())

    private suspend fun fromPlugin(): List<IssueTrackerCredentials> {
        val accountManager = PersistentGitLabAccountManager()

        return accountManager
            .accountsState
            .value
            .map { GitLabCredentials(it.name, accountManager.findCredentials(it), it.server) }
    }

    private fun fromTasks(): List<IssueTrackerCredentials>
        = TaskManager
            .getManager(project)
            .allRepositories
            .filter { it.repositoryType.isGitLab() }
            .mapIndexed(::mapToGitLabAccount)

    private fun mapToGitLabAccount(index: Int, server: TaskRepository): GitLabCredentials {
        val details = server as BaseRepository
        val serverPath = GitLabServerPath(details.url)

        var username = details
            .presentableName
            .removePrefix(serverPath.restApiUri.scheme)
            .removePrefix("://")
            .removePrefix(serverPath.restApiUri.host)
            .removePrefix("/")

        if (username.isEmpty())
            username = "Unnamed #${index + 1}"

        return GitLabCredentials(username, details.password, serverPath)
    }
}
