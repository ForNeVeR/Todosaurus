// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

/// Exit codes for the Todosaurus CLI, sorted by priority (0 = lowest, 6 = highest).
module Todosaurus.Cli.ExitCodes

/// All clear — no issues found.
let Success: int = 0

/// Strict mode: warnings were emitted (only with --strict).
let StrictModeWarnings: int = 1

/// Connected TODOs found but GitHub repository could not be determined.
let TrackerUnresolvable: int = 2

/// Connected TODOs refer to closed issues.
let ClosedIssues: int = 3

/// Connected TODOs refer to non-existent issues.
let NonExistentIssues: int = 4

/// Unresolved TODOs found (no issue number).
let UnresolvedTodos: int = 5

/// IgnoreTODO marker errors or configuration errors.
let MarkerErrors: int = 6

/// Configuration file error.
let ConfigError: int = 7
