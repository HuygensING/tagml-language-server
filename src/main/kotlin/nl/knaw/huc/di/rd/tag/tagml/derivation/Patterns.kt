package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.anyContent
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

object Patterns {

    val ANY_TEXT_TOKEN = TextToken("*")

    object Empty : Pattern {
        override val nullable: Boolean = true

        override fun toString(): String = "<empty/>"
    }

    object NotAllowed : Pattern {
        override val nullable: Boolean = false

        override fun toString(): String = "<notAllowed/>"
    }

    class Range(private val id: TagIdentifier, private val pattern: Pattern) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy { setOf(StartTagToken(determineTagName(id))) }

        private val lazySerialized: String by lazy { """<range id="$id">$pattern</range>""" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = (t is StartTagToken) && id.matches(t.tagName)

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                group(
                        pattern,
                        RangeClose(FixedIdentifier(s.tagName))
                )
    }

    class RangeClose(private val id: TagIdentifier) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy {
            val tagName = determineTagName(id)
            setOf(EndTagToken(tagName))
        }

        private val lazySerialized: String by lazy { """<rangeClose id="$id"/>""" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = (t is EndTagToken) && id.matches(t.tagName)

        override fun endTokenDeriv(e: EndTagToken): Pattern = Empty
    }

    object Text : Pattern {
        override val nullable: Boolean = true

        override val expectedTokens: Set<TAGMLToken> by lazy { setOf(ANY_TEXT_TOKEN) }

        override fun toString(): String = "<text/>"

        override fun matches(t: TAGMLToken): Boolean = (t is TextToken)

        // relaxng: A text pattern matches zero or more text nodes. Thus the derivative of Text with respect to a text node is Text, not Empty
        // TODO: if the parser does not return consecutive texttokens, then this can return Empty
        override fun textTokenDeriv(t: TextToken): Pattern {
//            return Empty
            return Text
        }
    }

    // combinators

