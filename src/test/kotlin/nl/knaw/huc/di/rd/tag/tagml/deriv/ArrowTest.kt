package nl.knaw.huc.di.rd.tag.tagml.deriv

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.some
import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import lambdada.parsec.parser.Response.Accept
import lambdada.parsec.parser.Response.Reject
import org.junit.Test
import kotlin.test.assertEquals

class ArrowTest {

    private fun <A, B> flattenItems(pair: Pair<B, List<Pair<A, B>>>): List<B> = listOf(pair.first) + pair.second.map { it.second }

    @Test
    fun test() {
        val item = not(char(',')).optrep.map { it.charsToString() }
        val csvLine = (item then (char(',') then item).optrep).map(this::flattenItems)
        val csvInput = asReader("axolotl,bear,cheetah,donkey,elephant,flamingo,giraffe,hyena,iguana")
        val csvResult = csvLine(csvInput)
//        val items = csvResult.fold({ it.value }, { null })

        when (csvResult) {
            is Accept -> println("accept; value = " + csvResult.value)
            is Reject -> println("reject at " + csvResult.location)
        }

    }

    private fun asReader(s: String) = Reader.string(s)

    @Test
    fun shouldFailsParserReturnsError() {
        val parser = fails<Char, Char>()
        val result = parser(givenAReader()).isSuccess()
        assertEquals(result, false)
    }

    @Test
    fun test1() {
        fun add(a: Int, b: Int): Int = a + b
        fun Int.minus3(): Int = this - 3
        fun <A, B, C> ((A, B) -> C).partial(a: A): (B) -> C = { b: B -> this(a, b) }
        assertEquals(2, 5.minus3())

        val plus4 = ::add.partial(4)
        assertEquals(6, plus4(2))

        val a: Option<Int> = 4.some()
        val r = when (a) {
            is Some -> a.t
            is None -> 0
        }
        assertEquals(4, r)

        val b: Option<Int> = 5.some()
        val res = a.flatMap { x -> b.map { it + x } }
        assertEquals(Some(9), res)
    }

    private fun givenAReader(s: String = "") = Reader.string(s)


    // TODO: make custom reader that encompasses a modelbuilder or at least an expectation stack


}