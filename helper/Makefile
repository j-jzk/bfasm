CFLAGS += $(shell gcc -v 2>&1 | grep enable-default-pie > /dev/null && echo \-no\-pie)
CFLAGS += -g -std=c99

main: compiled.o main.c
	gcc $(CFLAGS) compiled.o main.c -o main

compiled.o: compiled.asm
	nasm -g -f elf64 -Fdwarf compiled.asm
