package nl.knaw.huc.di.rd.tag.tagml.deriv

import lambdada.parsec.extension.charsToString
import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import lambdada.parsec.utils.Location
import org.junit.Test

class CSVParserTest {

    @Test
    fun test() {
        val csvIn = "a,b,c"
        val item = not(char(',')).optrep.map { it.charsToString() }
        val csvLine = (item then (char(',') then item).optrep).map(this::flattenItems)
        println(csvIn)
        val csvReader = asReader(csvIn)
        val csvResult = csvLine(csvReader)
        println(csvResult)
        assert(csvResult is Response.Accept)
        println(csvReader.location())
    }

    private fun <A, B> flattenItems(pair: Pair<B, List<Pair<A, B>>>): List<B> = listOf(pair.first) + pair.second.map { it.second }

    private fun asReader(s: String) = MyReader(Reader.string(s))

    class MyReader(private val reader: Reader<Char>) : Reader<Char> {
        override fun location(): Location {
            return reader.location()
        }

        override fun read(): Pair<Char, Reader<Char>>? {
            return reader.read()
        }

    }

}