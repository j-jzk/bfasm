package cz.j_jzk.bfasm.compile

import java.io.File
import com.github.ajalt.mordant.terminal.Terminal

class BuilderException(val errorCode: Int): Exception() {
	override fun toString() = "Subcommand failed with code $errorCode"
}

class Builder(val buildDir: File, val tapeLength: Int, val dynamicTape: Boolean) {
	private val ASM_SOURCE_NAME = "program.asm"
	private val ASM_OBJECT_NAME = "program.o"
	private val C_SOURCE_NAME = "main.c"
	private val BINARY_NAME = "main"

	private val printer = Terminal()

	init {
		buildDir.mkdir()
	}

	/** Generates a C runtime file */
	private fun writeC() {
		val source =
			if (!dynamicTape)
				"""
				unsigned char tape[$tapeLength] = {0};
				extern void bf_main(void);

				int main() {
					bf_main();
					return 0;
				}""".trimIndent()
			else
				"""
				#include <stdlib.h>
				#include <string.h>
				unsigned char *tape = NULL;
				extern void bf_main(void);

				int main() {
					tape = (unsigned char *)malloc($tapeLength);
					if (tape == NULL) return 1;
					memset(tape, 0, $tapeLength);
					bf_main();
					return 0;
				}
				""".trimIndent()

		File(buildDir, C_SOURCE_NAME).writeText(source)
	}

	private fun writeAsm(source: String) {
		File(buildDir, ASM_SOURCE_NAME).writeText(source)
	}

	private fun buildAsm() {
		printer.muted("nasm -f elf64 -Fdwarf $ASM_SOURCE_NAME", stderr=true)
		
		val returnCode = ProcessBuilder("nasm -f elf64 -Fdwarf $ASM_SOURCE_NAME".split(" "))
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.directory(buildDir)
			.start()
			.waitFor()

		if (returnCode != 0)
			throw BuilderException(returnCode)
	}

	private fun buildMain() {
		printer.muted("gcc -no-pie $ASM_OBJECT_NAME $C_SOURCE_NAME -o $BINARY_NAME", stderr=true)

		val returnCode = ProcessBuilder("gcc -no-pie $ASM_OBJECT_NAME $C_SOURCE_NAME -o $BINARY_NAME".split(" "))
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.directory(buildDir)
			.start()
			.waitFor()

		if (returnCode != 0)
			throw BuilderException(returnCode)
	}

	fun writeFiles(asmSource: String) {
		writeC()
		writeAsm(asmSource)
	}

	fun build() {
		buildAsm()
		buildMain()
	}
}
