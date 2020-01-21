package nl.knaw.huc.di.rd.tag.tagml.derivation

import com.google.common.base.Preconditions

abstract class AbstractPattern : Pattern {
    var nullable: Boolean? = null
    var allowsText: Boolean? = null
    var allowsAnnotations: Boolean? = null
    var onlyAnnotations: Boolean? = null

    var hashcode = javaClass.hashCode()

    override
    val isNullable: Boolean
        get() {
            if (nullable == null) {
                init()
                if (nullable == null) {
                    throw RuntimeException("nullable == null! Make sure nullable is initialized in the init() of " + javaClass.simpleName)
                }
            }
            return nullable!!
        }

    internal abstract fun init()

    override fun allowsText(): Boolean {
        if (allowsText == null) {
            init()
            if (allowsText == null) {
                throw RuntimeException("allowsText == null! Make sure allowsText is initialized in the init() of "
                        + javaClass.simpleName)
            }
        }
        return allowsText!!
    }

    override fun allowsAnnotations(): Boolean {
        if (allowsAnnotations == null) {
            init()
            if (allowsAnnotations == null) {
                throw RuntimeException("allowsAnnotations == null! Make sure allowsAnnotations is initialized in the init() of "
                        + javaClass.simpleName)
            }
        }
        return allowsAnnotations!!
    }

    override fun onlyAnnotations(): Boolean {
        if (onlyAnnotations == null) {
            init()
            if (onlyAnnotations == null) {
                throw RuntimeException("onlyAnnotations == null! Make sure onlyAnnotations is initialized in the init() of "
                        + javaClass.simpleName)
            }
        }
        return onlyAnnotations!!
    }

    override fun hashCode(): Int {
        return hashcode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractPattern) return false

        if (nullable != other.nullable) return false
        if (allowsText != other.allowsText) return false
        if (allowsAnnotations != other.allowsAnnotations) return false
        if (onlyAnnotations != other.onlyAnnotations) return false
        if (hashcode != other.hashcode) return false

        return true
    }

    companion object {
        fun setHashcode(abstractPattern: AbstractPattern, hashcode: Int) {
            Preconditions.checkState(hashcode != 0, "hashCode should not be 0!")
            abstractPattern.hashcode = hashcode
        }
    }
}
