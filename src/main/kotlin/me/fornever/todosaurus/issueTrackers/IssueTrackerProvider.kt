// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.tasks.TaskRepositoryType
import com.intellij.util.containers.toArray
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub

@Service(Service.Level.PROJECT)
class IssueTrackerProvider(private val project: Project) {
    companion object {
        fun getInstance(project: Project): IssueTrackerProvider = project.service()
    }

    fun provideAll(): Array<IssueTracker>
        = TaskRepositoryType
            .getRepositoryTypes()
            .mapNotNull { createTracker(it) }
            .toArray(emptyArray())

    fun provide(title: String): IssueTracker?
        = provideAll().singleOrNull { it.title == title }

    private fun createTracker(issueTrackerType: TaskRepositoryType<*>): IssueTracker? {
        val icon = issueTrackerType.icon
        val title = issueTrackerType.name

        if (title == GitHub::class.simpleName) // TODO: Mark GithubRepository in intellij-community as public. Then change this condition to "details.repositoryClass == GithubRepository::class.java"
            return GitHub(project, icon, title)

        return when (issueTrackerType.repositoryClass) {
            else -> null
        }
    }
}
