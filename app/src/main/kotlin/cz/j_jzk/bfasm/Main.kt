package cz.j_jzk.bfasm

import cz.j_jzk.klang.input.InputFactory
import cz.j_jzk.bfasm.compile.AssemblyGenerator

fun main() {
    // val input = InputFactory.fromString(readLine()!!, "STDIN")
    // println(bfParser.parse(input))
    val ast = bfParser.parse(InputFactory.fromStdin()) as List<BfStatement>
    println(AssemblyGenerator().genProgram(ast))
}
