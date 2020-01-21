package nl.knaw.huc.di.rd.tag.tagml.derivation

class All(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable && pattern2.isNullable
        allowsText = if (pattern1.isNullable)
            pattern1.allowsText() || pattern2.allowsText()
        else
            pattern1.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() && pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() || pattern2.onlyAnnotations()
    }
}