    class After(val pattern1: Pattern, val pattern2: Pattern) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens }

        private val lazySerialization: String by lazy { "<after>${aggregateSubPatterns().joinToString("")}</after>" }
        override fun toString(): String = lazySerialization

        override fun matches(t: TAGMLToken): Boolean {
            return if (!pattern1.matches(t) && pattern1.nullable)
                pattern2.matches(t)
            else
                pattern1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                after(pattern1.startTokenDeriv(s), pattern2)

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                after(pattern1.endTokenDeriv(e), pattern2)

        override fun textTokenDeriv(t: TextToken): Pattern =
                after(pattern1.textTokenDeriv(t), pattern2)

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            if (pattern1 is After)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            if (pattern2 is After)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    class All(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern1.nullable && pattern2.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens + pattern2.expectedTokens }

        private val lazySerialized: String by lazy { "<all>${aggregateSubPatterns().joinToString("")}</all>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) && pattern2.matches(t)

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            if (pattern1 is All)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            if (pattern2 is All)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    class Choice(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern1.nullable || pattern2.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens + pattern2.expectedTokens }

        private val lazySerialized: String by lazy {
            if (pattern1 is OneOrMore && pattern2 is Empty)
                "<zeroOrMore>${pattern1.pattern}</zeroOrMore>"
            else
                "<choice>${aggregateSubPatterns().joinToString("")}</choice>"
        }

        override fun toString(): String = lazySerialized

        private val lazyHashcode: Int by lazy { this.javaClass.hashCode() + pattern1.hashCode() + pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashcode

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.matches(t)

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                choice(pattern1.startTokenDeriv(s), pattern2.startTokenDeriv(s))

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                choice(pattern1.endTokenDeriv(e), pattern2.endTokenDeriv(e))

        override fun textTokenDeriv(t: TextToken): Pattern =
                choice(pattern1.textTokenDeriv(t), pattern2.textTokenDeriv(t))

        override fun equals(other: Any?): Boolean =
                (other is Choice) && (
                        (other.pattern1 == pattern1 && other.pattern2 == pattern2) ||
                        (other.pattern1 == pattern2 && other.pattern2 == pattern1))

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
    }

    class Concur(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern1.nullable && pattern2.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens + pattern2.expectedTokens }

        private val lazySerialized: String by lazy { "<concur>${aggregateSubPatterns().joinToString("")}</concur>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.matches(t)

        override fun textTokenDeriv(t: TextToken): Pattern =
                concur(
                        pattern1.textTokenDeriv(t),
                        pattern2.textTokenDeriv(t)
                )

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

        override fun endTokenDeriv(e: EndTagToken): Pattern {
            val d1 = pattern1.endTokenDeriv(e)
            val d2 = pattern2.endTokenDeriv(e)
            return choice(
                    choice(
                            concur(d1, pattern2),
                            concur(pattern1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            if (pattern1 is Concur)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            if (pattern2 is Concur)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    class Group(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern1.nullable && pattern2.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy {
            if (pattern1.nullable)
                pattern1.expectedTokens + pattern2.expectedTokens
            else
                pattern1.expectedTokens
        }

        private val lazySerialized: String by lazy { "<group>${aggregateSubPatterns().joinToString("")}</group>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean {
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

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            if (pattern1 is Group)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            if (pattern2 is Group)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    class Interleave(private val pattern1: Pattern, private val pattern2: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern1.nullable && pattern2.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens + pattern2.expectedTokens }

        private val lazySerialized: String by lazy {
            if (pattern1 is Text)
                "<mixed>$pattern2</mixed>"
            if (pattern2 is Text)
                "<mixed>$pattern1</mixed>"
            "<interleave>${aggregateSubPatterns().joinToString("")}</interleave>"
        }

        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.matches(t)

        override fun textTokenDeriv(t: TextToken): Pattern =
                choice(
                        interleave(pattern1.textTokenDeriv(t), pattern2),
                        interleave(pattern2.textTokenDeriv(t), pattern1)
                )

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                choice(
                        interleave(pattern1.startTokenDeriv(s), pattern2),
                        interleave(pattern2.startTokenDeriv(s), pattern1)
                )

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                choice(
                        interleave(pattern1.endTokenDeriv(e), pattern2),
                        interleave(pattern2.endTokenDeriv(e), pattern1)
                )

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            if (pattern1 is Interleave)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            if (pattern2 is Interleave)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    object HierarchyLevel : Pattern {
        private val pattern1: Pattern = Text
        private val pattern2: Lazy<Pattern> = lazy { Range(AnyTagIdentifier, HierarchyLevel) }

        override val nullable: Boolean by lazy { pattern1.nullable || pattern2.value.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens + pattern2.value.expectedTokens }

        override fun toString(): String = "<hierarchyLevel/>"

        private val lazyHashCode: Int = this.javaClass.hashCode() + pattern1.hashCode() + pattern2.hashCode()
        override fun hashCode(): Int = lazyHashCode

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.value.matches(t)

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                choice(pattern1.startTokenDeriv(s), pattern2.value.startTokenDeriv(s))

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                choice(pattern1.endTokenDeriv(e), pattern2.value.endTokenDeriv(e))

        override fun textTokenDeriv(t: TextToken): Pattern =
                choice(pattern1.textTokenDeriv(t), pattern2.value.textTokenDeriv(t))

        override fun equals(other: Any?): Boolean =
                (other is HierarchyLevel) && (
                        (other.pattern1 == pattern1 && other.pattern2 == pattern2) ||
                        (other.pattern1 == pattern2 && other.pattern2 == pattern1))
    }

    class OneOrMore(val pattern: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern.expectedTokens }

        private val lazySerialized: String by lazy { "<oneOrMore>$pattern</oneOrMore>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = pattern.matches(t)

        override fun textTokenDeriv(t: TextToken): Pattern =
                group(
                        pattern.textTokenDeriv(t),
                        choice(OneOrMore(pattern), Empty)
                )

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                group(
                        pattern.startTokenDeriv(s),
                        choice(OneOrMore(pattern), Empty)
                )

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                group(
                        pattern.endTokenDeriv(e),
                        choice(OneOrMore(pattern), Empty)
                )
    }

    class ConcurOneOrMore(private val pattern: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern.expectedTokens }

        private val lazySerialization: String by lazy { "<concurOneOrMore>$pattern</concurOneOrMore>" }
        override fun toString(): String = lazySerialization

        override fun matches(t: TAGMLToken): Boolean = pattern.matches(t)

        override fun textTokenDeriv(t: TextToken): Pattern =
                concur(
                        pattern.textTokenDeriv(t),
                        choice(ConcurOneOrMore(pattern), Empty)
                )

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                concur(
                        pattern.startTokenDeriv(s),
                        choice(ConcurOneOrMore(pattern), anyContent())
                )

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                concur(
                        pattern.endTokenDeriv(e),
                        choice(ConcurOneOrMore(pattern), anyContent())
                )
    }

    private fun determineTagName(id: TagIdentifier): String =
            when (id) {
                is AnyTagIdentifier -> "*"
                is FixedIdentifier  -> id.tagName
                else                -> "?"
            }
}

