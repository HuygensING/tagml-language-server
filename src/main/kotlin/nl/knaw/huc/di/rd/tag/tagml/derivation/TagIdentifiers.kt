package nl.knaw.huc.di.rd.tag.tagml.derivation

object TagIdentifiers {
    class FixedIdentifier(val tagName: String) : TagIdentifier {
        override fun matches(tagName: String): Boolean {
            return this.tagName == tagName
        }

        override fun toString(): String {
            return tagName
        }
    }

    object AnyTagIdentifier : TagIdentifier {
        override fun matches(tagName: String): Boolean {
            return true
        }

        override fun toString(): String {
            return "*"
        }
    }

}