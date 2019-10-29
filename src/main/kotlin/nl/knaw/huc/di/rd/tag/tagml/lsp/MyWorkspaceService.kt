package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

class MyWorkspaceService : WorkspaceService {
    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        for (fe in params?.changes!!) {
            println(fe.uri)

        }
    }

}