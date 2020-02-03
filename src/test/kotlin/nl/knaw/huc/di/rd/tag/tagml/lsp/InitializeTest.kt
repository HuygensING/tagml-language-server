package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.InitializeParams
import org.junit.Test

class InitializeTest {

    @Test
    fun testInitialize() {
        val server = TAGMLLanguageServer()
        val params = InitializeParams().also {
            it.processId = 42
        }
        val result = server.initialize(params).join()
        val triggerCharacters = result.capabilities.completionProvider.triggerCharacters
        assertThat(triggerCharacters).contains("<", "[", "|", " ")
    }
}