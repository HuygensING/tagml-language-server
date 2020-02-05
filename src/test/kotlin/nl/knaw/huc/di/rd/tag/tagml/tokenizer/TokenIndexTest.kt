package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.Test

class TokenIndexTest {

    @Test
    fun test() {
        val openRoot = StartTagToken("root")
        val lt1 = Pair(
                openRoot,
                r(0, 0, 0, 6)
        )

        val text = TextToken("Roses are red\nViolets are blue")
        val lt2 = Pair(
                text,
                r(1, 0, 2, 16)
        )

        val closeRoot = EndTagToken("root")
        val lt3 = Pair(
                closeRoot,
                r(3, 0, 3, 6)
        )

        val index = TokenIndex("test")
        index.locatedTokens = listOf(lt2, lt3, lt1).shuffled()

        // check sorting
        assertThat(index.locatedTokens).containsExactly(lt1, lt2, lt3)

        assertThat(index.tokenAt(p(0, 0))).isEqualTo(openRoot)
        assertThat(index.tokenAt(p(0, 3))).isEqualTo(openRoot)
        assertThat(index.tokenAt(p(0, 6))).isEqualTo(openRoot)
        assertThat(index.tokenAt(p(0, 10))).isNull()

        assertThat(index.tokenAt(p(1, 1))).isEqualTo(text)
        assertThat(index.tokenAt(p(1, 100))).isEqualTo(text)
        assertThat(index.tokenAt(p(2, 2))).isEqualTo(text)
        assertThat(index.tokenAt(p(2, 200))).isNull()

        assertThat(index.tokenAt(p(3, 0))).isEqualTo(closeRoot)
        assertThat(index.tokenAt(p(3, 3))).isEqualTo(closeRoot)
        assertThat(index.tokenAt(p(3, 6))).isEqualTo(closeRoot)

        assertThat(index.tokenAt(p(10, 10))).isNull()
    }

    private fun p(l: Int, c: Int): Position = Position(l, c)

    private fun r(l1: Int, c1: Int, l2: Int, c2: Int): Range = Range(p(l1, c1), p(l2, c2))
}