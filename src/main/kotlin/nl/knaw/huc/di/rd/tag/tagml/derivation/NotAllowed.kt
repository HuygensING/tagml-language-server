package nl.knaw.huc.di.rd.tag.tagml.derivation

open class NotAllowed : PatternWithoutParameters() {
    override fun toString(): String {
        return "NotAllowed()"
    }

    override fun init() {
        nullable = false
        allowsText = false
        allowsAnnotations = false
        onlyAnnotations = false
    }
}
