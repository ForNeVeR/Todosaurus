---
name: todosaurus-skill
description: Connect unresolved TODO markers to GitHub issues and use IgnoreTODO regions for false positives.
---

<!--
SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

# Todosaurus TODO-to-Issue Connector

Use this skill when a repository has unresolved `TODO` markers and they must be connected to GitHub issues, while allowing `IgnoreTODO` regions for intentional false positives.

## Installation prerequisites

Ensure Todosaurus is installed before running this workflow.

If it's not installed, you can install it globally:
```console
$ dotnet tool install --global FVNever.Todosaurus.Cli
```

Or install it locally in the repository:
```console
$ dotnet new tool-manifest
```
(only if `.config/dotnet-tools.json` is not present yet), then:
```console
$ dotnet tool install FVNever.Todosaurus.Cli
```

Verify installation:
```console
$ todosaurus --version
$ dotnet tool run todosaurus --version
```

If installed as a global tool, run it as `todosaurus`. If installed via a tool manifest, run it as `dotnet tool run todosaurus`.

## Workflow

1. Run Todosaurus in the repository root (global install):
   ```console
   $ todosaurus
   ```
   For local tool-manifest install, use:
   ```console
   $ dotnet tool run todosaurus
   ```
2. Parse unresolved TODO findings and classify each finding:
   - **Actionable TODO**: a real follow-up task that should be tracked with a GitHub issue.
   - **False positive**: code/content where the word `TODO` is intentional functionality or data (for example, search patterns, parser tests, sample text, or literal matching logic).
3. Group actionable TODOs that describe the same underlying problem into one group.
4. Make sure GitHub CLI is authenticated for issue creation:
   ```console
   $ gh auth status
   ```
   If not authenticated, ask the user to run:
   ```console
   $ gh auth login
   ```
5. For each actionable TODO group, create one GitHub issue with:
   - A concise, actionable title.
   - A body including:
     - Problem description.
     - Why it matters.
     - Affected code locations as commit-specific, line-specific links.
6. Use `gh` to create each issue, for example:
   ```console
   $ gh issue create --title "<title>" --body "<body>"
   ```
7. Replace each corresponding actionable TODO marker in code:
   - From: `TODO` or `TODO:`.
   - To: `TODO[#<issue-number>]:` (keep the rest of the text unchanged after `TODO`/`TODO:`).
8. For false positives, add `IgnoreTODO` exclusion regions around the smallest safe code fragment:
   - Use `IgnoreTODO-Start` and `IgnoreTODO-End` on separate lines with the file's comment syntax.
   - Never place a TODO and an IgnoreTODO marker on the same line.
   - Do not nest ignore regions, and always close each `IgnoreTODO-Start`.
   - Do not create a GitHub issue for TODOs intentionally excluded this way.
9. Commit changes, but **do not push**:
   - If total created issues count is up to 5: make a single commit covering all updates.
   - If more than 5: make separate commits per issue group.
   - Commit message format:
     ```text
     (#123, #456, #678) Connect TODOs with the issues
     ```
     Use only relevant issue numbers for that commit.
10. Run Todosaurus again and ensure there are no warnings except the optional local warning about no GitHub token found (`GITHUB_TOKEN`/`GH_TOKEN`).

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
