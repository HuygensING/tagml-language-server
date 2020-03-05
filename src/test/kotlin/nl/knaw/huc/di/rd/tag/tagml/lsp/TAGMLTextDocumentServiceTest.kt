package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TAGMLTextDocumentServiceTest {
    @Nested
    inner class TestDidOpen {
        @Test
        fun with_correct_TAGML() {
            val client = TestClient
            doDidOpen(client, "[tag>text<tag]")

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSize(2)
            val firstDiagnostic = diagnostics[0]
            assertThat(firstDiagnostic.severity).isEqualTo(DiagnosticSeverity.Information)
            println(firstDiagnostic)
        }

        @Test
        fun with_unparsable_TAGML() {
            val client = TestClient
            doDidOpen(client, "[[does not parse!]]]")

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSize(1)
            val firstDiagnostic = diagnostics[0]
            assertThat(firstDiagnostic.severity).isEqualTo(DiagnosticSeverity.Error)
            println(firstDiagnostic)
        }

        private fun doDidOpen(client: TestClient, tagml: String) {
            val tds = startTDS(client)
            val params = DidOpenTextDocumentParams(TextDocumentItem("uri", "tagml", 1, tagml))
            tds.didOpen(params)
            waitForDiagnosticsToBePublished(client)
        }
    }

    @Nested
    inner class TestDidChange {
        @Test
        fun testDidChange() {
            val client = TestClient
            val tds = startTDS(client)
            val uri = "file:///tmp/test.tagml"
            val params = DidOpenTextDocumentParams(TextDocumentItem(uri, "tagml", 1, "[tag>text<tag]"))
            tds.didOpen(params)
            waitForDiagnosticsToBePublished(client)

            val openDiagnostics = client.readDiagnostics()
            assertThat(openDiagnostics).hasSize(2)

            val textDocument = VersionedTextDocumentIdentifier(uri, 2)
            val contentChanges = listOf(TextDocumentContentChangeEvent("[[[throw me an error!"))
            val changeParams = DidChangeTextDocumentParams(textDocument, contentChanges)
            tds.didChange(changeParams)
            waitForDiagnosticsToBePublished(client)

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSize(1)
            val firstDiagnostic = diagnostics[0]
            assertThat(firstDiagnostic.severity).isEqualTo(DiagnosticSeverity.Error)
            println(firstDiagnostic)
        }
    }

    private fun startTDS(client: TestClient): TAGMLTextDocumentService {
        val ls = TAGMLLanguageServer
        ls.client = client
        val ip = InitializeParams()
        ls.initialize(ip).join()
        return TAGMLTextDocumentService(ls)
    }

    private fun waitForDiagnosticsToBePublished(client: TestClient) {
        while (client.publishDiagnosticsParams == null) {
            Thread.sleep(10)
        }
    }
}