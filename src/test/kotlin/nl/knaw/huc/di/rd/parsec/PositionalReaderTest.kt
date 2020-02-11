package nl.knaw.huc.di.rd.parsec

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.lsp4j.Position
import org.junit.Test

class PositionalReaderTest {

    @Test
    fun test() {
        val string = "Eenie\nMeenie\nMiny\nMoe!"
        var reader = PositionalReader.string(string)
        var r = reader.read()
        assertThat(r).isNotNull
        assertThat(r!!.first).isEqualTo('E')
        assertThat(r.second.lastPosition).isEqualTo(Position(0, 0))
        while (r != null) {
            val char = r.first
            val position = r.second.lastPosition
            println("$char at (${position.line},${position.character})")
            reader = r.second
            r = reader.read()
        }

    }
}