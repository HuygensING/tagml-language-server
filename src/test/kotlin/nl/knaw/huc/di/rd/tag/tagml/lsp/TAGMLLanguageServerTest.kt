package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.MessageType
import org.junit.Ignore
import org.junit.Test
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.OutputStreamWriter

class TAGMLLanguageServerTest {
    private val LOG = LoggerFactory.getLogger(TAGMLLanguageServerTest::class.java)

//    val TIMEOUT = 20000

    @Test
    @Ignore
    @Throws(IOException::class)
    fun testNotification() {
        val client = AssertingEndpoint()
        val p = MessageParams()
        p.message = "Hello World"
        p.type = MessageType.Info
        client.expectedNotifications["window/logMessage"] = p
//        serverLauncher.getRemoteProxy().logMessage(p)
        client.joinOnEmpty()
    }

    @Test
    fun test2() {
//        val p = MessageParams(MessageType.Info, "Hello World")
    }

    private fun OutputStreamWriter.writeResponse(response: String) {
        val responseLength = response.length
        this.write("Content-Length: $responseLength\r\n\r\n")
        this.write(response)
        this.flush()
    }

}