package nl.knaw.huc.di.rd.tag.util

import arrow.core.Either
import lambdada.parsec.parser.Response
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken

fun showErrorLocation(tagml: String, result: Either.Left<Response.Reject<Char, List<LSPToken>>>) {
    println(tagml)
    println(" ".repeat(result.a.location.position - 1) + "^")
}
