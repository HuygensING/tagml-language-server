package nl.knaw.huc.di.rd.tag.tagml.lsp

import nl.knaw.huc.di.rd.tag.tagml.lsp.AlexandriaUtil.toLSPRange
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.util.Positions
import nl.knaw.huc.di.tag.tagml.importer.Range as ARange
import nl.knaw.huc.di.tag.tagml.importer.RangePair as ARangePair

typealias LineIndex = MutableMap<Int, Long>

interface TAGMLDocumentModel {
    val base: BaseTAGMLDocumentModel
}

fun <O> ARangePair.map(function: (ARange) -> O): List<O> = listOf(this.startRange, this.endRange).map(function)

data class BaseTAGMLDocumentModel(val uri: String, val text: String, val version: Int)

data class CorrectTAGMLDocumentModel(
        override val base: BaseTAGMLDocumentModel,
        val rangePairMap: MutableMap<Long, ARangePair>
) : TAGMLDocumentModel {
    private val index by lazy { markupIndexOf(rangePairMap) }

    fun rangePairAt(position: Position): ARangePair? {
        val markupId: Long? = index.markupIdAt(position)
        return if (markupId == null) {
            null
        } else {
            this.rangePairMap[markupId]
        }
    }

    private fun markupIndexOf(rangePairMap: MutableMap<Long, ARangePair>): MarkupIndex {
        val markupTokenRanges =
                rangePairMap.flatMap { listOf((it.key to it.value.startRange), (it.key to it.value.endRange)) }
                        .map { (it.first to toLSPRange(it.second)) }
                        .map { MarkupTokenRange(it.first, it.second) }
        return MarkupIndex(markupTokenRanges)
    }
}

data class IncorrectTAGMLDocumentModel(
        override val base: BaseTAGMLDocumentModel,
        val diagnostics: List<Diagnostic>
) : TAGMLDocumentModel

data class MarkupTokenRange(val docId: Long, val range: Range)

class MarkupIndex(list: List<MarkupTokenRange>) {
    private var markupTokenRanges: List<MarkupTokenRange> = listOf()

    private val markupTokenRangeComparator = compareBy<MarkupTokenRange> { it.range.start.line }
            .thenBy { it.range.start.character }
            .thenBy { it.range.end.line }
            .thenBy { it.range.end.character }

    init {
        markupTokenRanges = list.sortedWith(markupTokenRangeComparator)
    }

    fun markupIdAt(position: Position): Long? {
        val index = markupTokenRanges.binarySearch { relativePosition(position, it.range) }
        return if (index < 0)
            null
        else
            markupTokenRanges[index].docId
    }

    private fun relativePosition(p: Position, r: Range): Int = when {
        Positions.isBefore(p, r.start) -> 1  // before range
        Positions.isBefore(r.end, p) -> -1   // after range
        else -> 0                            // in range
    }

}