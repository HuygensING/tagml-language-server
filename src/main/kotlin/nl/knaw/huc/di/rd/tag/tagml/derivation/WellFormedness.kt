package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.choice
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.concurOneOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Constructors.zeroOrMore
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Range
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.Text
import nl.knaw.huc.di.rd.tag.tagml.derivation.TagIdentifiers.AnyTagIdentifier
import nl.knaw.huc.di.rd.tag.tagml.tokenizer.TAGMLToken
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

object WellFormedness {
    private val LOG = LoggerFactory.getLogger(this::class.java)

    data class WellFormednessResult(val isWellFormed: Boolean, val errors: List<String>, val expectedTokens: List<TAGMLToken>)

    fun checkWellFormedness(tokens: List<TAGMLToken>): WellFormednessResult {
        val iterator = tokens.iterator()
        var expectation: Pattern = Range(
                AnyTagIdentifier(),
                concurOneOrMore(
                        choice(
                                Text(),
                                zeroOrMore(
                                        Range(AnyTagIdentifier(), Text())
                                )
                        )
                )
        )
        val errors = mutableListOf<String>()
        val expectedTokens = mutableListOf<TAGMLToken>()
        var stepsXML = "<wellformednesscheck>\n"

        val stepCount = AtomicInteger(1)
        var goOn = iterator.hasNext()
        while (goOn) {
            stepsXML += """<step n="${stepCount.getAndIncrement()}">"""
            stepsXML += """<expectation>$expectation</expectation>"""

            val token = iterator.next()
            val matches = expectation.matches(token)
            stepsXML += """<token matches="$matches"><![CDATA[${token.content}]]></token>"""
            LOG.info("expectation=$expectation, token=${token.content}, match=$matches")
            if (matches) {
                expectation = expectation.deriv(token)
                goOn = iterator.hasNext()
            } else {
//                _log.error("Unexpected token: found $token, but expected ${expectation.expectedTokens()}")
                expectedTokens.addAll(expectation.expectedTokens())
                errors.add("Unexpected token: found ${token.content}, but expected ${expectationString(expectation)}")
                goOn = false
            }
            stepsXML += "</step>\n"
        }
        stepsXML += """<final_expectation nullable="${expectation.nullable}">$expectation</final_expectation>"""
        LOG.info("remaining expectation=$expectation")
        if (errors.isEmpty() && !expectation.nullable) {
            expectedTokens.addAll(expectation.expectedTokens())
            errors.add("Out of tokens, but expected ${expectationString(expectation)}")
        }
        stepsXML += "\n</wellformednesscheck>"
        LOG.info("steps=\n{}\n", stepsXML)
        return WellFormednessResult(
                !iterator.hasNext() && expectation.nullable,
                errors,
                expectedTokens)
    }


    private fun expectationString(expectation: Pattern): String {
        val expectedTokens = expectation.expectedTokens().map { it.content }
        return when (expectedTokens.size) {
            0 -> "nothing"
            1 -> expectedTokens[0]
            else -> "any of $expectedTokens"
        }
    }

}