package nl.knaw.huc.di.rd.tag.tagml.lsp


import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentPositionParams
import org.junit.Test
import org.slf4j.LoggerFactory

class HoverTest {
    private val _log = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testHover() {
        val server = TAGMLLanguageServer()
        val textDocumentIdentifier = TextDocumentIdentifier("uri")
        val position = Position(0, 0)
        val positionParams = TextDocumentPositionParams(textDocumentIdentifier, position)
        val hover = server.textDocumentService.hover(positionParams).join()
        _log.info("hover={}", hover)
    }
}