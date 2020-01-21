package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.interleave

class Interleave(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable && pattern2.isNullable
        allowsText = pattern1.allowsText() || pattern2.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(s: String): Pattern {
        //textDeriv cx (Interleave p1 p2) s =
        //  choice (interleave (textDeriv cx p1 s) p2)
        //         (interleave p1 (textDeriv cx p2 s))
        return choice(
                interleave(pattern1.textDeriv(s), pattern2),
                interleave(pattern1, pattern2.textDeriv(s))
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (Interleave p1 p2) qn id =
        //   choice (interleave (startTagDeriv p1 qn id) p2)
        //          (interleave p1 (startTagDeriv p2 qn id))
        return choice(
                interleave(pattern1.startTagDeriv(id), pattern2),
                interleave(pattern1, pattern2.startTagDeriv(id))
        )
    }


    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (Interleave p1 p2) qn id =
        //   choice (interleave (endTagDeriv p1 qn id) p2)
        //          (interleave p1 (endTagDeriv p2 qn id))
        return choice(
                interleave(pattern1.endTagDeriv(id), pattern2),
                interleave(pattern1, pattern2.endTagDeriv(id))
        )
    }
}
