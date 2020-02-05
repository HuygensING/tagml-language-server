package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.utils.Location
import org.junit.Test

class TokenizerUtilsTest {

    @Test
    fun test() {
        val text = """
            1
            22
            333
            4444
            55555
            666666
            7777777
        """.trimIndent()
        val x = TokenizerUtils.PositionCalculator(text)
        println(text)
        println(text.length)
        println(x.lineLengths)
        for (i in 0..27) {
            val position = x.calculatePosition(Location(i))
            println("$i = (${position.line},${position.character})")
        }
    }
}

