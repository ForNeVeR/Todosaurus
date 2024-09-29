param(
    [Parameter(Mandatory = $true)] $Version,
    [Parameter(Mandatory = $true)] $ReleaseNotesFilePath,
    [Parameter(Mandatory = $true)] $FileToUpload
)

gh release create --title "Todosaurus v$Version" --notes-file $ReleaseNotesFilePath "v$Version" $FileToUpload
if (!$$) {
    throw "Error running gh release: $LASTEXITCODE."
}
