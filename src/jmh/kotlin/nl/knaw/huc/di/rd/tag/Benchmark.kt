package nl.knaw.huc.di.rd.tag

import arrow.core.Either
import nl.knaw.huc.di.rd.tag.tagml.parser.TAGMLParser.Companion.checkWellFormedness
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer
import nl.knaw.huc.di.rd.tag.util.showErrorLocation
import org.assertj.core.api.Assertions
import org.openjdk.jmh.annotations.Benchmark

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