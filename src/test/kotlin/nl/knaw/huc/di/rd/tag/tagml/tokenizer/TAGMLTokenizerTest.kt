package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import arrow.core.Either
import lambdada.parsec.io.Reader
import nl.knaw.huc.di.rd.parsec.ort
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.endTag
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.startTextVariation
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Ignore
import org.junit.Test
import java.net.URL

class TAGMLTokenizerTest {

    @Test
    @Ignore
    fun testNameSpaceDefinition() {
        val tagml = "[!ns ns1 http://example.org/namespace/ns1][hello>World!<hello]"

        val namespace = NameSpaceIdentifierToken("ns1", URL("http://example.org/namespace/ns1"))
        val startTag = StartTagToken("hello")
        val text = TextToken("World!")
        val endTag = EndTagToken("hello")
        val expectedTokens = listOf(namespace, startTag, text, endTag)
        assertTokenizingSucceeds(tagml, expectedTokens)
    }

    @Test
    fun testSchemaLocation() {
        val tagml = "[!schema http://example.org/schema.yaml][hello>World!<hello]"

        val schemaLocationToken = SchemaLocationToken(URL("http://example.org/schema.yaml"))
        val startTag = StartTagToken("hello")
        val text = TextToken("World!")
        val endTag = EndTagToken("hello")
        val expectedTokens = listOf(schemaLocationToken, startTag, text, endTag)

        assertTokenizingSucceeds(tagml, expectedTokens)
    }

    @Test
    fun tokenizeTest1() {
        val tagml = "[hello>World!<hello]"

        val startTag = StartTagToken("hello")
        val text = TextToken("World!")
        val endTag = EndTagToken("hello")
        val expectedTokens = listOf(startTag, text, endTag)

        assertTokenizingSucceeds(tagml, expectedTokens)
    }

    @Test
    fun tokenizeTest3() {
        val tagml = "[tag>[part1>Cookie Monster [part2>likes<part1] cookies<part2]<tag]"

        val startTag = StartTagToken("tag")
        val startPart1 = StartTagToken("part1")
        val startPart2 = StartTagToken("part2")
        val textCookieMonster = TextToken("Cookie Monster ")
        val textLikes = TextToken("likes")
        val textCookies = TextToken(" cookies")
        val endTag = EndTagToken("tag")
        val endPart1 = EndTagToken("part1")
        val endPart2 = EndTagToken("part2")
        val expectedTokens = listOf(startTag, startPart1, textCookieMonster, startPart2, textLikes, endPart1, textCookies, endPart2, endTag)

        assertTokenizingSucceeds(tagml, expectedTokens)
    }

    @Test
    fun tokenizeTest4() {
        val tagml = "[tag>[part1>Cookie Monster [part2>likes<part1] many<|cookies|candy|apples|><part2]<tag]"

        val startTag = StartTagToken("tag")
        val startPart1 = StartTagToken("part1")
        val startPart2 = StartTagToken("part2")
        val textCookieMonster = TextToken("Cookie Monster ")
        val textLikes = TextToken("likes")
        val startTextVariation = StartTextVariationToken()
        val textMany = TextToken(" many")
        val textCookies = TextToken("cookies")
        val separator1 = TextVariationSeparatorToken()
        val textCandy = TextToken("candy")
        val separator2 = TextVariationSeparatorToken()
        val textApples = TextToken("apples")
        val endTextVariation = EndTextVariationToken()
        val endTag = EndTagToken("tag")
        val endPart1 = EndTagToken("part1")
        val endPart2 = EndTagToken("part2")
        val expectedTokens = listOf(startTag, startPart1, textCookieMonster, startPart2, textLikes, endPart1, textMany, startTextVariation, textCookies, separator1, textCandy, separator2, textApples, endTextVariation, endPart2, endTag)

        assertTokenizingSucceeds(tagml, expectedTokens)
    }

    @Test
    fun tokenizeTest6() {
        val tagml = "<|"
        val tagmlReader = Reader.string(tagml)
        val p = endTag ort startTextVariation
        println(p(tagmlReader))
    }

    @Test
    fun tokenizeTest5() {
        val tagml = "Bla <|one|two|three|> boe."

        val textBla = TextToken("Bla ")
        val textOne = TextToken("one")
        val textTwo = TextToken("two")
        val textThree = TextToken("three")
        val textBoe = TextToken(" boe.")
        val startTextVariation = StartTextVariationToken()
        val separator1 = TextVariationSeparatorToken()
        val separator2 = TextVariationSeparatorToken()
        val endTextVariation = EndTextVariationToken()
        val expectedTokens = listOf(textBla, startTextVariation, textOne, separator1, textTwo, separator2, textThree, endTextVariation, textBoe)

        assertTokenizingSucceeds(tagml, expectedTokens)
    }

    private fun assertTokenizingSucceeds(tagml: String, expectedTokens: List<TAGMLToken>) {
        when (val result = tokenize(tagml).also { println(it) }) {
            is Either.Left -> {
                showErrorLocation(tagml, result)
                fail("Parsing failed: ${result.a}")
            }
            is Either.Right -> assertThat(result.b.toString()).isEqualTo(expectedTokens.toString())
        }
    }

}