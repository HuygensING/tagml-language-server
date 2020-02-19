package nl.knaw.huc.di.rd.tag.tagml.derivation

object TagIdentifiers {
    const val FIXED_IDENTIFIER_HASH_CODE = 2
    const val ANY_TAG_IDENTIFIER_HASH_CODE = 3

    class FixedIdentifier(val tagName: String) : TagIdentifier {
        override fun matches(tagName: String): Boolean = this.tagName == tagName

        override fun toString(): String = tagName

        override fun hashCode(): Int = FIXED_IDENTIFIER_HASH_CODE * tagName.hashCode()

        override fun equals(other: Any?): Boolean = other is FixedIdentifier && other.tagName == tagName
    }

    object AnyTagIdentifier : TagIdentifier {
        override fun matches(tagName: String): Boolean = true

        override fun toString(): String = "*"

        override fun hashCode() = ANY_TAG_IDENTIFIER_HASH_CODE
    }
}