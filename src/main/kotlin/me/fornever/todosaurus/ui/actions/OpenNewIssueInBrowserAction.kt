// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.actions

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.issues.IssueModel

class OpenNewIssueInBrowserAction(private val issue: IssueModel) : AnAction(TodosaurusBundle.message("action.OpenNewIssueInBrowser.text")) {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project ?: return
        BrowserUtil.browse(issue.url, project)
    }
}
