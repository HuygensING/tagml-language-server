package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.NOT_ALLOWED
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.OneOrMore

object Constructors {

    fun notAllowed(): Expectation {
        return NOT_ALLOWED
    }

    fun empty(): Expectation {
        return EMPTY
    }

    fun after(expectation1: Expectation, expectation2: Expectation): Expectation {
        if (expectation1 is NotAllowed || expectation2 is NotAllowed) {
            return notAllowed()
        }

        if (expectation1 is Empty) {
            return expectation2
        }

        if (expectation1 is After) {
            val p1 = expectation1.expectation1
            val p2 = expectation1.expectation2
            return after(p1, after(p2, expectation2))
        }

        return After(expectation1, expectation2)
    }

    fun choice(expectation1: Expectation, expectation2: Expectation): Expectation {
        //  choice p NotAllowed = p
        if (expectation1 is NotAllowed) {
            return expectation2
        }
        //  choice NotAllowed p = p
        if (expectation2 is NotAllowed) {
            return expectation1
        }
        //  choice Empty Empty = Empty
        return if (expectation1 is Empty && expectation2 is Empty) {
            expectation1
        } else Choice(expectation1, expectation2)
        //  choice p1 p2 = Choice p1 p2
    }

    fun zeroOrMore(expectation: Expectation): Expectation {
        return choice(oneOrMore(expectation), empty())
    }

    private fun oneOrMore(expectation: Expectation): Expectation {
        return if (expectation is NotAllowed
                || expectation is Empty) {
            expectation
        } else OneOrMore(expectation)
    }

    fun group(expectation1: Expectation, expectation2: Expectation): Expectation {
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
            return after(expectation1.expectation1, group(expectation1.expectation2, expectation2))
        }
        //  group p1 (After p2 p3) = after p2 (group p1 p3)
        return if (expectation2 is After) {
            after(expectation2.expectation1, group(expectation1, expectation2.expectation2))
        } else Group(expectation1, expectation2)
        //  group p1 p2 = Group p1 p2
    }

}
