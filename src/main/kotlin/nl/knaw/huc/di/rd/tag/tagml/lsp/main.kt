package nl.knaw.huc.di.rd.tag.tagml.lsp

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.eclipse.lsp4j.launch.LSPLauncher
import java.io.InputStream
import java.io.PrintStream
import java.io.PrintWriter

fun main(args: Array<String>) {
    val parser = ArgParser("tagml-language-server")
    val traceFile by parser.option(ArgType.String, fullName = "trace", shortName = "t", description = "LSP tracing on, write to indicated file")
    val verbose by parser.option(ArgType.Boolean, shortName = "v", description = "increase verbosity").default(false)
    parser.parse(args)
    val logfile by parser.option(ArgType.String, shortName = "l", description = "file to log to")
    if (verbose) println("starting tagml language server...")
    startServer(System.`in`, System.out, traceFile)
}

fun startServer(inputStream: InputStream, out: PrintStream, traceFile: String?) {
//    https://github.com/LucasBullen/LSP4J_Tutorial/blob/master/Exercises/1/1-README.md
    val server = TAGMLLanguageServer
    val l = if (traceFile != null) {
        val trace = PrintWriter(traceFile)
        LSPLauncher.createServerLauncher(server, inputStream, out, true, trace)
    } else {
        LSPLauncher.createServerLauncher(server, inputStream, out)
    }
    val startListening = l.startListening()
    server.connect(l.remoteProxy)
}

// add schemalocation setting to tagml to enhance the autocomplete
// https://github.com/angelozerr/lsp4xml