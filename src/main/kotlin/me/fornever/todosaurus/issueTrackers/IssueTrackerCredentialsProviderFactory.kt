// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub
import me.fornever.todosaurus.issueTrackers.gitHub.GitHubCredentialsProvider

@Service(Service.Level.PROJECT)
class IssueTrackerCredentialsProviderFactory(private val project: Project) {
    companion object {
        fun getInstance(project: Project): IssueTrackerCredentialsProviderFactory = project.service()
    }

    fun create(issueTracker: IssueTracker): IssueTrackerCredentialsProvider
        = when (issueTracker) {
            is GitHub -> GitHubCredentialsProvider(project)
            else -> error("Issue tracker ${issueTracker.title} not supported")
        }
}
