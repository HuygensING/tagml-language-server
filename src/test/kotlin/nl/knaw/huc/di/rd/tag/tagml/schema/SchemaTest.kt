package nl.knaw.huc.di.rd.tag.tagml.schema

import mu.KotlinLogging
import nl.knaw.huc.di.tag.schema.TAGMLSchemaFactory
import org.junit.Test
import kotlin.test.assertNotNull

class SchemaTest {
    private val logger = KotlinLogging.logger {}
    @Test
    fun test() {
        val schemaYAML = """
            |---
            |L1:
            |   root:
            |     - a
            |     - b
            |     - c
            |     - d:
            |         - d1
            |         - d2
            |L2:
            |  root:
            |    - x
            |    - 'y'
            |    - z:
            |        - z1
            |        - z2
            """.trimMargin()
        val result = TAGMLSchemaFactory.parseYAML(schemaYAML)
        logger.info("${result.errors}")
        assertNotNull(result)

    }
}
