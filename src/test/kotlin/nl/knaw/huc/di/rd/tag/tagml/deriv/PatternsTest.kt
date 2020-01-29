package nl.knaw.huc.di.rd.tag.tagml.deriv

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.after
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.all
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concur
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.group
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.TEXT
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers
import org.assertj.core.api.Assertions
import org.junit.Test

class PatternsTest {

    @Test
    fun testAfterPatternRepresentationAggregation() {
        val p1 = after(Patterns.Range(TagIdentifiers.FixedIdentifier("a"), TEXT), TEXT)
        val p2 = after(Patterns.Range(TagIdentifiers.FixedIdentifier("b"), TEXT), TEXT)
        val p3 = after(p1, p2)
        val p4 = after(p3, TEXT)
        Assertions.assertThat(p4.toString()).isEqualTo("""<after><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></after>""")
    }

    @Test
    fun testAllPatternRepresentationAggregation() {
        val p1 = all(Patterns.Range(TagIdentifiers.FixedIdentifier("a"), TEXT), TEXT)
        val p2 = all(Patterns.Range(TagIdentifiers.FixedIdentifier("b"), TEXT), TEXT)
        val p3 = all(p1, p2)
        val p4 = all(p3, TEXT)
        Assertions.assertThat(p4.toString()).isEqualTo("""<all><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></all>""")
    }

    @Test
    fun testChoicePatternRepresentationAggregation() {
        val p1 = choice(Patterns.Range(TagIdentifiers.FixedIdentifier("a"), TEXT), TEXT)
        val p2 = choice(Patterns.Range(TagIdentifiers.FixedIdentifier("b"), TEXT), TEXT)
        val p3 = choice(p1, p2)
        val p4 = choice(p3, Patterns.EMPTY)
        Assertions.assertThat(p4.toString()).isEqualTo("""<choice><range id="a"><text/></range><text/><range id="b"><text/></range><empty/></choice>""")
    }

    @Test
    fun testConcurPatternRepresentationAggregation() {
        val p1 = concur(Patterns.Range(TagIdentifiers.FixedIdentifier("a"), TEXT), TEXT)
        val p2 = concur(Patterns.Range(TagIdentifiers.FixedIdentifier("b"), TEXT), TEXT)
        val p3 = concur(p1, p2)
        val p4 = concur(p3, EMPTY)
        Assertions.assertThat(p4.toString()).isEqualTo("""<concur><range id="a"><text/></range><range id="b"><text/></range><empty/></concur>""")
    }

    @Test
    fun testGroupPatternRepresentationAggregation() {
        val p1 = group(Patterns.Range(TagIdentifiers.FixedIdentifier("a"), TEXT), TEXT)
        val p2 = group(Patterns.Range(TagIdentifiers.FixedIdentifier("b"), TEXT), TEXT)
        val p3 = group(p1, p2)
        val p4 = group(p3, TEXT)
        Assertions.assertThat(p4.toString()).isEqualTo("""<group><range id="a"><text/></range><text/><range id="b"><text/></range><text/><text/></group>""")
    }

}