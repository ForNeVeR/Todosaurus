// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.settings

import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(
    name = "me.fornever.todosaurus.settings.TodosaurusSettings",
    storages = [Storage("TodosaurusSettings.xml")]
)
class TodosaurusSettings : SimplePersistentStateComponent<TodosaurusSettings.State>(State.defaultState) {
    companion object {
        const val ISSUE_NUMBER_REPLACEMENT = "{ISSUE_NUMBER}"
        const val URL_REPLACEMENT = "{URL_REPLACEMENT}"

        fun getInstance(): TodosaurusSettings = service()
    }

    class State : BaseState() {
        companion object {
            private const val DEFAULT_NUMBER_PATTERN = "[#$ISSUE_NUMBER_REPLACEMENT]:"
            private const val DEFAULT_DESCRIPTION_TEMPLATE = "See the code near this line: $URL_REPLACEMENT\n\nAlso, look for the number of this issue in the project code base."

            val defaultState: State = State()
        }

        var numberPattern: String = DEFAULT_NUMBER_PATTERN
        var descriptionTemplate: String = DEFAULT_DESCRIPTION_TEMPLATE
    }
}
