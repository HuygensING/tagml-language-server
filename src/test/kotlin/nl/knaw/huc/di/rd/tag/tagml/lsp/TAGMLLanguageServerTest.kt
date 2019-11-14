package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.launch.LSPLauncher
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.CompletionException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

class TAGMLLanguageServerTest {
    val TIMEOUT = 20000

    @Test
    fun test() {
        val client = DummyClient()
        val inStream = PipedInputStream()
        val responseStream = PipedOutputStream()
        inStream.connect(responseStream)
        val responseWriter = OutputStreamWriter(responseStream)
        val out = ByteArrayOutputStream()
        val launcher = LSPLauncher.createClientLauncher(client, inStream, out, true, null)
        val future = launcher.startListening()
        val tdpp = TextDocumentPositionParams(TextDocumentIdentifier("foo"), Position(0, 0))
        val hoverResult = launcher.remoteProxy.textDocumentService.hover(tdpp)
        responseWriter.writeResponse("""{"jsonrpc":"2.0","id":"1","result":{"contents":[null,null]}}""")
        try {
            hoverResult.join()
            fail("Expected a CompletionException to be thrown.")
        } catch (exception: CompletionException) {
            assertEquals("""
                |Lists must not contain null references. Path: ${'$'}.result.contents[0]
                |Lists must not contain null references. Path: ${'$'}.result.contents[1]
                """.trimMargin(), exception.cause?.message)
            assertFalse(future.isDone)
        } finally {
            inStream.close()
        }
    }

    @Test
    fun test2() {
        val p = MessageParams(MessageType.Info, "Hello World")
    }


    private fun OutputStreamWriter.writeResponse(response: String) {
        val responseLength = response.length
        this.write("Content-Length: $responseLength\r\n\r\n")
        this.write(response)
        this.flush()
    }

}