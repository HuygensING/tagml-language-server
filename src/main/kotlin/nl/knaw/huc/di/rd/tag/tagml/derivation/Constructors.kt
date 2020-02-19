package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.All
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.ConcurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.OneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Text
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier

object Constructors {
    private val LNotAllowed = lazyOf(NotAllowed)
    private val LText = lazyOf(Text)
    private val LEmpty = lazyOf(Empty)

    fun anyContent(): LPattern = LText // might not cover it

    private fun layer(): LPattern = lazy { Range(AnyTagIdentifier, choice(LText, layer())) }

    internal fun mixed(lPattern: LPattern): LPattern = interleave(LText, lPattern)

    fun zeroOrMore(lPattern: LPattern): LPattern = choice(oneOrMore(lPattern), LEmpty)

    fun empty() = LEmpty
    fun text() = LText
    fun notAllowed() = LNotAllowed

    fun after(lPattern1: LPattern, lPattern2: LPattern): LPattern {
        val pattern1 = lPattern1.value
        if (pattern1 is NotAllowed || lPattern2.value is NotAllowed) return LNotAllowed

        if (pattern1 is Empty) return lPattern2

        if (pattern1 is After) {
            val p1 = pattern1.lPattern1
            val p2 = pattern1.lPattern2
            return after(p1, after(p2, lPattern2))
        }

        return lazy { After(lPattern1, lPattern2) }
    }

    fun all(lPattern1: LPattern, lPattern2: LPattern): LPattern {
        val pattern1 = lPattern1.value
        if (pattern1 is NotAllowed || lPattern2.value is NotAllowed) return lazy { NotAllowed }

        val pattern2 = lPattern2.value
        if (pattern2 is Empty) {
            return if (pattern1.nullable)
                lazy { Empty }
            else
                lazy { NotAllowed }
        }

        if (pattern1 is Empty) {
            return if (pattern2.nullable)
                lazy { Empty }
            else
                lazy { NotAllowed }
        }

        if (pattern1 is After && pattern2 is After) {
            val e1 = pattern1.lPattern1
            val e2 = pattern1.lPattern2
            val e3 = pattern2.lPattern1
            val e4 = pattern2.lPattern2
            return after(all(e1, e3), all(e2, e4))
        }

        return lazy { All(lPattern1, lPattern2) }
    }

    fun choice(lPattern1: LPattern, lPattern2: LPattern): LPattern {
        if (lPattern1 == lPattern2) return lPattern1
        if (lPattern1.value is NotAllowed) return lPattern2
        if (lPattern2.value is NotAllowed) return lPattern1
        return lazy { Choice(lPattern1, lPattern2) }
    }

    fun oneOrMore(lPattern: LPattern): LPattern {
        val pattern = lPattern.value
        return if (pattern is NotAllowed || pattern is Empty)
            lPattern
        else
            lazy { OneOrMore(lPattern) }
    }

    fun concurOneOrMore(lPattern: LPattern): LPattern =
            if (lPattern.value is NotAllowed || lPattern.value is Empty)
                lPattern
            else
                lazy { ConcurOneOrMore(lPattern) }

    fun concur(lPattern1: LPattern, lPattern2: LPattern): LPattern {
        val pattern1 = lPattern1.value
        if (pattern1 is NotAllowed || lPattern2.value is NotAllowed) return lazy { NotAllowed }
        if (pattern1 is Text) return lPattern2

        val pattern2 = lPattern2.value
        if (pattern2 is Text) return lPattern1

        if (pattern1 is After && pattern2 is After) {
            val e1 = pattern1.lPattern1
            val e2 = pattern1.lPattern2
            val e3 = pattern2.lPattern1
            val e4 = pattern2.lPattern2
            return after(all(e1, e3), concur(e2, e4))
        }

        if (pattern1 is After) {
            val e1 = pattern1.lPattern1
            val e2 = pattern1.lPattern2
            return after(e1, concur(e2, lPattern2))
        }

        if (pattern2 is After) {
            val e2 = pattern2.lPattern1
            val e3 = pattern2.lPattern2
            return after(e2, concur(lPattern1, e3))
        }

        // Concur(Concur(P1,P2),P2) = Concur(P1,P2)
        if (pattern1 is Concur && (pattern1.lPattern1 == lPattern2 || pattern1.lPattern2 == lPattern2))
            return lPattern1

        if (pattern2 is Concur && (pattern2.lPattern1 == lPattern1 || pattern2.lPattern2 == lPattern1))
            return lPattern2

        return lazy { Concur(lPattern1, lPattern2) }
    }

    fun group(lPattern1: LPattern, lPattern2: LPattern): LPattern {
        //  group p NotAllowed = NotAllowed
        //  group NotAllowed p = NotAllowed
        val pattern1 = lPattern1.value
        if (pattern1 is NotAllowed || lPattern2.value is NotAllowed) return lazy { NotAllowed }

        //  group Empty p = p
        if (pattern1 is Empty) return lPattern2

        //  group (After p1 p2) p3 = after p1 (group p2 p3)
        if (pattern1 is After) return after(pattern1.lPattern1, group(pattern1.lPattern2, lPattern2))

        val pattern2 = lPattern2.value
        //  group p Empty = p
        if (pattern2 is Empty) return lPattern1

        //  group p1 (After p2 p3) = after p2 (group p1 p3)
        return if (pattern2 is After) {
            after(pattern2.lPattern1, group(lPattern1, pattern2.lPattern2))
        } else lazy { Group(lPattern1, lPattern2) }
        //  group p1 p2 = Group p1 p2
    }

    fun interleave(lPattern1: LPattern, lPattern2: LPattern): LPattern {
        //  interleave p NotAllowed = NotAllowed
        //  interleave NotAllowed p = NotAllowed
        val pattern1 = lPattern1.value
        if (pattern1 is NotAllowed || lPattern2.value is NotAllowed) {
            return LNotAllowed
        }

        //  interleave Empty p = p
        if (pattern1 is Empty) return lPattern2

        //  interleave (After p1 p2) p3 = after p1 (interleave p2 p3)
        if (pattern1 is After) return after(pattern1.lPattern1, interleave(pattern1.lPattern2, lPattern2))

        val pattern2 = lPattern2.value
        //  interleave p Empty = p
        if (pattern2 is Empty) return lPattern1

        //  interleave p1 (After p2 p3) = after p2 (interleave p1 p3)
        return if (pattern2 is After) {
            after(pattern2.lPattern1, interleave(lPattern1, pattern2.lPattern2))
        } else lazy { Interleave(lPattern1, lPattern2) }
        //  interleave p1 p2 = Interleave p1 p2
    }
}
