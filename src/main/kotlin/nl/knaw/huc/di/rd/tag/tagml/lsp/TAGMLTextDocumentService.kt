package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Either.forLeft
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.Collections.synchronizedMap
import java.util.concurrent.CompletableFuture


class TAGMLTextDocumentService(private val tagmlLanguageServer: TAGMLLanguageServer) : TextDocumentService {
    //    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val docs: MutableMap<String, TAGMLDocumentModel> = synchronizedMap(hashMapOf())

    override fun didOpen(params: DidOpenTextDocumentParams) {
//        logger.info("TAGMLTextDocumentService.didOpen($params)")
//        checkServerIsInitialized()
        if (!tagmlLanguageServer.isInitialized) {
//            val exceptionalResult: CompletableFuture<*> = CompletableFuture<Any>()
            val error = ResponseError(
                    ResponseErrorCode.serverNotInitialized,
                    "server not initialized, expected 'initialize' first",
                    null
            )
            throw(ResponseErrorException(error))
        }

        val model = TAGMLDocumentModel(params.textDocument.uri, params.textDocument.text, params.textDocument.version)
        this.docs[params.textDocument.uri] = model
        CompletableFuture.runAsync {
            tagmlLanguageServer.client?.publishDiagnostics(
                    PublishDiagnosticsParams(params.textDocument.uri, validate(model))
            )
        }
    }


    override fun didChange(params: DidChangeTextDocumentParams) {
//        logger.info("TAGMLTextDocumentService.didChange($params)")
        val model = TAGMLDocumentModel(params.textDocument.uri, "text", params.textDocument.version)
        this.docs[params.textDocument.uri] = model
        CompletableFuture.runAsync {
            tagmlLanguageServer.client?.publishDiagnostics(
                    PublishDiagnosticsParams(params.textDocument.uri, validate(model))
            )
        }
    }

    override fun hover(position: TextDocumentPositionParams): CompletableFuture<Hover> {
        val contents = MarkupContent()
        contents.kind = "KIND"
        contents.value = "VALUE"
        return CompletableFuture.supplyAsync { Hover(contents) }
    }

    override fun documentHighlight(position: TextDocumentPositionParams?): CompletableFuture<MutableList<out DocumentHighlight>> {
        TODO()
    }

    override fun documentSymbol(params: DocumentSymbolParams?): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
        TODO()
    }

    private fun validate(model: TAGMLDocumentModel): List<Diagnostic> {
        // validate the tagml and on errors, return a list of diagnostics
        val text = model.text
        // parsing the tagml should also return a list of tokens (opentag, closetag, text) with their positions in the text
        // also: which opentag and closetag belong together
        // this goes into the TAGMLDocumentModel (?) -> so this contains the parsed tagml?
        // we need that for the autocompleter
        val res = mutableListOf<Diagnostic>()
        val start: Position = Position(1, 1)
        val end: Position = Position(1, 5)
        val diagnostic = Diagnostic(Range(start, end), "this is a test").apply {
            severity = DiagnosticSeverity.Error
        }
        res.add(diagnostic)
        return res
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didClose(params: DidCloseTextDocumentParams) {
        this.docs.remove(params.textDocument.uri)
    }

    override fun completion(position: CompletionParams?): CompletableFuture<Either<MutableList<CompletionItem>, CompletionList>> {
//        logger.info("completion($position)")
        val completionItem1 = CompletionItem().apply {
            label = "Hello World"
            kind = CompletionItemKind.Text
            preselect = true
        }
        val completionItem2 = CompletionItem().apply {
            label = "Goodbye"
            kind = CompletionItemKind.Text
        }
        val completionItemList = mutableListOf(completionItem1, completionItem2)
        return CompletableFuture.completedFuture(forLeft(completionItemList))
    }

    private fun checkServerIsInitialized() {
        if (!tagmlLanguageServer.isInitialized) {
            throw(ResponseErrorException(
                    ResponseError(
                            ResponseErrorCode.serverNotInitialized,
                            "server not initialized, expected 'initialize' first",
                            null
                    )
            ))
        }
    }
}
