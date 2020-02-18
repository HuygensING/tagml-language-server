package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.anyContent
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.oneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.EndTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.StartTagToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TextToken

object Patterns {

    fun LPattern.nullable() = this.value.nullable
    fun LPattern.expectedTokens() = this.value.expectedTokens
    fun LPattern.matches(t: TAGMLToken) = this.value.matches(t)
    fun LPattern.textTokenDeriv() = this.value.textTokenDeriv()
    fun LPattern.startTokenDeriv(s: StartTagToken) = this.value.startTokenDeriv(s)
    fun LPattern.endTokenDeriv(e: EndTagToken) = this.value.endTokenDeriv(e)

    val ANY_TEXT_TOKEN = TextToken("*")

    object Empty : Pattern {
        override val nullable: Boolean = true

        override fun toString(): String = "<empty/>"
    }

    object NotAllowed : Pattern {
        override val nullable: Boolean = false

        override fun toString(): String = "<notAllowed/>"
    }

    class Range(private val id: TagIdentifier, private val lPattern: LPattern) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy { setOf(StartTagToken(determineTagName(id))) }

        private val lazySerialized: String by lazy { """<range id="$id">${lPattern.value}</range>""" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = (t is StartTagToken) && id.matches(t.tagName)

        override fun startTokenDeriv(s: StartTagToken): LPattern =
                group(
                        lPattern,
                        lazy { RangeClose(FixedIdentifier(s.tagName)) }
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

        override fun endTokenDeriv(e: EndTagToken): LPattern = lazyOf(Empty)
    }

    object Text : Pattern {
        override val nullable: Boolean = true

        override val expectedTokens: Set<TAGMLToken> by lazy { setOf(ANY_TEXT_TOKEN) }

        override fun toString(): String = "<text/>"

        override fun matches(t: TAGMLToken): Boolean = (t is TextToken)

        // relaxng: A text pattern matches zero or more text nodes. Thus the derivative of Text with respect to a text node is Text, not Empty
        // TODO: if the parser does not return consecutive texttokens, then this can return Empty?
        override fun textTokenDeriv(): LPattern = lazyOf(Text)
    }

    // combinators

    class After(val lPattern1: LPattern, val lPattern2: LPattern) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy { lPattern1.expectedTokens() }

        private val lazySerialization: String by lazy { "<after>${aggregateSubPatterns().joinToString("")}</after>" }
        override fun toString(): String = lazySerialization

        override fun matches(t: TAGMLToken): Boolean {
            return if (!lPattern1.matches(t) && lPattern1.nullable())
                lPattern2.matches(t)
            else
                lPattern1.matches(t)
        }

        override fun startTokenDeriv(s: StartTagToken): LPattern =
                after(lPattern1.startTokenDeriv(s), lPattern2)

        override fun endTokenDeriv(e: EndTagToken): LPattern =
                after(lPattern1.endTokenDeriv(e), lPattern2)

        override fun textTokenDeriv(): LPattern = after(lPattern1.textTokenDeriv(), lPattern2)

        private fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            val pattern1 = lPattern1.value
            val pattern2 = lPattern2.value
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

    class All(private val lPattern1: LPattern, private val lPattern2: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern1.nullable() && lPattern2.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy { lPattern1.expectedTokens() + lPattern2.expectedTokens() }

        private val lazySerialized: String by lazy { "<all>${aggregateSubPatterns().joinToString("")}</all>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = lPattern1.matches(t) && lPattern2.matches(t)

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            val pattern1 = lPattern1.value
            val pattern2 = lPattern2.value
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

    class Choice(private val lPattern1: LPattern, private val lPattern2: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern1.nullable() || lPattern2.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy {
            lPattern1.expectedTokens() + lPattern2.expectedTokens()
        }

        private val lazySerialized: String by lazy {
            val pattern1 = lPattern1.value
            if (pattern1 is OneOrMore && lPattern2.value is Empty)
                "<zeroOrMore>${pattern1.lPattern}</zeroOrMore>"
            else
                "<choice>${aggregateSubPatterns().joinToString("")}</choice>"
        }

        override fun toString(): String = lazySerialized

        private val lazyHashcode: Int by lazy {
            this.javaClass.hashCode() + 10 * lPattern1.hashCode() + 100 * lPattern2
                    .hashCode()
        }

        override fun hashCode(): Int = lazyHashcode

        override fun matches(t: TAGMLToken): Boolean = lPattern1.matches(t) || lPattern2.matches(t)

        override fun startTokenDeriv(s: StartTagToken): LPattern =
                choice(lPattern1.startTokenDeriv(s), lPattern2.startTokenDeriv(s))

        override fun endTokenDeriv(e: EndTagToken): LPattern =
                choice(lPattern1.endTokenDeriv(e), lPattern2.endTokenDeriv(e))

        override fun textTokenDeriv(): LPattern = choice(lPattern1.textTokenDeriv(), lPattern2.textTokenDeriv())

        override fun equals(other: Any?): Boolean =
                (other is Choice) && (
                        (other.lPattern1 == lPattern1 && other.lPattern2 == lPattern2) ||
                        (other.lPattern1 == lPattern2 && other.lPattern2 == lPattern1))

        private fun aggregateSubPatterns(): Set<Pattern> {
            val aggregate = mutableSetOf<Pattern>()
            val pattern1 = lPattern1.value
            if (pattern1 is Choice)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)
            val pattern2 = lPattern2.value
            if (pattern2 is Choice)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    class Concur(private val lPattern1: LPattern, private val lPattern2: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern1.nullable() && lPattern2.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy { lPattern1.expectedTokens() + lPattern2.expectedTokens() }

        private val lazySerialized: String by lazy { "<concur>${aggregateSubPatterns().joinToString("")}</concur>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = lPattern1.matches(t) || lPattern2.matches(t)

        override fun textTokenDeriv(): LPattern = concur(
                lPattern1.textTokenDeriv(),
                lPattern2.textTokenDeriv()
        )

        override fun startTokenDeriv(s: StartTagToken): LPattern {
            val d1 = lPattern1.startTokenDeriv(s)
            val d2 = lPattern2.startTokenDeriv(s)
            return choice(
                    choice(
                            concur(d1, lPattern2),
                            concur(lPattern1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        override fun endTokenDeriv(e: EndTagToken): LPattern {
            val d1 = lPattern1.endTokenDeriv(e)
            val d2 = lPattern2.endTokenDeriv(e)
            return choice(
                    choice(
                            concur(d1, lPattern2),
                            concur(lPattern1, d2)
                    ),
                    concur(d1, d2)
            )
        }

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            val pattern1 = lPattern1.value
            val pattern2 = lPattern2.value
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

    class Group(private val lPattern1: LPattern, private val lPattern2: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern1.nullable() && lPattern2.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy {
            if (lPattern1.nullable())
                lPattern1.expectedTokens() + lPattern2.expectedTokens()
            else
                lPattern1.expectedTokens()
        }

        private val lazySerialized: String by lazy { "<group>${aggregateSubPatterns().joinToString("")}</group>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean {
            return if (!lPattern1.matches(t) && lPattern1.nullable())
                lPattern2.matches(t)
            else
                lPattern1.matches(t)
        }

        override fun textTokenDeriv(): LPattern {
            val p = group(lPattern1.textTokenDeriv(), lPattern2)
            return if (lPattern1.nullable())
                choice(p, lPattern2.textTokenDeriv())
            else p
        }

        override fun startTokenDeriv(s: StartTagToken): LPattern {
            val pattern1 = lPattern1.value
            val p = group(pattern1.startTokenDeriv(s), lPattern2)
            return if (pattern1.nullable)
                choice(p, lPattern2.startTokenDeriv(s))
            else p
        }

        override fun endTokenDeriv(e: EndTagToken): LPattern {
            val pattern1 = lPattern1.value
            val p = group(pattern1.endTokenDeriv(e), lPattern2)
            return if (pattern1.nullable)
                choice(p, lPattern2.endTokenDeriv(e))
            else p
        }

        fun aggregateSubPatterns(): List<Pattern> {
            val pattern1 = lPattern1.value
            val pattern2 = lPattern2.value
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

    class Interleave(private val lPattern1: LPattern, private val lPattern2: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern1.nullable() && lPattern2.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy { lPattern1.expectedTokens() + lPattern2.expectedTokens() }

        private val lazySerialized: String by lazy {
            if (lPattern1.value is Text)
                "<mixed>$lPattern2</mixed>"
            if (lPattern2.value is Text)
                "<mixed>$lPattern1</mixed>"
            "<interleave>${aggregateSubPatterns().joinToString("")}</interleave>"
        }

        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = lPattern1.matches(t) || lPattern2.matches(t)

        override fun textTokenDeriv(): LPattern = choice(
                interleave(lPattern1.textTokenDeriv(), lPattern2),
                interleave(lPattern2.textTokenDeriv(), lPattern1)
        )

        override fun startTokenDeriv(s: StartTagToken): LPattern = choice(
                interleave(lPattern1.startTokenDeriv(s), lPattern2),
                interleave(lPattern2.startTokenDeriv(s), lPattern1)
        )

        override fun endTokenDeriv(e: EndTagToken): LPattern = choice(
                interleave(lPattern1.endTokenDeriv(e), lPattern2),
                interleave(lPattern2.endTokenDeriv(e), lPattern1)
        )

        fun aggregateSubPatterns(): List<Pattern> {
            val aggregate = mutableListOf<Pattern>()
            val pattern1 = lPattern1.value
            if (pattern1 is Interleave)
                aggregate.addAll(pattern1.aggregateSubPatterns())
            else
                aggregate.add(pattern1)

            val pattern2 = lPattern2.value
            if (pattern2 is Interleave)
                aggregate.addAll(pattern2.aggregateSubPatterns())
            else
                aggregate.add(pattern2)
            return aggregate
        }
    }

    object HierarchyLevel : Pattern {
        private val lPattern: LPattern = lazy { Range(AnyTagIdentifier, lazyOf(HierarchyLevel)) }

        override val nullable: Boolean by lazy { Text.nullable || lPattern.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy { Text.expectedTokens + lPattern.expectedTokens() }

        override fun toString(): String = "<hierarchyLevel/>"

        private val lazyHashCode: Int = this.javaClass.hashCode() + Text.hashCode() + lPattern.hashCode()
        override fun hashCode(): Int = lazyHashCode

        override fun matches(t: TAGMLToken): Boolean = Text.matches(t) || lPattern.matches(t)

        override fun startTokenDeriv(s: StartTagToken): LPattern =
                choice(Text.startTokenDeriv(s), lPattern.startTokenDeriv(s))

        override fun endTokenDeriv(e: EndTagToken): LPattern =
                choice(Text.endTokenDeriv(e), lPattern.endTokenDeriv(e))

        private val lazyTextTokenDeriv by lazy { choice(Text.textTokenDeriv(), lPattern.textTokenDeriv()) }
        override fun textTokenDeriv(): LPattern = lazyTextTokenDeriv

        override fun equals(other: Any?): Boolean =
                (other is HierarchyLevel) && (other.lPattern == lPattern)
    }

    class OneOrMore(val lPattern: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy { lPattern.expectedTokens() }

        private val lazySerialized: String by lazy { "<oneOrMore>$lPattern</oneOrMore>" }
        override fun toString(): String = lazySerialized

        override fun matches(t: TAGMLToken): Boolean = lPattern.matches(t)

        override fun textTokenDeriv(): LPattern = group(
                lPattern.textTokenDeriv(),
                choice(oneOrMore(lPattern), empty())
        )

        override fun startTokenDeriv(s: StartTagToken): LPattern = group(
                lPattern.startTokenDeriv(s),
                choice(oneOrMore(lPattern), empty())
        )

        override fun endTokenDeriv(e: EndTagToken): LPattern = group(
                lPattern.endTokenDeriv(e),
                choice(oneOrMore(lPattern), empty())
        )
    }

    class ConcurOneOrMore(private val lPattern: LPattern) : Pattern {
        override val nullable: Boolean by lazy { lPattern.nullable() }

        override val expectedTokens: Set<TAGMLToken> by lazy { lPattern.expectedTokens() }

        private val lazySerialization: String by lazy { "<concurOneOrMore>$lPattern</concurOneOrMore>" }
        override fun toString(): String = lazySerialization

        override fun matches(t: TAGMLToken): Boolean = lPattern.matches(t)

        override fun textTokenDeriv(): LPattern = concur(
                lPattern.textTokenDeriv(),
                choice(concurOneOrMore(lPattern), empty())
        )

        override fun startTokenDeriv(s: StartTagToken): LPattern = concur(
                lPattern.startTokenDeriv(s),
                choice(concurOneOrMore(lPattern), anyContent())
        )

        override fun endTokenDeriv(e: EndTagToken): LPattern = concur(
                lPattern.endTokenDeriv(e),
                choice(concurOneOrMore(lPattern), anyContent())
        )
    }

    private fun determineTagName(id: TagIdentifier): String =
            when (id) {
                is AnyTagIdentifier -> "*"
                is FixedIdentifier  -> id.tagName
                else                -> "?"
            }
}

