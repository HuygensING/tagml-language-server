package nl.knaw.huc.di.rd.tag.tagml.lsp

import lambdada.parsec.utils.Location
import org.eclipse.lsp4j.Position

class PositionCalculator(val tagml: String) {

    internal val lineLengths = mutableListOf<Int>()

    init {
        val lineLengthList = tagml.split("\n").map { it.length }
        this.lineLengths.addAll(lineLengthList)
    }

    fun calculatePosition(location: Location): Position {
        var line = 0
        var total = 0
        while (total <= location.position && line < lineLengths.size) {
            total += lineLengths[line]
            line += 1
        }
        line -= 1
        val character = (location.position - total + lineLengths[line])
        return Position(line, character)
    }
}