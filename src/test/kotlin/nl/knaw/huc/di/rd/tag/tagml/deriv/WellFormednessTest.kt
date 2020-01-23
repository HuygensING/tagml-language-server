package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectation
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.EOF
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.slf4j.LoggerFactory

class WellFormednessTest {
    private val _log = LoggerFactory.getLogger(this::class.java)

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
    fun testWellFormedTAGML_Mixed() {
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
        assertTAGMLisNotWellFormed(tagml)
    }

    @Test
    fun testNotWellFormedTAGML2() {
        val tagml = "[tag>text<gat]"
        assertTAGMLisNotWellFormed(tagml)
    }

    private fun assertTAGMLisWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { assertThat(isWellFormed(it)).isTrue() }
    }

    private fun assertTAGMLisNotWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { assertThat(isWellFormed(it)).isFalse() }
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

    private fun isWellFormed(tokens: List<TAGMLToken>): Boolean {
        val iterator = tokens.iterator()
        var expectation: Expectation =  after(Range(TagIdentifiers.AnyTagIdentifier(), Expectations.Text()), EOF())

        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            _log.info("expectation=${expectation.javaClass.simpleName}, token=$token")
            if (expectation.matches(token)) {
                expectation = expectation.deriv(token)
                goOn = iterator.hasNext()
            } else {
                _log.error("Unexpected token: found $token, but expected ${expectation.expectedTokens()}")
                goOn = false
            }
        }
        _log.info("expectation=${expectation.javaClass.simpleName}")
        return !iterator.hasNext() && (expectation is EOF)
    }


}
