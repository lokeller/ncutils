/*
 * Copyright (c) 2011, EPFL - ARNI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the EPFL nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <assert.h>
#include <memory.h>
#include <sys/resource.h>
#include <ncutils.h>
#include <field_tables.h>

void test_decode_headers () {
    int i, j, k;

    /* intializes a random generator with a fixed seed to obtain
     * deterministic results.
     */

    srand(12312);

    /**
     * This is the lenght of a codig vector
     */
    int size = 10;

    /*
     * Creates as many linearly independent vectors as possible
     * by drawing random coefficients (this will create errors in
     * general with a small probability, with this random seed it
     * will not)
     *
     */
    ffe vectors[size*size];

    printf("Coding vector matrix\n");
    for ( i = 0; i < size; i++) {
        for ( j = 0; j < size ; j++) {            
            vectors[i*size + j] = rand() & 0xFF;
	    printf("%02X ", vectors[i*size + j]);
        }
	printf("\n");
    }

    /* initialize a decoder */
    coding_vector_decoder_t* d = create_coding_vector_decoder(size);


    /* decode one after another all the coding vectors */
    for ( i = 0 ; i < size ; i++) {

        int innovative = cvd_add_vector(d, vectors + i*size);

        if ( !innovative) {
            printf("Found lineraly dependant vector\n");
        } 

    }

    /* load from the decoder the inverted matrix */
    char invertedMatrix[size * size];

    for ( i= 0 ; i < size ; i++) {
	int id = cvd_decoded_coordinates_get_coordinate(d,i);
    	for ( k = 0 ; k < size ; k++) {
		ffe v2 = cvd_decoded_coordinates_get_coefficients(d,i)[k];
		invertedMatrix[id * size + k] = v2;
	}
    }

   
    /* print it on the screen */
    printf("Inverted coding vector matrix\n");
    for ( i= 0 ; i < size ; i++) {
    	for ( k = 0 ; k < size ; k++) {
		ffe v2 = invertedMatrix[i * size + k];
		printf("%02X ", v2);
	}
	printf("\n");
    }

    /* check that decoding matrix is indeed the inverse of the matrix composed by the input coding vectors */
    for ( i= 0 ; i < size ; i++) {
        for ( j = 0 ; j < size ; j++) {

            ffe sum = 0;
            for ( k = 0 ; k < size ; k++) {
                ffe v1 = vectors[i*size + k];
                ffe v2 = invertedMatrix[k*size + j];
                ffe val1 = mul_table[ (v1 << 8) + v2];

                sum = sum ^ val1;
            }


            assert( !(i == j && sum != 1));

            assert( !(i != j && sum != 0));
        }
    }


}


void test_decode() {

        int i, j, k ;
	/* number of packets in which the segment will be divided */
        int blockNumber = 10;

	/* length of the payload of each packet */
        int payloadLen = 10;        

        /* create the segment */
        unsigned char segment[blockNumber * payloadLen];

        for ( i = 0 ; i < blockNumber ; i++) {
            memset(segment + i*payloadLen, (char) (0XA0 +  i), payloadLen);
        }

        printf(" Input segment: \n");
        for ( i = 0; i < blockNumber ; i++ ) {
	    for ( j = 0 ; j < payloadLen; j++) {
		printf("%02X ", segment[i*payloadLen + j]);
	    }
            printf("\n");
        }

        /* create a set of linear combinations (random code) */

	ffe networkOutput[blockNumber * ( blockNumber * payloadLen)];

	memset(networkOutput, 0, blockNumber * ( blockNumber * payloadLen));

        srand(2131231);

        for ( i = 0 ; i < blockNumber ; i++) {

	    ffe *pos = networkOutput +  i * ( blockNumber + payloadLen);

            for ( j = 0 ; j < blockNumber ; j++) {
                ffe x = rand() & 0xFF;
		pos[j] = x;
		for ( k = 0 ; k < payloadLen ; k++) {
			ffe v1 = segment[j * payloadLen];
			pos[ blockNumber + k] = pos[ blockNumber + k] ^ mul_table[( x << 8 ) + v1];
		}
            }
        }

	/* display the coded packets */
        printf(" Coded packets:\n");
        for ( i = 0; i < blockNumber ; i++ ) {
	    for ( j = 0 ; j < payloadLen + blockNumber; j++) {
		printf("%02X ", networkOutput[i*(payloadLen + blockNumber) + j]);
	    }
            printf("\n");
        }

        /* decode the received packets */
        packet_decoder_t *decoder = create_packet_decoder(blockNumber, payloadLen);

        for ( i = 0; i < blockNumber ; i++) {
            int innovative = pd_add_packet(decoder, networkOutput + i * (blockNumber + payloadLen));

            if (!innovative) {              
		printf("Linearly dependent packet added");
            } 

        }

        printf(" Decoded segment: \n");
        for ( i = 0; i < blockNumber ; i++ ) {
	    for ( j = 0 ; j < payloadLen; j++) {
		printf("%02X ", pd_decoded_packets_get_packet(decoder, i)[j]);
	    }
            printf("\n");
        }

	destroy_packet_decoder(decoder);
}

/*
 * 
 */
int main(int argc, char** argv) {

    test_decode_headers();

    test_decode();
    
    return (EXIT_SUCCESS);
}



