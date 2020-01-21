package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import java.util.function.Function

class After(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = false
        allowsText = if (pattern1.isNullable)
            pattern1.allowsText() || pattern2.allowsText()
        else
            pattern1.allowsText()
        allowsAnnotations = if (pattern1.isNullable)
            pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        else
            pattern1.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(s: String): Pattern {
        //textDeriv cx (After p1 p2) s =
        //  after (textDeriv cx p1 s) p2
        return after(
                pattern1.textDeriv(s),
                pattern2
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (After p1 p2) qn id =
        //   after (startTagDeriv p1 qn id)
        //         p2
        return after(
                pattern1.startTagDeriv(id),
                pattern2
        )
    }

    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (After p1 p2) qn id =
        //   after (endTagDeriv p1 qn id) p2
        return after(
                pattern1.endTagDeriv(id),
                pattern2
        )
    }

    override fun applyAfter(f: Function<Pattern, Pattern>): Pattern {
        return after(
                pattern1,
                f.apply(pattern2)
        )
    }
}
