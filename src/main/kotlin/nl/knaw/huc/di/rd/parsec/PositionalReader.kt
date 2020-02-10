package nl.knaw.huc.di.rd.parsec

import lambdada.parsec.io.Reader
import lambdada.parsec.utils.Location
import org.eclipse.lsp4j.Position
import java.net.URL

class PositionalReader(private val source: List<Char>,
                       private val location: Int,
                       val lastPosition: Position,
                       private val newline: Boolean,
                       var startPosition: Position? = null,
                       var endPosition: Position? = null) : Reader<Char> {

    override fun location(): Location {
        return Location(location)
    }

    override fun read(): Pair<Char, PositionalReader>? {
        return source.getOrNull(location)?.let {
            val character = if (newline) 0 else lastPosition.character + 1
            val line = if (newline) lastPosition.line + 1 else lastPosition.line
            val newLastPosition = Position(line, character)
            if (startPosition == null) startPosition = newLastPosition
            it to PositionalReader(source, location + 1, newLastPosition, (it == '\n'), startPosition, newLastPosition)
        }
    }

    companion object {
        fun string(s: String): PositionalReader = PositionalReader(s.toList(), 0, Position(0, -1), false)
        fun url(s: URL): PositionalReader = string(s.readText())
    }

}