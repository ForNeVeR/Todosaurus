package me.fornever.todosaurus.issueTrackers.anonymous

import com.intellij.collaboration.api.ServerPath
import me.fornever.todosaurus.issueTrackers.IssueTrackerCredentials

class AnonymousCredentials(override val serverPath: ServerPath) : IssueTrackerCredentials {
    override val username: String
        get() = "Anonymous"
}
