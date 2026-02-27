// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

namespace Todosaurus.Cli

open TruePath

type SourceInfo = SourceInfo of workingDirectory: AbsolutePath * file: LocalPath * line: int

type internal LoggerContext = {
    mutable WarningCount: int
    mutable ErrorCount: int
    OnWarning: (string -> unit) option
    OnError: (string -> unit) option
}

module internal LoggerContext =
    let Create() = { WarningCount = 0; ErrorCount = 0; OnWarning = None; OnError = None }

/// <remarks>
/// For warnings/errors containing source info, on GitHub we will use special syntax:
/// https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands
/// </remarks>
type internal Logger =

    static let SourceLocation(path: LocalPath) = path.RelativeTo Env.CiWorkspace.Value
    static let FormatCiMessage (title: string) (message: string) (sourceInfo: SourceInfo) =
        let (SourceInfo(_, file, line)) = sourceInfo
        $"file=%s{(SourceLocation file).Value},line=%s{string line},title=%s{title}::%s{message}"

    static let FormatLocalMessage (title: string) (message: string) (sourceInfo: SourceInfo) =
        let (SourceInfo(cwd, file, line)) = sourceInfo
        $"%s{(file.RelativeTo cwd).Value}:%s{string line}: %s{title}: %s{message}"

    static member Info(message: string): unit =
        printfn $"%s{message}"

    /// Prints a warning.
    static member Warning(ctx: LoggerContext, message: string): unit =
        ctx.WarningCount <- ctx.WarningCount + 1
        match ctx.OnWarning with
        | Some collect -> collect message
        | None ->
            if Env.IsCi()
            then printfn $"::warning::%s{message}"
            else eprintfn $"WARNING: %s{message}"

    /// Prints a warning with a source location.
    static member Warning(ctx: LoggerContext, title: string, message: string, sourceInfo: SourceInfo): unit =
        ctx.WarningCount <- ctx.WarningCount + 1
        match ctx.OnWarning with
        | Some collect -> collect(FormatLocalMessage title message sourceInfo)
        | None ->
            if Env.IsCi()
            then printfn $"::warning %s{FormatCiMessage title message sourceInfo}"
            else eprintfn $"WARNING: %s{FormatLocalMessage title message sourceInfo}"

    /// Prints an error message.
    static member Error(ctx: LoggerContext, message: string): unit =
        ctx.ErrorCount <- ctx.ErrorCount + 1
        match ctx.OnError with
        | Some collect -> collect message
        | None ->
            if Env.IsCi()
            then printfn $"::error::%s{message}"
            else eprintfn $"ERROR: %s{message}"

    /// Prints an error with a source location.
    static member Error(ctx: LoggerContext, title: string, message: string, sourceInfo: SourceInfo): unit =
        ctx.ErrorCount <- ctx.ErrorCount + 1
        match ctx.OnError with
        | Some collect -> collect(FormatLocalMessage title message sourceInfo)
        | None ->
            if Env.IsCi()
            then printfn $"::error %s{FormatCiMessage title message sourceInfo}"
            else eprintfn $"ERROR: %s{FormatLocalMessage title message sourceInfo}"
