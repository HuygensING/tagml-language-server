package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndMarkupToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartMarkupToken

object Deriv {

    private val startTokenDerivCache = mutableMapOf<Pair<Pattern, StartMarkupToken>, Pattern>()
    private val endTokenDerivCache = mutableMapOf<Pair<Pattern, EndMarkupToken>, Pattern>()
    private val textTokenDerivCache = mutableMapOf<Pattern, Pattern>()

    fun memoizedStartTokenDeriv(p: Pattern, st: StartMarkupToken, function: () -> Pattern): Pattern {
        val key = Pair(p, st)
        return if (startTokenDerivCache.containsKey(key)) {
//            println("startTokenDerivCache used!")
            startTokenDerivCache[key]!!
        } else {
            val dp = function()
            startTokenDerivCache[key] = dp
            dp
        }
    }

    fun memoizedEndTokenDeriv(p: Pattern, et: EndMarkupToken, function: () -> Pattern): Pattern {
        val key = Pair(p, et)
        return if (endTokenDerivCache.containsKey(key)) {
//            println("endTokenDerivCache used!")
            endTokenDerivCache[key]!!
        } else {
            val dp = function()
            endTokenDerivCache[key] = dp
            dp
        }
    }

    fun memoizedTextTokenDeriv(p: Pattern, function: () -> Pattern): Pattern =
            if (textTokenDerivCache.containsKey(p)) {
//            println("textTokenDerivCache used!")
                textTokenDerivCache[p]!!
            } else {
                val dp = function()
                textTokenDerivCache[p] = dp
                dp
            }

}