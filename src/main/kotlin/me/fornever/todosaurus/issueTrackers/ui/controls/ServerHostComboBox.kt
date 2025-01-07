// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.TodosaurusBundle
import javax.swing.JList

class ServerHostComboBox : ComboBox<String>() {
    init {
        renderer = object : SimpleListCellRenderer<String>() {
            override fun customize(list: JList<out String>, value: String?, index: Int, selected: Boolean, focused: Boolean) {
                text = value ?: TodosaurusBundle.message("wizard.steps.chooseIssueTracker.serverHost.notFound.title")
            }
        }
    }
}
