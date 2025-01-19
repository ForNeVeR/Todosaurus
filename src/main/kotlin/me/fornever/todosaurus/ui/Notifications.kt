// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.issues.IssueModel
import me.fornever.todosaurus.ui.actions.OpenNewIssueInBrowserAction

object Notifications {
    object CreateNewIssue {
        fun success(issue: IssueModel, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusBundle.getMessage("notifications.createNewIssue.title"),
                    TodosaurusBundle.getMessage("notifications.createNewIssue.success.text", issue.number),
                    NotificationType.INFORMATION)
                .addAction(OpenNewIssueInBrowserAction(issue))
                .notify(project)

        fun memoizationWarning(exception: Exception, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusBundle.getMessage("notifications.createNewIssue.title"),
                    TodosaurusBundle.getMessage("notifications.createNewIssue.memoizationWarning.text", exception.message),
                    NotificationType.WARNING)
                .notify(project)

        fun creationFailed(exception: Exception, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusBundle.getMessage("notifications.createNewIssue.title"),
                    TodosaurusBundle.getMessage("notifications.createNewIssue.failed.text", exception.message),
                    NotificationType.ERROR)
                .notify(project)
    }

    object OpenReportedIssueInBrowser {
        fun failed(exception: Exception, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusBundle.getMessage("notifications.openReportedIssueInBrowser.title"),
                    TodosaurusBundle.getMessage("notifications.openReportedIssueInBrowser.failed.text", exception.message),
                    NotificationType.ERROR)
                .notify(project)
    }
}
