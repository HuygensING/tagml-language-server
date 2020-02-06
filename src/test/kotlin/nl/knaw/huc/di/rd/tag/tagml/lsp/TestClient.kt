package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture

class TestClient : LanguageClient {

    var logMessageParams: MessageParams? = null
    var publishDiagnosticsParams: PublishDiagnosticsParams? = null
    var showMessageParams: MessageParams? = null
    var showMessageRequestParams: ShowMessageRequestParams? = null
    var telemetryEventObject: Any? = null

    override fun logMessage(message: MessageParams?) {
        this.logMessageParams = message
    }

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams?) {
        this.publishDiagnosticsParams = diagnostics
    }

    override fun showMessage(messageParams: MessageParams?) {
        this.showMessageParams = messageParams
    }

    override fun showMessageRequest(requestParams: ShowMessageRequestParams?): CompletableFuture<MessageActionItem> {
        this.showMessageRequestParams = requestParams
        return CompletableFuture.supplyAsync { MessageActionItem("title") }
    }

    override fun telemetryEvent(`object`: Any?) {
        this.telemetryEventObject = `object`
    }

    fun readDiagnostics(): List<Diagnostic> {
        val list = if (publishDiagnosticsParams == null)
            listOf()
        else
            publishDiagnosticsParams!!.diagnostics
        publishDiagnosticsParams = null
        return list
    }


}
