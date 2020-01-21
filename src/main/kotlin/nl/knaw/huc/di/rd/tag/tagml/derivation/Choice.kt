package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import java.util.function.Function

class Choice(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable || pattern2.isNullable
        allowsText = pattern1.allowsText() || pattern2.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(s: String): Pattern {
        // textDeriv cx (Choice p1 p2) s =
        //  choice (textDeriv cx p1 s) (textDeriv cx p2 s)
        return choice(
                pattern1.textDeriv(s),
                pattern2.textDeriv(s)
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (Choice p1 p2) qn id =
        //   choice (startTagDeriv p1 qn id)
        //          (startTagDeriv p2 qn id)
        return choice(
                pattern1.startTagDeriv(id),
                pattern2.startTagDeriv(id)
        )
    }


    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (Choice p1 p2) qn id =
        //   choice (endTagDeriv p1 qn id)
        //          (endTagDeriv p2 qn id)
        return choice(
                pattern1.endTagDeriv(id),
                pattern2.endTagDeriv(id)
        )
    }


    override fun applyAfter(f: Function<Pattern, Pattern>): Pattern {
        return choice(
                pattern1.applyAfter(f),
                pattern2.applyAfter(f)
        )
    }

}
