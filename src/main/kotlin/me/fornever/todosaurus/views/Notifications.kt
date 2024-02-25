package me.fornever.todosaurus.views

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.TodosaurusBundle
import org.jetbrains.plugins.github.api.data.GithubIssue

object Notifications {
    fun issueCreated(issue: GithubIssue, project: Project) {
        val text = TodosaurusBundle.message("notification.issueCreated.text", issue.number)
        NotificationGroupManager.getInstance()
            .getNotificationGroup("TodosaurusNotifications")
            .createNotification(text, NotificationType.INFORMATION)
            .addAction(OpenIssueAction(issue))
            .notify(project)
    }
}

private class OpenIssueAction(private val issue: GithubIssue) : AnAction(
    TodosaurusBundle.message("notification.issueCreated.openIssue")
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val url = issue.htmlUrl
        BrowserUtil.browse(url, project)
    }
}
