package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.junit.Test
import org.slf4j.LoggerFactory

class DocumentLinkTest : RequestTest() {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testDocumentLinkTest() {
        val tagml = "[tagml>Hello World<tagml]"
        val textDocumentIdentifier = openDocument(tagml)

        try {
//            val documentSymbolParams = DocumentSymbolParams(textDocumentIdentifier)
//            val result = server.textDocumentService.documentLink()Symbol(documentSymbolParams).join()
//            _log.info("result={}", result)

        } finally {
            closeDocument(textDocumentIdentifier)
        }
    }

}
