// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.gitLab

import ai.grazie.utils.mpp.URLEncoder
import com.intellij.collaboration.api.httpclient.HttpClientUtil.inflateAndReadWithErrorHandlingAndLogging
import com.intellij.collaboration.api.httpclient.InflatedStreamReadingBodyHandler
import com.intellij.collaboration.util.resolveRelative
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.util.concurrency.annotations.RequiresReadLock
import git4idea.repo.GitRepository
import me.fornever.todosaurus.core.git.GitHostingRemote
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerClient
import me.fornever.todosaurus.core.issues.IssueModel
import me.fornever.todosaurus.core.issues.ToDoItem
import me.fornever.todosaurus.core.settings.TodosaurusSettings
import me.fornever.todosaurus.gitLab.api.GitLabErrorResponse
import me.fornever.todosaurus.gitLab.api.GitLabIssue
import org.jetbrains.plugins.gitlab.api.GitLabApi
import org.jetbrains.plugins.gitlab.api.GitLabRestJsonDataDeSerializer
import org.jetbrains.plugins.gitlab.api.GitLabServerPath
import java.io.InputStreamReader

@Suppress("UnstableApiUsage")
class GitLabClient(
    private val project: Project,
    private val restClient: GitLabApi.Rest,
    private val remote: GitHostingRemote,
    private val fileDocumentManager: FileDocumentManager
) : IssueTrackerClient {
    private val logger = logger<GitLabClient>()

    override suspend fun createIssue(toDoItem: ToDoItem): IssueModel {
        val issueBody = readAction {
            replacePatterns(restClient.server, toDoItem)
        }

        val projectId = URLEncoder.encode(remote.ownerAndName)
        val endpointPath = restClient
            .server
            .restApiUri
            .resolveRelative("projects/$projectId/issues")

        val request = restClient
            .request(endpointPath)
            .POST(restClient.jsonBodyPublisher(endpointPath, object {
                @Suppress("UNUSED") val title = toDoItem.title
                @Suppress("UNUSED") val description = issueBody
            }))
            .header("Content-Type", "application/json")
            .build()

        val responseHandler = inflateAndReadWithErrorHandlingAndLogging(logger, request) { reader, _ ->
            GitLabRestJsonDataDeSerializer.fromJson(reader, GitLabIssue::class.java)
        }

        val response = restClient.sendAndAwaitCancellable(request, responseHandler).body()
            ?: error("Unable to create new issue")

        return IssueModel(response.issueNumber.toString(), response.url)
    }

    override suspend fun getIssue(toDoItem: ToDoItem): IssueModel? {
        if (toDoItem !is ToDoItem.Reported)
            return null

        val projectId = URLEncoder.encode(remote.ownerAndName)
        val endpointPath = restClient
            .server
            .restApiUri
            .resolveRelative("projects/$projectId/issues/${toDoItem.issueNumber}")

        val request = restClient
            .request(endpointPath)
            .GET()
            .build()

        val responseHandler = InflatedStreamReadingBodyHandler { responseInfo, bodyStream ->
            InputStreamReader(bodyStream, Charsets.UTF_8).use { reader ->
                when (responseInfo.statusCode()) {
                    200 -> {
                        GitLabRestJsonDataDeSerializer.fromJson(reader, GitLabIssue::class.java)
                    }

                    404 -> {
                        null
                    }

                    else -> {
                        val errorResponse =
                            GitLabRestJsonDataDeSerializer.fromJson(reader, GitLabErrorResponse::class.java)
                        val errorMessage = errorResponse?.message
                            ?: errorResponse?.error
                            ?: "Unknown error occurred with ${responseInfo.statusCode()} HTTP status code."
                        error(errorMessage)
                    }
                }
            }
        }

        val gitLabIssue = restClient.sendAndAwaitCancellable(request, responseHandler).body() ?: return null

        return IssueModel(gitLabIssue.issueNumber.toString(), gitLabIssue.url)
    }

    @RequiresReadLock
    private fun replacePatterns(serverPath: GitLabServerPath, toDoItem: ToDoItem): String {
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
        val lineDesignator = if (startLineNumber == endLineNumber) "L$startLineNumber" else "L$startLineNumber-$endLineNumber"
        val linkText = "${serverPath.restApiUri.scheme}://${serverPath.restApiUri.host}/${remote.owner}/${remote.name}/-/blob/$currentCommit/$path#$lineDesignator"

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
