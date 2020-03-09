package nl.knaw.huc.di.rd.tag.tagml.lsp

import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import java.nio.file.Files
import java.util.function.Supplier

class Alexandria {

    private val tmpDir = Files.createTempDirectory("tmpDir")
            .also { it.toFile().deleteOnExit() }

    fun validate(tagml: String) {
        runInStore { store ->
            try {
                val document: TAGDocument = store.runInTransaction(Supplier { TAGMLImporter(store).importTAGML(tagml) })
            } catch (e: TAGMLSyntaxError) {
                e.message?.let { error(it) }
            }
        }
    }

    private fun runInStore(storeConsumer: (TAGStore) -> Unit) =
            getStore().use { store -> storeConsumer(store) }

    fun runInStoreTransaction(storeConsumer: (TAGStore) -> Unit) =
            getStore().use { store -> store.runInTransaction { storeConsumer(store) } }

    fun <T> runInStoreTransaction(storeFunction: (TAGStore) -> T): T =
            getStore().use { store -> return store.runInTransaction(Supplier { storeFunction(store) }) }

    private fun getStore(): TAGStore = BDBTAGStore(tmpDir.toString(), false)
}