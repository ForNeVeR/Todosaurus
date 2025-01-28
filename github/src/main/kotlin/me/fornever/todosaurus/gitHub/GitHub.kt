// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.ui.wizard.ChooseGitHostingRemoteStep
import me.fornever.todosaurus.core.issueTrackers.IssueTracker
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerClient
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.core.issueTrackers.TestConnectionResult
import me.fornever.todosaurus.core.issueTrackers.anonymous.AnonymousCredentials
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import org.jetbrains.plugins.github.api.GithubApiRequestExecutor
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.executeSuspend
import javax.swing.Icon

class GitHub(override val icon: Icon, override val title: String) : IssueTracker {
    override val id = "GitHub"

    private val fileDocumentManager: FileDocumentManager
        = FileDocumentManager.getInstance()

    override suspend fun checkConnection(credentials: IssueTrackerCredentials): TestConnectionResult {
        return try {
            val request = GithubApiRequests.CurrentUser.get(getGitHubPath(credentials))
            createRequestExecutor(credentials).executeSuspend(request)

            TestConnectionResult.Success
        } catch (exception: Exception) {
            TestConnectionResult.Failed(exception.message)
        }
    }

    override fun createClient(project: Project, credentials: IssueTrackerCredentials, placementDetails: IssuePlacementDetails): IssueTrackerClient {
        if (placementDetails !is GitBasedPlacementDetails)
            error("Only ${GitBasedPlacementDetails::class.simpleName} supported")

        val remote = placementDetails.remote
            ?: error("Remote must be specified")

        return GitHubClient(project, this, credentials, remote, fileDocumentManager)
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

    override fun createChooseRemoteStep(
        project: Project,
        context: TodosaurusWizardContext
    ) = ChooseGitHostingRemoteStep(project, context)

    override fun createCredentialsProvider(project: Project) = GitHubCredentialsProvider(project)
}

internal const val GITHUB_TASK_REPOSITORY_NAME = "GitHub"
internal fun TaskRepositoryType<*>.isGitHub() = this.name == GITHUB_TASK_REPOSITORY_NAME
