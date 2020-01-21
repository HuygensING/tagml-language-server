package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.DocumentSymbolParams
import org.slf4j.LoggerFactory

class DocumentSymbolTest : RequestTest() {
    //    https://microsoft.github.io/language-server-protocol/specifications/specification-3-14/#textDocument_documentSymbol
    private val _log = LoggerFactory.getLogger(this::class.java)

//    @Test
    fun testDocumentSymbol() {
        val tagml = "[tagml>Hello World<tagml]"
        val textDocumentIdentifier = openDocument(tagml)

        try {
            val documentSymbolParams = DocumentSymbolParams(textDocumentIdentifier)
            val result = server.textDocumentService.documentSymbol(documentSymbolParams).join()
            _log.info("result={}", result)

        } finally {
            closeDocument(textDocumentIdentifier)
        }
    }

}