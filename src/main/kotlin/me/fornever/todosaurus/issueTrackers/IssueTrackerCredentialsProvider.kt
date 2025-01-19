// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.issueTrackers

interface IssueTrackerCredentialsProvider {
    suspend fun provideAll(): Array<IssueTrackerCredentials>

    suspend fun provide(credentialsId: String): IssueTrackerCredentials?
}
