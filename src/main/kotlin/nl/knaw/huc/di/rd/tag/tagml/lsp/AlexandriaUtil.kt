package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range

object AlexandriaUtil {

    fun nl.knaw.huc.di.tag.tagml.importer.Range.toLSPRange(): Range =
            Range(
                    Position(this.startPosition.line - 1, this.startPosition.character - 1),
                    Position(this.endPosition.line - 1, this.endPosition.character - 1)
            )

}