package me.fornever.todosaurus.issueTrackers

import com.intellij.collaboration.api.ServerPath

interface IssueTrackerCredentials {
    val id: String

    val username: String

    val serverPath: ServerPath
}
