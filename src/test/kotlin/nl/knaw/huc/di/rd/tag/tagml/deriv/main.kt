package nl.knaw.huc.di.rd.tag.tagml.deriv

import lambdada.parsec.io.Reader
import lambdada.parsec.parser.*
import lambdada.parsec.parser.Response.Accept
import lambdada.parsec.parser.Response.Reject

fun main() {
// The example for a `Parser<Char, List<String>>`
    val foo: Parser<Char, List<Char>> = not(char(',')).rep
    val input = Reader.string("hello, parsec!")
    val result = foo(input)
    when (result) {
        is Accept -> println("good")
        is Reject -> println("bad")
    }
    println(result)
    val foo2: Parser<Char, List<Char>> = not(char('!')).rep
    val foo12 = foo then foo2
    val result2 = foo12(input)
    println(result2)
    when (result2) {
        is Accept -> println("good")
        is Reject -> println("bad")
    }

}
