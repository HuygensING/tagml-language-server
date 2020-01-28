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
    private val port = digits
    private val lowalpha = charIn(CharRange('a', 'z'))
    private val highalpha = charIn(CharRange('A', 'Z'))
    private val alpha = lowalpha or highalpha
    private val alphadigit = alpha or digit
    val domainlabel = (alphadigit then (alphadigit or char('-')).optrep then alphadigit)
    private val toplabel = alpha or (alpha then (alphadigit or char('-')).optrep then alphadigit)
    val hostname = (domainlabel then char('.')).optrep then toplabel
    private val hostnumber = digits then char('.') then digits then char('.') then digits then char('.') then digits
    private val host = hostname or hostnumber
    val hostport = host then (char(':') then port).opt
    private val hex = digit or charIn(CharRange('a', 'f')) or charIn(CharRange('A', 'F'))
    private val escape = char('%') then hex then hex
    private val safe = charIn("""$-_.+""")
    private val extra = charIn("""!*'(),""")
    private val unreserved = alpha or digit or safe or extra
    private val uchar = unreserved or escape
    private val hsegment = (uchar or charIn(";:@&=")).optrep
    private val search = hsegment
    private val hpath = hsegment then (char('/') then hsegment).optrep
    private val httpurl = (string("http") then char('s').opt then string("://") then hostport then (char('/') then hpath then (char('?') then search).opt).opt)
            .map { it.concatenateLeaves() }

    private val fsegment = hsegment
    private val fpath = fsegment then (char('/') then fsegment).optrep
    private val fileurl = (string("file://") then (host or string("localhost")).opt then char('/') then fpath)
            .map { it.concatenateLeaves() }

    val url = httpurl or fileurl

}