// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

// TODO[#38]: Remember last selected account
@Service(Service.Level.PROJECT)
class RememberChoiceStore {
    companion object {
        fun getInstance(project: Project): RememberChoiceStore = project.service()
    }

    suspend fun remember() {
        // TODO[#38]: Remember last selected account
    }

    suspend fun forget() {
        // TODO[#38]: Remember last selected account
    }
}
