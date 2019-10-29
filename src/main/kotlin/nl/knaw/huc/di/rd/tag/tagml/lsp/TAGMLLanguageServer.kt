package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.InitializeResult
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import org.eclipse.lsp4j.services.WorkspaceService
import java.util.concurrent.CompletableFuture

class TAGMLLanguageServer : LanguageServer {
    override fun initialize(params: InitializeParams?): CompletableFuture<InitializeResult> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWorkspaceService(): WorkspaceService {
        return MyWorkspaceService()
    }

    override fun getTextDocumentService(): TextDocumentService {
        return MyTextDocumentService()
    }

    override fun shutdown(): CompletableFuture<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exit() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}