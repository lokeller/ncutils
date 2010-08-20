/*
 * Copyright (c) 2010, EPFL - ARNI
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

#include "ncutils.h"

void test_decode_headers () {
    int i, j, k;

    finite_field_t* ff = create_extension_field(2,8);

    /* intializes a random generator with a fixed seed to obtain
     * deterministic results.
     */

    srand(12312);

    /**
     * This is the number of different blocks that are being sent
     */
    int size = 10;

    /*
     * Creates as many linearly independent vectors as possible
     * by drawing random coefficients (this will create errors in
     * general with a small probability, with this random seed it
     * will not)
     *
     */
    vector_t* vectors[size];

    for ( i = 0; i < size; i++) {
        vectors[i] = create_vector(size, ff);

        for ( j = 0; j < size ; j++) {
            //if (i == j) vectors[i]->coordinates[j] = 1;
            vector_set_coordinate(vectors[i], j, rand() % ff->q);
        }

    }

    /* initialize a decoder */
    coding_vector_decoder_t* d = create_coding_vector_decoder(size, ff);

    for ( i = 0 ; i < 10 ; i++) {
        char string[300];
        vector_to_string(vectors[i], string, 300);
        printf("%s\n", string);
    }

    /* store the start time of the decoding */
    long m = time(NULL);

    vector_t* inverse[size];

    /* decode one after another all the coding vectors */
    for ( i = 0 ; i < size ; i++) {

        /* the decode method returns the blocks that can be decoded
         * from the vector given as a parameter and the vectors that
         * were previously inserted
         */

        decoded_coordinates_t *o = coding_vector_decoder_add_vector(d, vectors[i]);

        if ( o == 0) {
            printf("Found lineraly dependant vector\n");
            continue;
        }

        
        printf("Decoded at round %d\n", i);
        for ( j = 0; j < o->count ; j++) {
            inverse[o->coordinates[j]] = vector_copy(o->coefficients[j]);
            char string[400];
            vector_to_string(inverse[o->coordinates[j]], string, 400);
            //vector_to_string(o->coefficients[j], string, 400);
            printf("%d -  %s\n\n", inverse[o->coordinates[j]]->length, string);
        }

        destroy_decoded_coordinates(o);

    }
    printf("\n");
    for ( i = 0 ; i < 10 ; i++) {
        char string[300];
        vector_to_string(inverse[i], string, 300);
        printf("%s\n", string);
    }


    for ( i= 0 ; i < size ; i++) {
        for ( j = 0 ; j < size ; j++) {

            int sum = 0;
            for ( k = 0 ; k < size ; k++) {
                int v1 = vector_get_coordinate(vectors[i], k);
                int v2 = vector_get_coordinate(inverse[k],j);
                int val1 = ff_mul(ff, v1, v2);

                sum = ff_sum(ff, sum, val1);
            }


            assert( !(i == j && sum != 1));

            assert( !(i != j && sum != 0));
        }
    }

    printf("Total decoding time : %ld\n" , time(NULL)  - m);

}


void test_decode() {


        finite_field_t *ff = create_extension_field(2,8);


        struct rlimit limit;

        limit.rlim_cur = 6553500;
        limit.rlim_max = 6553500;

        if (setrlimit(RLIMIT_DATA, &limit) != 0) {
          printf("setrlimit() failed with errno=\n");
          exit(1);
        }


        int i, j ;
        int blockNumber = 10;
        int payloadLen = 10;        

        /* create the uncoded packets */
        uncoded_packet_t **inputPackets = (uncoded_packet_t **) malloc(sizeof(uncoded_packet_t *)* blockNumber);

        char *payload = (char *) malloc(payloadLen);

        for ( i = 0 ; i < blockNumber ; i++) {
            memset(payload, (char) (0XA0 +  i), payloadLen);
            inputPackets[i] = create_uncoded_packet(i, payload, payloadLen);
        }

        free(payload);

        printf(" Input blocks: \n");
        for ( i = 0; i < blockNumber ; i++ ) {
            char output[500];
            uncoded_packet_to_string(inputPackets[i], output, 500);
            printf("%s\n", output);
        }

        /* prepare the input packets to be sent on the network */
        coded_packet_t **codewords = (coded_packet_t **) malloc(sizeof(coded_packet_t*) * blockNumber);

        for ( i = 0 ; i < blockNumber ; i++) {
            codewords[i] = create_coded_packet_from_uncoded(inputPackets[i], blockNumber, ff);
        }

        printf(" Codewords:\n");
        for ( i = 0; i < blockNumber ; i++ ) {
            char output[500];
            cp_to_string(codewords[i], output, 500);
            printf("%s\n", output);
        }

        /* create a set of linear combinations that simulate
         * the output of the network
         */

        coded_packet_t** networkOutput = (coded_packet_t **) malloc(sizeof(coded_packet_t*)*blockNumber);

        srand(2131231);

        for ( i = 0 ; i < blockNumber ; i++) {

            networkOutput[i] = create_coded_packet(blockNumber, payloadLen, ff);

            for ( j = 0 ; j < blockNumber ; j++) {
                int x = rand() % ff->q;
                coded_packet_t *copy = cp_scalar_multiply(codewords[j], x);
                coded_packet_t *tmp = cp_add(copy, networkOutput[i]);
                destroy_coded_packet(copy);
                destroy_coded_packet(networkOutput[i]);
                networkOutput[i] = tmp;
            }
        }

        printf(" Network output:\n");
        for ( i = 0; i < blockNumber ; i++ ) {
            char output[500];
            cp_to_string(networkOutput[i], output, 500);
            printf("%s\n", output);
        }

        /* decode the received packets */
        packet_decoder_t *decoder = create_packet_decoder(ff, blockNumber, payloadLen);

        printf(" Decoded packets: \n");
        for ( i = 0; i < blockNumber ; i++) {
            decoded_packets_t *packets = packet_decoder_add_packet(decoder, networkOutput[i]);

            if (packets == 0) {              
                continue;
            } 

            for (j = 0 ; j < packets->count; j++) {
                char output[500];
                uncoded_packet_to_string(packets->packets[j],output, 500);
                printf("%s\n", output);
            }

            destroy_decoded_packets(packets);
        }

}

/*
 * 
 */
int main(int argc, char** argv) {



    test_decode_headers();

    test_decode();

    finite_field_t *ff = create_extension_field(2,8);

    printf("%d", ff_sub(ff, 5 , 10));
    
    return (EXIT_SUCCESS);
}



