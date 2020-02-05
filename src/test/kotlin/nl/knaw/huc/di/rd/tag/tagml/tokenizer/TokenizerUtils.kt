package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.utils.Location
import org.eclipse.lsp4j.Position

object TokenizerUtils {

    class SomeClass(val tagml: String) {

        internal val lineLengths = mutableListOf<Int>()

        init {
            val lineLengthList = tagml.split("\n").map { it.length }
            this.lineLengths.addAll(lineLengthList)
        }

        fun calculatePosition(location: Location): Position {
            var line = 0
            var total = 0
            while (total <= location.position && line <= lineLengths.size) {
                total += lineLengths[line]
                line += 1
            }
            line -= 1
            val character = (location.position - total + lineLengths[line])
            return Position(line, character)
        }
    }
}