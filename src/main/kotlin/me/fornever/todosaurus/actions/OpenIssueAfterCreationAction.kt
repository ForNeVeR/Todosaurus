package me.fornever.todosaurus.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.TodosaurusBundle
import org.jetbrains.plugins.github.api.data.GithubIssue

class OpenIssueAfterCreationAction(private val issue: GithubIssue) : AnAction(
    TodosaurusBundle.message("notification.issueCreated.openIssue")
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val url = issue.htmlUrl
        BrowserUtil.browse(url, project)
    }
}
