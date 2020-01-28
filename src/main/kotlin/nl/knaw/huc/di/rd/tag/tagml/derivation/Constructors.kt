package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.All
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.ConcurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NOT_ALLOWED
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.OneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.TEXT
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Text

object Constructors {

    private fun notAllowed(): Pattern {
        return NOT_ALLOWED
    }

    fun empty(): Pattern {
        return EMPTY
    }

    fun text(): Pattern {
        return TEXT
    }

    fun after(expectation1: Pattern, expectation2: Pattern): Pattern {
        if (expectation1 is NotAllowed || expectation2 is NotAllowed) {
            return notAllowed()
        }

        if (expectation1 is Empty) {
            return expectation2
        }

        if (expectation1 is After) {
            val p1 = expectation1.pattern1
            val p2 = expectation1.pattern2
            return after(p1, after(p2, expectation2))
        }

        return After(expectation1, expectation2)
    }

    fun choice(expectation1: Pattern, expectation2: Pattern): Pattern {
        //  choice p NotAllowed = p
        if (expectation1 is NotAllowed) {
            return expectation2
        }
        //  choice NotAllowed p = p
        if (expectation2 is NotAllowed) {
            return expectation1
        }
        //  choice Empty Empty = Empty
        return if (expectation1 is Empty && expectation2 is Empty)
            expectation1
        else
            Choice(expectation1, expectation2)
        //  choice p1 p2 = Choice p1 p2
    }

    fun zeroOrMore(expectation: Pattern): Pattern {
        return choice(oneOrMore(expectation), empty())
    }

    private fun oneOrMore(expectation: Pattern): Pattern {
        return if (expectation is NotAllowed
                || expectation is Empty)
            expectation
        else
            OneOrMore(expectation)
    }

    fun concurOneOrMore(expectation: Pattern): Pattern {
        return if (expectation is NotAllowed
                || expectation is Empty)
            expectation
        else
            ConcurOneOrMore(expectation)
    }

    fun concur(expectation1: Pattern, expectation2: Pattern): Pattern {
        if (expectation1 is NotAllowed || expectation2 is NotAllowed) {
            return notAllowed()
        }

        if (expectation1 is Text) {
            return expectation2
        }

        if (expectation2 is Text) {
            return expectation1
        }

        if (expectation1 is After && expectation2 is After) {
            val e1 = expectation1.pattern1
            val e2 = expectation1.pattern2
            val e3 = expectation2.pattern1
            val e4 = expectation2.pattern2
            return after(all(e1, e3), concur(e2, e4))
        }

        if (expectation1 is After) {
            val e1 = expectation1.pattern1
            val e2 = expectation1.pattern2
            return after(e1, concur(e2, expectation2))
        }

        if (expectation2 is After) {
            val e2 = expectation2.pattern1
            val e3 = expectation2.pattern2
            return after(e2, concur(expectation1, e3))
        }

        return Concur(expectation1, expectation2)
    }

    private fun all(expectation1: Pattern, expectation2: Pattern): Pattern {

        if (expectation1 is NotAllowed || expectation2 is NotAllowed) {
            return notAllowed()
        }

        if (expectation2 is Empty) {
            return if (expectation1.nullable)
                empty()
            else
                notAllowed()
        }

        if (expectation1 is Empty) {
            return if (expectation2.nullable)
                empty()
            else
                notAllowed()
        }

        if (expectation1 is After && expectation2 is After) {
            val e1 = expectation1.pattern1
            val e2 = expectation1.pattern2
            val e3 = expectation2.pattern1
            val e4 = expectation2.pattern2
            return after(all(e1, e3), all(e2, e4))
        }

        return All(expectation1, expectation2)

    }

    fun group(expectation1: Pattern, expectation2: Pattern): Pattern {
        //  group p NotAllowed = NotAllowed
        //  group NotAllowed p = NotAllowed
        if (expectation1 is NotAllowed || expectation2 is NotAllowed) {
            return notAllowed()
        }
        //  group p Empty = p
        if (expectation2 is Empty) {
            return expectation1
        }
        //  group Empty p = p
        if (expectation1 is Empty) {
            return expectation2
        }
        //  group (After p1 p2) p3 = after p1 (group p2 p3)
        if (expectation1 is After) {
            return after(expectation1.pattern1, group(expectation1.pattern2, expectation2))
        }
        //  group p1 (After p2 p3) = after p2 (group p1 p3)
        return if (expectation2 is After) {
            after(expectation2.pattern1, group(expectation1, expectation2.pattern2))
        } else Group(expectation1, expectation2)
        //  group p1 p2 = Group p1 p2
    }

    fun anyContent(): Pattern = text() // might not cover it

}
