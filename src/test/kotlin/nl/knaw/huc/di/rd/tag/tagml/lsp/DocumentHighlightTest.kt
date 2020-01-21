package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentPositionParams
import org.slf4j.LoggerFactory

class DocumentHighlightTest : RequestTest() {
    // https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_documentHighlight
    private val _log = LoggerFactory.getLogger(this::class.java)
    // when on a tag, highlight the range

//    @Test
    fun testDocumentHighlight() {
        val tagml = "[tagml>Hello World<tagml]"
        val textDocumentIdentifier = openDocument(tagml)

        try {
            val position = Position(0, 0)
            val textDocumentPositionParams = TextDocumentPositionParams(textDocumentIdentifier, position)

            val result = server.textDocumentService.documentHighlight(textDocumentPositionParams).join()
            _log.info("result={}", result)

        } finally {
            closeDocument(textDocumentIdentifier)
        }
    }

}