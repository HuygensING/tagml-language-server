package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.TextDocumentService
import org.slf4j.LoggerFactory
import java.util.Collections.synchronizedMap
import java.util.concurrent.CompletableFuture


class TAGMLTextDocumentService(val tagmlLanguageServer: TAGMLLanguageServer) : TextDocumentService {
    private val logger = LoggerFactory.getLogger(this.javaClass)!!
    private val docs: MutableMap<String, TAGMLDocumentModel> = synchronizedMap(hashMapOf())

    override fun didOpen(params: DidOpenTextDocumentParams?) {
        logger.info("TAGMLTextDocumentService.didOpen($params)")
        val model = TAGMLDocumentModel(params?.textDocument?.text)
        this.docs[params?.textDocument?.uri!!] = model;
        CompletableFuture.runAsync {
            tagmlLanguageServer.client?.publishDiagnostics(
                    PublishDiagnosticsParams(params.textDocument.uri, validate(model))
            )
        }
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        logger.info("TAGMLTextDocumentService.didChange($params)")
        val model = TAGMLDocumentModel(params?.contentChanges?.get(0)?.text)
        this.docs[params?.textDocument?.uri!!] = model
        CompletableFuture.runAsync {
            tagmlLanguageServer.client?.publishDiagnostics(
                    PublishDiagnosticsParams(params.textDocument.uri, validate(model))
            )
        }
    }

    override fun hover(position: TextDocumentPositionParams?): CompletableFuture<Hover> {
        val contents = MarkupContent("kind", "value")
        return CompletableFuture.supplyAsync { Hover(contents) }
    }

    private fun validate(model: TAGMLDocumentModel): List<Diagnostic> {
        val res = mutableListOf<Diagnostic>()
        res.add(Diagnostic(Range(), "this is a test"))
        return res
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        this.docs.remove(params?.textDocument?.uri);
    }

}
