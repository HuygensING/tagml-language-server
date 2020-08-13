package nl.knaw.huc.di.rd.tag

import nl.knaw.huc.di.rd.tag.tagml.derivation.WellFormedness
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions.assertThat
import org.openjdk.jmh.annotations.Benchmark
import kotlin.test.fail

open class Benchmark {

    @Benchmark
    fun parseSmallTAGML(): Boolean {
        parseTAGMLFile("small.tagml")
        return true
    }

    @Benchmark
    fun parseMediumAGML(): Boolean {
        parseTAGMLFile("medium.tagml")
        return true
    }

    //    @Benchmark
    fun parseLargeTAGML(): Boolean {
        parseTAGMLFile("large.tagml")
        return true
    }

    private fun parseTAGMLFile(filename: String) {
        val clazz = this::class.java
        println(clazz)
        val resource = clazz.getResource(filename)
        println(resource)
        val tagml = resource.readText(Charsets.UTF_8)
        assertTAGMLisWellFormed(tagml)
    }

    private fun assertTAGMLisWellFormed(tagml: String) {
        mapTokenizedTAGML(tagml) { assertThat(WellFormedness.checkWellFormedness(it).isWellFormed).isTrue() }
    }

    private fun mapTokenizedTAGML(tagml: String, consumeTokens: (tokens: List<LSPToken>) -> Unit) {
        TAGMLTokenizer.tokenize(tagml).fold(
                { reject ->
                    showErrorLocation(tagml, reject)
                    fail("Parsing failed: $reject")
                },
                { tokens -> consumeTokens(tokens) }
        )
    }

}