CFLAGS += -fPIC -I../c

JAVA_INCLUDE = /usr/java/default/include/

all: libncutils.so

libncutils.so: Encoder.o Decoder.o ../c/coding_vector_decoder.o ../c/packet_decoder.o ../c/field_tables.o
	gcc $(CFLAGS) -I$(JAVA_INCLUDE)/include/ -I$(JAVA_INCLUDE)/linux --shared -o libncutils.so $^

clean:
	rm -f *.o libnctils.so NCUtilsC/*.o ../c/*.o libncutils.so
