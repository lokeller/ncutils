all: example

CFLAGS += -I../../src/c -g

example: example.o ../../src/c/coding_vector_decoder.o ../../src/c/packet_decoder.o ../../src/c/field_tables.o
	gcc -o example $^ -lm 

test_rate : CFLAGS += -DFIELD_SIZE=256 -pg -g
test_rate : test_rate.o libncutils-256.a
	gcc -o test_rate $^ -lm -pg
clean:
	rm -f *.o example ../../src/c/*.o
