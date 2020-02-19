package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

typealias LPattern = Lazy<Pattern>

interface Pattern {

    /**
     *  Does this pattern expect an "empty" token?
     */

    val nullable: Boolean

    val expectedTokens: Set<TAGMLToken>
        get() = emptySet()

    fun matches(t: TAGMLToken): Boolean = false

    fun deriv(token: TAGMLToken): LPattern =
            when (token) {
                is StartTagToken -> startTokenDeriv(token)
                is EndTagToken   -> endTokenDeriv(token)
                is TextToken     -> textTokenDeriv()
                else             -> lazy { NotAllowed }
            }

    fun startTokenDeriv(s: StartTagToken): LPattern = lazyOf(NotAllowed)

    fun endTokenDeriv(e: EndTagToken): LPattern = lazyOf(NotAllowed)

    fun textTokenDeriv(): LPattern = lazyOf(NotAllowed)
}
