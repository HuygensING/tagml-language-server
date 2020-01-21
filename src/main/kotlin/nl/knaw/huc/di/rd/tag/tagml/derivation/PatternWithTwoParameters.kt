package nl.knaw.huc.di.rd.tag.tagml.derivation

import com.google.common.base.Preconditions

abstract class PatternWithTwoPatternParameters internal constructor(val pattern1: Pattern, val pattern2: Pattern) : AbstractPattern() {

    init {
        Preconditions.checkNotNull(pattern1)
        Preconditions.checkNotNull(pattern2)
        Companion.setHashcode(this, javaClass.hashCode() + pattern1.hashCode() * pattern2.hashCode())
    }

    override fun equals(other: Any?): Boolean {
        return (other!!.javaClass == this.javaClass
                && pattern1 == (other as PatternWithTwoPatternParameters).pattern1
                && pattern2 == other.pattern2)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + pattern1.hashCode()
        result = 31 * result + pattern2.hashCode()
        return result
    }
}
