package me.fornever.todosaurus.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issues.IssueModel
import me.fornever.todosaurus.ui.actions.OpenNewIssueInBrowserAction

object Notifications {
    object CreateNewIssue {
        fun success(issue: IssueModel, project: Project) {
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification("Issue ${issue.number} has been created.", NotificationType.INFORMATION)
                .addAction(OpenNewIssueInBrowserAction(issue))
                .notify(project)
        }

        fun failed(exception: Exception, project: Project) {
            NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification("Create new issue", "Failed to create issue: ${exception.message}", NotificationType.ERROR)
                .notify(project)
        }
    }

    object OpenReportedIssueInBrowser {
        fun failed(exception: Exception, project: Project) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification("Failed to open issue in browser", exception.message ?: "Unexpected error", NotificationType.ERROR)
                .notify(project)
        }
    }
}
