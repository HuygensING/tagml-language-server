package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.deriv.WellformednessTest.Constructor.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken
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
        mapTokenizedTAGML(tagml) { Assertions.assertThat(isWellFormed(it)).isTrue() }
    }

    private fun assertTAGMLisNotWellformed(tagml: String) {
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
        var expectation: Expectation = after(Not(EOFExpectation()), EOFExpectation())
        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            println("expectation=${expectation.javaClass.simpleName}, token=$token")
            if (expectation.matches(token)) {
                expectation = expectation.deriv(token)
                goOn = iterator.hasNext()
            } else {
                goOn = false
            }
        }
        println("expectation=${expectation.javaClass.simpleName}")
        return !iterator.hasNext() && (expectation is EOFExpectation)
    }

    object Constructor {
        fun after(e1: Expectation, e2: Expectation): Expectation {
            return if (e1 is Empty)
                e2
            else
                AfterExpectation(e1, e2)
        }

    }

    interface Expectation {
        fun matches(t: TAGMLToken): Boolean = false
        fun startTokenDeriv(s: StartTagToken): Expectation = Unexpected()
        fun endTokenDeriv(s: EndTagToken): Expectation = Unexpected()
        fun textTokenDeriv(s: TextToken): Expectation = Unexpected()
        fun deriv(token: TAGMLToken): Expectation {
            return when (token) {
                is StartTagToken -> startTokenDeriv(token)
                is EndTagToken -> endTokenDeriv(token)
                is TextToken -> textTokenDeriv(token)
                else -> Unexpected()
            }
        }
    }

    class StartTokenExpectation(val id: TagIdentifier) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(
                    Not(EndTokenExpectation(FixedIdentifier(s.tagName))),
                    EndTokenExpectation(FixedIdentifier(s.tagName))
            )
        }
    }

    class EndTokenExpectation(val id: TagIdentifier) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is EndTagToken) && id.matches(t.tagName)
        }

        override fun endTokenDeriv(s: EndTagToken): Expectation {
            return Empty()
        }
    }

    class AfterExpectation(val e1: Expectation, val e2: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return e1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(e1.startTokenDeriv(s), e2)
        }

        override fun endTokenDeriv(s: EndTagToken): Expectation {
            return after(e1.endTokenDeriv(s), e2)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return after(e1.textTokenDeriv(t), e2)
        }
    }


//    class AnyExpectation() : Expectation {
//        override fun matches(t: TAGMLToken): Boolean {
//            return true
//        }
//
//        override fun startTokenDeriv(s: StartTagToken): Expectation {
//            return StartTokenExpectation(FixedIdentifier(s.tagName)).startTokenDeriv(s)
//        }
//
//        override fun endTokenDeriv(s: EndTagToken): Expectation {
//            return EndTokenExpectation(FixedIdentifier(s.tagName)).endTokenDeriv(s)
//        }
//
//        override fun textTokenDeriv(t: TextToken): Expectation {
//            return TextExpectation().textTokenDeriv(t)
//        }
//    }

    class Not(val e: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return !e.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return if (e.matches(s))
                Unexpected()
            else
                StartTokenExpectation(FixedIdentifier(s.tagName)).startTokenDeriv(s)
        }

        override fun endTokenDeriv(s: EndTagToken): Expectation {
            return if (e.matches(s))
                Unexpected()
            else
                EndTokenExpectation(FixedIdentifier(s.tagName)).endTokenDeriv(s)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return if (e.matches(t))
                Unexpected()
            else
                TextExpectation().textTokenDeriv(t)
        }
    }

    class TextExpectation() : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is TextToken)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return Empty()
        }
    }

    class Empty() : Expectation

    class Unexpected() : Expectation

    class EOFExpectation() : Expectation

    class FixedIdentifier(val tagName: String) : TagIdentifier {
        override fun matches(tagName: String): Boolean {
            return this.tagName == tagName
        }
    }

    class AnyTagIdentifier() : TagIdentifier {
        override fun matches(tagName: String): Boolean {
            return true
        }
    }

}
