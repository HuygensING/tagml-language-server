package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.Test

class LSPTokenTest {
    @Test
    fun test() {
        val token1 = TextToken("text")
        val range1 = Range(Position(0, 0), Position(0, 3))
        val token2 = TextToken("text")
        assertThat(token1).isEqualTo(token2)

        val lspt1 = LSPToken(token1, range1)
        val lspt2 = LSPToken(token2, range1)
        assertThat(lspt1).isEqualTo(lspt2)

        val range2 = Range(Position(1, 0), Position(1, 3))
        val lspt3 = LSPToken(token2, range2)
        assertThat(lspt1).isNotEqualTo(lspt3)
    }
}