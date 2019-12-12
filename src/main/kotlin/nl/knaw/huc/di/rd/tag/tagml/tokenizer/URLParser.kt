package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.parser.*

object URLParser {

    private fun <A, B> Pair<A, B>.concatenateLeaves(): String {
        val leftString = asString(first)
        val rightString = asString(second)
        return "$leftString$rightString"
    }

    private fun <A> asString(obj: A): String {
        return when (obj) {
            is Pair<*, *> -> obj.concatenateLeaves()
            is List<*> -> obj.joinToString("") { asString(it) }
            null -> ""
            else -> obj.toString()
        }
    }

    val digit = charIn(CharRange('0', '9'))
    val digits = digit.rep
    val port = digits
    val lowalpha = charIn(CharRange('a', 'z'))
    val highalpha = charIn(CharRange('A', 'Z'))
    val alpha = lowalpha or highalpha
    val alphadigit = alpha or digit
    val domainlabel = (alphadigit then (alphadigit or char('-')).optrep then alphadigit)
    val toplabel = alpha or (alpha then (alphadigit or char('-')).optrep then alphadigit)
    val hostname = (domainlabel then char('.')).optrep then toplabel
    val hostnumber = digits then char('.') then digits then char('.') then digits then char('.') then digits
    val host = hostname or hostnumber
    val hostport = host then (char(':') then port).opt
    val hex = digit or charIn(CharRange('a', 'f')) or charIn(CharRange('A', 'F'))
    val escape = char('%') then hex then hex
    val safe = charIn("""$-_.+""")
    val extra = charIn("""!*'(),""")
    val unreserved = alpha or digit or safe or extra
    val uchar = unreserved or escape
    val hsegment = (uchar or charIn(";:@&=")).optrep
    val search = hsegment
    val hpath = hsegment then (char('/') then hsegment).optrep
    val httpurl = (string("http") then char('s').opt then string("://") then hostport then (char('/') then hpath then (char('?') then search).opt).opt)
            .map { it.concatenateLeaves() }

    val fsegment = hsegment
    val fpath = fsegment then (char('/') then fsegment).optrep
    val fileurl = (string("file://") then (host or string("localhost")).opt then char('/') then fpath)
            .map { it.concatenateLeaves() }

    val url = httpurl or fileurl

}