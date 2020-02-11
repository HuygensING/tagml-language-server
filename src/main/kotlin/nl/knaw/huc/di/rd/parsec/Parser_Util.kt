package nl.knaw.huc.di.rd.parsec

import lambdada.parsec.parser.Parser
import lambdada.parsec.parser.Response
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import org.eclipse.lsp4j.Range
import lambdada.parsec.parser.`try` as tryp

// `or`  with `try`
infix fun <I, A> Parser<I, A>.ort(p: Parser<I, A>): Parser<I, A> = { reader ->
    val a = tryp(this)(reader)
    when (a.consumed) {
        true -> a
        false -> a.fold({ a }, { tryp(p)(reader) })
    }
}

infix fun <I, A> Parser<I, A>.toLSPToken(f: (A) -> TAGMLToken): Parser<I, LSPToken> = { reader ->
    this(reader).fold({
        val value = LSPToken(
                f(it.value),
                Range(
                        (it.input as PositionalReader).startPosition,
                        (it.input as PositionalReader).endPosition
                )
        )
        Response.Accept(value, it.input, it.consumed)
    }, {
        Response.Reject(it.location, it.consumed)
    })
}

