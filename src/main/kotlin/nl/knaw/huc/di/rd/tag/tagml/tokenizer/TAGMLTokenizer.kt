package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import arrow.core.Either
import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import java.net.URL

object TAGMLTokenizer {

    private val specialChar = charIn("""[]<>\|""")

    private val whitespace = charIn(""" \n\t""").optrep

    private val url = ((string("http://") or string("https://") or string("file://")) then (charIn(CharRange('a', 'z')) or charIn("/.")).rep).map { listOfNotNull(it.first) + it.second }

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

    private val namespaceDefinition = `try`(string("[!ns ") then whitespace thenRight namespaceIdentifier then whitespace then url thenLeft char(']'))
            .map { NameSpaceIdentifierToken(it.first.first, URL(it.second.toList().joinToString(separator = ""))) }

    private val startTag = `try`(char('[') thenRight tagName thenLeft char('>'))
            .map { StartTagToken(it) }

    private val endTag = (`try`(char('<')) thenRight tagName thenLeft char(']'))
            .map { EndTagToken(it) }

    private val text = (not(specialChar).map { it.toString() } or escapedSpecialChar).rep
            .map { TextToken(it.joinToString(separator = "")) }

    private val startTextVariation = string("<|").map { StartTextVariationToken() }
    private val textVariationSeparator = `try`(char('|')).map { TextVariationSeparatorToken() }
    private val endTextVariation = `try`(string("|>")).map { EndTextVariationToken() }

    val tagmlParser = (schemaLocation.opt then
            (startTag or text or endTag or startTextVariation or textVariationSeparator or endTextVariation).rep
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
