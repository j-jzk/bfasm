package cz.j_jzk.bfasm.compile

import java.io.File

class Builder(val buildDir: File, val tapeLength: Int) {
	private val ASM_SOURCE_NAME = "program.asm"
	private val ASM_OBJECT_NAME = "program.o"
	private val C_SOURCE_NAME = "main.c"
	private val BINARY_NAME = "main"

	init {
		buildDir.mkdir()
	}

	/** Generates a C runtime file */
	private fun writeC() {
		val source = """
			unsigned char tape[$tapeLength] = {0};
			extern void bf_main(void);

			int main() {
				bf_main();
				return 0;
			}""".trimIndent()

		File(buildDir, C_SOURCE_NAME).writeText(source)
	}

	private fun writeAsm(source: String) {
		File(buildDir, ASM_SOURCE_NAME).writeText(source)
	}

	private fun buildAsm() {
		ProcessBuilder("nasm -f elf64 -Fdwarf $ASM_SOURCE_NAME".split(" "))
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.directory(buildDir)
			.start()
			.waitFor()
	}

	private fun buildMain() {
		ProcessBuilder("gcc -no-pie $ASM_OBJECT_NAME $C_SOURCE_NAME -o $BINARY_NAME".split(" "))
			.redirectOutput(ProcessBuilder.Redirect.INHERIT)
			.redirectError(ProcessBuilder.Redirect.INHERIT)
			.directory(buildDir)
			.start()
			.waitFor()
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
