// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.vcs.git.ui.controls

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.TodosaurusBundle
import me.fornever.todosaurus.vcs.git.GitHostingRemote
import javax.swing.JList

class GitHostingRemoteComboBox : ComboBox<GitHostingRemote>() {
    init {
        renderer = object : SimpleListCellRenderer<GitHostingRemote?>() {
            override fun customize(
				list: JList<out GitHostingRemote?>,
				value: GitHostingRemote?,
				index: Int,
				selected: Boolean,
				hasFocus: Boolean
            ) {
                text = value?.ownerAndName ?: TodosaurusBundle.message("wizard.steps.chooseGitHostingRemote.remoteUrl.notFound.title")
            }
        }
    }
}
