package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.slf4j.LoggerFactory
import kotlin.test.assertNotNull

class CompletionTest {
    private val _log = LoggerFactory.getLogger(this::class.java)

//    @Test
    fun testCompletion() {
        val server = TAGMLLanguageServer()
        val textDocument = TextDocumentIdentifier("uri")
        val position = Position(0, 0)
        val completionParams = CompletionParams(textDocument, position)
        val completion = server.textDocumentService.completion(completionParams)
        val join = completion.join()
        val left = join.left
    _log.info("left={}", left)
        val right = join.right
    _log.info("right={}", right)
        assertNotNull(right)
        assertNotNull(left)
    }

}