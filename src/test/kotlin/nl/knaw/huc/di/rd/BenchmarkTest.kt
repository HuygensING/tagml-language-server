package nl.knaw.huc.di.rd

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.parser.TAGMLParser.Companion.checkWellFormedness
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.junit.Ignore
import org.junit.Test

class BenchmarkTest {

    @Ignore
    @Test(timeout = 10_000)
    fun parseSmallTAGML() {
        parseTAGMLFile("small.tagml")
    }

    @Ignore
    @Test(timeout = 10_000)
    fun parseMediumTAGML() {
        parseTAGMLFile("medium.tagml")
    }

    @Ignore
    @Test(timeout = 10_000)
    fun parseLargeTAGML() {
        parseTAGMLFile("large.tagml")
    }

    private fun parseTAGMLFile(s: String) {
        val tagml = this::class.java.getResource(s).readText(Charsets.UTF_8)
        assertTAGMLisWellFormed(tagml)
    }

    private fun assertTAGMLisWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { Assertions.assertThat(checkWellFormedness(it).isWellFormed).isTrue() }
    }

    private inline fun mapTokenizedTAGML(tagml: String, funk: (tokens: List<LSPToken>) -> Unit) {
        when (val result = TAGMLTokenizer.tokenize(tagml)) {
            is Either.Left -> {
                showErrorLocation(tagml, result)
                Assertions.fail("Parsing failed: ${result.a}")
            }
            is Either.Right -> funk(result.b)
        }
    }
}