package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.derivation.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Pattern
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.function.Function

class WellformednessTest() {

    @Test
    fun testWellformedTAGML1() {
        val tagml = "[tag>text<tag]"
        assertTAGMLisWellformed(tagml)
    }

    @Test
    fun testNotWellformedTAGML1() {
        val tagml = "[tag>text"
        assertTAGMLisNotWellformed(tagml)
    }

    private fun assertTAGMLisWellformed(tagml: String) {
        mapTokenizedTAGML(tagml) { Assertions.assertThat(isWellformed(it)).isTrue() }
    }

    private fun assertTAGMLisNotWellformed(tagml: String) {
        mapTokenizedTAGML(tagml) { Assertions.assertThat(isWellformed(it)).isFalse() }
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

    private fun isWellformed(tokens: List<TAGMLToken>): Boolean {
        val iterator = tokens.iterator()
        var expectation: Pattern = StartToken(AnyTagIdentifier())))
        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            if (token matchesExpectation expectation) {
                expectation = deriveNext(expectation, token)
                goOn = iterator.hasNext()
            } else {
                goOn = false
            }
        }
        return !iterator.hasNext() && expectation.empty
    }

    private infix fun TAGMLToken.matchesExpectation(p: Pattern): Boolean = p.matches(this)




}
