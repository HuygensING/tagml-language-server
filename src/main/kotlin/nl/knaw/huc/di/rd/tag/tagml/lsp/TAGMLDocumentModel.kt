package nl.knaw.huc.di.rd.tag.tagml.lsp

import arrow.core.Either
import lambdada.parsec.parser.Response
import lambdada.parsec.utils.Location
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLTokenizer.tokenize
import org.eclipse.lsp4j.Position

class TAGMLDocumentModel(val uri: String, val text: String, val version: Int) {
    var errorMessage: String? = null
    var hasParseFailure: Boolean = false
    private var tokens: List<TAGMLToken>? = null
    private var reject: Response.Reject<Char, List<TAGMLToken>>? = null
    var errorPosition: Position? = null

    init {
        when (val result = tokenize(text)) {
            is Either.Left -> onFailure(result.a, text)
            is Either.Right -> onSuccess(result.b)
            else -> throw RuntimeException("unexpected result: $result")
        }
    }

    private fun onSuccess(tokenList: List<TAGMLToken>) {
        this.tokens = tokenList
    }

    private fun onFailure(reject: Response.Reject<Char, List<TAGMLToken>>, text: String) {
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
        while (total <= location.position && line <= lineLengths.size) {
            total += lineLengths[line]
            line += 1
        }
        line -= 1
        val character = (location.position - total + lineLengths[line])
        return Position(line, character)
    }
}
