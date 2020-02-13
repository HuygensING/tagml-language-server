package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem

open class RequestTest {
    var server = TAGMLLanguageServer

    internal fun openDocument(tagml: String, uri: String = "uri://some-uri"): TextDocumentIdentifier {
        val textDocumentItem = TextDocumentItem(uri, "tagml", 1, tagml)
        val didOpenTextDocumentParams = DidOpenTextDocumentParams(textDocumentItem)
        server.textDocumentService.didOpen(didOpenTextDocumentParams)
        return TextDocumentIdentifier(uri)
    }

    internal fun closeDocument(textDocumentIdentifier: TextDocumentIdentifier) {
        val didCloseTextDocumentParams = DidCloseTextDocumentParams(textDocumentIdentifier)
        server.textDocumentService.didClose(didCloseTextDocumentParams)
    }

}