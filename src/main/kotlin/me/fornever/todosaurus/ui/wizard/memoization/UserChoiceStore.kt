// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.wizard.memoization

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import me.fornever.todosaurus.issueTrackers.anonymous.AnonymousCredentials

@Service(Service.Level.PROJECT)
@State(
    name = "me.fornever.todosaurus.settings.UserChoiceStore",
    storages = [Storage("TodosaurusChoice.xml")]
)
class UserChoiceStore : SimplePersistentStateComponent<UserChoiceStore.State>(State.defaultState) {
    companion object {
        fun getInstance(project: Project): UserChoiceStore = project.service()
    }

    class State : BaseState() {
        companion object {
            val defaultState: State = State()
        }

        var userChoice by string()
    }

    fun rememberChoice(userChoice: UserChoice) {
        if (userChoice.credentialsId == AnonymousCredentials.ID)
            error("Saving an anonymous account is not supported")

        val choiceWriter = UserChoiceWriter()
        userChoice.accept(choiceWriter)

        state.userChoice = choiceWriter.json.toString()
    }

    fun forgetChoice() {
        state.userChoice = null
    }

    fun getChoiceOrNull(): UserChoice? {
        val choiceReader = UserChoiceReader(parseChoice() ?: return null)
        val userChoice = UserChoice()

        try {
            userChoice.accept(choiceReader)
        }
        catch (_: Exception) {
            return null
        }

        return userChoice
    }

    private fun parseChoice(): JsonObject? {
        val userChoice = state.userChoice
            ?: return null

        if (userChoice.isBlank())
            return null

        val json = Json.parseToJsonElement(userChoice)

        if (json !is JsonObject)
            return null

        return json.jsonObject
    }
}
