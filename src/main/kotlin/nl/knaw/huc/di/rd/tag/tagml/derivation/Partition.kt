package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.empty

class Partition(pattern: Pattern) : PatternWithOnePatternParameter(pattern) {

    override fun textDeriv(s: String): Pattern {
        //For Partition, we create an After pattern that contains the derivative.

        //textDeriv cx (Partition p) s =
        //  after (textDeriv cx p s) Empty
        return after(
                pattern.textDeriv(s),
                empty()
        )
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (Partition p) qn id =
        //   after (startTagDeriv p qn id) Empty
        return after(
                pattern.startTagDeriv(id),
                empty()
        )
    }

    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (Partition p) qn id =
        //   after (endTagDeriv p qn id)
        //         Empty
        return after(
                pattern.endTagDeriv(id),
                empty()
        )
    }
}