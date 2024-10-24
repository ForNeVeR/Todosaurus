package me.fornever.todosaurus.issueTrackers.gitHub

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskManager
import com.intellij.tasks.TaskRepository
import com.intellij.tasks.impl.BaseRepository
import com.intellij.util.containers.toArray
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentialsProvider
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.authentication.GHAccountsUtil
import org.jetbrains.plugins.github.authentication.accounts.GHAccountManager

class GitHubCredentialsProvider(private val project: Project) : IssueTrackerCredentialsProvider {
    override suspend fun provideAll(): Array<IssueTrackerCredentials>
        = fromPlugin()
            .plus(fromTasks())
            .toArray(emptyArray())

    override suspend fun provide(id: String): IssueTrackerCredentials?
        = provideAll().singleOrNull { it.id == id }

    private suspend fun fromPlugin(): List<IssueTrackerCredentials> {
        val pluginManager = service<GHAccountManager>()

        return GHAccountsUtil
            .accounts
            .map { GitHubCredentials(it.name, pluginManager.findCredentials(it), it.server) }
    }

    private fun fromTasks(): List<IssueTrackerCredentials>
        = TaskManager
            .getManager(project)
            .allRepositories
            .filter { it.repositoryType.name == GitHub::class.simpleName } // TODO: Mark GithubRepositoryType in intellij-community as public. Then change this condition to "type is GithubRepositoryType"
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
