// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.gitHub

import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issueTrackers.IssueTracker
import me.fornever.todosaurus.issueTrackers.IssueTrackerClient
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.issueTrackers.TestConnectionResult
import me.fornever.todosaurus.issueTrackers.anonymous.AnonymousCredentials
import me.fornever.todosaurus.issues.IssuePlacementDetails
import me.fornever.todosaurus.vcs.git.GitBasedPlacementDetails
import org.jetbrains.plugins.github.api.GithubApiRequestExecutor
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.executeSuspend
import javax.swing.Icon

class GitHub(val project: Project, override val icon: Icon, override val title: String) : IssueTracker {
    override suspend fun checkConnection(credentials: IssueTrackerCredentials): TestConnectionResult {
        return try {
            val request = GithubApiRequests.CurrentUser.get(getGitHubPath(credentials))
            createRequestExecutor(credentials).executeSuspend(request)

            TestConnectionResult.Success
        } catch (exception: Exception) {
            TestConnectionResult.Failed(exception.message)
        }
    }

    override fun createClient(credentials: IssueTrackerCredentials, placementDetails: IssuePlacementDetails): IssueTrackerClient {
        if (placementDetails !is GitBasedPlacementDetails)
            error("Only ${GitBasedPlacementDetails::class.simpleName} supported")

        val remote = placementDetails.remote
            ?: error("Remote must be specified")

        return GitHubClient(this, credentials, remote)
    }

    fun getGitHubPath(credentials: IssueTrackerCredentials): GithubServerPath
        = credentials.serverPath as? GithubServerPath
            ?: error("Only ${GithubServerPath::class.simpleName} supported")

    fun createRequestExecutor(credentials: IssueTrackerCredentials): GithubApiRequestExecutor {
        if (credentials is AnonymousCredentials)
            return GithubApiRequestExecutor.Factory.getInstance().create()

        if (credentials is GitHubCredentials) {
            val token = credentials.apiToken ?: error("Bad credentials")
            return GithubApiRequestExecutor.Factory.getInstance().create(credentials.serverPath, token)
        }

        error("Only ${GitHubCredentials::class.simpleName} supported")
    }
}
