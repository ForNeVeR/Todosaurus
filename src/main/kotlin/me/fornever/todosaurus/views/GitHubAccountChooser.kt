// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.views

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import javax.swing.JList

class GitHubAccountChooser(repos: Array<GithubAccount>) : ComboBox<GithubAccount>(repos) {
    init {
        renderer = object : SimpleListCellRenderer<GithubAccount?>() {
            override fun customize(
                list: JList<out GithubAccount?>,
                value: GithubAccount?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                text = value?.name
            }
        }
    }
}
