LIBS=-ll

all: test

test: main.c lex.yy.c
	$(CC) -o $@ $^ $(LIBS)

lex.yy.c: scheme.l
	flex $<
