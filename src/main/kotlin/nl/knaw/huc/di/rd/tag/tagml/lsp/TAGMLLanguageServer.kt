package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess


class TAGMLLanguageServer : LanguageServer, LanguageClientAware {

//    private val logger = LoggerFactory.getLogger(this.javaClass)!!

    private val textDocumentService = TAGMLTextDocumentService(this)
    private val workspaceService = TAGMLWorkspaceService()
    var client: LanguageClient? = null
    private var shutdownRequested = false
    var isInitialized = false

    override fun initialize(params: InitializeParams): CompletableFuture<InitializeResult> {
//        logger.info("initialize($params)")
        val serverCapabilities = ServerCapabilities().apply {
            setCodeActionProvider(true)
            completionProvider = CompletionOptions()
            definitionProvider = true
            hoverProvider = true
            referencesProvider = true
            setTextDocumentSync(TextDocumentSyncKind.Full)
            documentSymbolProvider = true
        }
        isInitialized = true

        return CompletableFuture.supplyAsync { InitializeResult(serverCapabilities) }
    }

    override fun getWorkspaceService(): WorkspaceService = workspaceService

    override fun getTextDocumentService(): TextDocumentService = textDocumentService

    // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#shutdown
    override fun shutdown(): CompletableFuture<Any> {
        shutdownRequested = true
        return CompletableFuture.supplyAsync { null }
    }

    // https://microsoft.github.io/language-server-protocol/specifications/specification-current/#exit
    override fun exit() {
        if (shutdownRequested)
            exitProcess(0)
        else
            exitProcess(1)
    }

    override fun connect(client: LanguageClient?) {
        this.client = client
    }

// https://github.com/eclipse/eclipse.jdt.ls/blob/bff2b303f3e6bae05c2ca9d0f592f38ccb9e6985/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/handlers/JDTLanguageServer.java
// https://langserver.org/
// https://github.com/eclipse/lsp4j/tree/master/documentation
// https://github.com/LucasBullen/LSP4J_Tutorial

}
