package me.fornever.todosaurus.settings

import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
import me.fornever.todosaurus.issueTrackers.IssueTracker
import me.fornever.todosaurus.issueTrackers.IssueTrackerProvider

class IssueTrackerConverter : Converter<IssueTracker>() {
    override fun toString(issueTracker: IssueTracker?, context: ConvertContext): String? {
        if (issueTracker == null)
            return null

        return issueTracker.title
    }

    override fun fromString(issueTracker: String?, context: ConvertContext): IssueTracker? {
        if (issueTracker == null)
            return  null

        return IssueTrackerProvider
            .getInstance(context.project)
            .provide(issueTracker)
    }
}
