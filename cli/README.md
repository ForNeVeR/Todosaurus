<!--
SPDX-FileCopyrightText: 2024-2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

Todosaurus [![FVNever.Todosaurus.Cli on nuget.org][nuget.badge]][nuget]
=============
CLI for Todosaurus — a tool to process TODO issues in a repository.

Installation
------------
Todosaurus CLI is a [.NET tool][dotnet-tools], so it can be installed by getting a [.NET SDK][dotnet] 10 or later, and then executing a shell command

```console
$ dotnet tool install --global FVNever.Todosaurus.Cli
```
for global installation or
```console
$ dotnet new tool-manifest
$ dotnet tool install FVNever.Todosaurus.Cli
```
for local solution-wide installation.

Usage
-----
After installation, the tool will be available in shell as `dotnet todosaurus`.

Syntax:
```
dotnet todosaurus [switches] [command]
```

Command-line switches:
- `--help | -h | -?` — print the help;
- `--version` — print the program version. 

### Commands
#### `todosaurus scan`
This is **the default command** — it runs when `todosaurus` is invoked without a subcommand.

Scans all eligible text files (see [file discovery](#file-discovery) below) for unresolved TODO items and reports them. A TODO is considered unresolved if it matches the pattern `\b(?i)TODO(?-i)\b:?(?!\[.*?\])` — that is, a case-insensitive `TODO` (with an optional colon) that is **not** immediately followed by a bracketed issue reference like `[#123]`. This is the same pattern used by the [Todosaurus IntelliJ plugin][intellij].

The command exits with code **1** if any unresolved TODOs are found, and **0** otherwise, making it suitable for CI enforcement.

##### Output format
The output format depends on the environment:
- **CI** (when the [`CI` environment variable][ci-env] is set, as is standard for GitHub Actions, GitLab CI, Travis CI, Azure Pipelines, and most other CI providers): each finding is printed as a [GitHub Actions workflow command][gh-workflow-commands] `::warning` annotation. This makes unresolved TODOs appear as inline annotations in pull request diffs.
- **Local** (when `CI` is not set): each finding is printed in a human-readable format: `file(line): text`.

#### `todosaurus files`
This is a diagnostic command.

Lists text files in the current directory (recursively) that are eligible for TODO checking. Outputs one relative path per line, sorted alphabetically.

##### File discovery
- **Anywhere inside of a Git repository** (if a `.git` folder detected): runs `git ls-files` to list tracked files and untracked files that are not ignored by `.gitignore`. This means newly created files appear even before they are staged, but files matching `.gitignore` patterns are excluded.
  - **Git executable not found**: if the `git` command is not available on `PATH`, a warning is printed to stderr and the command falls back to recursive filesystem enumeration.
- **Outside of a Git repository**: recursively enumerates all the files under the current directory.

##### Binary file detection
Files are classified as text or binary using a heuristic: the first 8000 bytes of each file are read; if any NUL (`0x00`) byte is found, the file is considered binary and excluded from the output. Files that cannot be read (permission errors, etc.) are skipped with a warning.

Documentation
-------------
- [Changelog][docs.changelog]
- [Contributor Guide][docs.contributing]
- [Maintainer Guide][docs.maintaining]

License
-------
The project is distributed under the terms of [the MIT license][docs.license].

The license indication in the project's sources is compliant with the [REUSE specification v3.3][reuse.spec].

[ci-env]: https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/store-information-in-variables#default-environment-variables
[docs.changelog]: ../CHANGELOG.md
[docs.contributing]: CONTRIBUTING.md
[docs.license]: ../LICENSE.txt
[docs.maintaining]: ../MAINTAINING.md
[dotnet-tools]: https://learn.microsoft.com/en-us/dotnet/core/tools/dotnet-tool-list
[gh-workflow-commands]: https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/workflow-commands-for-github-actions
[intellij]: ../intellij/
[dotnet]: https://dotnet.microsoft.com/en-us/
[nuget.badge]: https://img.shields.io/nuget/v/FVNever.Todosaurus.Cli
[nuget]: https://www.nuget.org/packages/FVNever.Todosaurus.Cli
[reuse.spec]: https://reuse.software/spec-3.3/
[reuse]: https://reuse.software/