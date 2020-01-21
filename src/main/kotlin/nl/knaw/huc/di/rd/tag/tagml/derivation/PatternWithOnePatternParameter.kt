package nl.knaw.huc.di.rd.tag.tagml.derivation

import com.google.common.base.Preconditions

abstract class PatternWithOnePatternParameter internal constructor(val pattern: Pattern) : AbstractPattern() {

    init {
        Preconditions.checkNotNull(pattern)
        Companion.setHashcode(this, javaClass.hashCode() * pattern.hashCode())
    }

    override fun init() {
        nullable = pattern.isNullable
        allowsText = pattern.allowsText()
        allowsAnnotations = pattern.allowsText()
        onlyAnnotations = pattern.onlyAnnotations()
    }
}
