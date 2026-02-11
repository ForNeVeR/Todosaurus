// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.core.issueTrackers.IssueTracker
import javax.swing.JList

class IssueTrackerComboBox : ComboBox<IssueTracker>() {
    init {
    	renderer = object : SimpleListCellRenderer<IssueTracker>() {
            override fun customize(list: JList<out IssueTracker>, value: IssueTracker?, index: Int, selected: Boolean, focused: Boolean) {
                if (value == null)
                    return

                icon = value.icon
                text = value.title
            }
        }
    }
}
