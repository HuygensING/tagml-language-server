package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.io.Reader
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.URLParser.url
import org.junit.Test

class URLParserTest() {
    @Test
    fun testURLs() {
        assertURLParses("http://example.org")
    }

    private fun assertURLParses(urlString: String) {
        val urlReader = Reader.string(urlString)
        val response = url(urlReader)
        when (response) {

        }
    }
}