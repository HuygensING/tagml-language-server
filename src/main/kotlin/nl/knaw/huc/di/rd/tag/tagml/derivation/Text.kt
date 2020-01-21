package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.text

open class Text : PatternWithoutParameters() {
    override fun init() {
        nullable = true
        allowsText = true
        allowsAnnotations = false
        onlyAnnotations = false
    }

    override fun textDeriv(s: String): Pattern {
        //textDeriv cx Text _ = Text
        return text()
    }

    override fun toString(): String {
        return "Text()"
    }
}
