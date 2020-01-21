package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructor.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

object Expectations {
    class StartTokenExpectation(val id: TagIdentifier) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(
                    Not(EndTokenExpectation(FixedIdentifier(s.tagName))),
                    EndTokenExpectation(FixedIdentifier(s.tagName))
            )
        }
    }

    class EndTokenExpectation(val id: TagIdentifier) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is EndTagToken) && id.matches(t.tagName)
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return Empty()
        }
    }

    class AfterExpectation(val e1: Expectation, val e2: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return e1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(e1.startTokenDeriv(s), e2)
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return after(e1.endTokenDeriv(e), e2)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return after(e1.textTokenDeriv(t), e2)
        }
    }


//    class AnyExpectation() : Expectation {
//        override fun matches(t: TAGMLToken): Boolean {
//            return true
//        }
//
//        override fun startTokenDeriv(s: StartTagToken): Expectation {
//            return StartTokenExpectation(FixedIdentifier(s.tagName)).startTokenDeriv(s)
//        }
//
//        override fun endTokenDeriv(s: EndTagToken): Expectation {
//            return EndTokenExpectation(FixedIdentifier(s.tagName)).endTokenDeriv(s)
//        }
//
//        override fun textTokenDeriv(t: TextToken): Expectation {
//            return TextExpectation().textTokenDeriv(t)
//        }
//    }

    class Not(val e: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return !e.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return if (e.matches(s))
                Unexpected()
            else
                StartTokenExpectation(FixedIdentifier(s.tagName)).startTokenDeriv(s)
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return if (this.e.matches(e))
                Unexpected()
            else
                EndTokenExpectation(FixedIdentifier(e.tagName)).endTokenDeriv(e)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return if (e.matches(t))
                Unexpected()
            else
                TextExpectation().textTokenDeriv(t)
        }
    }

    class TextExpectation() : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is TextToken)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return Empty()
        }
    }

    class Empty() : Expectation

    class Unexpected() : Expectation

    class EOFExpectation() : Expectation

}