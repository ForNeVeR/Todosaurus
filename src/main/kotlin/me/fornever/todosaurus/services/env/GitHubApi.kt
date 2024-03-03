package me.fornever.todosaurus.services.env

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.plugins.github.api.GithubApiRequest
import org.jetbrains.plugins.github.api.GithubApiRequestExecutor
import org.jetbrains.plugins.github.api.data.GithubIssue

interface GitHubApi {
    suspend fun createIssue(token: String, request: GithubApiRequest.Post<GithubIssue>): GithubIssue
}

@Service
class IntelliJGitHubApi : GitHubApi {

    companion object {
        fun getInstance(): IntelliJGitHubApi = service()
    }

    override suspend fun createIssue(token: String, request: GithubApiRequest.Post<GithubIssue>): GithubIssue {
        val executor = GithubApiRequestExecutor.Factory.getInstance().create(token)
        return withContext(Dispatchers.IO) {
            executor.execute(request)
        }
    }
}
