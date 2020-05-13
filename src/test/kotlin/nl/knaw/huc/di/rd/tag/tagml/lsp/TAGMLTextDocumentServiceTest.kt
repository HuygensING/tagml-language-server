package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.apache.commons.lang3.time.StopWatch
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

class TAGMLTextDocumentServiceTest {

//    @Nested
//    inner class TestSymbolReference {
//        @Test
//        fun with_correct_tagml() {
//            val client = TestClient
//            val (tds, uri) = openTAGML(client, "[tag>text<tag]")
//            val textDocument = VersionedTextDocumentIdentifier(uri, 1)
//
//            val documentSymbolParams = DocumentSymbolParams(textDocument)
//            val locations = tds.documentSymbol(documentSymbolParams).join()
//        }
//    }

    @ExperimentalTime
    @Nested
    inner class TestDefinition {
        @Test
        fun with_correct_tagml() {
            val client = TestClient
            val (tds, uri) = openTAGML(client, "[tag>text<tag]")
            val textDocument = VersionedTextDocumentIdentifier(uri, 1)

            val positionInStartTag = Position(0, 1)
            val startPositionParams = TextDocumentPositionParams(textDocument, positionInStartTag)
            val locations = tds.definition(startPositionParams).join()

            val positionInEndTag = Position(0, 11)
            val endPositionParams = TextDocumentPositionParams(textDocument, positionInEndTag)
            val locations2 = tds.definition(endPositionParams).join()

            assertThat(locations).hasSize(2)
            assertThat(locations2).isEqualTo(locations)
            with(locations[0]) {
                assertThat(uri).isEqualTo(uri)
                assertThat(this.range).isEqualTo(range(0, 0, 0, 5))
            }
            with(locations[1]) {
                assertThat(uri).isEqualTo(uri)
                assertThat(this.range).isEqualTo(range(0, 9, 0, 14))
            }
        }
    }

    private fun range(startLine: Int, startChar: Int, endLine: Int, endChar: Int): Range =
            Range(Position(startLine, startChar), Position(endLine, endChar))

    @Nested
    inner class TestDidOpen {
        @Test
        fun with_correct_TAGML() {
            val client = TestClient
            openTAGML(client, "[tag>text<tag]")

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSize(0)
        }

        @Test
        fun with_unparsable_TAGML() {
            val client = TestClient
            doDidOpen(client, "[[does not parse!]]]")

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSizeGreaterThan(0)
            val firstDiagnostic = diagnostics[0]
            assertThat(firstDiagnostic.severity).isEqualTo(DiagnosticSeverity.Error)
            println(firstDiagnostic)
        }

        @Test
        fun with_incorrect_TAGML() {
            val client = TestClient
            doDidOpen(client, "[a>text<b]")

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSize(2)
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
            val (tds, uri) = openTAGML(client, "[tag>text<tag]")

            val textDocument = VersionedTextDocumentIdentifier(uri, 2)
            val contentChanges = listOf(TextDocumentContentChangeEvent("[[[throw me an error!"))
            val changeParams = DidChangeTextDocumentParams(textDocument, contentChanges)
            tds.didChange(changeParams)
            waitForDiagnosticsToBePublished(client)

            val diagnostics = client.readDiagnostics()
            assertThat(diagnostics).hasSizeGreaterThan(0)
            val firstDiagnostic = diagnostics[0]
            assertThat(firstDiagnostic.severity).isEqualTo(DiagnosticSeverity.Error)
            println(firstDiagnostic)
        }
    }

    private fun openTAGML(client: TestClient, tagml: String): Pair<TAGMLTextDocumentService, String> {
        val tds = startTDS(client)
        val uri = "file:///tmp/test.tagml"
        val params = DidOpenTextDocumentParams(TextDocumentItem(uri, "tagml", 1, tagml))
        tds.didOpen(params)
        waitForDiagnosticsToBePublished(client)

        val openDiagnostics = client.readDiagnostics()
        assertThat(openDiagnostics).hasSize(0)
        return Pair(tds, uri)
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

    private fun logTimeSpent(sw: StopWatch, s: String) {
        println("$s took ${sw.getTime(TimeUnit.MILLISECONDS)} ms")
        sw.reset()
        sw.start()
    }

}