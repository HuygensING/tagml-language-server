package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group

class Group(pattern1: Pattern, pattern2: Pattern) : PatternWithTwoPatternParameters(pattern1, pattern2) {

    override fun init() {
        nullable = pattern1.isNullable && pattern2.isNullable
        allowsText = if (pattern1.isNullable)
            pattern1.allowsText() || pattern2.allowsText()
        else
            pattern1.allowsText()
        allowsAnnotations = pattern1.allowsAnnotations() || pattern2.allowsAnnotations()
        onlyAnnotations = pattern1.onlyAnnotations() && pattern2.onlyAnnotations()
    }

    override fun textDeriv(s: String): Pattern {
        //textDeriv cx (Group p1 p2) s =
        //  let p = group (textDeriv cx p1 s) p2
        //  in if nullable p1 then choice p (textDeriv cx p2 s)
        //                    else p
        val p = group(pattern1.textDeriv(s), pattern2)
        return if (pattern1.isNullable)
            choice(p, pattern2.textDeriv(s))
        else
            p
    }

    override fun startTagDeriv(id: TagIdentifier): Pattern {
        // startTagDeriv (Group p1 p2) qn id =
        //   let d = group (startTagDeriv p1 qn id) p2
        //   in if nullable p1 then choice d (startTagDeriv p2 qn id)
        //                     else d
        val d = group(pattern1.startTagDeriv(id), pattern2)
        return if (pattern1.isNullable)
            choice(d, pattern2.startTagDeriv(id))
        else
            d
    }

    override fun endTagDeriv(id: TagIdentifier): Pattern {
        // endTagDeriv (Group p1 p2) qn id =
        //   let p = group (endTagDeriv p1 qn id) p2
        //   if nullable p1 then choice p
        //                             (endTagDeriv p2 qn id)
        //                  else p
        val p = group(pattern1.endTagDeriv(id), pattern2)
        return if (pattern1.isNullable)
            choice(p, pattern2.endTagDeriv(id))
        else
            p
    }


}
