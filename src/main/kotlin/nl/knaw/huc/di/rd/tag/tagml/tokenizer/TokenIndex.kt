package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.util.Positions

typealias LocatedToken = Pair<TAGMLToken, Range>

class TokenIndex(val uri: String) {
    // list of pairs Token, Range, sorted by range

    var locatedTokens: List<LocatedToken> = listOf()
        set(_locatedTokens: List<LocatedToken>) {
            field = _locatedTokens.sortedWith(locatedTokenComparator)
        }

    fun tokenAt(position: Position): LocatedToken? {
        val index = locatedTokens.binarySearch { relativePosition(position, it.second) }
        return locatedTokens.elementAtOrNull(index)
    }
}

private fun relativePosition(p: Position, r: Range): Int = when {
    Positions.isBefore(p, r.start) -> 1  // before range
    Positions.isBefore(r.end, p) -> -1   // after range
    else -> 0                            // in range
}

private val locatedTokenComparator = compareBy<LocatedToken> { it.second.start.line }
        .thenBy { it.second.start.character }
        .thenBy { it.second.end.line }
        .thenBy { it.second.end.character }
