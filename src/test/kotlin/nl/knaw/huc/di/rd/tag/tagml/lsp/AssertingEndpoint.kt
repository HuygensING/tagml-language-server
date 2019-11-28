package nl.knaw.huc.di.rd.tag.tagml.lsp

import arrow.core.left
import arrow.core.right
import org.eclipse.lsp4j.jsonrpc.Endpoint
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class AssertingEndpoint : Endpoint {
    private val expectedRequests = mutableMapOf<String, Pair<Any, Any>>()

    override fun request(method: String, parameter: Any): CompletableFuture<*> {
        assertTrue(expectedRequests.containsKey(method))
        val result = expectedRequests.remove(method)!!
        assertEquals(result.left().toString(), parameter.toString())
        return CompletableFuture.completedFuture(result.right())
    }

    val expectedNotifications = LinkedHashMap<String, Any>()

    override fun notify(method: String, parameter: Any) {
        assertTrue(expectedNotifications.containsKey(method))
        val obj = expectedNotifications.remove(method)!!
        assertEquals(obj.toString(), parameter.toString())
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
        fail("expectations weren't empty " + toString())
    }

    override fun toString(): String {
        return ToStringBuilder(this).addAllFields().toString()
    }
}