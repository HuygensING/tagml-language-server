package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken

object Deriv {

    private val startTokenDerivCache = mutableMapOf<Pair<Pattern, StartTagToken>, Pattern>()
    private val endTokenDerivCache = mutableMapOf<Pair<Pattern, EndTagToken>, Pattern>()
    private val textTokenDerivCache = mutableMapOf<Pattern, Pattern>()

    fun memoizedStartTokenDeriv(p: Pattern, st: StartTagToken, function: () -> Pattern): Pattern {
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

    fun memoizedEndTokenDeriv(p: Pattern, et: EndTagToken, function: () -> Pattern): Pattern {
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

    fun memoizedTextTokenDeriv(p: Pattern, function: () -> Pattern): Pattern {
        return if (textTokenDerivCache.containsKey(p)) {
//            println("textTokenDerivCache used!")
            textTokenDerivCache[p]!!
        } else {
            val dp = function()
            textTokenDerivCache[p] = dp
            dp
        }
    }

}