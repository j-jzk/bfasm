package cz.j_jzk.bfasm

import cz.j_jzk.klang.lesana.lesana
import cz.j_jzk.klang.parse.NodeID
import cz.j_jzk.klang.prales.useful.list
import com.github.ajalt.mordant.terminal.Terminal

/**
 * A brainfuck program command/statement.
 * The basic commands are run-length encoded for optimization ("+++" becomes Incr(3))
 */
sealed class BfStatement {
    /** + */
    data class Incr(val count: Int): BfStatement()
    /** - */
    data class Decr(val count: Int): BfStatement()
    /** > */
    data class Right(val count: Int): BfStatement()
    /** < */
    data class Left(val count: Int): BfStatement()

    /** , */
    class Read: BfStatement()
    /** . */
    class Print: BfStatement()

    /** [...] */
    data class Loop(val children: List<BfStatement>): BfStatement()
}

val bfParser = lesana<List<BfStatement>> {
    val command = NodeID<BfStatement>("command")
    val commandList = include(list(command))

    command to def(re("\\++")) { BfStatement.Incr(it.v1.length) }
    command to def(re("-+")) { BfStatement.Decr(it.v1.length) }
    command to def(re(">+")) { BfStatement.Right(it.v1.length) }
    command to def(re("<+")) { BfStatement.Left(it.v1.length) }

    command to def(re(",")) { BfStatement.Read() }
    command to def(re("\\.")) { BfStatement.Print() }

    command to def(re("\\["), commandList, re("\\]")) { (_, cmds, _) -> BfStatement.Loop(cmds) }

    ignoreRegexes("[^+\\-<>.,[\\]]")
    // ignoreRegexes("\\s")

    val top = NodeID<List<BfStatement>>()
    top to def(commandList) { it.v1 }
    setTopNode(top)

    onUnexpectedToken { err ->
        Terminal().danger("${err.got.position.inputId}: character ${err.got.position.character}: $err")
    }
}.getLesana()
