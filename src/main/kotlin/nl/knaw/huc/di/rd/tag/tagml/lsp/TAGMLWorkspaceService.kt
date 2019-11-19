package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService

class TAGMLWorkspaceService : WorkspaceService {
    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
    }

    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
    }

}
