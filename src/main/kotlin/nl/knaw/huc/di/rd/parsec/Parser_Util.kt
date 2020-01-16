package nl.knaw.huc.di.rd.parsec

import lambdada.parsec.parser.Parser
import lambdada.parsec.parser.`try` as tryp

// `or`  with `try`
infix fun <I, A> Parser<I, A>.ort(p: Parser<I, A>): Parser<I, A> = { reader ->
    val a = tryp(this)(reader)
    when (a.consumed) {
        true -> a
        false -> a.fold({ a }, { tryp(p)(reader) })
    }
}
