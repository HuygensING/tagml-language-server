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

    const val TEXT_HASH_CODE = 1
    const val ERROR_HASH_CODE = 3
    const val EMPTY_HASH_CODE = 5
    const val NOT_ALLOWED_HASH_CODE = 7
    const val CHOICE_HASH_CODE = 11
    const val GROUP_HASH_CODE = 13
    const val INTERLEAVE_HASH_CODE = 17
    const val ONE_OR_MORE_HASH_CODE = 19
    const val ELEMENT_HASH_CODE = 23
    const val VALUE_HASH_CODE = 27
    const val ATTRIBUTE_HASH_CODE = 29
    const val DATA_HASH_CODE = 31
    const val LIST_HASH_CODE = 37
    const val AFTER_HASH_CODE = 41
    const val ALL_HASH_CODE = 43
    const val CONCUR_HASH_CODE = 47
    const val CONCUR_ONE_OR_MORE_HASH_CODE = 53
    const val HIERARCHY_LEVEL_HASH_CODE = 59
    const val RANGE_HASH_CODE = 61
    const val RANGE_CLOSE_HASH_CODE = 67

    val ANY_TEXT_TOKEN = TextToken("*")

    object Empty : Pattern {
        override val nullable: Boolean = true

        override fun toString(): String = "<empty/>"

        override fun hashCode(): Int = EMPTY_HASH_CODE
    }

    object NotAllowed : Pattern {
        override val nullable: Boolean = false

        override fun toString(): String = "<notAllowed/>"
        override fun hashCode(): Int = NOT_ALLOWED_HASH_CODE
    }

    class Range(private val id: TagIdentifier, private val pattern: Pattern) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy { setOf(StartTagToken(determineTagName(id))) }

        private val lazySerialized: String by lazy { """<range id="$id">$pattern</range>""" }
        override fun toString(): String = lazySerialized

        private val lazyHashCode: Int by lazy { RANGE_HASH_CODE * pattern.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is Range &&
                other.id == id &&
                other.pattern == pattern

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

        private val lazyHashCode: Int by lazy { RANGE_CLOSE_HASH_CODE * id.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is RangeClose &&
                other.id == id

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
        override fun textTokenDeriv(): Pattern = Text
    }

    // combinators

    class After(val pattern1: Pattern, val pattern2: Pattern) : Pattern {
        override val nullable: Boolean = false

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens }

        private val lazySerialization: String by lazy { "<after>${aggregateSubPatterns().joinToString("")}</after>" }
//        private val lazySerialization: String by lazy { "<after>$pattern1$pattern2</after>" }

        override fun toString(): String = lazySerialization

        private val lazyHashCode: Int by lazy { AFTER_HASH_CODE * pattern1.hashCode() * pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is After &&
                other.pattern1 == pattern1 &&
                other.pattern2 == other.pattern2

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

        private val lazyTextTokenDeriv: Pattern by lazy { after(pattern1.textTokenDeriv(), pattern2) }
        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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
//        private val lazySerialized: String by lazy { "<all>$pattern1$pattern2</all>" }

        override fun toString(): String = lazySerialized

        private val lazyHashCode: Int by lazy { ALL_HASH_CODE * pattern1.hashCode() * pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is All &&
                other.pattern1 == pattern1 &&
                other.pattern2 == pattern2

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
//                "<choice>$pattern1$pattern2</choice>"
        }

        override fun toString(): String = lazySerialized

        private val lazyHashcode: Int by lazy { CHOICE_HASH_CODE * pattern1.hashCode() * pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashcode

        override fun equals(other: Any?): Boolean =
                (other is Choice) && (
                        (other.pattern1 == pattern1 && other.pattern2 == pattern2) ||
                        (other.pattern1 == pattern2 && other.pattern2 == pattern1))

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.matches(t)

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                choice(pattern1.startTokenDeriv(s), pattern2.startTokenDeriv(s))

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                choice(pattern1.endTokenDeriv(e), pattern2.endTokenDeriv(e))

        private val lazyTextTokenDeriv: Pattern by lazy { choice(pattern1.textTokenDeriv(), pattern2.textTokenDeriv()) }
        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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

    class Concur(internal val pattern1: Pattern, internal val pattern2: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern1.nullable && pattern2.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern1.expectedTokens + pattern2.expectedTokens }

        private val lazySerialized: String by lazy { "<concur>${aggregateSubPatterns().joinToString("")}</concur>" }
//        private val lazySerialized: String by lazy { "<concur>$pattern1$pattern2</concur>" }

        override fun toString(): String = lazySerialized

        private val lazyHashcode: Int by lazy { CONCUR_HASH_CODE * pattern1.hashCode() * pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashcode

        override fun equals(other: Any?): Boolean =
                (other is Concur) && (
                        (other.pattern1 == pattern1 && other.pattern2 == pattern2) ||
                        (other.pattern1 == pattern2 && other.pattern2 == pattern1))

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.matches(t)

        private val lazyTextTokenDeriv: Pattern by lazy {
            concur(
                    pattern1.textTokenDeriv(),
                    pattern2.textTokenDeriv()
            )
        }

        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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
//        private val lazySerialized: String by lazy { "<group>$pattern1$pattern2</group>" }

        override fun toString(): String = lazySerialized

        private val lazyHashCode: Int by lazy { GROUP_HASH_CODE * pattern1.hashCode() * pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is Group &&
                other.pattern1 == pattern1 &&
                other.pattern2 == pattern2

        override fun matches(t: TAGMLToken): Boolean {
            return if (!pattern1.matches(t) && pattern1.nullable)
                pattern2.matches(t)
            else
                pattern1.matches(t)
        }

        private val lazyTextTokenDeriv: Pattern by lazy {
            val p = group(pattern1.textTokenDeriv(), pattern2)
            if (pattern1.nullable)
                choice(p, pattern2.textTokenDeriv())
            else p
        }

        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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
//            "<interleave>$pattern1$pattern2</interleave>"
            "<interleave>${aggregateSubPatterns().joinToString("")}</interleave>"
        }

        override fun toString(): String = lazySerialized

        private val lazyHashcode: Int by lazy { INTERLEAVE_HASH_CODE * pattern1.hashCode() * pattern2.hashCode() }
        override fun hashCode(): Int = lazyHashcode

        override fun equals(other: Any?): Boolean =
                (other is Interleave) && (
                        (other.pattern1 == pattern1 && other.pattern2 == pattern2) ||
                        (other.pattern1 == pattern2 && other.pattern2 == pattern1))

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.matches(t)

        private val lazyTextTokenDeriv by lazy {
            choice(
                    interleave(pattern1.textTokenDeriv(), pattern2),
                    interleave(pattern2.textTokenDeriv(), pattern1)
            )
        }

        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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

        override fun hashCode(): Int = HIERARCHY_LEVEL_HASH_CODE

        override fun matches(t: TAGMLToken): Boolean = pattern1.matches(t) || pattern2.value.matches(t)

        override fun startTokenDeriv(s: StartTagToken): Pattern =
                choice(pattern1.startTokenDeriv(s), pattern2.value.startTokenDeriv(s))

        override fun endTokenDeriv(e: EndTagToken): Pattern =
                choice(pattern1.endTokenDeriv(e), pattern2.value.endTokenDeriv(e))

        private val lazyTextTokenDeriv by lazy { choice(pattern1.textTokenDeriv(), pattern2.value.textTokenDeriv()) }
        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv
    }

    class OneOrMore(val pattern: Pattern) : Pattern {
        override val nullable: Boolean by lazy { pattern.nullable }

        override val expectedTokens: Set<TAGMLToken> by lazy { pattern.expectedTokens }

        private val lazySerialized: String by lazy { "<oneOrMore>$pattern</oneOrMore>" }
        override fun toString(): String = lazySerialized

        private val lazyHashCode: Int by lazy { ONE_OR_MORE_HASH_CODE * pattern.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is OneOrMore &&
                other.pattern == pattern

        override fun matches(t: TAGMLToken): Boolean = pattern.matches(t)

        private val lazyTextTokenDeriv: Pattern by lazy {
            group(
                    pattern.textTokenDeriv(),
                    choice(OneOrMore(pattern), Empty)
            )
        }

        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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

        private val lazyHashCode: Int by lazy { CONCUR_ONE_OR_MORE_HASH_CODE * pattern.hashCode() }
        override fun hashCode(): Int = lazyHashCode

        override fun equals(other: Any?): Boolean =
                other is ConcurOneOrMore &&
                other.pattern == pattern

        override fun matches(t: TAGMLToken): Boolean = pattern.matches(t)

        private val lazyTextTokenDeriv: Pattern by lazy {
            concur(
                    pattern.textTokenDeriv(),
                    choice(ConcurOneOrMore(pattern), Empty)
            )
        }

        override fun textTokenDeriv(): Pattern = lazyTextTokenDeriv

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

