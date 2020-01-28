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

object Patterns {

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

    class Range(private val id: TagIdentifier, private val pattern: Pattern) : Pattern {
        override val nullable: Boolean
            get() = false

        override fun matches(t: TAGMLToken): Boolean {
            return (t is StartTagToken) && id.matches(t.tagName)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return group(
                    pattern,
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
            return """<range id="$id">$pattern</range>"""
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

    class RangeClose(private val id: TagIdentifier) : Pattern {
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
    class After(val pattern1: Pattern, val pattern2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = false

        override fun matches(t: TAGMLToken): Boolean {
            return if (!pattern1.matches(t) && pattern1.nullable)
                pattern2.matches(t)
            else
                pattern1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return after(pattern1.startTokenDeriv(s), pattern2)
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return after(pattern1.endTokenDeriv(e), pattern2)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return after(pattern1.textTokenDeriv(t), pattern2)
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return pattern1.expectedTokens()
        }

        override fun toString(): String {
            return "<after>$pattern1$pattern2</after>"
        }

    }

    class Choice(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = pattern1.nullable || pattern2.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return pattern1.matches(t) || pattern2.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return choice(pattern1.startTokenDeriv(s), pattern2.startTokenDeriv(s))
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return choice(pattern1.endTokenDeriv(e), pattern2.endTokenDeriv(e))
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return choice(pattern1.textTokenDeriv(t), pattern2.textTokenDeriv(t))
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return pattern1.expectedTokens() + pattern2.expectedTokens()
        }

        fun aggregateSubPatterns(): Set<Pattern> {
            val aggregate = mutableSetOf<Pattern>()
            if (pattern1 is Choice)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            if (pattern2 is Choice)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }

        override fun toString(): String {
            return if (pattern1 is OneOrMore && pattern2 is Empty)
                "<zeroOrMore>${pattern1.pattern}</zeroOrMore>"
//            else if (pattern1 is Choice && pattern2 is Choice)
//                "<choice>${pattern1.pattern1}${pattern1.pattern2}${pattern2.pattern1}${pattern2.pattern2}</choice>"
//            else if (pattern1 is Choice)
//                "<choice>${pattern1.pattern1}${pattern1.pattern2}$pattern2</choice>"
//            else if (pattern2 is Choice)
//                "<choice>pattern1${pattern2.pattern1}${pattern2.pattern2}</choice>"
            else
                "<choice>${aggregateSubPatterns().joinToString("")}</choice>"
        }
    }

    class OneOrMore(val pattern: Pattern) : Pattern {

        override val nullable: Boolean
            get() = pattern.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return pattern.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return group(
                    pattern.textTokenDeriv(t),
                    choice(OneOrMore(pattern), empty())
            )
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return group(
                    pattern.startTokenDeriv(s),
                    choice(OneOrMore(pattern), empty())
            )
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return group(
                    pattern.endTokenDeriv(e),
                    choice(OneOrMore(pattern), empty())
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return pattern.expectedTokens()
        }

        override fun toString(): String {
            return "<oneOrMore>$pattern</oneOrMore>"
        }

    }

    class Concur(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = pattern1.nullable && pattern2.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return pattern1.matches(t) || pattern2.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return concur(
                    pattern1.textTokenDeriv(t),
                    pattern2.textTokenDeriv(t)
            )
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            val d1 = pattern1.startTokenDeriv(s)
            val d2 = pattern2.startTokenDeriv(s)
            return choice(
                    choice(
                            concur(d1, pattern2),
                            concur(pattern1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        override fun endTokenDeriv(t: EndTagToken): Pattern {
            val d1 = pattern1.endTokenDeriv(t)
            val d2 = pattern2.endTokenDeriv(t)
            return choice(
                    choice(
                            concur(d1, pattern2),
                            concur(pattern1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return pattern1.expectedTokens() + pattern2.expectedTokens()
        }

        override fun toString(): String {
            return "<concur>$pattern1$pattern2</concur>"
        }

    }

    class All(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = pattern1.nullable && pattern2.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return pattern1.matches(t) && pattern2.matches(t)
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return pattern1.expectedTokens() + pattern2.expectedTokens()
        }

        override fun toString(): String {
            return "<all>$pattern1$pattern2</all>"
        }
    }

    class ConcurOneOrMore(private val pattern: Pattern) : Pattern {

        override val nullable: Boolean
            get() = pattern.nullable

        override fun matches(t: TAGMLToken): Boolean {
            return pattern.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            return concur(
                    pattern.textTokenDeriv(t),
                    choice(ConcurOneOrMore(pattern), empty())
            )
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            return concur(
                    pattern.startTokenDeriv(s),
                    choice(ConcurOneOrMore(pattern), anyContent())
            )
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            return concur(
                    pattern.endTokenDeriv(e),
                    choice(ConcurOneOrMore(pattern), anyContent())
            )
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return pattern.expectedTokens()
        }

        override fun toString(): String {
            return "<concurOneOrMore>$pattern</concurOneOrMore>"
        }

    }

    class Group(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean
            get() = pattern1.nullable && pattern2.nullable

        override fun matches(t: TAGMLToken): Boolean {
//            _log.info("pattern1=$pattern1 ; pattern1.matches(t)=${pattern1.matches(t)}; pattern1.nullable = ${pattern1.nullable} ")
            return if (!pattern1.matches(t) && pattern1.nullable)
                pattern2.matches(t)
            else
                pattern1.matches(t)
        }

        override fun textTokenDeriv(t: TextToken): Pattern {
            val p = group(pattern1.textTokenDeriv(t), pattern2)
            return if (pattern1.nullable)
                choice(p, pattern2.textTokenDeriv(t))
            else p
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern {
            val p = group(pattern1.startTokenDeriv(s), pattern2)
            return if (pattern1.nullable)
                choice(p, pattern2.startTokenDeriv(s))
            else p
        }

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            val p = group(pattern1.endTokenDeriv(e), pattern2)
            return if (pattern1.nullable)
                choice(p, pattern2.endTokenDeriv(e))
            else p
        }

        override fun expectedTokens(): List<TAGMLToken> {
            return if (pattern1.nullable)
                pattern1.expectedTokens() + pattern2.expectedTokens()
            else
                pattern1.expectedTokens()
        }

        override fun toString(): String {
            return "<group>$pattern1$pattern2</group>"
        }

    }

//    class Not(val pattern: Expectation) : Expectation {
//
//        override fun matches(t: TAGMLToken): Boolean {
//            return !pattern.matches(t)
//        }
//
//        override fun startTokenDeriv(s: StartTagToken): Expectation {
//            return if (pattern.matches(s))
//                notAllowed()
//            else
//                RangeOpen(FixedIdentifier(s.tagName)).startTokenDeriv(s)
//        }
//
//        override fun endTokenDeriv(e: EndTagToken): Expectation {
//            return if (this.pattern.matches(e))
//                notAllowed()
//            else
//                RangeClose(FixedIdentifier(e.tagName)).endTokenDeriv(e)
//        }
//
//        override fun textTokenDeriv(t: TextToken): Expectation {
//            return if (pattern.matches(t))
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
//            return "<not>$pattern</not>"
//        }
//
//    }


}