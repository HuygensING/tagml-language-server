package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import arrow.core.Either
import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import java.net.URL

object TAGMLTokenizer {

    private fun <A> Pair<A, List<A>>.flatten(): List<A> {
        val l = mutableListOf<A>(this.first).apply {}
        l.addAll(this.second)
        return l.toList()
    }

    private val specialChar = charIn("""[]<>\""")

    private val url = ((string("http://") or string("https://") or string("file://")) then (charIn(CharRange('a', 'z')) or charIn("/.")).rep).map { it.flatten() }

    private val escapedSpecialChar = (char('\\') then specialChar)
            .map { "${it.first}${it.second}" }

    private val tagName = charIn(CharRange('a', 'z')).rep
            .map { it.charsToString() }

    private val schemaLocation = (string("[!schema ") thenRight url thenLeft char(']'))
            .map { SchemaLocationToken(URL(it.toList().joinToString(separator = ""))) }

    private val startTag = (char('[') thenRight tagName thenLeft char('>'))
            .map { StartTagToken(it) }

    private val endTag = (char('<') thenRight tagName thenLeft char(']'))
            .map { EndTagToken(it) }

    private val text = (not(specialChar).map { it.toString() } or escapedSpecialChar).rep
            .map { TextToken(it.joinToString(separator = "")) }

    val tagmlParser = (schemaLocation.opt then (startTag or text or endTag).rep thenLeft eos()).map { it.flatten() }

    fun tokenize(tagml: String): Either<Response.Reject<Char, List<TAGMLToken>>, List<TAGMLToken>> {
        val tagmlReader = Reader.string(tagml)
        return tagmlParser(tagmlReader)
                .fold(
                        { Either.right(it.value) },
                        { Either.left(it) }
                )
    }

//    fun tokenize(tagml:/\ String) = println(tagml)

}
