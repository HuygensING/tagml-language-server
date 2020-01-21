package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group

class OneOrMore(pattern: Pattern) : PatternWithOnePatternParameter(pattern) {

    override fun textDeriv(s: String): Pattern {
        // textDeriv cx (OneOrMore p) s =
        //   group (textDeriv cx p s)
        //         (choice (OneOrMore p) Empty)
        return group(
                pattern.textDeriv(s),
                choice(OneOrMore(pattern), empty())
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (OneOrMore p) qn id =
        //   group (startTagDeriv p qn id)
        //         (choice (OneOrMore p) Empty)
        return group(
                pattern.startTagDeriv(id),
                choice(OneOrMore(pattern), empty())
        )
    }

    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (OneOrMore p) qn id =
        //   group (endTagDeriv p qn id)
        //         (choice (OneOrMore p) Empty)
        return group(
                pattern.endTagDeriv(id),
                choice(OneOrMore(pattern), empty())
        )
    }
}
