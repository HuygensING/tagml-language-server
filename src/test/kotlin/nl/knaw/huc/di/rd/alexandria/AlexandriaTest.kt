package nl.knaw.huc.di.rd.alexandria

import nl.knaw.huc.di.rd.tag.tagml.lsp.Alexandria
import nl.knaw.huc.di.rd.tag.tagml.lsp.BaseTAGMLDocumentModel
import nl.knaw.huc.di.rd.tag.tagml.lsp.CorrectTAGMLDocumentModel
import nl.knaw.huc.di.rd.tag.tagml.lsp.IncorrectTAGMLDocumentModel
import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.fail

class AlexandriaTest {
    @Test
    fun test_correct_tagml() {
        val tagML = "[tagml>text<tagml]"
        val a = Alexandria()
        val base = BaseTAGMLDocumentModel("uri", tagML, 1)
        val tagmlDocumentModel = a.validate(base)
        assertThat(tagmlDocumentModel is CorrectTAGMLDocumentModel).isTrue
        assertThat((tagmlDocumentModel as CorrectTAGMLDocumentModel).rangePairMap).isNotEmpty
    }

    @Test
    fun test_incorrect_tagml() {
        val tagML = "[tagml>text"
        val a = Alexandria()
        val base = BaseTAGMLDocumentModel("uri", tagML, 1)
        val tagmlDocumentModel = a.validate(base)
        assertThat(tagmlDocumentModel is IncorrectTAGMLDocumentModel).isTrue
    }

    @Test
    fun testOpeningMarkupShouldBeClosedLast() {
        val tagML = "[a|+A>AAA AA [b|+B>BBBAAA<a]BBBB<b]"
        val expectedErrors = """
            line 1:29 : No text or markup allowed after the root markup [a] has been ended.
            parsing aborted!
            """.trimIndent()
        parseWithExpectedErrors(tagML, expectedErrors)
    }

    // private methods
    private fun parseWithExpectedErrors(tagML: String, expectedErrors: String) {
        runInStoreTransaction { store ->
            try {
                val document = parseTAGML(tagML, store)
//                logDocumentGraph(document, tagML)
                fail("TAGMLSyntaxError expected!")
            } catch (e: TAGMLSyntaxError) {
                assertThat(e).hasMessage("Parsing errors:\n$expectedErrors")
            }
        }
    }

    private fun runInStoreTransaction(storeConsumer: (TAGStore) -> Unit) {
        getStore().use { it?.runInTransaction(Runnable { storeConsumer(it) }) }
    }

    private fun parseTAGML(tagML: String, store: TAGStore): TAGDocument? {
        //    LOG.info("TAGML=\n{}\n", tagML);
        val trimmedTagML = tagML.trim { it <= ' ' }
//        printTokens(trimmedTagML)
        val document = TAGMLImporter(store).importTAGML(trimmedTagML)
//        logDocumentGraph(document, trimmedTagML)
        return document
    }

    private val tmpDir: Path? = mkTmpDir()

    private fun getStore(): TAGStore? {
        return BDBTAGStore(tmpDir.toString(), false)
    }

    @Throws(IOException::class)
    private fun mkTmpDir(): Path? {
        val sysTmp = System.getProperty("java.io.tmpdir")
        var tmpPath = Paths.get(sysTmp, ".alexandria")
        if (!tmpPath.toFile().exists()) {
            tmpPath = Files.createDirectory(tmpPath)
        }
        return tmpPath
    }
}