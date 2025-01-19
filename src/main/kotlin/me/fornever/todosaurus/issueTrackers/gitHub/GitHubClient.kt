// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.gitHub

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
import me.fornever.todosaurus.issueTrackers.IssueTrackerClient
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import me.fornever.todosaurus.issues.IssueModel
import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.settings.TodosaurusSettings
import me.fornever.todosaurus.vcs.git.GitHostingRemote
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath

class GitHubClient(
    private val project: Project,
    private val gitHub: GitHub,
    private val credentials: IssueTrackerCredentials,
    private val remote: GitHostingRemote
) : IssueTrackerClient {
    override suspend fun createIssue(toDoItem: ToDoItem): IssueModel {
        val serverPath = gitHub.getGitHubPath(credentials)

        val issueBody = readAction {
            replacePatterns(serverPath, toDoItem)
        }

        val request = GithubApiRequests.Repos.Issues.create(
            serverPath,
            remote.owner,
            remote.name,
            toDoItem.title,
            issueBody
        )

        val response = withContext(Dispatchers.IO) {
            gitHub.createRequestExecutor(credentials).execute(request)
        }

        return IssueModel(response.number.toString(), response.htmlUrl)
    }

    override suspend fun getIssue(toDoItem: ToDoItem): IssueModel? {
        val issueNumber = readAction { toDoItem.issueNumber }
            ?: return null

        val request = GithubApiRequests.Repos.Issues.get(
            gitHub.getGitHubPath(credentials),
            remote.owner,
            remote.name,
            issueNumber
        )

        val response = withContext(Dispatchers.IO) { gitHub.createRequestExecutor(credentials).execute(request) }
            ?: return null

        return IssueModel(response.number.toString(), response.htmlUrl)
    }

    @RequiresReadLock
    private fun replacePatterns(serverPath: GithubServerPath, toDoItem: ToDoItem): String {
        if (!toDoItem.description.contains(TodosaurusSettings.URL_REPLACEMENT))
            return toDoItem.description

        val rootPath = remote.rootPath
        val filePath = FileDocumentManager.getInstance().getFile(toDoItem.toDoRange.document)?.toNioPath()
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
