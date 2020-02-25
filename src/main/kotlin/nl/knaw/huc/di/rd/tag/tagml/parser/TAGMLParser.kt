package nl.knaw.huc.di.rd.tag.tagml.parser

import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken

data class WellFormednessResult(
        val isWellFormed: Boolean,
        val errors: List<String>,
        val expectedTokens: Set<TAGMLToken>
)

class TAGMLParser {
    companion object {
        fun checkWellFormedness(tagml: List<LSPToken>): WellFormednessResult {
            TODO()
        }
    }
}