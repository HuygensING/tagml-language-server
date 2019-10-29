package nl.knaw.huc.di.rd.tag.tagml.lsp

class Methods {
    // https://microsoft.github.io/language-server-protocol/specifications/specification-3-14
    companion object {
        const val initialize = "initialize"
        const val initialized = "initialized"
        const val shutdown = "shutdown"
        const val exit = "exit"
        const val cancelRequest = "$/cancelRequest"
    }

    internal object Window {
        const val showMessage = "window/showMessage"
        const val showMessageRequest = "window/showMessageRequest"
        const val logMessage = "window/logMessage"
    }

    internal object Telemetry {
        const val event = "telemetry/event"
    }

    internal object Client {
        const val registerCapability = "client/registerCapability"
        const val unregisterCapability = "client/unregisterCapability"
    }

    internal object Workspace {
        const val workspaceFolders = "workspace/workspaceFolders"
        const val didChangeWorkspaceFolders = "workspace/didChangeWorkspaceFolders"
        const val didChangeConfiguration = "workspace/didChangeConfiguration"
        const val configuration = "workspace/configuration"
        const val didChangeWatchedFiles = "workspace/didChangeWatchedFiles"
        const val symbol = "workspace/symbol"
        const val executeCommand = "workspace/executeCommand"
        const val applyEdit = "workspace/applyEdit"
    }

    internal object TextDocument {
        const val didOpen = "textDocument/didOpen"
        const val didChange = "textDocument/didChange"
        const val willSave = "textDocument/willSave"
        const val willSaveWaitUntil = "textDocument/willSaveWaitUntil"
        const val didSave = "textDocument/didSave"
        const val didClose = "textDocument/didClose"
        const val publishDiagnostics = "textDocument/publishDiagnostics"
        const val completion = "textDocument/completion"
        const val hover = "textDocument/hover"
        const val signatureHelp = "textDocument/signatureHelp"
        const val declaration = "textDocument/declaration"
        const val definition = "textDocument/definition"
        const val typeDefinition = "textDocument/typeDefinition"
        const val implementation = "textDocument/implementation"
        const val references = "textDocument/references"
        const val documentHighlight = "textDocument/documentHighlight"
        const val documentSymbol = "textDocument/documentSymbol"
        const val codeAction = "textDocument/codeAction"
        const val codeLens = "textDocument/codeLens"
        const val documentLink = "textDocument/documentLink"
        const val documentColor = "textDocument/documentColor"
        const val colorPresentation = "textDocument/colorPresentation"
        const val formatting = "textDocument/formatting"
        const val rangeFormatting = "textDocument/rangeFormatting"
        const val onTypeFormatting = "textDocument/onTypeFormatting"
        const val rename = "textDocument/rename"
        const val prepareRename = "textDocument/prepareRename"
        const val foldingRange = "textDocument/foldingRange"
    }

    internal object CompletionItem {
        const val event = "completionItem/resolve"
    }

    internal object CodeLens {
        const val resolve = "codeLens/resolve"
    }

    internal object DocumentLink {
        const val resolve = "documentLink/resolve"
    }

}
