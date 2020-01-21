package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.notAllowed
import java.util.function.Function

interface Pattern {

    val isNullable: Boolean

    fun allowsText(): Boolean

    fun allowsAnnotations(): Boolean

    fun onlyAnnotations(): Boolean

    fun textDeriv(s: String): Pattern {
        // No other patterns can match a text event; the default is specified as
        // textDeriv _ _ _ = NotAllowed
        return notAllowed()
    }

    fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv _ _ _ = NotAllowed
        return notAllowed()
    }

    fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv _ _ _ = NotAllowed
        return notAllowed()
    }

    fun applyAfter(f: Function<Pattern, Pattern>): Pattern {
        return notAllowed()
    }

    fun flip(): Pattern {
        if (this !is PatternWithTwoPatternParameters) {
            return this
        }
        val p0 = this
        val p1 = p0.pattern1
        val p2 = p0.pattern2
        try {
            val constructor = javaClass
                    .getConstructor(Pattern::class.java, Pattern::class.java)
            return constructor.newInstance(p2, p1)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

}