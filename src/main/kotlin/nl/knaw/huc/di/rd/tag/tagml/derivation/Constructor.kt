package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.NOT_ALLOWED
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.NotAllowed

object Constructor {

    fun notAllowed(): Expectation {
        return NOT_ALLOWED
    }

    fun empty(): Expectation {
        return EMPTY
    }

    fun after(e1: Expectation, e2: Expectation): Expectation {
        if (e1 is NotAllowed || e2 is NotAllowed) {
            return notAllowed()
        }

        if (e1 is Empty) {
            return e2
        }

        if (e1 is After) {
            val p1 = e1.expectation1
            val p2 = e1.expectation2
            return after(p1, after(p2, e2))
        }

        return After(e1, e2)
    }
}
