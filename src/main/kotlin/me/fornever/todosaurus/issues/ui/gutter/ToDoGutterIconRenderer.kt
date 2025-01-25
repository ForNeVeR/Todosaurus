// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issues.ui.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ToDoGutterIconRenderer(lineMarker: ToDoLineMarker) : LineMarkerInfo.LineMarkerGutterIconRenderer<PsiElement>(lineMarker) {
    override fun equals(other: Any?)
        = other is ToDoGutterIconRenderer

    override fun hashCode() = 0

    override fun getIcon(): Icon
        = AllIcons.General.TodoDefault

    override fun getPopupMenuActions(): ActionGroup? = ActionGroup.EMPTY_GROUP

    override fun isDumbAware() = true
}
