package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.services.LanguageClient
import java.util.concurrent.CompletableFuture

class TestClient : LanguageClient {

    private var logMessageParams: MessageParams? = null
    var publishDiagnosticsParams: PublishDiagnosticsParams? = null
    private var showMessageParams: MessageParams? = null
    private var showMessageRequestParams: ShowMessageRequestParams? = null
    private var telemetryEventObject: Any? = null

    override fun logMessage(message: MessageParams?) {
        this.logMessageParams = message
    }

    fun readLogMessage(): Pair<MessageType?, String?> {
        val logMessage = logMessageParams?.message
        val type = logMessageParams?.type
        logMessageParams = null
        return type to logMessage
    }

    override fun publishDiagnostics(diagnostics: PublishDiagnosticsParams?) {
        this.publishDiagnosticsParams = diagnostics
    }

    fun readDiagnostics(): List<Diagnostic> {
        val list = if (publishDiagnosticsParams == null)
            listOf()
        else
            publishDiagnosticsParams!!.diagnostics
        publishDiagnosticsParams = null
        return list
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

}
