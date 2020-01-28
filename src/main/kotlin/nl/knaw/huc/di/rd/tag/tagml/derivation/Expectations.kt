package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.anyContent
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.text
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken
import org.slf4j.LoggerFactory

object Expectations {
    private val _log = LoggerFactory.getLogger(this::class.java)

    val EMPTY: Pattern = Empty()

    val NOT_ALLOWED: Pattern = NotAllowed()

    val TEXT: Pattern = Text()

    class Empty : Pattern {
        override val nullable: Boolean
            get() = true

        override fun toString(): String {
            return "<empty/>"
        }
    }

    class NotAllowed : Pattern {
        override val nullable: Boolean
            get() = false

        override fun toString(): String {
            return "<notAllowed/>"
        }
    }

    class Range(val id: TagIdentifier, val expectation: Pattern) : Pattern {
        override val nullable: Boolean
            get() = false

        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return group(
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

//    class RangeOpen(val id: TagIdentifier) : Expectation {
//        override val nullable: Boolean
//            get() = false
//
//        override fun matches(t: TAGMLToken): Boolean {
//            return (t is StartTagToken) && id.matches(t.tagName)
//        }
//
//        override fun startTokenDeriv(s: StartTagToken): Expectation {
//            return after(
//                    Not(RangeClose(FixedIdentifier(s.tagName))),
//                    RangeClose(FixedIdentifier(s.tagName))
//            )
//        }
//
//        override fun expectedTokens(): List<TAGMLToken> {
//            val tagName = when (id) {
//                is TagIdentifiers.AnyTagIdentifier -> "*"
//                is FixedIdentifier -> id.tagName
//                else -> "?"
//            }
//            return listOf(StartTagToken(tagName))
//        }
//
//        override fun toString(): String {
//            return """<rangeOpen id="$id">"""
//        }
//    }

    class RangeClose(val id: TagIdentifier) : Pattern {
        override val nullable: Boolean
            get() = false

        override fun matches(t: TAGMLToken): Boolean {
            return (t is EndTagToken) && id.matches(t.tagName)
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return empty()
        }

        override fun expectedTokens(): List<TAGMLToken> {
            val tagName = when (id) {
                is TagIdentifiers.AnyTagIdentifier -> "*"
                is FixedIdentifier -> id.tagName
                else -> "?"
            }
            return listOf(EndTagToken(tagName))
        }

        override fun toString(): String {
            return """<rangeClose id="$id"/>"""
        }

    }

    class Text : Pattern {
        override val nullable: Boolean
            get() = true

        override fun matches(t: TAGMLToken): Boolean {
            return (t is TextToken)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return text()
        }

        override fun toString(): String {
            return "<text/>"
        }
    }

    // combinators
    class After(val expectation1: Pattern, val expectation2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = false

        override fun matches(t: TAGMLToken): Boolean {
            return if (!expectation1.matches(t) && expectation1.nullable)
                expectation2.matches(t)
            else
                expectation1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return after(expectation1.startTokenDeriv(s), expectation2)
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return after(expectation1.endTokenDeriv(e), expectation2)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return after(expectation1.textTokenDeriv(t), expectation2)
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation1.expectedTokens()
        }

        override fun toString(): String {
            return "<after>$expectation1$expectation2</after>"
        }

    }

    class Choice(val expectation1: Pattern, val expectation2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = expectation1.nullable || expectation2.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t) || expectation2.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return choice(expectation1.startTokenDeriv(s), expectation2.startTokenDeriv(s))
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return choice(expectation1.endTokenDeriv(e), expectation2.endTokenDeriv(e))
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
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

    class OneOrMore(val expectation: Pattern) : Pattern {

        override val nullable: Boolean
            get() = expectation.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return expectation.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return group(
                    expectation.textTokenDeriv(t),
                    choice(OneOrMore(expectation), empty())
            )
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return group(
                    expectation.startTokenDeriv(s),
                    choice(OneOrMore(expectation), empty())
            )
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return group(
                    expectation.endTokenDeriv(e),
                    choice(OneOrMore(expectation), empty())
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation.expectedTokens()
        }

        override fun toString(): String {
            return "<oneOrMore>$expectation</oneOrMore>"
        }

    }

    class Concur(val expectation1: Pattern, val expectation2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = expectation1.nullable && expectation2.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t) || expectation2.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return concur(
                    expectation1.textTokenDeriv(t),
                    expectation2.textTokenDeriv(t)
            )
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            val d1 = expectation1.startTokenDeriv(s)
            val d2 = expectation2.startTokenDeriv(s)
            return choice(
                    choice(
                            concur(d1, expectation2),
                            concur(expectation1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        override fun endTokenDeriv(t: EndTagToken): Pattern {
            val d1 = expectation1.endTokenDeriv(t)
            val d2 = expectation2.endTokenDeriv(t)
            return choice(
                    choice(
                            concur(d1, expectation2),
                            concur(expectation1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation1.expectedTokens() + expectation2.expectedTokens()
        }

        override fun toString(): String {
            return "<concur>$expectation1$expectation2</concur>"
        }

    }

    class All(val expectation1: Pattern, val expectation2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = expectation1.nullable && expectation2.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return expectation1.matches(t) && expectation2.matches(t)
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation1.expectedTokens() + expectation2.expectedTokens()
        }

        override fun toString(): String {
            return "<all>$expectation1$expectation2</all>"
        }
    }

    class ConcurOneOrMore(val expectation: Pattern) : Pattern {

        override val nullable: Boolean
            get() = expectation.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return expectation.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return concur(
                    expectation.textTokenDeriv(t),
                    choice(ConcurOneOrMore(expectation), empty())
            )
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return concur(
                    expectation.startTokenDeriv(s),
                    choice(ConcurOneOrMore(expectation), anyContent())
            )
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return concur(
                    expectation.endTokenDeriv(e),
                    choice(ConcurOneOrMore(expectation), anyContent())
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return expectation.expectedTokens()
        }

        override fun toString(): String {
            return "<concurOneOrMore>$expectation</concurOneOrMore>"
        }

    }

    class Group(val expectation1: Pattern, val expectation2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = expectation1.nullable && expectation2.nullable

        override fun matches(t: TAGMLToken): Boolean {
//            _log.info("expectation1=$expectation1 ; expectation1.matches(t)=${expectation1.matches(t)}; expectation1.nullable = ${expectation1.nullable} ")
            return if (!expectation1.matches(t) && expectation1.nullable)
                expectation2.matches(t)
            else
                expectation1.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            val p = group(expectation1.textTokenDeriv(t), expectation2)
            return if (expectation1.nullable)
                choice(p, expectation2.textTokenDeriv(t))
            else p
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            val p = group(expectation1.startTokenDeriv(s), expectation2)
            return if (expectation1.nullable)
                choice(p, expectation2.startTokenDeriv(s))
            else p
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            val p = group(expectation1.endTokenDeriv(e), expectation2)
            return if (expectation1.nullable)
                choice(p, expectation2.endTokenDeriv(e))
            else p
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return if (expectation1.nullable)
                expectation1.expectedTokens() + expectation2.expectedTokens()
            else
                expectation1.expectedTokens()
        }

        override fun toString(): String {
            return "<group>$expectation1$expectation2</group>"
        }

    }

//    class Not(val expectation: Expectation) : Expectation {
//
//        override fun matches(t: TAGMLToken): Boolean {
//            return !expectation.matches(t)
//        }
//
//        override fun startTokenDeriv(s: StartTagToken): Expectation {
//            return if (expectation.matches(s))
//                notAllowed()
//            else
//                RangeOpen(FixedIdentifier(s.tagName)).startTokenDeriv(s)
//        }
//
//        override fun endTokenDeriv(e: EndTagToken): Expectation {
//            return if (this.expectation.matches(e))
//                notAllowed()
//            else
//                RangeClose(FixedIdentifier(e.tagName)).endTokenDeriv(e)
//        }
//
//        override fun textTokenDeriv(t: TextToken): Expectation {
//            return if (expectation.matches(t))
//                notAllowed()
//            else
//                Text().textTokenDeriv(t)
//        }
//
//        override fun expectedTokens(): List<TAGMLToken> {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun toString(): String {
//            return "<not>$expectation</not>"
//        }
//
//    }


}