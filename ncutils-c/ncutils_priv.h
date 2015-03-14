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

#ifndef _NCUTILS_PRIV_H
#define	_NCUTILS_PRIV_H

    /* a finite field object. used to store finite
     * field operation tables */

    typedef struct _finite_field_t {
        /* summation table a + b = sum[a * q + b] */
        int *sum;
        /* division table a / b = div[a * q + b] */
        int *div;
        /* multiplication table a * b = mul[a * q + b] */
        int *mul;
        /* substraction table a - b = sub[a * q + b] */
        int *sub;
        /* cardinality of the field */
        int q;
    } finite_field_t;

    /* a vector with elements form a given finite field*/
    typedef struct _vector_t {
        /* the values of coordinates of the vector:
         * cordinate n = coordinates[n]; */
        int *coordinates;
        /* number of coordinates of this vector*/
        int length;
        /** finite field used to define this vector */
        finite_field_t *ff;
    } vector_t;

    /* a coded packet, it is composed from a coding coefficient
     * vector and a payload vector. Both vectors are built on the same
     * finite field */
    typedef struct _coded_packet_t {
        /* the coding vector, its length is equal to the maximum number
         * of blocks that can be sent */
        vector_t *coding_vector;
        /* the payload of the packet expressed as a finite field vector */
        vector_t *payload;
    } coded_packet_t;

    /* an uncoded packet. it is composed by the id of the block and the
     * corresponding payload */
    typedef struct _uncoded_packet_t {
        /* id of the block corresponding to this packet */
        int id;
        /* payload, its length is stored in the payloadLen field */
        char* payload;
        /* length of the payload stored in this packet*/
        int payloadLen;
    } uncoded_packet_t;

    /* a coding vector decoder, this structure keeps track of the
     * interals of the coding vector decoding process */
    typedef struct _coding_vector_decoder_t {
        int *decodeMatrix;
        int maxPackets;
        int *pivotPos;
        char* isPivot;
        char* decoded;
        int packetCount;
        finite_field_t* ff;
    } coding_vector_decoder_t;

    /* This structure represent the output of the coding vector decoder
     * when a given coding vector is added. It is composed by two tables
     * of length written in the field count. These two tables give a list
     * of blocks that can be decoded and the corresponding linear combination
     * of packets that has to be used.
     * see coding_vector_decoder_add_vector for more information*/
    typedef struct _decoded_coordinates_t {
        /* number of blocks that are described in the struct */
        int count;
        /* ids of the blocks that can be described (points to with count
         * entries) */
        int* coordinates;
        /* a list of vector that give the coefficients that can be used to
         * retrieve a given packet. Entry i describes how to recover block
         * coordinate[i] */
        vector_t **coefficients;
    } decoded_coordinates_t;

    /* List of packets that were decoded, see packet_decoder_add_packet for
     * more information. */
    typedef struct _decoded_packets_t {
        /* number of packets contained in the list */
        int count;
        /* array of decoded packets */
        uncoded_packet_t** packets;
    } decoded_packets_t;

    /* structure used to keep track of the packet decoding process*/
    typedef struct _packet_decoder_t {
        coded_packet_t **packets;
        int packet_count;
        int maxPackets;
        coding_vector_decoder_t* codingVectorDecoder;
        int payloadCoordinatesCount;
        finite_field_t *ff;
    } packet_decoder_t;

#endif	/* _NCUTILS_PRIV_H */

