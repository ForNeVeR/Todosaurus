// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.views

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.actions.OpenIssueAfterCreationAction
import org.jetbrains.plugins.github.api.data.GithubIssue

object Notifications {
    object CreateIssue {
        fun success(issue: GithubIssue, project: Project) {
            val text = TodosaurusBundle.message("notification.issueCreated.text", issue.number)
            NotificationGroupManager.getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(text, NotificationType.INFORMATION)
                .addAction(OpenIssueAfterCreationAction(issue))
                .notify(project)
        }
    }

    object OpenIssueInBrowser {
        fun failed(exception: Exception, project: Project) {
            val title = TodosaurusBundle.message("notification.openIssueInBrowser.fail.title")
            NotificationGroupManager.getInstance()
                .getNotificationGroup("TodosaurusNotifications")
                .createNotification(title, exception.message ?: "Unexpected error", NotificationType.ERROR)
                .notify(project)
        }
    }
}
