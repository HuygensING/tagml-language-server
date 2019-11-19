package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.Collections.synchronizedMap
import java.util.concurrent.CompletableFuture


class TAGMLTextDocumentService(val tagmlLanguageServer: TAGMLLanguageServer) : TextDocumentService {
    private val docs: MutableMap<String, TAGMLDocumentModel> = synchronizedMap(hashMapOf())

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        val model = TAGMLDocumentModel(params?.textDocument?.text)
        this.docs[params?.textDocument?.uri!!] = model;
        CompletableFuture.runAsync {
            tagmlLanguageServer.client?.publishDiagnostics(
                    PublishDiagnosticsParams(params.textDocument.uri, validate(model))
            )
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        val model = TAGMLDocumentModel(params?.contentChanges?.get(0)?.text)
        this.docs[params?.textDocument?.uri!!] = model
        CompletableFuture.runAsync {
            tagmlLanguageServer.client?.publishDiagnostics(
                    PublishDiagnosticsParams(params.textDocument.uri, validate(model))
            )
        }

    }

    private fun validate(model: TAGMLDocumentModel): List<Diagnostic> {
        val res = listOf<Diagnostic>()
        return res
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        this.docs.remove(params?.textDocument?.uri);
    }

}
