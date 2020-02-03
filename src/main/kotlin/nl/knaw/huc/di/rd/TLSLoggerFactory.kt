package nl.knaw.huc.di.rd

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.Configurator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.spi.ContextAwareBase
import nl.knaw.huc.di.rd.tag.tagml.lsp.Constants.LOGFILE_PROPERTY

class TLSLoggerFactory() : ContextAwareBase(), Configurator {
    private val loggerContext = LoggerContext()

    init {
        configure(loggerContext)
    }

    fun getLogger(name: String): Logger = loggerContext.getLogger(name)

    fun <A> getLogger(clazz: Class<A>): Logger = loggerContext.getLogger(clazz)

    override fun configure(lc: LoggerContext) {
        val fa = FileAppender<ILoggingEvent>()
        fa.context = lc
        fa.name = "file"
        fa.file = System.getProperty(LOGFILE_PROPERTY)
        val encoder = LayoutWrappingEncoder<ILoggingEvent>()
        encoder.context = lc
        val layout = PatternLayout()
        layout.pattern = "%d{YYYY-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
        layout.context = lc
        layout.start()
        encoder.layout = layout
        fa.encoder = encoder
        fa.start()
        val rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.addAppender(fa)
    }

}