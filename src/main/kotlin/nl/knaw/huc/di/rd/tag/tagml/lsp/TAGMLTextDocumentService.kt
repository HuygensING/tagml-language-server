package nl.knaw.huc.di.rd.tag.tagml.lsp

import nl.knaw.huc.di.rd.tag.tagml.lsp.AlexandriaUtil.toLSPRange
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.ResponseErrorException
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.messages.Either.forLeft
import org.eclipse.lsp4j.jsonrpc.messages.ResponseError
import org.eclipse.lsp4j.jsonrpc.messages.ResponseErrorCode
import org.eclipse.lsp4j.services.TextDocumentService
import java.util.Collections.synchronizedMap
import java.util.concurrent.CompletableFuture

typealias CodeActionList = MutableList<Either<Command, CodeAction>>

class TAGMLTextDocumentService(private val tagmlLanguageServer: TAGMLLanguageServer) : TextDocumentService {
    //    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val docs: MutableMap<String, TAGMLDocumentModel> = synchronizedMap(hashMapOf())
    private val alexandria = Alexandria()

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

        val docId = params.textDocument.uri
        val model = tagmlModelOf(BaseTAGMLDocumentModel(docId, params.textDocument.text, params.textDocument.version))
        this.docs[docId] = model
        publishDiagnostics(docId, model)
    }

    private fun tagmlModelOf(base: BaseTAGMLDocumentModel): TAGMLDocumentModel = alexandria.validate(base)

    override fun didChange(params: DidChangeTextDocumentParams) {
//        logger.info("TAGMLTextDocumentService.didChange($params)")
        val docId = params.textDocument.uri
        val originalText = docs[docId]?.base?.text
        val changedText = updateText(originalText, params.contentChanges)

        val model = tagmlModelOf(BaseTAGMLDocumentModel(docId, changedText, params.textDocument.version))
        this.docs[docId] = model
        publishDiagnostics(docId, model)
    }

    private fun updateText(originalText: String?, contentChanges: List<TextDocumentContentChangeEvent>): String {
        var orig = originalText ?: ""
        for (changeEvent in contentChanges) {
            val range = changeEvent.range
            val text = changeEvent.text
            if (range == null) {
                orig = text
            } else {
                TODO()
            }
        }
        return orig
    }

    private fun publishDiagnostics(uri: String, model: TAGMLDocumentModel) {
        CompletableFuture.runAsync {
            tagmlLanguageServer.client.publishDiagnostics(
                    PublishDiagnosticsParams(
                            uri,
                            diagnostics(model)
                    )
            )
        }
    }

    override fun hover(position: TextDocumentPositionParams): CompletableFuture<Hover> {
//        val docId = position.textDocument.uri
//        val model = docs[docId]
//        val token = model?.tokenIndex?.tokenAt(position.position)
        val contents = MarkupContent().apply {
            value = "TODO"
            kind = "markdown" // alternatively:, "plaintext"
        }
//        contents.value = "**${token}**"
        return CompletableFuture.supplyAsync { Hover(contents) }
    }

    override fun documentHighlight(position: TextDocumentPositionParams?): CompletableFuture<MutableList<out DocumentHighlight>> {
        TODO()
    }

    override fun documentSymbol(params: DocumentSymbolParams?): CompletableFuture<MutableList<Either<SymbolInformation, DocumentSymbol>>> {
        TODO()
    }

    override fun codeAction(params: CodeActionParams?): CompletableFuture<CodeActionList> {
        val docId = params?.textDocument?.uri
        val model = docs[docId]
        val range = params?.range
        return CompletableFuture.supplyAsync {
            val actionList = mutableListOf<Either<Command, CodeAction>>()
            actionList
        }
    }

    private fun diagnostics(model: TAGMLDocumentModel): List<Diagnostic> {
        return when (model) {
            is CorrectTAGMLDocumentModel -> listOf()
            is IncorrectTAGMLDocumentModel -> model.diagnostics
            else -> TODO("Unhandled type ${model.javaClass}")
        }
    }

    override fun definition(params: TextDocumentPositionParams?): CompletableFuture<MutableList<out Location>> {
        // TODO: implement: if the position points to a tag (open/close), refer to the complementing tag (close/open)
        val locationList = mutableListOf<Location>()
        if (params != null) {
            val uri = params.textDocument?.uri
//            val textDocument = params.textDocument
            val tagmlDocumentModel = docs[uri]!!
            if (tagmlDocumentModel is CorrectTAGMLDocumentModel) {
                tagmlDocumentModel.rangePairAt(params.position)
                        ?.map { it.toLSPRange() }
                        ?.map { Location(uri, it) }
                        ?.forEach { locationList.add(it) }
            }
        }
        return CompletableFuture.supplyAsync { locationList }
    }

    private fun addTestDiagnostic(res: MutableList<Diagnostic>) {
        val start: Position = Position(1, 1)
        val end: Position = Position(1, 5)
        val diagnostic = Diagnostic(Range(start, end), "this is a test").apply {
            severity = DiagnosticSeverity.Information
        }
        res.add(diagnostic)
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
//        TODO("not implemented")
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
