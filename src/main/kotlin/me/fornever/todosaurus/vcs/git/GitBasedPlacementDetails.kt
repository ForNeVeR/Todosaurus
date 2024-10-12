package me.fornever.todosaurus.vcs.git

import me.fornever.todosaurus.issues.IssuePlacementDetails

class GitBasedPlacementDetails : IssuePlacementDetails {
    var remote: GitRemote? = null
}
