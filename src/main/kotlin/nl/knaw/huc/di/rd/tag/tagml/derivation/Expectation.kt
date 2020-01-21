package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

interface Expectation {
    fun matches(t: TAGMLToken): Boolean = false
    fun startTokenDeriv(s: StartTagToken): Expectation = NotAllowed()
    fun endTokenDeriv(e: EndTagToken): Expectation = NotAllowed()
    fun textTokenDeriv(t: TextToken): Expectation = NotAllowed()
    fun deriv(token: TAGMLToken): Expectation {
        return when (token) {
            is StartTagToken -> startTokenDeriv(token)
            is EndTagToken -> endTokenDeriv(token)
            is TextToken -> textTokenDeriv(token)
            else -> NotAllowed()
        }
    }
}
