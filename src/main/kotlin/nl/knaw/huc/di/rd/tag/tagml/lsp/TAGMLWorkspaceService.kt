package nl.knaw.huc.di.rd.tag.tagml.lsp

import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.services.WorkspaceService
import org.slf4j.LoggerFactory

class TAGMLWorkspaceService : WorkspaceService {
    private val logger = LoggerFactory.getLogger(this.javaClass)!!

    override fun didChangeWatchedFiles(params: DidChangeWatchedFilesParams?) {
        logger.info("didChangeWatchedFiles: params = $params")
    }

    override fun didChangeConfiguration(params: DidChangeConfigurationParams?) {
        logger.info("didChangeConfiguration: params = $params")
    }

}
