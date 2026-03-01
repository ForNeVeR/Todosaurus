<!--
SPDX-FileCopyrightText: 2024-2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

Todosaurus [![Status Enfer][status-enfer]][andivionian-status-classifier]
==========
Todosaurus is a tool that helps to work with [TODO][wiki.todo] notes in software development projects.

It will help you to connect any TODO with an issue in the issue tracker, and make sure that no TODOs in the whole project's source code are left unattended.

The following programs, useable separately, are included into Todosaurus:
1. [Todosaurus plugin for IntelliJ-based IDEs][docs.intellij] — allows to quickly create tracker issues from TODO notes.
2. [Todosaurus CLI][docs.cli] — helps to verify the TODO notes from the command line.

Quick Installation Links
------------------------
- **IntelliJ Plugin:** [![Download IntelliJ Plugin][badge.plugin]][marketplace.plugin] 
- **CLI:** [![FVNever.Todosaurus.Cli on nuget.org][nuget.badge]][nuget]

Read the documentation of each Todosaurus component if you want to know more.

TODO Format Specification
-------------------------
Both Todosaurus components share a common TODO format.

### Unresolved TODOs
A TODO is considered **unresolved** if it matches the regex pattern:
```
\b(?i)TODO(?-i)\b:?(?!\[.*?\])
```
That is, a case-insensitive word `TODO` (with an optional colon) that is **not** immediately followed by a bracketed issue reference like `[#123]`.

### Resolved TODOs
Appending `[#<number>]` **immediately** after `TODO` marks it as resolved:
```
// TODO[#123]: Fix this code
```
A space before the bracket does **not** resolve the TODO — `TODO [#123]` is still considered unresolved.

### Multiple TODOs on One Line
Multiple TODOs on the same line are allowed. Each is matched independently — a `[#issue]` resolves only the TODO it immediately follows. The line is flagged if **any** unresolved TODO remains. For example:
- `// TODO[#1]: done TODO fix` — flagged (second TODO is unresolved)
- `// TODO[#1]: done TODO[#2]: also done` — not flagged

### IgnoreTODO Markers
Regions of a file can be excluded from scanning using marker comments:
```
// IgnoreTODO-Start
// TODO: this will not be reported
// IgnoreTODO-End
```
Lines between `IgnoreTODO-Start` and `IgnoreTODO-End` are skipped. The marker lines themselves are not scanned for TODOs.

The following are **errors** (non-zero exit code in the CLI):
- Unclosed `IgnoreTODO-Start` (no matching `IgnoreTODO-End` before end of file)
- Nested `IgnoreTODO-Start` (opening a new region while one is already open)
- `IgnoreTODO-End` without a matching `IgnoreTODO-Start`
- Multiple IgnoreTODO markers on the same line
- An IgnoreTODO marker and a TODO on the same line

Documentation
-------------
- [Changelog][docs.changelog]
- [IntelliJ Plugin][docs.intellij]
- [CLI][docs.cli]
- [Contributor Guide (General)][docs.contributing]
- [Contributor Guide (IntelliJ Plugin)][docs.contributing.intellij]
- [Contributor Guide (CLI)][docs.contributing.cli]
- [Maintainer Guide][docs.maintaining]

License
-------
The project is distributed under the terms of [the MIT license][docs.license] (unless a particular file states otherwise).

The license indication in the project's sources is compliant with the [REUSE specification v3.3][reuse.spec].

[andivionian-status-classifier]: https://andivionian.fornever.me/v1/#status-enfer-
[badge.plugin]: https://img.shields.io/jetbrains/plugin/v/23838.svg
[docs.changelog]: CHANGELOG.md
[docs.cli]: cli/README.md
[docs.contributing.cli]: cli/CONTRIBUTING.md
[docs.contributing.intellij]: intellij/CONTRIBUTING.md
[docs.contributing]: CONTRIBUTING.md
[docs.intellij]: intellij/README.md
[docs.license]: LICENSE.txt
[docs.maintaining]: MAINTAINING.md
[marketplace.plugin]: https://plugins.jetbrains.com/plugin/23838
[nuget.badge]: https://img.shields.io/nuget/v/FVNever.Todosaurus.Cli
[nuget]: https://www.nuget.org/packages/FVNever.Todosaurus.Cli
[reuse.spec]: https://reuse.software/spec-3.3/
[reuse]: https://reuse.software/
[status-enfer]: https://img.shields.io/badge/status-enfer-orange.svg
[wiki.todo]: https://en.wikipedia.org/wiki/TODO_(tag)
