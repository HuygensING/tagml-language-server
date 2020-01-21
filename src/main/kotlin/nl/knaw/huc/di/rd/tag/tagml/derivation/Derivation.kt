package nl.knaw.huc.di.rd.tag.tagml.derivation




//class SchemaLocation() : Pattern {
//    override val empty = false
//}
//
//class NamespaceDefinition() : Pattern {
//    override val empty = false
//}
//
//class StartToken(id: TagIdentifier) : Pattern {
//    override val empty = false
//}
//
//class AnyTagName() : TagIdentifier {}
//
//
//fun Choice.matches(s: TAGMLToken) = this.p1.matches(s) || this.p2.matches(s)
//
//
//fun deriveNext(c: Choice, t: StartTagToken): Pattern = Choice(deriveNext(c.p1, t), deriveNext(c.p2, t))
//fun deriveNext(p: NotAllowed, t: TextToken): Pattern = TODO()
//fun deriveNext(p: NotAllowed, t: EndTagToken): Pattern = TODO()
//
//fun deriveNext(p: Pattern, t: TAGMLToken): Pattern {
//    println("fun deriveNext(p: ${p.javaClass.simpleName}, t:${t.javaClass.simpleName}): Pattern = TODO()")
//    return NotAllowed()
//}
//
//fun Pattern.matches(t: TAGMLToken): Boolean {
//    return true
//}
//
//fun notAllowed(): Pattern {
//    return NOT_ALLOWED
//}
//
//val NOT_ALLOWED: Pattern = NotAllowed()