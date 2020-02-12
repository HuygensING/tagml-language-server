package nl.knaw.huc.di.rd

import nl.knaw.huc.di.rd.tag.tagml.lsp.Constants.LOGFILE_PROPERTY
import org.junit.Test

class LogConfiguratorTest {

    @Test
    fun configure() {
        System.setProperty(LOGFILE_PROPERTY, "tls.log")
        val logger = TLSLoggerFactory.getLogger(this.javaClass)
        logger.trace("TRACE")
        logger.info("INFO")
        logger.debug("DEBUG")
        logger.error("ERROR")
    }
}