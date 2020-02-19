package nl.knaw.huc.di.rd.tag.tagml.deriv

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.all
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.empty
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.text
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.After
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.ConcurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.HierarchyLevel
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Interleave
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NotAllowed
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.OneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.RangeClose
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.FixedIdentifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatternsTest {

    @Test
    fun testEmptyEquivalency() {
        val p1 = empty()
        val p2 = empty()
        assertThat(p1).isEqualTo(p2)
    }

    @Test
    fun testNotAllowedEquivalency() {
        val p1 = NotAllowed
        val p2 = NotAllowed
        assertThat(p1).isEqualTo(p2)
    }

    @Test
    fun testTextEquivalency() {
        val p1 = text()
        val p2 = text()
        assertThat(p1).isEqualTo(p2)
    }

    @Test
    fun testHierarchyLevelEquivalency() {
        val p1 = HierarchyLevel
        val p2 = HierarchyLevel
        assertThat(p1).isEqualTo(p2)
    }

    @Test
    fun testRangeEquivalency() {
        val p1 = Range(FixedIdentifier("x"), empty())
        val p2 = Range(FixedIdentifier("x"), empty())
        assertThat(p1).isEqualTo(p2)
    }

    @Test
    fun testRangeCloseEquivalency() {
        val p1 = RangeClose(FixedIdentifier("x"))
        val p2 = RangeClose(FixedIdentifier("x"))
        assertThat(p1).isEqualTo(p2)
    }

    @Test
    fun testAfterEquivalency() {
        val p1 = After(text(), empty())
        val p2 = After(text(), empty())
        val p3 = After(empty(), text())
        assertThat(p1).isEqualTo(p2).isNotEqualTo(p3)
    }

    @Test
    fun testChoiceEquivalency() {
        val c1 = Choice(text(), empty())
        val c2 = Choice(text(), empty())
        val c3 = Choice(empty(), text())
        val c4 = Choice(empty(), lazy { RangeClose(FixedIdentifier("some")) })
        assertThat(c1).isEqualTo(c2).isEqualTo(c3)
        assertThat(c2).isEqualTo(c1).isEqualTo(c3)
        assertThat(c3).isEqualTo(c1).isEqualTo(c2)
        assertThat(c4).isNotEqualTo(c2)
    }

    @Test
    fun testConcurEquivalency() {
        val c1 = Concur(text(), empty())
        val c2 = Concur(text(), empty())
        val c3 = Concur(empty(), text())
        val c4 = Concur(empty(), lazy { RangeClose(FixedIdentifier("some")) })
        assertThat(c1).isEqualTo(c2).isEqualTo(c3)
        assertThat(c2).isEqualTo(c1).isEqualTo(c3)
        assertThat(c3).isEqualTo(c1).isEqualTo(c2)
        assertThat(c4).isNotEqualTo(c2)
    }

    @Test
    fun testGroupEquivalency() {
        val p1 = Group(text(), empty())
        val p2 = Group(text(), empty())
        val p3 = Group(empty(), text())
        assertThat(p1).isEqualTo(p2).isNotEqualTo(p3)
    }

    @Test
    fun testInterleaveEquivalency() {
        val c1 = Interleave(text(), empty())
        val c2 = Interleave(text(), empty())
        val c3 = Interleave(empty(), text())
        val c4 = Interleave(empty(), lazy { RangeClose(FixedIdentifier("some")) })
        assertThat(c1).isEqualTo(c2).isEqualTo(c3)
        assertThat(c2).isEqualTo(c1).isEqualTo(c3)
        assertThat(c3).isEqualTo(c1).isEqualTo(c2)
        assertThat(c4).isNotEqualTo(c2)
    }

    @Test
    fun testOneOrMoreEquivalency() {
        val c1 = OneOrMore(text())
        val c2 = OneOrMore(text())
        val c3 = OneOrMore(empty())
        assertThat(c1).isEqualTo(c2).isNotEqualTo(c3)
    }

    @Test
    fun testConcurOneOrMoreEquivalency() {
        val c1 = ConcurOneOrMore(text())
        val c2 = ConcurOneOrMore(text())
        val c3 = ConcurOneOrMore(empty())
        assertThat(c1).isEqualTo(c2).isNotEqualTo(c3)
    }

    @Test
    fun testAfterPatternRepresentationAggregation() {
        val p1 = after(lazy { Range(FixedIdentifier("a"), text()) }, text())
        val p2 = after(lazy { Range(FixedIdentifier("b"), text()) }, text())
        val p3 = after(p1, p2)
        val p4 = after(p3, text()).value
        assertThat(p4.toString()).isEqualTo("""<after><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></after>""")
    }

    @Test
    fun testAllPatternRepresentationAggregation() {
        val p1 = all(lazy { Range(FixedIdentifier("a"), text()) }, text())
        val p2 = all(lazy { Range(FixedIdentifier("b"), text()) }, text())
        val p3 = all(p1, p2)
        val p4 = all(p3, text()).value
        assertThat(p4.toString()).isEqualTo("""<all><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></all>""")
    }

    @Test
    fun testChoicePatternRepresentationAggregation() {
        val p1 = choice(lazy { Range(FixedIdentifier("a"), text()) }, text())
        val p2 = choice(lazy { Range(FixedIdentifier("b"), text()) }, text())
        val p3 = choice(p1, p2)
        val p4 = choice(p3, empty()).value
        assertThat(p4.toString()).isEqualTo("""<choice><range id="a"><text/></range><text/><range id="b"><text/></range><empty/></choice>""")
    }

    @Test
    fun testConcurPatternRepresentationAggregation() {
        val p1 = concur(lazy { Range(FixedIdentifier("a"), text()) }, text())
        val p2 = concur(lazy { Range(FixedIdentifier("b"), text()) }, text())
        val p3 = concur(p1, p2)
        val p4 = concur(p3, empty()).value
        assertThat(p4.toString()).isEqualTo("""<concur><range id="a"><text/></range><range id="b"><text/></range><empty/></concur>""")
    }

    @Test
    fun testGroupPatternRepresentationAggregation() {
        val p1 = group(lazy { Range(FixedIdentifier("a"), text()) }, text())
        val p2 = group(lazy { Range(FixedIdentifier("b"), text()) }, text())
        val p3 = group(p1, p2)
        val p4 = group(p3, text()).value
        assertThat(p4.toString()).isEqualTo("""<group><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></group>""")
    }

    @Test
    fun testInterleavePatternRepresentationAggregation() {
        val rangeA = Range(FixedIdentifier("a"), text())
        val rangeB = Range(FixedIdentifier("b"), text())
        val rangeC = Range(FixedIdentifier("c"), text())
        val rangeD = Range(FixedIdentifier("d"), text())
        val rangeE = Range(FixedIdentifier("e"), text())
        val p1 = interleave(lazy { rangeA }, lazy { rangeB })
        val p2 = interleave(lazy { rangeC }, lazy { rangeD })
        val p3 = interleave(p1, p2)
        val p4 = interleave(p3, lazy { rangeE }).value
        assertThat(p4.toString()).isEqualTo("""<interleave><range id="a"><text/></range><range id="b"><text/></range><range id="c"><text/></range><range id="d"><text/></range><range id="e"><text/></range></interleave>""")
    }
}