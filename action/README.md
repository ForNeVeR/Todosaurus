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

### Notes
Please note that this action installs a required version of .NET SDK during its execution. It's recommended to isolate it from other build steps in your CI.

[docs.cli]: ../cli/README.md
