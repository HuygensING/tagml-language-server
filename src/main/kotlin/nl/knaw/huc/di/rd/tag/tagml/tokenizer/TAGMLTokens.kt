package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import java.net.URL

abstract class TAGMLToken {
    abstract val content: String
}

class SchemaLocationToken(private val url: URL) : TAGMLToken() {
    override fun toString() = "SchemaLocation($url)"
    override val content: String
        get() = "[!schema $url]"
}

class NameSpaceIdentifierToken(private val id: String, private val url: URL) : TAGMLToken() {
    override fun toString() = "NameSpace($id,$url)"
    override val content: String
        get() = "[!ns $id $url]"
}

class StartTagToken(val tagName: String) : TAGMLToken() {
    override fun toString() = "Start($tagName)"
    override val content: String
        get() = "[$tagName>"

}

class EndTagToken(val tagName: String) : TAGMLToken() {
    override fun toString() = "End($tagName)"
    override val content: String
        get() = "<$tagName]"
}

class TextToken(private val textContent: String) : TAGMLToken() {
    override fun toString() = "Text(${textContent.replace("\n", "\\n")})"
    override val content: String
        get() = textContent
}

class StartTextVariationToken : TAGMLToken() {
    override fun toString() = "StartTextVariation()"
    override val content: String
        get() = "<|"
}

class EndTextVariationToken : TAGMLToken() {
    override fun toString() = "EndTextVariation()"
    override val content: String
        get() = "|>"
}

class TextVariationSeparatorToken : TAGMLToken() {
    override fun toString() = "TextVariationSeparator()"
    override val content: String
        get() = "|"
}
