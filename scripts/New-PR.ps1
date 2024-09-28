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

git config user.name $GitUserName
if (!$?) { throw "Error running git config: $LASTEXITCODE." }
git config user.email $GitUserEmail
if (!$?) { throw "Error running git config: $LASTEXITCODE." }

git switch --force-create $BranchName
if (!$?) { throw "Error running git switch: $LASTEXITCODE." }
git commit --all --message $CommitMessage
if (!$?) { throw "Error running git commit: $LASTEXITCODE." }
git push --set-upstream origin $BranchName
if (!$?) { throw "Error running git push: $LASTEXITCODE." }

gh pr create `
    --title $PrTitle `
    --body-file $PrBodyPath `
    --head $BranchName
if (!$?) { throw "Error running gh pr create: $LASTEXITCODE." }
