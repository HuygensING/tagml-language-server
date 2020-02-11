package nl.knaw.huc.di.rd.tag.tagml.lsp

import arrow.core.Either
import lambdada.parsec.parser.Response
import lambdada.parsec.utils.Location
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TokenIndex
import org.eclipse.lsp4j.Position

class TAGMLDocumentModel(private val uri: String, val text: String, val version: Int) {
    var hasParseFailure: Boolean = false
    var errorPosition: Position? = null
    var errorMessage: String? = null
    var tokens: List<LSPToken>? = null
    private var reject: Response.Reject<Char, List<LSPToken>>? = null
    private var tokenIndex: TokenIndex? = null

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

class PositionCalculator(val tagml: String) {

    internal val lineLengths = mutableListOf<Int>()

    init {
        val lineLengthList = tagml.split("\n").map { it.length }
        this.lineLengths.addAll(lineLengthList)
    }

    fun calculatePosition(location: Location): Position {
        var line = 0
        var total = 0
        while (total <= location.position && line < lineLengths.size) {
            total += lineLengths[line]
            line += 1
        }
        line -= 1
        val character = (location.position - total + lineLengths[line])
        return Position(line, character)
    }
}
