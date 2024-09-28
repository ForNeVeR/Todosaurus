package me.fornever.todosaurus.services

import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.writeAction
import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import git4idea.repo.GitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.models.CreateIssueModel
import me.fornever.todosaurus.models.GetIssueModel
import me.fornever.todosaurus.models.RepositoryModel
import me.fornever.todosaurus.views.CreateIssueDialog
import me.fornever.todosaurus.views.Notifications
import org.jetbrains.plugins.github.api.data.GithubIssue
import org.jetbrains.plugins.github.authentication.GHAccountsUtil
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import java.net.URI

@Service(Service.Level.PROJECT)
class ToDoService(private val project: Project, private val scope: CoroutineScope) {

    companion object {
        fun getInstance(project: Project): ToDoService = project.service()
    }

    fun showCreateIssueDialog(range: RangeMarker) {
        val data = createIssue(range)
        CreateIssueDialog(project, scope, collectAccounts(), collectRepositories(), data).show()
    }

    suspend fun openIssueInBrowser(range: RangeMarker) {
        // TODO[#91]: We need to decide which repository and account to use to open the link
        val repository = collectRepositories().firstOrNull()
        val account = collectAccounts().firstOrNull()
        val toDoItem = ToDoItem(range)

        try {
            val issueNumber = readAction { toDoItem.issueNumber } ?: error("Issue number must be specified.")

            val data = GetIssueModel(repository, account, issueNumber)
            val issue = GitHubService.getInstance(project).getIssue(data) ?: error("Issue with number \"${issueNumber}\" not found.")

            withContext(Dispatchers.IO) {
                BrowserUtil.browse(issue.htmlUrl, project)
            }
        }
        catch (exception: Exception)
        {
            thisLogger()
                .warn(exception)

            Notifications.OpenIssueInBrowser.failed(exception, project)
        }
    }

    suspend fun updateDocumentText(toDoItem: ToDoItem, issue: GithubIssue) {
        @Suppress("UnstableApiUsage")
        writeAction {
            executeCommand(project, TodosaurusBundle.message("command.update.todo.item")) {
                toDoItem.markAsReported(issue.number)
            }
        }
    }

    private fun createIssue(range: RangeMarker): CreateIssueModel {
        val toDoItem = ToDoItem(range)
        return CreateIssueModel(null, null, toDoItem)
    }

    private fun collectAccounts(): Array<GithubAccount> {
        return GHAccountsUtil.accounts.toTypedArray()
    }

    private fun collectRepositories(): Array<RepositoryModel> {
        val repositoryManager = VcsRepositoryManager.getInstance(project)
        return repositoryManager.repositories
            .asSequence()
            .filterIsInstance<GitRepository>()
            .flatMap(::getRepositoryModels)
            .distinct()
            .toList()
            .toTypedArray()
    }

    private fun getRepositoryModels(repo: GitRepository): Sequence<RepositoryModel> {
        val urls = repo.remotes.asSequence().flatMap { it.urls }.flatMap(::getGitHubRepoUri)
        return urls.map { RepositoryModel(it, repo.root.toNioPath()) }
    }

    private fun getGitHubRepoUri(remoteUrl: String) = when {
        remoteUrl.startsWith("https://github.com/") -> listOf(remoteFromHttpsUrl(remoteUrl))
        remoteUrl.startsWith("git@github.com:") -> listOf(remoteFromSshUrl(remoteUrl))
        else -> emptyList()
    }

    private fun remoteFromHttpsUrl(remoteUrl: String) = URI(remoteUrl.removeSuffix(".git"))
    private fun remoteFromSshUrl(remoteUrl: String) = URI(
        "https://" +
            remoteUrl
                .removePrefix("git@")
                .replace(":", "/")
                .removeSuffix(".git")
    )
}
