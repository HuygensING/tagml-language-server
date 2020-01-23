package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.notAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

object Expectations {

    val EMPTY: Expectation = Empty()
    val NOT_ALLOWED: Expectation = NotAllowed()
    val TEXT: Expectation = Text()

    class Empty : Expectation

    class NotAllowed : Expectation

    class EOF : Expectation

    class Range(val id: TagIdentifier, val expection: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(
                    expection,
                    RangeClose(FixedIdentifier(s.tagName))
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            val tagName = when (id) {
                is TagIdentifiers.AnyTagIdentifier -> "*"
                is FixedIdentifier -> id.tagName
                else -> "?"
            }
            return listOf(StartTagToken(tagName))
        }
    }

    class RangeOpen(val id: TagIdentifier) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(
                    Not(RangeClose(FixedIdentifier(s.tagName))),
                    RangeClose(FixedIdentifier(s.tagName))
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            val tagName = when (id) {
                is TagIdentifiers.AnyTagIdentifier -> "*"
                is FixedIdentifier -> id.tagName
                else -> "?"
            }
            return listOf(StartTagToken(tagName))
        }
    }

    class RangeClose(val id: TagIdentifier) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is EndTagToken) && id.matches(t.tagName)
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return empty()
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return listOf()
        }

    }

    class Text : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is TextToken)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return empty()
        }

    }

    // combinators
    class After(val expectation1: Expectation, val expectation2: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(expectation1.startTokenDeriv(s), expectation2)
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return after(expectation1.endTokenDeriv(e), expectation2)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return after(expectation1.textTokenDeriv(t), expectation2)
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation1.expectedTokens()
        }
    }

    class Choice(val expectation1: Expectation, val expectation2: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t) || expectation2.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return choice(expectation1.startTokenDeriv(s), expectation2.startTokenDeriv(s))
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return choice(expectation1.endTokenDeriv(e), expectation2.endTokenDeriv(e))
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return choice(expectation1.textTokenDeriv(t), expectation2.textTokenDeriv(t))
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation1.expectedTokens() + expectation2.expectedTokens()
        }
    }

    class OneOrMore(val expectation: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return expectation.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return group(
                    expectation.textTokenDeriv(t),
                    choice(OneOrMore(expectation), empty())
            )
        }

    }

    class Group(val expectation1: Expectation, val expectation2: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t)
        }

    }

    class Not(val expectation: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return !expectation.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return if (expectation.matches(s))
                notAllowed()
            else
                RangeOpen(FixedIdentifier(s.tagName)).startTokenDeriv(s)
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return if (this.expectation.matches(e))
                notAllowed()
            else
                RangeClose(FixedIdentifier(e.tagName)).endTokenDeriv(e)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return if (expectation.matches(t))
                notAllowed()
            else
                Text().textTokenDeriv(t)
        }

        override fun expectedTokens(): List<TAGMLToken> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }


}