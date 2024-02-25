package me.fornever.todosaurus.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.serviceAsync
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.models.CreateIssueModel
import org.jetbrains.plugins.github.api.GithubApiRequestExecutor
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.data.GithubIssue
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.github.util.GHCompatibilityUtil

@Service(Service.Level.PROJECT)
class GitHubService(private val project: Project) {

    companion object {
        suspend fun getInstance(project: Project): GitHubService = project.serviceAsync()
    }

    suspend fun createIssue(model: CreateIssueModel): GithubIssue {
        val repository = model.selectedRepository ?: error("Repository is not selected.")
        val account = model.selectedAccount ?: error("Account is not selected.")

        val token = getApiToken(account)
        val executor = GithubApiRequestExecutor.Factory.getInstance().create(token)
        val request = GithubApiRequests.Repos.Issues.create(
            GithubServerPath.DEFAULT_SERVER,
            repository.owner,
            repository.name,
            model.title,
            body = model.description
        )

        return withContext(Dispatchers.IO) {
            executor.execute(request)
        }
    }

    private fun getApiToken(account: GithubAccount): String {
        return GHCompatibilityUtil.getOrRequestToken(account, project) ?: error("Token is not found.")
    }
}
