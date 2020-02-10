package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.util.Positions

class TokenIndex(val uri: String) {

    internal var lspTokens: List<LSPToken> = listOf()

    constructor(uri: String, list: List<LSPToken>) : this(uri) {
        lspTokens = list.sortedWith(locatedTokenComparator)
    }

    fun tokenAt(position: Position): TAGMLToken? {
        val index = lspTokens.binarySearch { relativePosition(position, it.range) }
        return if (index < 0)
            null
        else
            lspTokens[index].token
    }
}

private fun relativePosition(p: Position, r: Range): Int = when {
    Positions.isBefore(p, r.start) -> 1  // before range
    Positions.isBefore(r.end, p) -> -1   // after range
    else -> 0                            // in range
}

private val locatedTokenComparator = compareBy<LSPToken> { it.range.start.line }
        .thenBy { it.range.start.character }
        .thenBy { it.range.end.line }
        .thenBy { it.range.end.character }
