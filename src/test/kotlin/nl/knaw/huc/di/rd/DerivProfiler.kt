package nl.knaw.huc.di.rd

import nl.knaw.huc.di.rd.DerivProfiler.parseTAGMLFile
import nl.knaw.huc.di.rd.tag.tagml.derivation.WellFormedness
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer

object DerivProfiler {

    fun parseTAGMLFile(s: String) {
        val tagml = this::class.java.getResource(s).readText(Charsets.UTF_8)
        TAGMLTokenizer.tokenize(tagml).fold(
                {
                    println(tagml)
                    println(" ".repeat(it.location.position - 1) + "^")
                },
                { WellFormedness.checkWellFormedness(it) }
        )
    }
}

fun main() {
    println("Press [Enter] when profiling is started in VisualVM:")
    readLine()
    println("starting benchmark...")
    for (i in 1..10) {
        parseTAGMLFile("large.tagml")
    }
}
