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

    class Empty : Expectation {
        override fun toString(): String {
            return "<empty/>"
        }
    }

    class NotAllowed : Expectation {
        override fun toString(): String {
            return "<notAllowed/>"
        }
    }

    class EOF : Expectation {
        override fun toString(): String {
            return "<eof/>"
        }
    }

    class Range(val id: TagIdentifier, val expectation: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return after(
                    expectation,
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

        override fun toString(): String {
            return """<range id="$id">$expectation</range>"""
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

        override fun toString(): String {
            return """<rangeOpen id="$id">"""
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

        override fun toString(): String {
            return """<rangeClose id="$id"/>"""
        }

    }

    class Text : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return (t is TextToken)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return empty()
        }

        override fun toString(): String {
            return "<text/>"
        }

    }

    // combinators
    class After(val expectation1: Expectation, val expectation2: Expectation) : Expectation {
        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t)
//            return if (expectation1.nullable)
//                expectation2.matches(t)
//            else
//                expectation1.matches(t)

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

        override fun toString(): String {
            return "<after>$expectation1$expectation2</after>"
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

        override fun toString(): String {
            return if (expectation1 is OneOrMore && expectation2 is Empty)
                "<zeroOrMore>${expectation1.expectation}</zeroOrMore>"
            else
                "<choice>$expectation1$expectation2</choice>"
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

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return group(
                    expectation.startTokenDeriv(s),
                    choice(OneOrMore(expectation), empty())
            )
        }

        override fun endTokenDeriv(e: EndTagToken): Expectation {
            return group(
                    expectation.endTokenDeriv(e),
                    choice(OneOrMore(expectation), empty())
            )
        }

        override fun toString(): String {
            return "<oneOrMore>$expectation</oneOrMore>"
        }

    }

    class Group(val expectation1: Expectation, val expectation2: Expectation) : Expectation {

        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Expectation {
            return expectation2
        }

        override fun startTokenDeriv(s: StartTagToken): Expectation {
            return expectation2
        }

        override fun endTokenDeriv(s: EndTagToken): Expectation {
            return expectation2
        }

        override fun toString(): String {
            return "<group>$expectation1$expectation2</group>"
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

        override fun toString(): String {
            return "<not>$expectation</not>"
        }

    }


}