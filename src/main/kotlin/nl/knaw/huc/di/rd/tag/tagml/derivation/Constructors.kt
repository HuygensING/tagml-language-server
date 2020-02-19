package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.All
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.ConcurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.OneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Text
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier

object Constructors {

    fun anyContent(): Pattern = Text // might not cover it

    private fun layer(): Pattern = Range(AnyTagIdentifier, choice(Text, layer()))

    internal fun mixed(pattern: Pattern): Pattern = interleave(Text, pattern)

    fun zeroOrMore(pattern: Pattern): Pattern = choice(oneOrMore(pattern), Empty)

    fun after(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return NotAllowed

        if (pattern1 is Empty) return pattern2

        if (pattern1 is After) {
            val p1 = pattern1.pattern1
            val p2 = pattern1.pattern2
            return after(p1, after(p2, pattern2))
        }

        return After(pattern1, pattern2)
    }

    fun all(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return NotAllowed

        if (pattern2 is Empty) {
            return if (pattern1.nullable)
                Empty
            else
                NotAllowed
        }

        if (pattern1 is Empty) {
            return if (pattern2.nullable)
                Empty
            else
                NotAllowed
        }

        if (pattern1 is After && pattern2 is After) {
            val e1 = pattern1.pattern1
            val e2 = pattern1.pattern2
            val e3 = pattern2.pattern1
            val e4 = pattern2.pattern2
            return after(all(e1, e3), all(e2, e4))
        }

        return All(pattern1, pattern2)
    }

    fun choice(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 == pattern2) return pattern1
        if (pattern1 is NotAllowed) return pattern2
        if (pattern2 is NotAllowed) return pattern1
        return Choice(pattern1, pattern2)
    }

    private fun oneOrMore(pattern: Pattern): Pattern {
        return if (pattern is NotAllowed || pattern is Empty)
            pattern
        else
            OneOrMore(pattern)
    }

    fun concurOneOrMore(pattern: Pattern): Pattern {
        return if (pattern is NotAllowed || pattern is Empty)
            pattern
        else
            ConcurOneOrMore(pattern)
    }

    fun concur(pattern1: Pattern, pattern2: Pattern): Pattern {
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return NotAllowed
        if (pattern1 is Text) return pattern2
        if (pattern2 is Text) return pattern1

        if (pattern1 is After && pattern2 is After) {
            val e1 = pattern1.pattern1
            val e2 = pattern1.pattern2
            val e3 = pattern2.pattern1
            val e4 = pattern2.pattern2
            return after(all(e1, e3), concur(e2, e4))
        }

        if (pattern1 is After) {
            val e1 = pattern1.pattern1
            val e2 = pattern1.pattern2
            return after(e1, concur(e2, pattern2))
        }

        if (pattern2 is After) {
            val e2 = pattern2.pattern1
            val e3 = pattern2.pattern2
            return after(e2, concur(pattern1, e3))
        }

        // Concur(Concur(P1,P2),P2) = Concur(P1,P2)
        if (pattern1 is Concur && (pattern1.pattern1 == pattern2 || pattern1.pattern2 == pattern2))
            return pattern1

        if (pattern2 is Concur && (pattern2.pattern1 == pattern1 || pattern2.pattern2 == pattern1))
            return pattern2

        return Concur(pattern1, pattern2)
    }

    fun group(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  group p NotAllowed = NotAllowed
        //  group NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return NotAllowed

        //  group p Empty = p
        if (pattern2 is Empty) return pattern1

        //  group Empty p = p
        if (pattern1 is Empty) return pattern2

        //  group (After p1 p2) p3 = after p1 (group p2 p3)
        if (pattern1 is After) return after(pattern1.pattern1, group(pattern1.pattern2, pattern2))

        //  group p1 (After p2 p3) = after p2 (group p1 p3)
        return if (pattern2 is After) {
            after(pattern2.pattern1, group(pattern1, pattern2.pattern2))
        } else Group(pattern1, pattern2)
        //  group p1 p2 = Group p1 p2
    }

    fun interleave(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  interleave p NotAllowed = NotAllowed
        //  interleave NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) return NotAllowed

        //  interleave p Empty = p
        if (pattern2 is Empty) return pattern1

        //  interleave Empty p = p
        if (pattern1 is Empty) return pattern2

        //  interleave (After p1 p2) p3 = after p1 (interleave p2 p3)
        if (pattern1 is After) return after(pattern1.pattern1, interleave(pattern1.pattern2, pattern2))

        //  interleave p1 (After p2 p3) = after p2 (interleave p1 p3)
        return if (pattern2 is After) {
            after(pattern2.pattern1, interleave(pattern1, pattern2.pattern2))
        } else Interleave(pattern1, pattern2)
        //  interleave p1 p2 = Interleave p1 p2
    }
}
