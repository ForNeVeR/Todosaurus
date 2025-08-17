<!--
SPDX-FileCopyrightText: 2024-2025 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

dotnet-repo-template [![Status Aquana][status-aquana]][andivionian-status-classifier]
====================
An opinionated repository template for my .NET projects.

Main Features
-------------
- GitHub Actions powered by [Generaptor][generaptor]: build, test and publish workflow.
- Documentation publishing using [docfx][] (published to GitHub Pages).
- License management using [REUSE][reuse].
- Automatic dependency update using [Renovate][renovate].
  - This includes GitHub Action runners and other dependencies such as VerifyEncoding, everything is auto-updated.

Deployment
----------
1. Clone the repository or just copy the repository files.
2. Fill the package description in `FVNeverDotNetTemplate/FVNeverDotNetTemplate.csproj`.
3. Replace the `FVNeverDotNetTemplateOwner` in the project sources with the repository owner's name on GitHub.
4. Replace the `FVNeverDotNetTemplate` with the project name
   - in the file and folder names,
   - in the sources.
5. Enable GitHub Pages publishing on the page `https://github.com/<owner>/<repository>/settings/pages`, choose **GitHub Actions**.
6. Update the project summary in `README.md` and `docs/index.md`.
7. Enable [Renovate][renovate] for your repository.
8. Delete this file (`/.github/README.md`).

License
-------
The project is distributed under the terms of [the MIT license][docs.license].

The license indication in the project's sources is compliant with the [REUSE specification v3.3][reuse.spec].

[andivionian-status-classifier]: https://andivionian.fornever.me/v1/#status-aquana-
[docfx]: https://dotnet.github.io/docfx/
[docs.license]: ../LICENSE.txt
[generaptor]: https://github.com/ForNeVeR/Generaptor/
[renovate]: https://github.com/apps/renovate
[reuse.spec]: https://reuse.software/spec-3.3/
[reuse]: https://reuse.software/
[status-aquana]: https://img.shields.io/badge/status-aquana-yellowgreen.svg
