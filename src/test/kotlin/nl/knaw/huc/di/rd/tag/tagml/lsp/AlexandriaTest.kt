package nl.knaw.huc.di.rd.tag.tagml.lsp

import nl.knaw.huygens.alexandria.exporter.ColorPicker
import kotlin.test.assertEquals

fun main() {
    val cp = ColorPicker("rood", "wit", "blauw")
    assertEquals("rood", cp.nextColor())
    assertEquals("wit", cp.nextColor())
    assertEquals("blauw", cp.nextColor())
    assertEquals("rood", cp.nextColor())
}
