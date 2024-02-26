package cz.j_jzk.bfasm.compile

import cz.j_jzk.bfasm.BfStatement

class AssemblyGenerator {
    private val header = """
    bits 64
    section .data
    extern tape

    section .text
    extern putchar
    extern getchar

    global bf_main
bf_main:
    enter 0,0
    mov rbx, 0
"""
    // TODO: restore rbx value?
    private val footer = """
    leave
    ret
"""

    /*
    * The tape pointer is stored in RBX.
    */

    // TODO: use StringBuilders more effectively (ideally use just one)

    private fun genStatement(statement: BfStatement): String =
        when (statement) {
            is BfStatement.Right -> "    add rbx, ${statement.count}\n"
            is BfStatement.Left -> "    sub rbx, ${statement.count}\n"
            is BfStatement.Incr -> "    add byte [tape + rbx], ${statement.count}\n"
            is BfStatement.Decr -> "    sub byte [tape + rbx], ${statement.count}\n"
            is BfStatement.Read ->
"""    call getchar
    mov [tape + rbx], al
"""
            is BfStatement.Print ->
"""    mov dil, [tape + rbx]
    call putchar
"""
            is BfStatement.Loop -> genLoop(statement)
        }

    // used for generating loop labels
    private var loopCount = 0
    private fun genLoop(loop: BfStatement.Loop): String {
        val beginLbl = ".begin_l${loopCount}"
        val endLbl = ".end_l${loopCount}"
        loopCount++

        val result = StringBuilder()
        result.appendLine("${beginLbl}:")
        result.appendLine("    cmp byte [tape + rbx], 0")
        result.appendLine("    je ${endLbl}")
        result.append(genList(loop.children))
        result.appendLine("    jmp ${beginLbl}")
        result.appendLine("${endLbl}:\n")
        return result.toString()
    }

    private fun genList(statements: List<BfStatement>): String = statements.map(::genStatement).joinToString("")

    fun genProgram(source: List<BfStatement>): String =
        header + genList(source) + footer
}
