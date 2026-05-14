---
name: todosaurus-skill
description: Connect unresolved TODO markers to GitHub issues and update source TODOs with issue references.
---

<!--
SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

# Todosaurus TODO-to-Issue Connector

Use this skill when a repository has unresolved `TODO` markers and they must be connected to GitHub issues.

## Installation prerequisites

Ensure Todosaurus is installed before running this workflow:

```console
$ todosaurus --version
```

If it's not installed, you can install it globally:
```console
$ dotnet tool install --global FVNever.Todosaurus.Cli
```

Or install it locally in the repository:
```console
$ dotnet new tool-manifest
$ dotnet tool install FVNever.Todosaurus.Cli
```

If installed as a global tool, run it as `todosaurus`.

## Workflow

1. Run Todosaurus in the repository root:
   ```console
   $ todosaurus
   ```
2. Parse unresolved TODO findings and group TODOs that describe the same underlying problem into one group.
3. Make sure GitHub CLI is authenticated for issue creation:
   ```console
   $ gh auth status
   ```
   If not authenticated, ask the user to run:
   ```console
   $ gh auth login
   ```
4. For each TODO group, create one GitHub issue with:
   - A concise, actionable title.
   - A body including:
     - Problem description.
     - Why it matters.
     - Affected code locations as commit-specific, line-specific links.
5. Use `gh` to create each issue, for example:
   ```console
   $ gh issue create --title "<title>" --body "<body>"
   ```
6. Replace each corresponding TODO marker in code:
   - From: `TODO` or `TODO:`
   - To: `TODO[#<issue-number>]` (keep the rest of the text unchanged).
7. Commit changes, but **do not push**:
   - If total created issues count is up to 5: make a single commit covering all updates.
   - If more than 5: make separate commits per issue group.
   - Commit message format:
     ```text
     (#123, #456, #678) Connect TODOs with the issues
     ```
     Use only relevant issue numbers for that commit.
8. Run Todosaurus again and ensure there are no warnings except the optional local warning about missing `GITHUB_TOKEN`.

## Code link format for issue body

Use commit-specific and line-specific GitHub links so previews render correctly:

```text
https://github.com/<owner>/<repo>/blob/<commit-sha>/<path>#L<start>-L<end>
```

(`-L<end>` is optional if the TODO is on a single line.)

Example:

```text
https://github.com/ForNeVeR/Todosaurus/blob/0123456789abcdef0123456789abcdef01234567/cli/Cli/Program.fs#L42-L56
```

When multiple TODOs are mapped to one issue, include all corresponding links in that issue body.
