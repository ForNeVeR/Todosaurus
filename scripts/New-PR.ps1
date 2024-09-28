param (
    [Parameter(Mandatory = $true)] $BranchName,
    [Parameter(Mandatory = $true)] $CommitMessage,
    [Parameter(Mandatory = $true)] $PrTitle,
    [Parameter(Mandatory = $true)] $PrBodyPath,

    $GitUserName = 'Todosaurus Automation',
    $GitUserEmail = 'friedrich@fornever.me'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

Write-Output 'Configuring Git…'
git config user.name $GitUserName
if (!$?) { throw "Error running git config: $LASTEXITCODE." }
git config user.email $GitUserEmail
if (!$?) { throw "Error running git config: $LASTEXITCODE." }

Write-Output 'Generating a branch…'
git switch --force-create $BranchName
if (!$?) { throw "Error running git switch: $LASTEXITCODE." }
git commit --all --message $CommitMessage
if (!$?) { throw "Error running git commit: $LASTEXITCODE." }

Write-Output 'Checking if a PR already exists…'
[array] $issues = gh pr list --head $BranchName --json url | ConvertFrom-Json
if (!$?) { throw "Error running gh pr list: $LASTEXITCODE." }
if ($issues) {
    Write-Output "PR already exists: $($issues[0].url)."
    Write-Output 'Comparing the local branch with the server one…'
    $localTreeHash = git rev-parse 'HEAD^{tree}'
    if (!$?) { throw "Error running git rev-parse: $LASTEXITCODE." }

    $remoteTreeHash = git rev-parse "origin/$BranchName^{tree}"
    if (!$?) { throw "Error running git rev-parse: $LASTEXITCODE." }

    if ($localTreeHash -eq $remoteTreeHash) {
        Write-Output "Local tree hash is the same as the remote tree hash: `"$localTreeHash`"."
    } else {
        Write-Output "Local tree hash `"$localTreeHash`" is not the same as the remote tree hash `"$remoteTreeHash`". Skipping the branch push."

        Write-Output 'Force-pushing the branch…'
        git push --force --set-upstream origin $BranchName
        if (!$?) { throw "Error running git push: $LASTEXITCODE." }
    }
} else {
    Write-Output 'Force-pushing the branch…'
    git push --force --set-upstream origin $BranchName
    if (!$?) { throw "Error running git push: $LASTEXITCODE." }

    Write-Output 'Creating a pull request…'
    gh pr create `
        --title $PrTitle `
        --body-file $PrBodyPath `
        --head $BranchName
    if (!$?) { throw "Error running gh pr create: $LASTEXITCODE." }
}
