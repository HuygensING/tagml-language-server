package nl.knaw.huc.di.rd.tag.tagml.derivation

object TagIdentifiers {
    class FixedIdentifier(val tagName: String) : TagIdentifier {
        override fun matches(tagName: String): Boolean {
            return this.tagName == tagName
        }
    }

    class AnyTagIdentifier() : TagIdentifier {
        override fun matches(tagName: String): Boolean {
            return true
        }
    }

}