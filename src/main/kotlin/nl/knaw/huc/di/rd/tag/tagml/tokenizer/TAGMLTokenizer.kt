package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import arrow.core.Either
import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import nl.knaw.huc.di.rd.parsec.ort
import java.net.URL

object TAGMLTokenizer {

    private val specialChar = charIn("""[]<>\|""")

    private val whitespace = charIn(""" \n\t""").optrep

    private val url = ((string("http://") ort string("https://") or string("file://")) then (charIn(CharRange('a', 'z')) or charIn("/.")).rep).map { listOfNotNull(it.first) + it.second }

    private val escapedSpecialChar = (char('\\') then specialChar)
            .map { "${it.first}${it.second}" }

    private val variableName = (
            charIn(CharRange('a', 'z')) then
                    charIn("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_").rep
            ).map { (listOfNotNull(it.first) + it.second).charsToString() }

    private val tagName = variableName

    private val namespaceIdentifier = variableName

    // use `try` because schemaLocation and startTag both start with '['
    private val schemaLocation = `try`(string("[!schema ") thenRight url thenLeft char(']'))
            .map { SchemaLocationToken(URL(it.toList().joinToString(separator = ""))) }

    private val namespaceDefinition = (string("[!ns ") then whitespace thenRight namespaceIdentifier then whitespace then url thenLeft char(']'))
            .map { NameSpaceIdentifierToken(it.first.first, URL(it.second.toList().joinToString(separator = ""))) }

    private val startTag = (char('[') thenRight tagName thenLeft char('>'))
            .map { StartTagToken(it) }

    val endTag = (char('<') thenRight tagName thenLeft char(']'))
            .map { EndTagToken(it) }

    private val text = (not(specialChar).map { it.toString() } or escapedSpecialChar).rep
            .map { TextToken(it.joinToString(separator = "")) }

    val startTextVariation = string("<|").map { StartTextVariationToken() }
    private val textVariationSeparator = char('|').map { TextVariationSeparatorToken() }
    private val endTextVariation = string("|>").map { EndTextVariationToken() }

    val tagmlParser = (schemaLocation.opt then
            (startTag ort text ort endTag ort startTextVariation ort endTextVariation ort textVariationSeparator).rep
            thenLeft eos())
            .map { listOfNotNull(it.first) + it.second }


    fun tokenize(tagml: String): Either<Response.Reject<Char, List<TAGMLToken>>, List<TAGMLToken>> {
        val tagmlReader = Reader.string(tagml)
        return tagmlParser(tagmlReader)
                .fold(
                        { Either.Right(it.value) },
                        { Either.Left(it) }
                )
    }

}
