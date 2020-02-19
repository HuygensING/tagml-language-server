package nl.knaw.huc.di.rd

import org.junit.Test
import kotlin.random.Random

class MemoizationTest {

    data class MyClass(val a: Int, val b: Int) {
        val x = a / b

        val res: Int by lazy {
            println("hello")
            a + b
        }

        val calc: Int by lazy { Random.nextInt() }

        val exp: Int
            get() = exp()

        private fun exp(): Int {
            println("exp() called")
            return Random.nextInt()
        }

        private val lazyExp2 by lazy { println("lazyExp2 called");Random.nextInt() }
        val exp2: Int
            get() = lazyExp2
    }

    @Test
    fun test() {
        val a = MyClass(1, 2)
        val b = MyClass(2, 1)
        val s = a.res
        println(s)
        val s2 = a.res
        println(s2)
        println("a.calc: ${a.calc},${a.calc}")
        println("a.exp: ${a.exp},${a.exp}")
        println("a.exp2: ${a.exp2},${a.exp2}")
    }
}