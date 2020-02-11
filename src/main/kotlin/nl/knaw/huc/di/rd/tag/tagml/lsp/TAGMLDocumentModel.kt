package nl.knaw.huc.di.rd.tag.tagml.lsp

import arrow.core.Either
import lambdada.parsec.parser.Response
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TokenIndex
import org.eclipse.lsp4j.Position

class TAGMLDocumentModel(private val uri: String, val text: String, val version: Int) {
    var hasParseFailure: Boolean = false
    var errorPosition: Position? = null
    var errorMessage: String? = null
    var tokens: List<LSPToken>? = null
    var tokenIndex: TokenIndex? = null
    private var reject: Response.Reject<Char, List<LSPToken>>? = null

    init {
        when (val result = tokenize(text)) {
            is Either.Left -> onFailure(result.a, text)
            is Either.Right -> onSuccess(result.b)
            else -> throw RuntimeException("unexpected result: $result")
        }
    }

    private fun onSuccess(tokenList: List<LSPToken>) {
        this.tokens = tokenList
        this.tokenIndex = TokenIndex(uri, tokenList)
    }

    private fun onFailure(reject: Response.Reject<Char, List<LSPToken>>, text: String) {
        this.reject = reject
        this.hasParseFailure = true
        val pc = PositionCalculator(text)
        errorPosition = pc.calculatePosition(reject.location)
        errorMessage = "Parsing error (TODO:details!)"
    }
}

