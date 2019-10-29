package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.services.TextDocumentService

class MyTextDocumentService : TextDocumentService {
    override fun didOpen(params: DidOpenTextDocumentParams?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didSave(params: DidSaveTextDocumentParams?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didClose(params: DidCloseTextDocumentParams?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didChange(params: DidChangeTextDocumentParams?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
