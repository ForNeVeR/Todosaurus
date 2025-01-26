// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues.ui.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import me.fornever.todosaurus.issues.ToDoItem
import me.fornever.todosaurus.issues.ui.gutter.actions.CreateNewIssueForAction
import me.fornever.todosaurus.issues.ui.gutter.actions.OpenReportedIssueWithNumberAction
import javax.swing.Icon

class ToDoGutterIconRenderer(private val lineMarker: ToDoLineMarker) : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(lineMarker) {
    override fun equals(other: Any?)
        = other is ToDoGutterIconRenderer

    override fun hashCode() = 0

    override fun getIcon(): Icon
        = AllIcons.General.TodoDefault

    override fun getPopupMenuActions(): ActionGroup? {
        if (lineMarker.toDoItems.isEmpty())
            return ActionGroup.EMPTY_GROUP

        return object : ActionGroup() {
            override fun getChildren(actionEvent: AnActionEvent?): Array<AnAction>
                = lineMarker.toDoItems.map { when(it) {
                    is ToDoItem.New -> CreateNewIssueForAction(it)
                    is ToDoItem.Reported -> OpenReportedIssueWithNumberAction(it)
                } }
                .toTypedArray()
        }
    }

    override fun isDumbAware() = true
}
