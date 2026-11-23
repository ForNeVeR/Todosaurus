// SPDX-FileCopyrightText: 2025-2026 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT
package me.fornever.todosaurus.gitLab

import com.intellij.collaboration.api.HttpStatusErrorException
import com.intellij.collaboration.util.resolveRelative
import com.intellij.collaboration.util.withQuery
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.application.readAction
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
import me.fornever.todosaurus.core.issues.labels.LabelsOptions
import me.fornever.todosaurus.core.issues.ui.wizard.IssueOptions
import me.fornever.todosaurus.gitLab.api.GitLabError
import me.fornever.todosaurus.gitLab.api.GitLabIssue
import me.fornever.todosaurus.gitLab.api.GitLabLabel
import org.jetbrains.plugins.gitlab.api.GitLabApi
import org.jetbrains.plugins.gitlab.api.GitLabRestJsonDataDeSerializer
import org.jetbrains.plugins.gitlab.api.GitLabServerPath
import java.io.StringReader
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class GitLabClient(
    private val project: Project,
    private val restClient: GitLabApi.Rest,
    private val remote: GitHostingRemote,
    private val fileDocumentManager: FileDocumentManager
) : IssueTrackerClient {
    override suspend fun createIssue(toDoItem: ToDoItem, issueOptions: List<IssueOptions>): IssueModel {
        val issueBody = readAction {
            replacePatterns(restClient.server, toDoItem)
        }

        val projectId = URLEncoder.encode(remote.ownerAndName, StandardCharsets.UTF_8)
        val endpointPath = restClient
            .server
            .restApiUri
            .resolveRelative("projects/$projectId/issues")

        val labels = issueOptions
            .filterIsInstance<LabelsOptions>()
            .firstOrNull()
            ?.getSelectedLabels()
            ?.joinToString(",") { it.name }

        val request = restClient
            .postJson(endpointPath, object {
                @Suppress("UNUSED") val title = toDoItem.title
                @Suppress("UNUSED") val description = issueBody
                @Suppress("UNUSED") val labels = labels
            })
            .build()

        val response = restClient.loadJsonValueByClass(request, GitLabIssue::class.java).body()

        return IssueModel(response.issueNumber.toString(), response.url)
    }

    override suspend fun getIssue(toDoItem: ToDoItem): IssueModel? {
        if (toDoItem !is ToDoItem.Reported)
            return null

        val projectId = URLEncoder.encode(remote.ownerAndName, StandardCharsets.UTF_8)
        val endpointPath = restClient
            .server
            .restApiUri
            .resolveRelative("projects/$projectId/issues/${toDoItem.issueNumber}")

        val request = restClient
            .request(endpointPath)
            .GET()
            .build()

        val gitLabIssue = try {
            restClient.loadJsonValueByClass(request, GitLabIssue::class.java).body()
        } catch (e: HttpStatusErrorException) {
            if (e.statusCode == 404)
                return null

            throw gitLabError(e)
        }

        return IssueModel(gitLabIssue.issueNumber.toString(), gitLabIssue.url)
    }

    suspend fun getLabels(): Iterable<GitLabLabel> {
        val projectId = URLEncoder.encode(remote.ownerAndName, StandardCharsets.UTF_8)
        val perPage = 100 // Default value from GitLab Docs
        val baseUri = restClient
            .server
            .restApiUri
            .resolveRelative("projects/$projectId/labels")

        var currentPage: Int? = 1
        val labels = mutableListOf<GitLabLabel>()

        while (currentPage != null && currentPage != 0) {
            val endpointPath = baseUri.withQuery("per_page=$perPage&page=$currentPage")

            val request = restClient
                .request(endpointPath)
                .GET()
                .build()

            val response = try {
                restClient.loadJsonListByClass(request, GitLabLabel::class.java)
            } catch (e: HttpStatusErrorException) {
                throw gitLabError(e)
            }

            labels.addAll(response.body())

            currentPage = response
                .headers()
                .firstValue("X-Next-Page")
                .orElse(null)
                ?.toIntOrNull()
        }

        return labels
    }

    private fun gitLabError(exception: HttpStatusErrorException): IllegalStateException {
        val errorResponse = exception.body?.let { body ->
            runCatching {
                StringReader(body).use { GitLabRestJsonDataDeSerializer.fromJson(it, GitLabError::class.java) }
            }.getOrNull()
        }

        val errorMessage = errorResponse?.message
            ?: errorResponse?.error
                ?: "Unknown error occurred with ${exception.statusCode} HTTP status code."

        return IllegalStateException(errorMessage, exception)
    }

    @RequiresReadLock
    private fun replacePatterns(serverPath: GitLabServerPath, toDoItem: ToDoItem): String {
        if (toDoItem !is ToDoItem.New || toDoItem.urlReplacements.isEmpty())
            return toDoItem.description

        val rootPath = remote.rootPath
        val filePath = fileDocumentManager.getFile(toDoItem.toDoRange.document)?.toNioPath()
            ?: error("Cannot find file for the requested document")

        val path = FileUtil.getRelativePath(rootPath.toFile(), filePath.toFile())?.replace('\\', '/')
            ?: error("Cannot calculate relative path between \"${remote.rootPath}\" and \"${filePath}\"")

        val currentCommit = getCurrentCommitHash()

        val urlReplacements = toDoItem
            .urlReplacements
            .getAll()
            .sortedBy { it.positionInDescription.first }

        var description = toDoItem.description
        var replacementDelta = 0

        for (urlReplacement in urlReplacements) {
            val startReplacementOffset = urlReplacement.positionInDescription.first + replacementDelta
            val endReplacementOffset = urlReplacement.positionInDescription.last + replacementDelta

            if (startReplacementOffset !in 0..endReplacementOffset || endReplacementOffset > description.length)
                continue

            val lineDesignator = if (urlReplacement.startLineNumber == urlReplacement.endLineNumber) "L${urlReplacement.startLineNumber}" else "L${urlReplacement.startLineNumber}-L${urlReplacement.endLineNumber}"
            val linkText = "${serverPath.restApiUri.scheme}://${serverPath.restApiUri.host}/${remote.owner}/${remote.name}/-/blob/$currentCommit/$path#$lineDesignator"
            description = description.replaceRange(startReplacementOffset, endReplacementOffset, linkText)

            replacementDelta += (linkText.length - endReplacementOffset + startReplacementOffset)
        }

        return description
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
