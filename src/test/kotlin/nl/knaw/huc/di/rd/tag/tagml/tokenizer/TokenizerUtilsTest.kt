package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.utils.Location
import nl.knaw.huc.di.rd.tag.tagml.lsp.PositionCalculator
import org.junit.Test

class TokenizerUtilsTest {

    @Test
    fun testPositionCalculator() {
        val text = """
            1
            22
            333
            4444
            55555
            666666
            7777777
        """.trimIndent()
        val x = PositionCalculator(text)
        println(text)
        println(text.length)
        println(x.lineLengths)
        for (i in 0..27) {
            val position = x.calculatePosition(Location(i))
            println("$i = (${position.line},${position.character})")
        }
    }

    @Test
    fun testPositionCalculator2() {
        val text = ""
        val x = PositionCalculator(text)
        println(text)
        println(text.length)
        println(x.lineLengths)
        for (i in 0..1) {
            val position = x.calculatePosition(Location(i))
            println("$i = (${position.line},${position.character})")
        }
    }

}

