package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
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


}
