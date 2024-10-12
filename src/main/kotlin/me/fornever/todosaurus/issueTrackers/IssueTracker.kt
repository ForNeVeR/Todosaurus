package me.fornever.todosaurus.issueTrackers

import me.fornever.todosaurus.issues.IssuePlacementDetails
import javax.swing.Icon

interface IssueTracker {
    val icon: Icon

    val title: String

    suspend fun checkConnection(credentials: IssueTrackerCredentials): TestConnectionResult

    fun createClient(credentials: IssueTrackerCredentials, placementDetails: IssuePlacementDetails): IssueTrackerClient
}

