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

### Command-Line Syntax
```
dotnet todosaurus [global-switches…] [command] [command-switches…]
```

Global switches:
- `--config <path>` — path to configuration file (default: `todosaurus.toml` in working directory);
- `--help | -h | -?` — print the help;
- `--version` — print the program version.

#### `todosaurus files`
This is a diagnostic command.

Lists text files in the current directory (recursively) that are eligible for TODO checking. Outputs one relative path per line, sorted alphabetically.

#### `todosaurus scan`
This is **the default command** — it runs when `todosaurus` is invoked without a subcommand.

Scans all eligible text files (see [file discovery](#file-discovery) below) for unresolved TODO items (see [TODO Format Specification][docs.todo-format]) and reports them.

The command exits with a code reflecting the highest-priority issue found, making it suitable for CI enforcement.

When Todosaurus encounters a connected TODO (e.g., `TODO[#123]`), it can verify the referenced issue against GitHub. If the issue is closed or does not exist, Todosaurus reports a warning.

The output format depends on the environment:
- **CI** (when the [`CI` environment variable][ci-env] is set, as is standard for GitHub Actions, GitLab CI, Travis CI, Azure Pipelines, and most other CI providers): each finding is printed as a [GitHub Actions workflow command][gh-workflow-commands] `::warning` and `::error` annotation. This makes unresolved TODOs appear as inline annotations in pull request diffs.
- **Local** (when `CI` is not set): each finding is printed in a human-readable format: `file(line): text`.

### Configuration File
Todosaurus reads settings from a TOML configuration file. By default, it looks for `todosaurus.toml` in the working directory. Use the `--config` switch to specify a different path.

If `--config` is not provided and no `todosaurus.toml` exists, Todosaurus runs with default settings. If `--config` points to a missing file, the command exits with an error.

Example `todosaurus.toml`:
```toml
exclusions = [
    "build/**",
    "*.generated.cs",
]

[tracker]
url = "https://github.com/owner/repo"
```

| Key           | Description                                                                    |
|---------------|--------------------------------------------------------------------------------|
| `tracker.url` | GitHub repository URL for issue checking (must be a full GitHub URL).          |
| `exclusions`  | Array of glob patterns. Files matching any pattern are excluded from scanning. |

Glob patterns follow the syntax of [`Microsoft.Extensions.FileSystemGlobbing`][glob-syntax] — `*` matches within a directory, `**` matches across directories.

If `tracker.url` is not configured, Todosaurus determines the repository automatically:
1. Reads the URL of the `origin` Git remote (`git remote get-url origin`).
2. Parses the GitHub owner and repository name from the URL.

If neither source is available and connected TODOs exist, the command exits with code **5**.

### Environment Variables
#### `CI`
Todosaurus reads the `CI` environment variable to determine if it's running in a CI environment. Certain aspects of behavior depend on it:
1. In the CI environment, `GITHUB_WORKSPACE` is mandatory.
2. In the CI environment, the output format is changed to [GitHub Actions workflow command][gh-workflow-commands] syntax.

#### `GITHUB_TOKEN`, `GH_TOKEN`: GitHub Authentication
Todosaurus reads a GitHub token from environment variables (checked in order):
1. `GITHUB_TOKEN` — automatically available in GitHub Actions.
2. `GH_TOKEN` — used by the [GitHub CLI][gh-cli].

Without a token, only public repositories can be checked, subject to [GitHub's unauthenticated rate limit][gh-rate-limits] of 60 requests per hour.

#### `GITHUB_WORKSPACE`
When running in the CI environment, Todosaurus will use `GITHUB_WORKSPACE` to find the path to the repository root. 

### Exit Codes
| Code | Meaning                                                             |
|------|---------------------------------------------------------------------|
| 2    | IgnoreTODO marker errors                                            |
| 1    | Unresolved TODOs found (no issue number)                            |
| 3    | Connected TODOs reference non-existent issues                       |
| 4    | Connected TODOs reference closed issues                             |
| 5    | Connected TODOs found but GitHub repository could not be determined |
| 0    | All clear — no issues found                                         |

When multiple conditions are present, the highest-priority code is returned (priority: **2** > **1** > **3** > **4** > **5** > **0**).

### GitHub Actions setup
To use Todosaurus with connected TODO checking in a GitHub Actions workflow:

```yaml
name: TODOs
on: [push, pull_request]

jobs:
  permissions:
    contents: read
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-dotnet@v4
      - run: dotnet tool install --global FVNever.Todosaurus.Cli
      - name: Check TODOs
        env:
          GITHUB_TOKEN: ${{ github.token }}
        run: dotnet todosaurus scan
```

The default `GITHUB_TOKEN` provided by GitHub Actions has read access to the repository's issues, which is sufficient for connected TODO checking. No additional secrets or permissions are required.

### File discovery
- **Anywhere inside of a Git repository** (if a `.git` folder detected): runs `git ls-files` to list tracked files and untracked files that are not ignored by `.gitignore`. This means newly created files appear even before they are staged, but files matching `.gitignore` patterns are excluded.
  - **Git executable not found**: if the `git` command is not available on `PATH`, a warning is printed to stderr and the command falls back to recursive filesystem enumeration.
- **Outside of a Git repository**: recursively enumerates all the files under the current directory.

#### Binary file detection
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
[docs.todo-format]: ../README.md#todo-format-specification
[dotnet-tools]: https://learn.microsoft.com/en-us/dotnet/core/tools/dotnet-tool-list
[dotnet]: https://dotnet.microsoft.com/en-us/
[gh-cli]: https://cli.github.com/
[gh-rate-limits]: https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api
[gh-workflow-commands]: https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/workflow-commands-for-github-actions
[glob-syntax]: https://learn.microsoft.com/en-us/dotnet/core/extensions/file-globbing
[intellij]: ../intellij/README.md
[nuget.badge]: https://img.shields.io/nuget/v/FVNever.Todosaurus.Cli
[nuget]: https://www.nuget.org/packages/FVNever.Todosaurus.Cli
[reuse.spec]: https://reuse.software/spec-3.3/
[reuse]: https://reuse.software/
