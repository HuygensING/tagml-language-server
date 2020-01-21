package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur

class Concur(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable && pattern2.isNullable
        allowsText = pattern1.allowsText() && pattern2.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(s: String): Pattern {
        //For Concur, text is only allowed if it is allowed by both of the sub-patterns: we create a new Concur whose
        // sub-patterns are the derivatives of the original sub-patterns.

        //textDeriv cx (Concur p1 p2) s =
        //  concur (textDeriv cx p1 s)
        //         (textDeriv cx p2 s)
        return concur(
                pattern1.textDeriv(s),
                pattern2.textDeriv(s)
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (Concur p1 p2) qn id =
        //   let d1 = startTagDeriv p1 qn id
        //       d2 = startTagDeriv p2 qn id
        //   in choice (choice (concur d1 p2) (concur p1 d2))
        //             (concur d1 d2)
        val d1 = pattern1.startTagDeriv(id)
        val d2 = pattern2.startTagDeriv(id)
        return choice(
                choice(
                        concur(d1, pattern2),
                        concur(pattern1, d2)
                ),
                concur(d1, d2)
        )
    }

    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (Concur p1 p2) qn id =
        //   let d1 = endTagDeriv p1 qn id
        //       d2 = endTagDeriv p2 qn id
        //   in choice (choice (concur d1 p2)
        //                     (concur p1 d2))
        //             (concur d1 d2)
        val d1 = pattern1.endTagDeriv(id)
        val d2 = pattern2.endTagDeriv(id)
        return choice(
                choice(
                        concur(d1, pattern2),
                        concur(pattern1, d2)
                ),
                concur(d1, d2)
        )
    }
}
