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

Documentation
-------------
- [Changelog][docs.changelog]
- [Contributor Guide][docs.contributing]
- [Maintainer Guide][docs.maintaining]

License
-------
The project is distributed under the terms of [the MIT license][docs.license].

The license indication in the project's sources is compliant with the [REUSE specification v3.3][reuse.spec].

[docs.changelog]: CHANGELOG.md
[docs.contributing]: CONTRIBUTING.md
[docs.license]: ../LICENSE.txt
[docs.maintaining]: ../MAINTAINING.md
[dotnet-tools]: https://learn.microsoft.com/en-us/dotnet/core/tools/dotnet-tool-list
[dotnet]: https://dotnet.microsoft.com/en-us/
[nuget.badge]: https://img.shields.io/nuget/v/FVNever.Todosaurus.Cli
[nuget]: https://www.nuget.org/packages/FVNever.Todosaurus.Cli
[reuse.spec]: https://reuse.software/spec-3.3/
[reuse]: https://reuse.software/