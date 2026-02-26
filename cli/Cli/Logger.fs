// SPDX-FileCopyrightText: 2026 Friedrich von Never <friedrich@fornever.me>
//
// SPDX-License-Identifier: MIT

namespace Todosaurus.Cli

open System.Threading
open TruePath

type SourceInfo = SourceInfo of workingDirectory: AbsolutePath * file: LocalPath * line: int

/// <remarks>
/// For warnings/errors containing source info, on GitHub we will use special syntax:
/// https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-commands
/// </remarks>
type internal Logger =

    static let warningCollector = AsyncLocal<(string -> unit) option>()
    static let errorCollector = AsyncLocal<(string -> unit) option>()

    static let SourceLocation(path: LocalPath) = path.RelativeTo Env.CiWorkspace.Value
    static let FormatCiMessage (title: string) (message: string) (sourceInfo: SourceInfo) =
        let (SourceInfo(_, file, line)) = sourceInfo
        $"file=%s{(SourceLocation file).Value},line=%s{string line},title=%s{title}::%s{message}"

    static let FormatLocalMessage (title: string) (message: string) (sourceInfo: SourceInfo) =
        let (SourceInfo(cwd, file, line)) = sourceInfo
        $"%s{(file.RelativeTo cwd).Value}:%s{string line}: %s{title}: %s{message}"

    static member internal SetCollectors
        (onWarning: (string -> unit) option, onError: (string -> unit) option): unit =
        warningCollector.Value <- onWarning
        errorCollector.Value <- onError

    static member Info(message: string): unit =
        printfn $"%s{message}"

    /// Prints a warning.
    static member Warning(message: string): unit =
        match warningCollector.Value with
        | Some collect -> collect message
        | None ->
            if Env.IsCi()
            then printfn $"::warning::%s{message}"
            else eprintfn $"WARNING: %s{message}"

    /// Prints a warning with a source location.
    static member Warning(title: string, message: string, sourceInfo: SourceInfo): unit =
        match warningCollector.Value with
        | Some collect -> collect(FormatLocalMessage title message sourceInfo)
        | None ->
            if Env.IsCi()
            then printfn $"::warning %s{FormatCiMessage title message sourceInfo}"
            else eprintfn $"WARNING: %s{FormatLocalMessage title message sourceInfo}"

    /// Prints an error message.
    static member Error(message: string): unit =
        match errorCollector.Value with
        | Some collect -> collect message
        | None ->
            if Env.IsCi()
            then printfn $"::error::%s{message}"
            else eprintfn $"ERROR: %s{message}"

    /// Prints an error with a source location.
    static member Error(title: string, message: string, sourceInfo: SourceInfo): unit =
        match errorCollector.Value with
        | Some collect -> collect(FormatLocalMessage title message sourceInfo)
        | None ->
            if Env.IsCi()
            then printfn $"::error %s{FormatCiMessage title message sourceInfo}"
            else eprintfn $"ERROR: %s{FormatLocalMessage title message sourceInfo}"
