// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.core.TodosaurusCoreBundle
import me.fornever.todosaurus.core.issues.IssueModel
import me.fornever.todosaurus.core.ui.actions.OpenNewIssueInBrowserAction

object Notifications {
    object CreateNewIssue {
        fun success(issue: IssueModel, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusCoreBundle.getMessage("notifications.createNewIssue.title"),
                    TodosaurusCoreBundle.getMessage("notifications.createNewIssue.success.text", issue.number),
                    NotificationType.INFORMATION)
                .addAction(OpenNewIssueInBrowserAction(issue))
                .notify(project)

        fun memoizationWarning(exception: Exception, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusCoreBundle.getMessage("notifications.createNewIssue.title"),
                    TodosaurusCoreBundle.getMessage("notifications.createNewIssue.memoizationWarning.text", exception),
                    NotificationType.WARNING)
                .notify(project)

        fun creationFailed(exception: Exception, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusCoreBundle.getMessage("notifications.createNewIssue.title"),
                    TodosaurusCoreBundle.getMessage("notifications.createNewIssue.failed.text", exception.message),
                    NotificationType.ERROR)
                .notify(project)
    }

    object OpenReportedIssueInBrowser {
        fun failed(exception: Exception, project: Project)
            = NotificationGroupManager
                .getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(
                    TodosaurusCoreBundle.getMessage("notifications.openReportedIssueInBrowser.title"),
                    TodosaurusCoreBundle.getMessage("notifications.openReportedIssueInBrowser.failed.text", exception.message),
                    NotificationType.ERROR)
                .notify(project)
    }
}
