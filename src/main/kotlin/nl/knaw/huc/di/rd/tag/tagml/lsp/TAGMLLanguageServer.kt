package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.*
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture


class TAGMLLanguageServer : LanguageServer, LanguageClientAware {

    private val logger = LoggerFactory.getLogger(this.javaClass)!!

    private val textDocumentService = TAGMLTextDocumentService(this)
    private val workspaceService = TAGMLWorkspaceService()
    var client: LanguageClient? = null

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

        return CompletableFuture.supplyAsync { InitializeResult(serverCapabilities) }
    }

    override fun initialized(params: InitializedParams?) {
        super.initialized(params)
    }

    override fun getWorkspaceService(): WorkspaceService = workspaceService

    override fun getTextDocumentService(): TextDocumentService = textDocumentService

    override fun shutdown(): CompletableFuture<Any> = CompletableFuture.supplyAsync { true }

    //    val exited = CompletableFuture<String>()
    override fun exit() {
//        exited.complete(null)
    }

    override fun connect(client: LanguageClient?) {
        this.client = client
    }

// https://github.com/eclipse/eclipse.jdt.ls/blob/bff2b303f3e6bae05c2ca9d0f592f38ccb9e6985/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/handlers/JDTLanguageServer.java
// https://langserver.org/
// https://github.com/eclipse/lsp4j/tree/master/documentation
// https://github.com/LucasBullen/LSP4J_Tutorial

}
