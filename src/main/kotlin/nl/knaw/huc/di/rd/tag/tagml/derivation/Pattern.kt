package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

interface Pattern {

    /**
     *  Does this pattern expect an "empty" token?
     */
    val nullable: Boolean

    val expectedTokens: Set<TAGMLToken>
        get() = emptySet()

    fun matches(t: TAGMLToken): Boolean = false

    fun deriv(token: TAGMLToken): Pattern =
            when (token) {
                is StartTagToken -> startTokenDeriv(token)
                is EndTagToken   -> endTokenDeriv(token)
                is TextToken     -> textTokenDeriv()
                else             -> NotAllowed
            }

    fun startTokenDeriv(s: StartTagToken): Pattern = NotAllowed

    fun endTokenDeriv(e: EndTagToken): Pattern = NotAllowed

    fun textTokenDeriv(): Pattern = NotAllowed
}
