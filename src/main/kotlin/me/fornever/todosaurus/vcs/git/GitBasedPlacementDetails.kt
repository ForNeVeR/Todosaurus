// SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
//
// SPDX-License-Identifier: MIT

package me.fornever.todosaurus.vcs.git

import me.fornever.todosaurus.issues.IssuePlacementDetails

class GitBasedPlacementDetails : IssuePlacementDetails {
    var remote: GitRemote? = null
}
