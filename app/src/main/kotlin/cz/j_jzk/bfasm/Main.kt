package cz.j_jzk.bfasm

import cz.j_jzk.klang.input.InputFactory
import cz.j_jzk.klang.parse.SyntaxError
import cz.j_jzk.bfasm.compile.AssemblyGenerator
import cz.j_jzk.bfasm.compile.Builder
import cz.j_jzk.bfasm.compile.BuilderException
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import java.io.File
import com.github.ajalt.mordant.terminal.Terminal
import kotlin.system.exitProcess

class MainCommand: CliktCommand() {
    val inputFile: File by argument().file(mustExist=true, canBeDir=false, mustBeReadable=true)
    val buildDir: File
        by option("-d", "--build-dir", help="directory for the generated files (default .)")
            .file(canBeFile=false, canBeDir=true)
            .default(File("."))

    val tapeLength: Int by option("-t", "--tape-size", help="the nuber of tape cells to allocate (default 256)").int().default(256)
    val dynamicTape: Boolean by option("--dynamic-tape", help="allocate the tape on the heap").flag()

    val noBuild: Boolean by option("-n", "--no-build", help="only generate ASM & C source files, don't build them").flag()

    override fun run() {
        val printer = Terminal()

        printer.info("Generating assembly for '${inputFile.path}'", stderr=true)
        val parsed: List<BfStatement>
        try {
            parsed = bfParser.parse(InputFactory.fromFile(inputFile.path)) as List<BfStatement>
        } catch (e: SyntaxError) {
            printer.danger("Syntax error", stderr=true)
            exitProcess(1)
        }
        val assembly = AssemblyGenerator().genProgram(parsed)

        try {
            val b = Builder(buildDir, tapeLength, dynamicTape)
            b.writeFiles(assembly)
            if (!noBuild) {
                printer.info("Building the binary", stderr=true)
                b.build()
            }
        } catch (e: BuilderException) {
            printer.danger(e.toString(), stderr=true)
            exitProcess(2)
        }

        printer.success("OK")
    }
}

fun main(args: Array<String>) {
    MainCommand().main(args)
}
