package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import org.eclipse.lsp4j.Range
import java.net.URL

abstract class TAGMLToken {
    abstract val content: String
}

data class LSPToken(val token: TAGMLToken, val range: Range) {
    //    A range in a text document expressed as (zero-based) start and end positions.
    //    A range is comparable to a selection in an editor. Therefore the end position is exclusive.
    //    If you want to specify a range that contains a line including the line ending character(s)
    //    then use an end position denoting the start of the next line.
    private val lazySerialized: String by lazy {
        "$token at [(${range.start.line},${range.start.character})-(${range.end.line},${range.end.character})]"
    }

    override fun toString(): String = lazySerialized
}

class SchemaLocationToken(private val url: URL) : TAGMLToken() {
    override val content: String by lazy { "[!schema $url]" }

    private val lazySerialized: String by lazy { "SchemaLocation($url)" }
    override fun toString() = lazySerialized
}

class NameSpaceIdentifierToken(private val id: String, private val url: URL) : TAGMLToken() {
    override val content: String by lazy { "[!ns $id $url]" }

    private val lazySerialized: String by lazy { "NameSpace($id,$url)" }
    override fun toString() = lazySerialized
}

class StartTagToken(val tagName: String) : TAGMLToken() {
    override val content: String by lazy { "[$tagName>" }

    val lazySerialized by lazy { "Start($tagName)" }
    override fun toString(): String {
        return lazySerialized
    }

    override fun hashCode(): Int {
        return this.javaClass.hashCode() + tagName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is StartTagToken && other.tagName == tagName
    }
}

class EndTagToken(val tagName: String) : TAGMLToken() {
    override fun toString() = "End($tagName)"
    override val content: String
        get() = "<$tagName]"

    override fun hashCode(): Int {
        return this.javaClass.hashCode() + tagName.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is EndTagToken && other.tagName == tagName
    }
}

class TextToken(private val textContent: String) : TAGMLToken() {
    override fun toString() = "Text(${textContent.replace("\n", "\\n")})"
    override val content: String
        get() = textContent

    override fun hashCode(): Int {
        return this.javaClass.hashCode() + textContent.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is TextToken && other.textContent == textContent
    }
}

object StartTextVariationToken : TAGMLToken() {
    override fun toString() = "StartTextVariation()"
    override val content: String
        get() = "<|"
}

object EndTextVariationToken : TAGMLToken() {
    override fun toString() = "EndTextVariation()"
    override val content: String
        get() = "|>"
}

object TextVariationSeparatorToken : TAGMLToken() {
    override fun toString() = "TextVariationSeparator()"
    override val content: String
        get() = "|"
}
