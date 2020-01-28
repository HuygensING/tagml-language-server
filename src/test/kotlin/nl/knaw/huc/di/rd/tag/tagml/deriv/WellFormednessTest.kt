package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.TEXT
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import nl.knaw.huc.di.rd.tag.tagml.derivation.WellFormedness.checkWellFormedness
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WellFormednessTest {

    @Test
    fun testWellFormedTAGML1() {
        // TagML: Expectation = After(Range("tag", Text("text)), EOF)
        val tagml = "[tag>text<tag]"
        assertTAGMLisWellFormed(tagml)
    }

    @Test
    fun testWellFormedTAGML_nested() {
        // TagML: Expectation = Range("*", ZeroOrMore(Range("*", Text))
        val tagml = "[tag>[color>Green<color][food>Eggs<food][food>Ham<food]<tag]"
        assertTAGMLisWellFormed(tagml)
    }

    @Test
    fun testWellFormedTAGML_mixed() {
        // TagML: Expectation = Range("*", Mixed((Range("*", Text())
        val tagml = "[tag>[color>Green<color] [food>Eggs<food] and [food>Ham<food]<tag]"
        assertTAGMLisWellFormed(tagml)
    }

    @Test
    fun testWellFormedTAGML_overlapping() {
        // TagML: Expectation = Range("*", concur(Range("a", Text)), Range("b", Text())
        //TagML: Expectation = Range("*", concur(Range("*", Layer A, Text)), Range("*", Layer B, Text())
        val tagml = "[tag>[a>Cookiemonster [b>likes<a] cookies<b]<tag]" // TODO: layer info
        assertTAGMLisWellFormed(tagml)
    }

    @Test
    fun testNotWellFormedTAGML1() {
        val tagml = "[tag>text"
        assertTAGMLisNotWellFormed(tagml, "Out of tokens, but expected any of [[*>, <tag]]")
    }

    @Test
    fun testNotWellFormedTAGML2() {
        val tagml = "[tag>text<gat]"
        assertTAGMLisNotWellFormed(tagml, "Unexpected token: found <gat], but expected any of [[*>, <tag]]")
    }

    @Test
    fun testPatternRepresentationAggregation() {
        val p1 = choice(Range(FixedIdentifier("a"), TEXT), TEXT)
        val p2 = choice(Range(FixedIdentifier("b"), TEXT), TEXT)
        val p3 = choice(p1, p2)
        val p4 = choice(p3, EMPTY)
        assertThat(p4.toString()).isEqualTo("""<choice><range id="a"><text/></range><text/><range id="b"><text/></range><empty/></choice>""")
    }

    private fun assertTAGMLisWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { assertThat(checkWellFormedness(it).isWellFormed).isTrue() }
    }

    private fun assertTAGMLisNotWellFormed(tagml: String, error: String) {
        mapTokenizedTAGML(tagml) {
            val checkWellFormedness = checkWellFormedness(it)
            assertThat(checkWellFormedness.isWellFormed).isFalse()
            assertThat(checkWellFormedness.errors).contains(error)
        }
    }

    private fun mapTokenizedTAGML(tagml: String, funk: (tokens: List<TAGMLToken>) -> Unit) {
        when (val result = tokenize(tagml)
                .also { println(it) }) {
            is Either.Left -> {
                showErrorLocation(tagml, result)
                Assertions.fail("Parsing failed: ${result.a}")
            }
            is Either.Right -> funk(result.b)
        }
    }

}
