package nl.knaw.huc.di.rd.tag.tagml.derivation

object TagIdentifiers {
    class FixedIdentifier(val tagName: String) : TagIdentifier {
        override fun matches(tagName: String): Boolean = this.tagName == tagName

        override fun toString(): String = tagName
    }

    object AnyTagIdentifier : TagIdentifier {
        override fun matches(tagName: String): Boolean = true

        override fun toString(): String = "*"
    }

}