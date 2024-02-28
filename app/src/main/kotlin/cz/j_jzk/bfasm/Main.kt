package cz.j_jzk.bfasm

import cz.j_jzk.klang.input.InputFactory
import cz.j_jzk.bfasm.compile.AssemblyGenerator
import cz.j_jzk.bfasm.compile.Builder
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File



class MainCommand: CliktCommand() {
    val inputFile: File by argument().file(mustExist=true, canBeDir=false, mustBeReadable=true)
    val buildDir: File
        by option("-d", "--build-dir", help="directory for the generated files")
            .file(canBeFile=false, canBeDir=true)
            .default(File("."))
    
    val tapeLength: Int by option("--tape-size", help="the nuber of tape cells to allocate").int().default(256)

    override fun run() {
        //println("reading ${inputFile}, allocatin ${tapeLength} memory, building into ${buildDir}")
        val parsed = bfParser.parse(InputFactory.fromFile(inputFile.path)) as List<BfStatement>
        val assembly = AssemblyGenerator().genProgram(parsed)

        val b = Builder(buildDir, tapeLength)
        b.writeFiles(assembly)
        b.build()
    }
}

fun main(args: Array<String>) {
    // val input = InputFactory.fromString(readLine()!!, "STDIN")
    // println(bfParser.parse(input))
    // val ast = bfParser.parse(InputFactory.fromStdin()) as List<BfStatement>
    // println(AssemblyGenerator().genProgram(ast))
    MainCommand().main(args)
}
