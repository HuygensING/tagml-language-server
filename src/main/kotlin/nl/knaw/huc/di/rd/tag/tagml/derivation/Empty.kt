package nl.knaw.huc.di.rd.tag.tagml.derivation

class Empty : PatternWithoutParameters() {
    override fun toString(): String {
        return "Empty()"
    }

    override fun init() {
        nullable = true
        allowsText = false
        allowsAnnotations = false
        onlyAnnotations = false
    }
}
