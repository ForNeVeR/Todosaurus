// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.gitHub

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.concurrency.annotations.RequiresReadLock
import git4idea.repo.GitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerClient
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.core.issues.IssueModel
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptions
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.gitHub.labels.ui.wizard.LabelsOptions
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.data.GithubIssueLabel
import org.jetbrains.plugins.github.api.data.request.GithubRequestPagination

class GitHubClient(
    private val project: Project,
    private val gitHub: GitHub,
    private val credentials: IssueTrackerCredentials,
    private val remote: GitHostingRemote,
    private val fileDocumentManager: FileDocumentManager
) : IssueTrackerClient {
    override suspend fun createIssue(toDoItem: ToDoItem, issueOptions: List<IssueOptions>): IssueModel {
        val serverPath = gitHub.getGitHubPath(credentials)

        val issueBody = readAction {
            replacePatterns(serverPath, toDoItem)
        }

        val labelsOptions = issueOptions
            .filterIsInstance<LabelsOptions>()
            .firstOrNull()

        val request = GithubApiRequests.Repos.Issues.create(
            serverPath,
            remote.owner,
            remote.name,
            toDoItem.title,
            issueBody,
            labels = labelsOptions?.selectedLabels
        )

        val response = withContext(Dispatchers.IO) {
            gitHub.createRequestExecutor(credentials).execute(request)
        }

        return IssueModel(response.number.toString(), response.htmlUrl)
    }

    override suspend fun getIssue(toDoItem: ToDoItem): IssueModel? {
        if (toDoItem !is ToDoItem.Reported)
            return null

        val request = GithubApiRequests.Repos.Issues.get(
            gitHub.getGitHubPath(credentials),
            remote.owner,
            remote.name,
            toDoItem.issueNumber
        )

        val response = withContext(Dispatchers.IO) { gitHub.createRequestExecutor(credentials).execute(request) }
            ?: return null

        return IssueModel(response.number.toString(), response.htmlUrl)
    }

    suspend fun getLabels(): Iterable<GithubIssueLabel> {
        val requestExecutor = gitHub.createRequestExecutor(credentials)

        val labels = mutableListOf<GithubIssueLabel>()

        val cursorRequest = GithubApiRequests.Repos.Labels.get(
            gitHub.getGitHubPath(credentials),
            remote.owner,
            remote.name,
            GithubRequestPagination.DEFAULT
        )

        val pageCursor = withContext(Dispatchers.IO) {
            requestExecutor.execute(cursorRequest)
        }

        labels.addAll(pageCursor.items)

        var nextLink = pageCursor.nextLink

        while (nextLink != null) {
            val pageRequest = GithubApiRequests.Repos.Labels.get(nextLink)
            val nextPage = withContext(Dispatchers.IO) {
                requestExecutor.execute(pageRequest)
            }

            labels.addAll(nextPage.items)

            nextLink = nextPage.nextLink
        }

        return labels
    }

    @RequiresReadLock
    private fun replacePatterns(serverPath: GithubServerPath, toDoItem: ToDoItem): String {
        if (toDoItem !is ToDoItem.New || !toDoItem.description.contains(TodosaurusSettings.URL_REPLACEMENT))
            return toDoItem.description

        val rootPath = remote.rootPath
        val filePath = fileDocumentManager.getFile(toDoItem.toDoRange.document)?.toNioPath()
            ?: error("Cannot find file for the requested document")

        val path = FileUtil.getRelativePath(rootPath.toFile(), filePath.toFile())?.replace('\\', '/')
            ?: error("Cannot calculate relative path between \"${remote.rootPath}\" and \"${filePath}\"")

        val currentCommit = getCurrentCommitHash()
        val startLineNumber = toDoItem.toDoRange.document.getLineNumber(toDoItem.toDoRange.startOffset) + 1
        val endLineNumber = toDoItem.toDoRange.document.getLineNumber(toDoItem.toDoRange.endOffset) + 1
        val lineDesignator = if (startLineNumber == endLineNumber) "L$startLineNumber" else "L$startLineNumber-L$endLineNumber"
        val linkText = "${serverPath.schema}://${serverPath.host}/${remote.owner}/${remote.name}/blob/$currentCommit/$path#$lineDesignator"

        return toDoItem.description.replace(TodosaurusSettings.URL_REPLACEMENT, linkText)
    }

    private fun getCurrentCommitHash(): String {
        val virtualRoot = LocalFileSystem.getInstance().findFileByNioFile(remote.rootPath)
            ?: error("Cannot find virtual file for \"${remote.rootPath}\"")

        val repository = VcsRepositoryManager
            .getInstance(project)
            .getRepositories()
            .asSequence()
            .filterIsInstance<GitRepository>()
            .filter { it.root == virtualRoot }
            .singleOrNull()
                ?: error("Cannot find a Git repository for \"${remote.rootPath}\"")

        return repository.info.currentRevision
            ?: error("Cannot determine the current revision for \"${remote.rootPath}\"")
    }
}
