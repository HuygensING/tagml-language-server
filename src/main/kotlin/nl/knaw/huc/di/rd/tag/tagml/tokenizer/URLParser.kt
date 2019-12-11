package nl.knaw.huc.di.rd.tag.tagml.tokenizer

import lambdada.parsec.parser.*

object URLParser {
    val digit = charIn('0', '9')
    val digits = digit.rep
    val port = digits
    val lowalpha = charIn('a', 'z')
    val highalpha = charIn('A', 'Z')
    val alpha = lowalpha or highalpha
    val alphadigit = alpha or digit
    val domainlabel = alphadigit or (alphadigit then (alphadigit or char('-')).optrep then alphadigit)
    val toplabel = alpha or (alpha then (alphadigit or char('-')).optrep then alphadigit)
    val hostname = (domainlabel then char('.')).optrep then toplabel
    val hostnumber = digits then char('.') then digits then char('.') then digits then char('.') then digits
    val host = hostname or hostnumber
    val hostport = host then (char(':') then port).opt
    val hex = digit or charIn('a', 'f') or charIn('A', 'F')
    val escape = char('%') then hex then hex
    val safe = charIn("""$-_.+""")
    val extra = charIn("""!*'(),""")
    val unreserved = alpha or digit or safe or extra
    val uchar = unreserved or escape
    val hsegment = (uchar or charIn(";:@&=")).optrep
    val search = (uchar or charIn(";:@&=")).optrep
    val hpath = hsegment then (char('/') then hsegment).optrep
    val httpurl = string("http") then char('s').opt then string("://") then hostport then (char('/') then hpath then (char('?') then search).opt).opt
    val fsegment = (uchar or charIn(";:@&=")).optrep
    val fpath = fsegment then (char('/') then fsegment).optrep
    val fileurl = string("file://") then (host or string("localhost")).opt then char('/') then fpath

    val url = httpurl or fileurl

}