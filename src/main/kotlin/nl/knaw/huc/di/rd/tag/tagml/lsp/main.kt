package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.launch.LSPLauncher
import java.io.InputStream
import java.io.PrintStream


fun main(args: Array<String>) {
    println("starting tagml language server...")
    startServer(System.`in`, System.out)
}

fun startServer(inputStream: InputStream?, out: PrintStream?) {
//    https://github.com/LucasBullen/LSP4J_Tutorial/blob/master/Exercises/1/1-README.md
    val server = TAGMLLanguageServer()
    val l = LSPLauncher.createServerLauncher(server, inputStream, out)
    val startListening = l.startListening()
    server.setRemoteProxy(l.remoteProxy);
}

