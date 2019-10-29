package nl.knaw.huc.di.rd.tag.tagml.lsp

import nl.knaw.huygens.alexandria.exporter.ColorPicker

fun main() {
    val cp = ColorPicker("rood", "wit", "blauw")
    println(cp.nextColor())
    println(cp.nextColor())
    println(cp.nextColor())
    println(cp.nextColor())
}
