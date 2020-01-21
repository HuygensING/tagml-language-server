package nl.knaw.huc.di.rd.tag.tagml.derivation

import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.EMPTY
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.NOT_ALLOWED
import nl.knaw.huc.di.rd.tag.tagml.derivation.Patterns.TEXT

object Constructors {

    /*
  Constructors

  When we create a derivative, we often need to create a new pattern.
  These constructors take into account special handling of NotAllowed, Empty and After patterns.
   */

    //  choice :: Pattern -> Pattern -> Pattern
    fun choice(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  choice p NotAllowed = p
        if (pattern1 is NotAllowed) {
            return pattern2
        }
        //  choice NotAllowed p = p
        if (pattern2 is NotAllowed) {
            return pattern1
        }
        //  choice Empty Empty = Empty
        return if (pattern1 is Empty && pattern2 is Empty) {
            pattern1
        } else Choice(pattern1, pattern2)
        //  choice p1 p2 = Choice p1 p2
    }

    //  group :: Pattern -> Pattern -> Pattern
    fun group(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  group p NotAllowed = NotAllowed
        //  group NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) {
            return notAllowed()
        }
        //  group p Empty = p
        if (pattern2 is Empty) {
            return pattern1
        }
        //  group Empty p = p
        if (pattern1 is Empty) {
            return pattern2
        }
        //  group (After p1 p2) p3 = after p1 (group p2 p3)
        if (pattern1 is After) {
            return after(pattern1.pattern1, group(pattern1.pattern2, pattern2))
        }
        //  group p1 (After p2 p3) = after p2 (group p1 p3)
        return if (pattern2 is After) {
            after(pattern2.pattern1, group(pattern1, pattern2.pattern2))
        } else Group(pattern1, pattern2)
        //  group p1 p2 = Group p1 p2
    }

    //  interleave :: Pattern -> Pattern -> Pattern
    fun interleave(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  interleave p NotAllowed = NotAllowed
        //  interleave NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) {
            return notAllowed()
        }
        //  interleave p Empty = p
        if (pattern2 is Empty) {
            return pattern1
        }
        //  group Empty p = p
        if (pattern1 is Empty) {
            return pattern2
        }
        //  interleave (After p1 p2) p3 = after p1 (interleave p2 p3)
        if (pattern1 is After) {
            return after(pattern1.pattern1, interleave(pattern1.pattern2, pattern2))
        }
        //  interleave p1 (After p2 p3) = after p2 (interleave p1 p3)
        return if (pattern2 is After) {
            after(pattern2.pattern1, interleave(pattern1, pattern2.pattern2))
        } else Interleave(pattern1, pattern2)
        //  interleave p1 p2 = Interleave p1 p2
    }

    //  concur :: Pattern -> Pattern -> Pattern
    fun concur(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  concur p NotAllowed = NotAllowed
        //  concur NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) {
            return notAllowed()
        }

        //  concur p Text = p
        if (pattern2 is Text) {
            return pattern1
        }

        //  concur Text p = p
        if (pattern1 is Text) {
            return pattern2
        }

        //  concur (After p1 p2) (After p3 p4) = after (all p1 p3) (concur p2 p4)
        if (pattern1 is After && pattern2 is After) {
            val p1 = pattern1.pattern1
            val p2 = pattern1.pattern2
            val p3 = pattern2.pattern1
            val p4 = pattern2.pattern2
            return after(all(p1, p3), concur(p2, p4))
        }

        //  concur (After p1 p2) p3 = after p1 (concur p2 p3)
        if (pattern1 is After) {
            val p1 = pattern1.pattern1
            val p2 = pattern1.pattern2
            return after(p1, concur(p2, pattern2))
        }

        //  concur p1 (After p2 p3) = after p2 (concur p1 p3)
        if (pattern2 is After) {
            val p2 = pattern2.pattern1
            val p3 = pattern2.pattern2
            return after(p2, concur(pattern1, p3))
        }

        //  concur p1 p2 = Concur p1 p2
        return Concur(pattern1, pattern2)
    }

    //  partition :: Pattern -> Pattern
    fun partition(pattern: Pattern): Pattern {
        //  partition NotAllowed = NotAllowed
        //  partition Empty = Empty
        return if (pattern is NotAllowed
                || pattern is Empty) {
            pattern
        } else Partition(pattern)
        //  partition p = Partition p
    }

    //  oneOrMore :: Pattern -> Pattern
    internal fun oneOrMore(pattern: Pattern): Pattern {
        //  oneOrMore NotAllowed = NotAllowed
        //  oneOrMore Empty = Empty
        return if (pattern is NotAllowed
                || pattern is Empty) {
            pattern
        } else OneOrMore(pattern)
        //  oneOrMore p = OneOrMore p
    }

    //  concurOneOrMore :: Pattern -> Pattern
    internal fun concurOneOrMore(pattern: Pattern): Pattern {
        //  concurOneOrMore NotAllowed = NotAllowed
        //  concurOneOrMore Empty = Empty
        return if (pattern is NotAllowed
                || pattern is Empty) {
            pattern
        } else ConcurOneOrMore(pattern)
        //  concurOneOrMore p = ConcurOneOrMore p
    }

    //  after :: Pattern -> Pattern -> Pattern
    fun after(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  after p NotAllowed = NotAllowed
        //  after NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed
                || pattern2 is NotAllowed) {
            return notAllowed()
        }

        //  after Empty p = p
        if (pattern1 is Empty) {
            return pattern2
        }

        //  after (After p1 p2) p3 = after p1 (after p2 p3)
        if (pattern1 is After) {
            val p1 = pattern1.pattern1
            val p2 = pattern1.pattern2
            return after(p1, after(p2, pattern2))
        }

        //  after p1 p2 = After p1 p2
        return After(pattern1, pattern2)
    }

    //  all :: Pattern -> Pattern -> Pattern
    internal fun all(pattern1: Pattern, pattern2: Pattern): Pattern {
        //  all p NotAllowed = NotAllowed
        //  all NotAllowed p = NotAllowed
        if (pattern1 is NotAllowed || pattern2 is NotAllowed) {
            return notAllowed()
        }

        //  all p Empty = if nullable p then Empty else NotAllowed
        if (pattern2 is Empty) {
            return if (pattern1.isNullable) empty() else notAllowed()
        }

        //  all Empty p = if nullable p then Empty else NotAllowed
        if (pattern1 is Empty) {
            return if (pattern2.isNullable) empty() else notAllowed()
        }

        //  all (After p1 p2) (After p3 p4) = after (all p1 p3) (all p2 p4)
        if (pattern1 is After && pattern2 is After) {
            val p1 = pattern1.pattern1
            val p2 = pattern1.pattern2
            val p3 = pattern2.pattern1
            val p4 = pattern2.pattern2
            return after(all(p1, p3), all(p2, p4))
        }

        //  all p1 p2 = All p1 p2
        return All(pattern1, pattern2)
    }

    fun empty(): Pattern {
        return EMPTY
    }

    fun notAllowed(): Pattern {
        return NOT_ALLOWED
    }

    fun text(): Pattern {
        return TEXT
    }

    internal fun zeroOrMore(pattern: Pattern): Pattern {
        return choice(oneOrMore(pattern), empty())
    }

    internal fun concurZeroOrMore(pattern: Pattern): Pattern {
        return choice(concurOneOrMore(pattern), empty())
    }

    internal fun optional(pattern: Pattern): Pattern {
        return choice(pattern, empty())
    }

    internal fun mixed(pattern: Pattern): Pattern {
        return interleave(text(), pattern)
    }

    fun nullPattern(): Pattern {
        return Empty()
    }

    fun anyContent(): Pattern {
        return text()
    }

}
