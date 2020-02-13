package nl.knaw.huc.di.rd.tag

import org.openjdk.jmh.annotations.Benchmark

open class Benchmark {

    @Benchmark
    fun benchmark() {
        val a = 1
    }
}