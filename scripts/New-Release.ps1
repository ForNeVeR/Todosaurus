# SPDX-FileCopyrightText: 2024 Todosaurus contributors <https://github.com/ForNeVeR/Todosaurus>
#
# SPDX-License-Identifier: MIT

param(
    [Parameter(Mandatory = $true)] $Version,
    [Parameter(Mandatory = $true)] $ReleaseNotesFilePath,
    [Parameter(Mandatory = $true)] $FileToUpload
)

gh release create --title "Todosaurus v$Version" --notes-file $ReleaseNotesFilePath "v$Version" $FileToUpload
if (!$$) {
    throw "Error running gh release: $LASTEXITCODE."
}
