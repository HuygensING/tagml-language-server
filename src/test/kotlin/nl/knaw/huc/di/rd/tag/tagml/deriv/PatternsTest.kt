package nl.knaw.huc.di.rd.tag.tagml.deriv

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.all
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.RangeClose
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.TEXT
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatternsTest {

    @Test
    fun testChoiceEquivalency() {
        val c1 = Choice(TEXT, EMPTY)
        val c2 = Choice(TEXT, EMPTY)
        val c3 = Choice(EMPTY, TEXT)
        val c4 = Choice(EMPTY, RangeClose(FixedIdentifier("some")))
        assertThat(c1).isEqualTo(c2)
        assertThat(c1).isEqualTo(c3)
        assertThat(c2).isEqualTo(c1)
        assertThat(c2).isEqualTo(c3)
        assertThat(c3).isEqualTo(c1)
        assertThat(c3).isEqualTo(c2)
        assertThat(c4).isNotEqualTo(c2)
    }

    @Test
    fun testAfterPatternRepresentationAggregation() {
        val p1 = after(Range(FixedIdentifier("a"), TEXT), TEXT)
        val p2 = after(Range(FixedIdentifier("b"), TEXT), TEXT)
        val p3 = after(p1, p2)
        val p4 = after(p3, TEXT)
        assertThat(p4.toString()).isEqualTo("""<after><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></after>""")
    }

    @Test
    fun testAllPatternRepresentationAggregation() {
        val p1 = all(Range(FixedIdentifier("a"), TEXT), TEXT)
        val p2 = all(Range(FixedIdentifier("b"), TEXT), TEXT)
        val p3 = all(p1, p2)
        val p4 = all(p3, TEXT)
        assertThat(p4.toString()).isEqualTo("""<all><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></all>""")
    }

    @Test
    fun testChoicePatternRepresentationAggregation() {
        val p1 = choice(Range(FixedIdentifier("a"), TEXT), TEXT)
        val p2 = choice(Range(FixedIdentifier("b"), TEXT), TEXT)
        val p3 = choice(p1, p2)
        val p4 = choice(p3, EMPTY)
        assertThat(p4.toString()).isEqualTo("""<choice><range id="a"><text/></range><text/><range id="b"><text/></range><empty/></choice>""")
    }

    @Test
    fun testConcurPatternRepresentationAggregation() {
        val p1 = concur(Range(FixedIdentifier("a"), TEXT), TEXT)
        val p2 = concur(Range(FixedIdentifier("b"), TEXT), TEXT)
        val p3 = concur(p1, p2)
        val p4 = concur(p3, EMPTY)
        assertThat(p4.toString()).isEqualTo("""<concur><range id="a"><text/></range><range id="b"><text/></range><empty/></concur>""")
    }

    @Test
    fun testGroupPatternRepresentationAggregation() {
        val p1 = group(Range(FixedIdentifier("a"), TEXT), TEXT)
        val p2 = group(Range(FixedIdentifier("b"), TEXT), TEXT)
        val p3 = group(p1, p2)
        val p4 = group(p3, TEXT)
        assertThat(p4.toString()).isEqualTo("""<group><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></group>""")
    }

    @Test
    fun testInterleavePatternRepresentationAggregation() {
        val rangeA = Range(FixedIdentifier("a"), TEXT)
        val rangeB = Range(FixedIdentifier("b"), TEXT)
        val rangeC = Range(FixedIdentifier("c"), TEXT)
        val rangeD = Range(FixedIdentifier("d"), TEXT)
        val rangeE = Range(FixedIdentifier("e"), TEXT)
        val p1 = interleave(rangeA, rangeB)
        val p2 = interleave(rangeC, rangeD)
        val p3 = interleave(p1, p2)
        val p4 = interleave(p3, rangeE)
        assertThat(p4.toString()).isEqualTo("""<interleave><range id="a"><text/></range><range id="b"><text/></range><range id="c"><text/></range><range id="d"><text/></range><range id="e"><text/></range></interleave>""")
    }

}