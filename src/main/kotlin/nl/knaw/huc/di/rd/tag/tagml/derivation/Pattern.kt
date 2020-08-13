package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndMarkupToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartMarkupToken
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
                is StartMarkupToken -> startTokenDeriv(token)
                is EndMarkupToken -> endTokenDeriv(token)
                is TextToken -> textTokenDeriv()
                else -> NotAllowed
            }

    fun startTokenDeriv(s: StartMarkupToken): Pattern = NotAllowed

    fun endTokenDeriv(e: EndMarkupToken): Pattern = NotAllowed

    fun textTokenDeriv(): Pattern = NotAllowed
}
