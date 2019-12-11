package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import java.net.URL

open class TAGMLToken {}

class SchemaLocationToken(val url: URL) : TAGMLToken() {
    override fun toString() = "SchemaLocation($url)"
}

class StartTagToken(val tagName: String) : TAGMLToken() {
    override fun toString() = "Start($tagName)"
}

class EndTagToken(val tagName: String) : TAGMLToken() {
    override fun toString() = "End($tagName)"
}

class TextToken(val content: String) : TAGMLToken() {
    override fun toString() = "Text(${content.replace("\n", "\\n")})"
}

