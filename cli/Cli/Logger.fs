// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

namespace Todosaurus.Cli

open System
open TruePath

type SourceInfo = SourceInfo of workingDirectory: AbsolutePath * file: LocalPath * line: int

/// <remarks>
/// For warnings/errors containing source info, on GitHub we will use special syntax:
/// https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands
/// </remarks>
type internal Logger =

    static let IsCi(): bool =
        let var = Environment.GetEnvironmentVariable("CI")
        not(isNull var) && var <> "0" && not (String.Equals(var, "false", StringComparison.InvariantCultureIgnoreCase))

    static let CiWorkspace = lazy AbsolutePath(nonNull <| Environment.GetEnvironmentVariable "GITHUB_WORKSPACE")

    static let SourceLocation(path: LocalPath) = path.RelativeTo CiWorkspace.Value
    static let FormatCiMessage (title: string) (message: string) (sourceInfo: SourceInfo) =
        let (SourceInfo(_, file, line)) = sourceInfo
        $"file=%s{(SourceLocation file).Value},line=%s{string line},title=%s{title}::%s{message}"

    static let FormatLocalMessage (title: string) (message: string) (sourceInfo: SourceInfo) =
        let (SourceInfo(cwd, file, line)) = sourceInfo
        $"%s{(file.RelativeTo cwd).Value}:%s{string line}: %s{title}: %s{message}"

    static member Info(message: string): unit =
        printfn $"%s{message}"

    /// Prints a warning.
    static member Warning(message: string): unit =
        if IsCi()
        then printfn $"::warning::%s{message}"
        else eprintfn $"WARNING: %s{message}"

    /// Prints a warning with a source location.
    static member Warning(title: string, message: string, sourceInfo: SourceInfo): unit =
        if IsCi()
        then printfn $"::warning %s{FormatCiMessage title message sourceInfo}"
        else eprintfn $"WARNING: %s{FormatLocalMessage title message sourceInfo}"

    /// Prints an error message.
    static member Error(message: string): unit =
        if IsCi()
        then printfn $"::error::%s{message}"
        else eprintfn $"ERROR: %s{message}"

    /// Prints an error with a source location.
    static member Error(title: string, message: string, sourceInfo: SourceInfo): unit =
        if IsCi()
        then printfn $"::error %s{FormatCiMessage title message sourceInfo}"
        else eprintfn $"ERROR: %s{FormatLocalMessage title message sourceInfo}"
