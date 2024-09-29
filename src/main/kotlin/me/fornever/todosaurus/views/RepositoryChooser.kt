// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.views

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import me.fornever.todosaurus.models.RepositoryModel
import javax.swing.JList

class RepositoryChooser(repos: Array<RepositoryModel>) : ComboBox<RepositoryModel>(repos) {
    init {
        renderer = object : SimpleListCellRenderer<RepositoryModel?>() {
            override fun customize(
                list: JList<out RepositoryModel?>,
                value: RepositoryModel?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                text = value?.ownerAndName
            }
        }
    }
}
