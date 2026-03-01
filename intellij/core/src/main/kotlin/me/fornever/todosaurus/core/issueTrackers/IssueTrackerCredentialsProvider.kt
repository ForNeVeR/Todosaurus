// SPDX-FileCopyrightText: 2024–2025 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.core.issueTrackers

interface IssueTrackerCredentialsProvider {
    suspend fun provideAll(): Array<IssueTrackerCredentials>

    suspend fun provide(credentialsId: String): IssueTrackerCredentials?
}
