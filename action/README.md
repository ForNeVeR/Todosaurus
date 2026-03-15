<!--
SPDX-FileCopyrightText: 2024-2026 Friedrich von Never <friedrich@fornever.me>

SPDX-License-Identifier: MIT
-->

Todosaurus GitHub Action
========================

Usage
-----
```yaml
jobs:
  todos:
    - name: Check out the sources
      uses: actions/checkout@v6
    - name: Check TODOs
      uses: ForNeVeR/Todosaurus/action@v1
      with:
        config: todosaurus.toml # optional
        strict: 'true' # optional
        github-token: ${{ secrets.GITHUB_TOKEN }} # optional, recommended
```

Read more on the configuration file in [the CLI documentation][docs.cli].

### Inputs

| Input              | Description                                              | Required | Default   |
|--------------------|----------------------------------------------------------|----------|-----------|
| `config`           | Path to the configuration file.                          | No       |           |
| `strict`           | Enable strict mode for validation.                       | No       | `false`   |
| `github-token`     | Token to fetch issues. Helps avoid rate-limiting.        | No       |           |
| `build-from-source`| Build from source instead of installing the NuGet tool.  | No       | `false`   |
| `version`          | Version of the NuGet tool to install.                    | No       | `1.10.1`  |

### Notes
Please note that this action installs a required version of .NET SDK during its execution. It's recommended to isolate it from other build steps in your CI.

When `build-from-source` is set to `'true'`, the action builds the CLI from the repository sources instead of installing a published NuGet package. This is primarily used for integration testing.

[docs.cli]: ../cli/README.md
