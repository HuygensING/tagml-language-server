package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import org.slf4j.LoggerFactory

object WelllFormedness {
    private val _log = LoggerFactory.getLogger(this::class.java)

    fun isWellFormed(tokens: List<TAGMLToken>): Boolean {
        val iterator = tokens.iterator()
        var expectation: Expectation = Constructors.after(Expectations.Range(TagIdentifiers.AnyTagIdentifier(), Expectations.Text()), Expectations.EOF())

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
        return !iterator.hasNext() && (expectation is Expectations.EOF)
    }

}