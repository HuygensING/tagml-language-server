package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

object AlexandriaUtil {

    fun toLSPRange(it: nl.knaw.huc.di.tag.tagml.importer.Range) =
            Range(
                    Position(it.startPosition.line - 1, it.startPosition.character - 1),
                    Position(it.endPosition.line - 1, it.endPosition.character - 1)
            )

}