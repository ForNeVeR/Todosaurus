package me.fornever.todosaurus.issueTrackers.ui.wizard

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import me.fornever.todosaurus.issueTrackers.IssueTracker
import me.fornever.todosaurus.issueTrackers.gitHub.GitHub
import me.fornever.todosaurus.vcs.git.ui.wizard.ChooseGitRemoteStep

@Service(Service.Level.APP)
class SpecificIssueTrackerStepProvider {
    companion object {
        fun getInstance(): SpecificIssueTrackerStepProvider = service()
    }

    fun create(issueTracker: IssueTracker): Any?
        = when (issueTracker) {
            is GitHub -> ChooseGitRemoteStep.id
            else -> null
        }
}
