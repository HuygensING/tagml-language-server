package nl.knaw.huc.di.rd.tag.util

import lambdada.parsec.parser.Response
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken

fun showErrorLocation(tagml: String, reject: Response.Reject<Char, List<LSPToken>>) {
    println(tagml)
    println(" ".repeat(reject.location.position - 1) + "^")
}
