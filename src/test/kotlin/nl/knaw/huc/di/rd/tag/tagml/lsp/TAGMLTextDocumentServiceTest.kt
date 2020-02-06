package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.TextDocumentItem
import org.junit.Test

class TAGMLTextDocumentServiceTest {

    @Test
    fun testDidOpen() {
        val client = TestClient()
        val tds = startTDS(client)
        val params = DidOpenTextDocumentParams(TextDocumentItem("uri", "tagml", 1, "[[does not parse!]]]"))
        tds.didOpen(params)
        waitForDidOpenToFinish(client)

        val diagnostics = client.publishDiagnosticsParams?.diagnostics
        assertThat(diagnostics).hasSize(1)
        val firstDiagnostic = diagnostics!![0]
        assertThat(firstDiagnostic.severity).isEqualTo(DiagnosticSeverity.Error)
        println(firstDiagnostic)
    }

    private fun startTDS(client: TestClient): TAGMLTextDocumentService {
        val ls = TAGMLLanguageServer()
        ls.client = client
        val ip = InitializeParams()
        ls.initialize(ip).join()
        return TAGMLTextDocumentService(ls)
    }

    private fun waitForDidOpenToFinish(client: TestClient) {
        while (client.publishDiagnosticsParams == null) {
            Thread.sleep(10)
        }
    }
}