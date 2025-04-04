// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.ui.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import me.fornever.todosaurus.core.issues.ToDoItem

class ToDoLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName() = null

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (psiElement.children.isNotEmpty())
            return null

        if (!ToDoItem.containsToDo(psiElement))
            return null

        return ToDoLineMarker(psiElement)
    }
}
