package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.zeroOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Text
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import org.slf4j.LoggerFactory

object WellFormedness {
    private val _log = LoggerFactory.getLogger(this::class.java)

    data class WellFormednessResult(val isWellFormed: Boolean, val errors: List<String>)

    fun checkWellFormedness(tokens: List<TAGMLToken>): WellFormednessResult {
        val iterator = tokens.iterator()
        var expectation: Pattern = Range(AnyTagIdentifier(), concurOneOrMore(choice(Text(), zeroOrMore(Range(AnyTagIdentifier(), Text())))))
        val errors = mutableListOf<String>()

        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            _log.info("expectation=$expectation, token=${token.content}, match=${expectation.matches(token)}")
            if (expectation.matches(token)) {
                expectation = expectation.deriv(token)
                goOn = iterator.hasNext()
            } else {
//                _log.error("Unexpected token: found $token, but expected ${expectation.expectedTokens()}")

                errors.add("Unexpected token: found ${token.content}, but expected ${expectationString(expectation)}")
                goOn = false
            }
        }
        _log.info("remaining expectation=${expectation}")
        if (errors.isEmpty() && !expectation.nullable) {
            errors.add("Out of tokens, but expected ${expectationString(expectation)}")
        }
        return WellFormednessResult(!iterator.hasNext() && expectation.nullable, errors)
    }


    private fun expectationString(expectation: Pattern): String {
        val expectedTokens = expectation.expectedTokens().map { it.content }
        return when (expectedTokens.size) {
            0 -> "nothing"
            1 -> expectedTokens[0]
            else -> "any of $expectedTokens"
        }
    }

}