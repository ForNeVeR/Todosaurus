// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials
import javax.swing.*

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
