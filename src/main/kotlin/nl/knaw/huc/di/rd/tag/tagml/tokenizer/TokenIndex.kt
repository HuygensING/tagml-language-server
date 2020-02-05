package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.util.Positions

data class RangedToken(val token: TAGMLToken, val range: Range)

class TokenIndex(val uri: String) {

    internal var rangedTokens: List<RangedToken> = listOf()

    constructor(uri: String, list: List<RangedToken>) : this(uri) {
        rangedTokens = list.sortedWith(locatedTokenComparator)
    }

    fun tokenAt(position: Position): TAGMLToken? {
        val index = rangedTokens.binarySearch { relativePosition(position, it.range) }
        return if (index < 0)
            null
        else
            rangedTokens[index].token
    }
}

private fun relativePosition(p: Position, r: Range): Int = when {
    Positions.isBefore(p, r.start) -> 1  // before range
    Positions.isBefore(r.end, p) -> -1   // after range
    else -> 0                            // in range
}

private val locatedTokenComparator = compareBy<RangedToken> { it.range.start.line }
        .thenBy { it.range.start.character }
        .thenBy { it.range.end.line }
        .thenBy { it.range.end.character }
