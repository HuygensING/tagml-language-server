package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class TAGMLLanguageServer : LanguageServer {
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        val result = InitializeResult()
        return CompletableFuture.completedFuture(result)
    }

    override fun getWorkspaceService(): WorkspaceService {
        return MyWorkspaceService()
    }

    override fun getTextDocumentService(): TextDocumentService {
        return MyTextDocumentService()
    }

    override fun shutdown(): CompletableFuture<Any> {
        TODO()
    }
//    override fun shutdown(): CompletableFuture<Any>? {}

    val exited = CompletableFuture<String>()
    override fun exit() {
        exited.complete(null)
    }
// https://github.com/eclipse/eclipse.jdt.ls/blob/bff2b303f3e6bae05c2ca9d0f592f38ccb9e6985/org.eclipse.jdt.ls.core/src/org/eclipse/jdt/ls/core/internal/handlers/JDTLanguageServer.java
    // https://langserver.org/
    // https://github.com/eclipse/lsp4j/tree/master/documentation
// https://github.com/LucasBullen/LSP4J_Tutorial
}