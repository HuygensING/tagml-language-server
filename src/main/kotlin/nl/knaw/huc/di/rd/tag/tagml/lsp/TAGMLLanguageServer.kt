package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.lang.Boolean.TRUE
import java.util.concurrent.CompletableFuture


class TAGMLLanguageServer : LanguageServer {

    private val textDocumentService = TAGMLTextDocumentService(this)
    private val workspaceService = TAGMLWorkspaceService()
    var client: LanguageClient? = null

    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        val res = InitializeResult(ServerCapabilities())
        res.capabilities.setCodeActionProvider(TRUE)
        res.capabilities.completionProvider = CompletionOptions()
        res.capabilities.definitionProvider = TRUE
        res.capabilities.hoverProvider = TRUE
        res.capabilities.referencesProvider = TRUE
        res.capabilities.setTextDocumentSync(TextDocumentSyncKind.Full)
        res.capabilities.documentSymbolProvider = TRUE

        return CompletableFuture.supplyAsync { res }
    }

    override fun getWorkspaceService(): WorkspaceService {
        return workspaceService
    }

    override fun getTextDocumentService(): TextDocumentService {
        return textDocumentService
    }

    override fun shutdown(): CompletableFuture<Any> {
        return CompletableFuture.supplyAsync<Boolean> { TRUE } as CompletableFuture<Any>
    }

    //    val exited = CompletableFuture<String>()
    override fun exit() {
//        exited.complete(null)
    }

    fun setRemoteProxy(remoteProxy: LanguageClient?) {
        this.client = remoteProxy
    }
// https://github.com/eclipse/eclipse.jdt.ls/blob/bff2b303f3e6bae05c2ca9d0f592f38ccb9e6985/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/handlers/JDTLanguageServer.java
    // https://langserver.org/
    // https://github.com/eclipse/lsp4j/tree/master/documentation
// https://github.com/LucasBullen/LSP4J_Tutorial

}
