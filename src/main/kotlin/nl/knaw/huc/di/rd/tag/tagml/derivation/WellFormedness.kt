package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.text
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.zeroOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.HierarchyLevel
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.LSPToken
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import org.apache.commons.lang3.time.StopWatch
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

object WellFormedness {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    data class WellFormednessResult(val isWellFormed: Boolean, val errors: List<String>, val expectedTokens: Set<TAGMLToken>)

    fun checkWellFormedness(tokens: List<LSPToken>): WellFormednessResult {
        val iterator = tokens.iterator()
        var expectation: Lazy<Pattern> = lazy {
            Range(
                    AnyTagIdentifier,
                    concurOneOrMore(
                            choice(
                                    text(),
                                    zeroOrMore(
                                            lazy { HierarchyLevel }
                                    )
                            )
                    )
            )
        }
        val errors = mutableListOf<String>()
        val expectedTokens = mutableSetOf<TAGMLToken>()
        val stepsXML = StringBuilder("<wellformednesscheck>\n")

        val stepCount = AtomicInteger(1)
        var goOn = iterator.hasNext()
        while (goOn) {
            val sw = StopWatch()
            sw.start()
            LOG.info("step ${stepCount.get()}")
            stepsXML.append("""<step n="${stepCount.getAndIncrement()}">""")
            stepsXML.append("""<expectation>$expectation</expectation>""")
            stepsXML.append("""<expectedTokens>${expectation.value.expectedTokens}</expectedTokens>""")

            val token = iterator.next().token
            LOG.info("  token $token")
            val matches = expectation.value.matches(token)
            sw.stop()
            LOG.info("  match took ${sw.nanoTime} ns ($sw)")
            sw.reset()
            stepsXML.append("""<token matches="$matches"><![CDATA[${token.content}]]></token>""")
            LOG.info("expectation=$expectation, token=${token.content}, match=$matches")
            if (matches) {
                sw.start()
                expectation = expectation.value.deriv(token)
                sw.stop()
                LOG.info("  deriv took ${sw.nanoTime} ns ($sw)")
                goOn = iterator.hasNext()
            } else {
                expectedTokens.addAll(expectation.value.expectedTokens)
                errors.add("Unexpected token: found ${token.content}, but expected ${expectationString(expectation
                        .value)}")
                goOn = false
            }
            stepsXML.append("</step>\n")
        }
        stepsXML.append("""<final_expectation nullable="${expectation
                .value.nullable}">$expectation</final_expectation>""")
//        LOG.info("remaining expectation=$expectation")
        if (errors.isEmpty() && !expectation.value.nullable) {
            expectedTokens.addAll(expectation.value.expectedTokens)
            errors.add("Out of tokens, but expected ${expectationString(expectation.value)}")
        }
        stepsXML.append("\n</wellformednesscheck>")
        LOG.info("steps=\n{}\n", stepsXML)
        return WellFormednessResult(
                !iterator.hasNext() && expectation.value.nullable,
                errors,
                expectedTokens)
    }

    private fun expectationString(expectation: Pattern): String {
        val expectedTokens = expectation.expectedTokens.map { it.content }
        return when (expectedTokens.size) {
            0    -> "nothing"
            1    -> expectedTokens[0]
            else -> "any of $expectedTokens"
        }
    }
}