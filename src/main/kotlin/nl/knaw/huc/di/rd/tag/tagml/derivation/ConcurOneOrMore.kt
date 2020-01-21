package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.anyContent
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.text

class ConcurOneOrMore(pattern: Pattern) : PatternWithOnePatternParameter(pattern) {

    override fun textDeriv(s: String): Pattern {
        //For ConcurOneOrMore, we partially expand the ConcurOneOrMore into a Concur. This mirrors the derivative for
        // OneOrMore, except that a new Concur pattern is constructed rather than a Group, and the second sub-pattern is a
        // choice between a ConcurOneOrMore and Text.

        //textDeriv cx (ConcurOneOrMore p) s =
        //  concur (textDeriv cx p s)
        //         (choice (ConcurOneOrMore p) Text)
        return concur(
                pattern.textDeriv(s),
                choice(ConcurOneOrMore(pattern), text())
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (ConcurOneOrMore p) qn id =
        //   concur (startTagDeriv p qn id)
        //          (choice (ConcurOneOrMore p) anyContent)
        return concur(
                pattern.startTagDeriv(id),
                choice(ConcurOneOrMore(pattern), anyContent())
        )
    }

    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (ConcurOneOrMore p) qn id =
        //   concur (endTagDeriv p qn id)
        //          (choice (ConcurOneOrMore p) anyContent)
        return concur(
                pattern.endTagDeriv(id),
                choice(ConcurOneOrMore(pattern), anyContent())
        )
    }
}
