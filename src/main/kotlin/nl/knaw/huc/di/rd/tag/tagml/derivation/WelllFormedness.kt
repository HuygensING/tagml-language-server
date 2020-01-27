package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.zeroOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Text
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import org.slf4j.LoggerFactory

object WelllFormedness {
    private val _log = LoggerFactory.getLogger(this::class.java)

    fun isWellFormed(tokens: List<TAGMLToken>): Boolean {
        val iterator = tokens.iterator()
        var expectation: Expectation = Range(AnyTagIdentifier(), zeroOrMore(Range(AnyTagIdentifier(), Text())))

        var goOn = iterator.hasNext()
        while (goOn) {
            val token = iterator.next()
            _log.info("expectation=$expectation, token=$token")
            if (expectation.matches(token)) {
                expectation = expectation.deriv(token)
                goOn = iterator.hasNext()
            } else {
                _log.error("Unexpected token: found $token, but expected ${expectation.expectedTokens()}")
                goOn = false
            }
        }
        _log.info("expectation=${expectation}")
        return !iterator.hasNext() && expectation.nullable
    }

}