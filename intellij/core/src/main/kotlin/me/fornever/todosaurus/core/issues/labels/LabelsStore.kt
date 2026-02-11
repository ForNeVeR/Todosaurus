// SPDX-FileCopyrightText: 2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issues.labels

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "me.fornever.todosaurus.settings.LabelsStore",
    storages = [Storage("TodosaurusLabels.xml")]
)
class LabelsStore : SimplePersistentStateComponent<LabelsStore.State>(State.defaultState) {
    companion object {
        fun getInstance(project: Project): LabelsStore = project.service()
    }

    class State : BaseState() {
        companion object {
            val defaultState: State = State()
        }

        var labels: List<String> = emptyList()
    }
}
