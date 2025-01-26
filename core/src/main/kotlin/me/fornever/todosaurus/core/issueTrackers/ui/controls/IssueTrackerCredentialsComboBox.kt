// SPDX-FileCopyrightText: 2024-2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.core.issueTrackers.IssueTrackerCredentials
import javax.swing.JList

class IssueTrackerCredentialsComboBox : ComboBox<IssueTrackerCredentials>() {
    init {
        renderer = object : SimpleListCellRenderer<IssueTrackerCredentials>() {
            override fun customize(list: JList<out IssueTrackerCredentials>, value: IssueTrackerCredentials?, index: Int, selected: Boolean, focused: Boolean) {
                if (value == null)
                    return

                text = value.username
            }
        }
    }
}
