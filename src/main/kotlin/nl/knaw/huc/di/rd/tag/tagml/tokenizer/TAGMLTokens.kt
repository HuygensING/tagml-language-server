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

class StartMarkupToken(val tagName: String) : TAGMLToken() {
    override val content: String by lazy { "[$tagName>" }

    private val lazySerialized by lazy { "Start($tagName)" }
    override fun toString(): String = lazySerialized

    private val lazyHashCode: Int by lazy { this.javaClass.hashCode() + tagName.hashCode() }
    override fun hashCode(): Int = lazyHashCode

    override fun equals(other: Any?): Boolean = other is StartMarkupToken && other.tagName == tagName
}

class ResumeMarkupToken(val tagName: String) : TAGMLToken() {
    override val content: String by lazy { "[+$tagName>" }

    private val lazySerialized by lazy { "Resume($tagName)" }
    override fun toString(): String = lazySerialized

    private val lazyHashCode: Int by lazy { this.javaClass.hashCode() + tagName.hashCode() }
    override fun hashCode(): Int = lazyHashCode

    override fun equals(other: Any?): Boolean = other is ResumeMarkupToken && other.tagName == tagName
}

class EndMarkupToken(val tagName: String) : TAGMLToken() {
    override val content: String by lazy { "<$tagName]" }

    private val lazySerialized: String by lazy { "End($tagName)" }
    override fun toString() = lazySerialized

    private val lazyHashCode: Int by lazy { this.javaClass.hashCode() + tagName.hashCode() }
    override fun hashCode(): Int = lazyHashCode

    override fun equals(other: Any?): Boolean = other is EndMarkupToken && other.tagName == tagName
}

class SuspendMarkupToken(val tagName: String) : TAGMLToken() {
    override val content: String by lazy { "<-$tagName]" }

    private val lazySerialized: String by lazy { "Suspend($tagName)" }
    override fun toString() = lazySerialized

    private val lazyHashCode: Int by lazy { this.javaClass.hashCode() + tagName.hashCode() }
    override fun hashCode(): Int = lazyHashCode

    override fun equals(other: Any?): Boolean = other is SuspendMarkupToken && other.tagName == tagName
}

class TextToken(private val textContent: String) : TAGMLToken() {
    override val content: String = textContent

    private val lazySerialized: String by lazy { "Text(${textContent.replace("\n", "\\n")})" }
    override fun toString() = lazySerialized

    private val lazyHashCode: Int by lazy { this.javaClass.hashCode() + textContent.hashCode() }
    override fun hashCode(): Int = lazyHashCode

    override fun equals(other: Any?): Boolean = other is TextToken && other.textContent == textContent
}

object StartTextVariationToken : TAGMLToken() {
    override val content: String = "<|"
    override fun toString() = "StartTextVariation()"
}

object EndTextVariationToken : TAGMLToken() {
    override val content: String = "|>"
    override fun toString() = "EndTextVariation()"
}

object TextVariationSeparatorToken : TAGMLToken() {
    override val content: String = "|"
    override fun toString() = "TextVariationSeparator()"
}
