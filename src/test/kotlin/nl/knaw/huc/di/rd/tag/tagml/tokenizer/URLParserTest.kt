package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import mu.KotlinLogging
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.digit
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.digits
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.domainlabel
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.hostname
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.hostport
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.url
import org.assertj.core.api.Assertions.assertThat

class URLParserTest {
    private val logger = KotlinLogging.logger {}

    //    @Test
    fun testABBA() {
        val p = char('a') then charIn("ab").optrep then char('a')
        assertParses("aa", p)
        assertParses("aba", p)
        assertParses("abba", p)
        assertParses("ababa", p)
        assertDoesNotParse("ababab", p)
    }


    //    @Test
    fun test1() {
        assertParses("0", digit)
        assertParses("1", digit)
        assertParses("9", digit)
        assertDoesNotParse("X", digit)
        assertParses("123", digits)
        assertParses("example", domainlabel)
        assertParses("example.org", hostname)
        assertParses("example.org", hostport)
    }

    //    @Test
    fun testURLs() {
        assertParses("http://example.org", url)
        assertParses("http://example.org/schema.yaml", url)
        assertParses("http://example.org/schema?format=yaml", url)
        assertParses("https://example.org/schema.yaml", url)
        assertParses("file:///home/user/schema.yaml", url)
    }

    private fun <A> assertParses(string: String, p: Parser<Char, A>) {
        val response = parse(string, p)
        assertThat(response.isSuccess()).isTrue()
    }

    private fun <A> assertDoesNotParse(string: String, p: Parser<Char, A>) {
        val response = parse(string, p)
        assertThat(response.isSuccess()).isFalse()
    }

    private fun <A> parse(string: String, p: Parser<Char, A>): Response<Char, A> {
        val parser = p thenLeft eos()
        val reader = Reader.string(string)
        val response = parser(reader)
        logger.info("string='$string', response=${response}")
        return response
    }
}