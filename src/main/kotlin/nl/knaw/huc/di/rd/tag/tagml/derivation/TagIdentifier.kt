package nl.knaw.huc.di.rd.tag.tagml.derivation

interface TagIdentifier {
    fun matches(tagName: String): Boolean
}