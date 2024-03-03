package me.fornever.todosaurus.services.env

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.github.util.GHCompatibilityUtil

interface GitHubTokenStorage {
    fun getOrRequestToken(account: GithubAccount): String?
}

@Service(Service.Level.PROJECT)
class IntelliJGitHubTokenStorage(private val project: Project) : GitHubTokenStorage {

    companion object {
        fun getInstance(project: Project): IntelliJGitHubTokenStorage = project.service()
    }

    override fun getOrRequestToken(account: GithubAccount): String? {
        return GHCompatibilityUtil.getOrRequestToken(account, project)
    }
}
