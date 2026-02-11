// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitLab

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepositoryType
import me.fornever.todosaurus.core.git.GitBasedPlacementDetails
import me.fornever.todosaurus.core.git.ui.wizard.ChooseGitHostingRemoteStep
import me.fornever.todosaurus.core.issueTrackers.*
import me.fornever.todosaurus.core.issueTrackers.anonymous.AnonymousCredentials
import me.fornever.todosaurus.core.issues.IssuePlacementDetails
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardContext
import me.fornever.todosaurus.core.ui.wizard.TodosaurusWizardStep
import org.jetbrains.plugins.gitlab.api.GitLabApi
import org.jetbrains.plugins.gitlab.api.GitLabApiManager
import org.jetbrains.plugins.gitlab.api.GitLabServerPath
import org.jetbrains.plugins.gitlab.api.request.checkIsGitLabServer
import javax.swing.Icon

@Suppress("UnstableApiUsage")
class GitLab(override val icon: Icon, override val title: String) : IssueTracker {
    override val id = "GitLab"

    private val apiManager by lazy { service<GitLabApiManager>() }
    private val fileDocumentManager by lazy { FileDocumentManager.getInstance() }

    override suspend fun checkConnection(credentials: IssueTrackerCredentials): TestConnectionResult
        = try {
        when (createRestClient(credentials).checkIsGitLabServer()) {
            true -> TestConnectionResult.Success
            false -> TestConnectionResult.Failed("Bad credentials")
        }
    }
        catch (exception: Exception) {
            TestConnectionResult.Failed(exception.message)
        }

    override fun createChooseRemoteStep(project: Project, context: TodosaurusWizardContext<*>): TodosaurusWizardStep
        = ChooseGitHostingRemoteStep(project, context)

    override fun createClient(project: Project, credentials: IssueTrackerCredentials, placementDetails: IssuePlacementDetails): IssueTrackerClient {
        if (placementDetails !is GitBasedPlacementDetails)
            error("Only ${GitBasedPlacementDetails::class.simpleName} supported")

        val remote = placementDetails.remote
            ?: error("Remote must be specified")

        return GitLabClient(project, createRestClient(credentials), remote, fileDocumentManager)
    }

    override fun createCredentialsProvider(project: Project): IssueTrackerCredentialsProvider
        = GitLabCredentialsProvider(project)

    private fun getGitLabPath(credentials: IssueTrackerCredentials): GitLabServerPath
        = credentials.serverPath as? GitLabServerPath
            ?: error("Only ${GitLabServerPath::class.simpleName} supported")

    private fun createRestClient(credentials: IssueTrackerCredentials): GitLabApi.Rest {
        val gitLabPath = getGitLabPath(credentials)

        val client = when (credentials) {
            is AnonymousCredentials -> apiManager.getUnauthenticatedClient(gitLabPath)

            is GitLabCredentials -> {
                val token = credentials.apiToken ?: error("Bad credentials")
                apiManager.getClient(gitLabPath, token)
            }

            else -> error("Only ${GitLabCredentials::class.simpleName} supported")
        }

        return client.rest
    }
}

internal const val GITLAB_TASK_REPOSITORY_NAME = "Gitlab"
internal fun TaskRepositoryType<*>.isGitLab() = this.name == GITLAB_TASK_REPOSITORY_NAME
