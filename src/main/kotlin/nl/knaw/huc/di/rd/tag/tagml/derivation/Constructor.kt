package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.AfterExpectation
import nl.knaw.huc.di.rd.tag.tagml.derivation.Expectations.Empty

object Constructor {
    fun after(e1: Expectation, e2: Expectation): Expectation {
        return if (e1 is Empty)
            e2
        else
            AfterExpectation(e1, e2)
    }

}
