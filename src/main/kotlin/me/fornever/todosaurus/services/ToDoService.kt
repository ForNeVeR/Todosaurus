package me.fornever.todosaurus.services

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import kotlinx.coroutines.CoroutineScope
import me.fornever.todosaurus.views.CreateIssueDialog
import me.fornever.todosaurus.views.CreateIssueModel
import me.fornever.todosaurus.views.RepositoryModel
import java.net.URI

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project, private val scope: CoroutineScope) {

    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    private val newToDoItemPattern = "TODO:".toRegex(RegexOption.IGNORE_CASE)
    private val issueDescriptionTemplate = """
        See the code near this line: $\{GITHUB_CODE_URL}

        Also, look for the number of this issue in the project code base.
    """.trimIndent()

    fun hasNewToDoItem(range: RangeMarker): Boolean {
        val text = range.document.getText(range.textRange)
        return newToDoItemPattern.containsMatchIn(text)
    }

    fun createIssue(range: RangeMarker) {
        val data = calculateData(range)
        CreateIssueDialog(scope, collectRepositories(), data).show() // TODO: Pass the text and any required context
    }

    private fun calculateData(range: RangeMarker): CreateIssueModel {
        val text = range.document.getText(range.textRange)
        val title = text.substringBefore('\n')
        val description =
            (if (text.contains("\n")) text.substringAfter('\n') + "\n" else "") +
                issueDescriptionTemplate
        // TODO: Replace ${GITHUB_CODE_URL} with GitHub text range URL
        return CreateIssueModel(title, description)
    }

    private fun collectRepositories(): Array<RepositoryModel> {
        val repositoryManager = VcsRepositoryManager.getInstance(project)
        return repositoryManager.repositories
            .asSequence()
            .filterIsInstance<GitRepository>()
            .flatMap { it.remotes }
            .flatMap { it.urls }
            .flatMap { getGitHubRepoUri(it) }
            .distinct()
            .map(::RepositoryModel)
            .toList()
            .toTypedArray()
    }

    private fun getGitHubRepoUri(remoteUrl: String) = when {
        remoteUrl.startsWith("https://github.com/") -> listOf(remoteFromHttpsUrl(remoteUrl))
        remoteUrl.startsWith("git@github.com:") -> listOf(remoteFromSshUrl(remoteUrl))
        else -> emptyList()
    }

    private fun remoteFromHttpsUrl(remoteUrl: String) = URI(remoteUrl)
    private fun remoteFromSshUrl(remoteUrl: String) = URI(
        "https://" +
            remoteUrl
                .removePrefix("git@")
                .replace(":", "/")
                .removeSuffix(".git")
    )
}
