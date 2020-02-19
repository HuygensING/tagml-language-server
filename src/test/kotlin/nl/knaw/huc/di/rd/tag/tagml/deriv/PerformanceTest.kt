package nl.knaw.huc.di.rd.tag.tagml.deriv

import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.system.measureNanoTime

class PerformanceTest {

    private fun func() = println("Your secret number is ${Random.nextInt()}")

    @Test
    fun test() {
        val benchmark = measureNanoTime(this::func)
        println(benchmark)
        val a = AtomicInteger()
        val i = a.getAndIncrement()
    }
}