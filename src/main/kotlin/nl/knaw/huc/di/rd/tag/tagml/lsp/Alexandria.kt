package nl.knaw.huc.di.rd.tag.tagml.lsp

import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.ErrorListener
import nl.knaw.huygens.alexandria.ErrorListener.CustomError
import nl.knaw.huygens.alexandria.ErrorListener.TAGSyntaxError
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.nio.file.Files
import java.util.function.Supplier

class Alexandria {

    private val tmpDir = Files.createTempDirectory("tmpDir")
            .also { it.toFile().deleteOnExit() }

    fun validate(tagml: String): List<Diagnostic> {
        val diagnostics = mutableListOf<Diagnostic>()
        runInStore { store ->
            try {
                val document: TAGDocument = store.runInTransaction(Supplier { TAGMLImporter(store).importTAGML(tagml) })
            } catch (e: TAGMLSyntaxError) {
                diagnostics.addAll(e.errors.map { toDiagnostic(it) })
            }
        }
        return diagnostics
    }

    private fun toDiagnostic(tagError: ErrorListener.TAGError): Diagnostic {
        return when (tagError) {
            is TAGSyntaxError -> Diagnostic(range(tagError), tagError.message, DiagnosticSeverity.Error, "lexer")
            is CustomError    -> Diagnostic(range(tagError), tagError.message, DiagnosticSeverity.Error, "parser")
            else              -> Diagnostic(
                    Range(
                            Position(0, 0),
                            Position(0, 0))
                    , tagError.message,
                    DiagnosticSeverity.Error,
                    "lexer"
            )
        }
    }

    private fun range(tagError: TAGSyntaxError): Range {
        return Range(
                Position(tagError.position.line - 1, tagError.position.character - 1),
                Position(tagError.position.line - 1, tagError.position.character - 1)
        )
    }

    private fun range(tagError: CustomError): Range? =
            Range(
                    Position(tagError.startPos.line - 1, tagError.startPos.character - 1),
                    Position(tagError.endPos.line - 1, tagError.endPos.character - 1))

    private fun runInStore(storeConsumer: (TAGStore) -> Unit) =
            getStore().use { store -> storeConsumer(store) }

    fun runInStoreTransaction(storeConsumer: (TAGStore) -> Unit) =
            getStore().use { store -> store.runInTransaction { storeConsumer(store) } }

    fun <T> runInStoreTransaction(storeFunction: (TAGStore) -> T): T =
            getStore().use { store -> return store.runInTransaction(Supplier { storeFunction(store) }) }

    private fun getStore(): TAGStore = BDBTAGStore(tmpDir.toString(), false)
}