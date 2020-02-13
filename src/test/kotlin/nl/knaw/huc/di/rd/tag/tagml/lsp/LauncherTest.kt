package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.Endpoint
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageProducer
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.xtext.xbase.lib.Pair
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.LinkedHashMap
import kotlin.collections.set

class LauncherTest {
    @Test
    @Throws(IOException::class)
    fun testNotification() {
        val p = MessageParams().apply {
            message = "Hello World"
            type = MessageType.Info
        }
        client!!.expectedNotifications["window/logMessage"] = p
        serverLauncher!!.remoteProxy.logMessage(p)
        client!!.joinOnEmpty()
    }

    @Test
    @Throws(Exception::class)
    fun testRequest() {
        val p = CompletionParams().apply {
            position = Position(1, 1)
            textDocument = TextDocumentIdentifier("test/foo.txt")
        }
        val result = CompletionList().apply {
            setIsIncomplete(true)
            items = ArrayList()
        }
        val item = CompletionItem().apply {
            detail = "test"
            setDocumentation("doc")
            filterText = "filter"
            insertText = "insert"
            kind = CompletionItemKind.Field
        }
        result.items.add(item)
        server!!.expectedRequests["textDocument/completion"] = Pair<Any, Any>(p, result)
        val future = clientLauncher!!.remoteProxy.textDocumentService.completion(p)
        Assert.assertEquals(Either.forRight<Any, CompletionList>(result).toString(), future[TIMEOUT, TimeUnit.MILLISECONDS].toString())
        client!!.joinOnEmpty()
    }

    internal class AssertingEndpoint : Endpoint {
        var expectedRequests: MutableMap<String, Pair<Any, Any>?> = LinkedHashMap()
        override fun request(method: String, parameter: Any): CompletableFuture<*> {
            Assert.assertTrue(expectedRequests.containsKey(method))
            val result = expectedRequests.remove(method)
            Assert.assertEquals(result!!.key.toString(), parameter.toString())
            return CompletableFuture.completedFuture(result.value)
        }

        var expectedNotifications: MutableMap<String, Any?> = LinkedHashMap()
        override fun notify(method: String, parameter: Any) {
            Assert.assertTrue(expectedNotifications.containsKey(method))
            val `object` = expectedNotifications.remove(method)
            Assert.assertEquals(`object`.toString(), parameter.toString())
        }

        /**
         * wait max 1 sec for all expectations to be removed
         */
        fun joinOnEmpty() {
            val before = System.currentTimeMillis()
            do {
                if (expectedNotifications.isEmpty() && expectedNotifications.isEmpty()) {
                    return
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) { // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            } while (System.currentTimeMillis() < before + 1000)
            Assert.fail("expectations weren't empty " + toString())
        }

        override fun toString(): String = ToStringBuilder(this).addAllFields().toString()
    }

    private var server: AssertingEndpoint? = null
    private var serverLauncher: Launcher<LanguageClient>? = null
    private var serverListening: Future<*>? = null
    private var client: AssertingEndpoint? = null
    private var clientLauncher: Launcher<LanguageServer>? = null
    private var clientListening: Future<*>? = null
    private var logLevel: Level? = null

    @Before
    @Throws(IOException::class)
    fun setup() {
        val inClient = PipedInputStream()
        val outClient = PipedOutputStream()
        val inServer = PipedInputStream()
        val outServer = PipedOutputStream()
        inClient.connect(outServer)
        outClient.connect(inServer)

        server = AssertingEndpoint()
        serverLauncher = LSPLauncher.createServerLauncher(ServiceEndpoints.toServiceObject(server, LanguageServer::class.java), inServer, outServer)
        serverListening = (serverLauncher as Launcher<LanguageClient>).startListening()

        client = AssertingEndpoint()
        clientLauncher = LSPLauncher.createClientLauncher(ServiceEndpoints.toServiceObject(client, LanguageClient::class.java), inClient, outClient)
        clientListening = (clientLauncher as Launcher<LanguageServer>).startListening()

        val logger = Logger.getLogger(StreamMessageProducer::class.java.name)
        logLevel = logger.level
        logger.level = Level.SEVERE
    }

    @After
    @Throws(InterruptedException::class, ExecutionException::class)
    fun teardown() {
        clientListening!!.cancel(true)
        serverListening!!.cancel(true)
        Thread.sleep(10)
        val logger = Logger.getLogger(StreamMessageProducer::class.java.name)
        logger.level = logLevel
    }

    companion object {
        private const val TIMEOUT: Long = 2000
    }
}