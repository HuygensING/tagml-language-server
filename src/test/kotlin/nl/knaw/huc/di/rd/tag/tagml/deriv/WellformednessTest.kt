package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.junit.Test

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
        val expectations = mutableListOf(oneOf(schemalocation, namespacedefinition, starttoken(any)))
        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            if (token matchesExpectation expectations.first) {
                expectation = deriveNextExpectation(expectation, token)
                goOn = iterator.hasNext()
            } else {
                goOn = false
            }
        }
        return !iterator.hasNext() && expectation.isEmpty()
    }

    infix fun TAGMLToken.matchesExpectation(e: Expectation): Boolean = e.matches(this)


    class Expectation() {

    }

    fun Expectation.matches(t: TAGMLToken): Boolean {
        return true
    }

//    De validatie in creole is altijd op basis van een schema.
//    Voor dit isssue willen we derivatives gebruiken om well-formedness te checken.
//    Ik zoek in de creole code naar een manier om een algemeen schema te maken die alleen de well-formedness van een lmnl tekst valideert.
//    Daar moet in  ieder geval een Range in zitten, die open en close tags verbind.
//    Het Range Pattern heeft 2 properties:
//    - een NameClass voor de tagName, hiervoor is een optie AnyName, waardoor ranges kunt matchen zonder van te voren te weten welke tagnames er zijn.
//    - een child Pattern. Door deze hierarchische vorm lijkt het me niet mogelijk om een creole schema te maken die zonder voorwaarde voor de tagnames alleen op well-formednes checkt.

}
