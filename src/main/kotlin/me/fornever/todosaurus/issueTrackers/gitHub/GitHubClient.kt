// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.gitHub

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.fileEditor.FileDocumentManager
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
import me.fornever.todosaurus.vcs.git.GitRemote
import org.jetbrains.plugins.github.api.GithubApiRequests
import org.jetbrains.plugins.github.api.GithubServerPath
import org.jetbrains.plugins.github.api.executeSuspend

class GitHubClient(private val gitHub: GitHub, private val credentials: IssueTrackerCredentials, private val gitRemote: GitRemote) : IssueTrackerClient {
    override suspend fun createIssue(toDoItem: ToDoItem): IssueModel {
        val serverPath = gitHub.getGitHubPath(credentials)

        val issueBody = readAction {
            replacePatterns(serverPath, toDoItem)
        }

        val request = GithubApiRequests.Repos.Issues.create(
            serverPath,
            gitRemote.owner,
            gitRemote.name,
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
            gitRemote.owner,
            gitRemote.name,
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

        val rootPath = gitRemote.rootPath
        val filePath = FileDocumentManager.getInstance().getFile(toDoItem.toDoRange.document)?.toNioPath()
            ?: error("Cannot find file for the requested document")

        val path = FileUtil.getRelativePath(rootPath.toFile(), filePath.toFile())?.replace('\\', '/')
            ?: error("Cannot calculate relative path between \"${gitRemote.rootPath}\" and \"${filePath}\"")

        val currentCommit = getCurrentCommitHash()
        val startLineNumber = toDoItem.toDoRange.document.getLineNumber(toDoItem.toDoRange.startOffset) + 1
        val endLineNumber = toDoItem.toDoRange.document.getLineNumber(toDoItem.toDoRange.endOffset) + 1
        val lineDesignator = if (startLineNumber == endLineNumber) "L$startLineNumber" else "L$startLineNumber-L$endLineNumber"
        val linkText = "${serverPath.schema}://${serverPath.host}/${gitRemote.owner}/${gitRemote.name}/blob/$currentCommit/$path#$lineDesignator"

        return toDoItem.description.replace(TodosaurusSettings.URL_REPLACEMENT, linkText)
    }

    private fun getCurrentCommitHash(): String {
        val virtualRoot = LocalFileSystem.getInstance().findFileByNioFile(gitRemote.rootPath)
            ?: error("Cannot find virtual file for \"${gitRemote.rootPath}\"")

        val repository = VcsRepositoryManager
            .getInstance(gitHub.project)
            .repositories
            .asSequence()
            .filterIsInstance<GitRepository>()
            .filter { it.root == virtualRoot }
            .singleOrNull()
                ?: error("Cannot find a Git repository for \"${gitRemote.rootPath}\"")

        return repository.info.currentRevision
            ?: error("Cannot determine the current revision for \"${gitRemote.rootPath}\"")
    }
}
