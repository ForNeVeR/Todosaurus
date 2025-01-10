// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.ui.wizard.memoization

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.nio.file.Path

@Service(Service.Level.PROJECT)
class UserChoiceStore {
    companion object {
        const val STORE_FILE_NAME = "TodosaurusChoice.json"
        fun getInstance(project: Project): UserChoiceStore = project.service()
    }

    suspend fun rememberChoice(userChoice: UserChoice) {
        val choiceWriter = UserChoiceWriter()
        userChoice.accept(choiceWriter)

        val storeFile = openOrCreateStoreFile()

        storeFile.writeText(
            choiceWriter.json.toString())
    }

    suspend fun forgetChoice()
        = forgetChoice(
            openOrCreateStoreFile())

    suspend fun getChoice(): UserChoice? {
        val storeFile = openOrCreateStoreFile()

        val choiceJson = parseChoice(storeFile)
            ?: return null

        val choiceReader = UserChoiceReader(choiceJson)
        val userChoice = UserChoice()

        try {
            userChoice.accept(choiceReader)
        }
        catch (exception: Exception) {
            return null
        }

        return userChoice
    }

    private fun forgetChoice(storeFile: File)
        = storeFile.writeText(
            JsonObject(emptyMap()).toString())

    private fun parseChoice(storeFile: File): JsonObject? {
        val fileContent = storeFile.readText()

        if (fileContent.isBlank())
            return null

        val json = Json.parseToJsonElement(fileContent)

        if (json !is JsonObject)
            return null

        return json.jsonObject
    }

    private suspend fun openOrCreateStoreFile(): File {
        val storeFile = Path.of(PathManager.getOptionsPath(), STORE_FILE_NAME).toFile()

        if (withContext(Dispatchers.IO) { storeFile.createNewFile() })
            forgetChoice(storeFile)

        return storeFile
    }
}
