# bfasm
_Run your Brainfuck programs **blazingly fast**_

An optimizing Brainfuck compiler into x86 assembly.

## Usage
Generates an assembly source for the program and a supporting C main file, then builds them with `nasm` and `gcc`.
(The build step can be disabled by `-n`)

```
Usage: main [<options>] <inputfile>

Options:
  -d, --build-dir=<path>  directory for the generated files
  -t, --tape-size=<int>   the nuber of tape cells to allocate
  -n, --no-build          only generate ASM & C source files, don't build them
  -h, --help              Show this message and exit
```
