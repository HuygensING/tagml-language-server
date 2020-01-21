package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectation
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.EOF
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Not
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.junit.Test
import org.slf4j.LoggerFactory

class WellFormednessTest {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testWellFormedTAGML1() {
        val tagml = "[tag>text<tag]"
        assertTAGMLisWellFormed(tagml)
    }

    @Test
    fun testNotWellFormedTAGML1() {
        val tagml = "[tag>text"
        assertTAGMLisNotWellFormed(tagml)
    }

    private fun assertTAGMLisWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { Assertions.assertThat(isWellFormed(it)).isTrue() }
    }

    private fun assertTAGMLisNotWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { Assertions.assertThat(isWellFormed(it)).isFalse() }
    }

    private fun mapTokenizedTAGML(tagml: String, funk: (tokens: List<TAGMLToken>) -> Unit) {
        when (val result = tokenize(tagml).also { println(it) }) {
            is Either.Left -> {
                showErrorLocation(tagml, result)
                Assertions.fail("Parsing failed: ${result.a}")
            }
            is Either.Right -> funk(result.b)
        }
    }

    private fun isWellFormed(tokens: List<TAGMLToken>): Boolean {
        val iterator = tokens.iterator()
        var expectation: Expectation = after(Not(EOF()), EOF())
        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            LOG.info("expectation=${expectation.javaClass.simpleName}, token=$token")
            if (expectation.matches(token)) {
                expectation = expectation.deriv(token)
                goOn = iterator.hasNext()
            } else {
                goOn = false
            }
        }
        LOG.info("expectation=${expectation.javaClass.simpleName}")
        return !iterator.hasNext() && (expectation is EOF)
    }


}
