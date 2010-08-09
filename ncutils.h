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

#ifndef _NCUTILS_H
#define	_NCUTILS_H

#ifdef	__cplusplus
extern "C" {
#endif

    /* a finite field object. used to store finite
     * field operation tables */

    typedef struct {
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
    typedef struct {
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
    typedef struct {
        /* the coding vector, its length is equal to the maximum number
         * of blocks that can be sent */
        vector_t *coding_vector;
        /* the payload of the packet expressed as a finite field vector */
        vector_t *payload;
    } coded_packet_t;

    /* an uncoded packet. it is composed by the id of the block and the
     * corresponding payload */
    typedef struct {
        /* id of the block corresponding to this packet */
        int id;
        /* payload, its length is stored in the payloadLen field */
        char* payload;
        /* length of the payload stored in this packet*/
        int payloadLen;
    } uncoded_packet_t;

    /* a coding vector decoder, this structure keeps track of the
     * interals of the coding vector decoding process */
    typedef struct {
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
    typedef struct {
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
    typedef struct {
        /* number of packets contained in the list */
        int count;
        /* array of decoded packets */
        uncoded_packet_t** packets;
    } decoded_packets_t;

    /* structure used to keep track of the packet decoding process*/
    typedef struct {
        coded_packet_t **packets;
        int packet_count;
        int maxPackets;
        coding_vector_decoder_t* codingVectorDecoder;
        int payloadCoordinatesCount;
        finite_field_t *ff;
    } packet_decoder_t;

/*
 * creates a new coded packet from the specified uncoded packet. The coding vector
 * is initialized with a vector of length max_packets with coordinate data->id set
 * to 1. The payload vector is initialized using the payload of the data packet.
 * The ff parameter specifies the finite field to be used to create coding and
 * payload vectors
 */
coded_packet_t *create_coded_packet_from_uncoded(uncoded_packet_t *data, int max_packets, finite_field_t *ff) ;

/*
 * Creates a new zero coded packet with a coding vector of length max_packets and
 * a payload vector big enough to contain payload_bytes_len bytes of data. The vectors
 * are create using the finite field ff.
 */
coded_packet_t *create_coded_packet(int max_packets, int payload_bytes_len, finite_field_t *ff) ;

/*
 * Destroys the specified coded packet and releases the associated memory
 */
void destroy_coded_packet(coded_packet_t *this) ;

/*
 * Sets the index-th coordinate of the packet to value. This function sees the
 * packet as a vector resulting form the concatenation of coding and payload
 * vectors.
 */
void cp_set_coordinate(coded_packet_t *this, int index, int value) ;

/*
 * Gets the index-th coordinate of the packet. This function sees the
 * packet as a vector resulting form the concatenation of coding and payload
 * vectors.
 */
int cp_get_coordinate(coded_packet_t *this, int index) ;

/* Creates a copy the specified coded packet */
coded_packet_t *cp_copy(coded_packet_t *this ) ;

/* Creates a new coded packet that is the sum of two coded packets. This
 * function sees the packet as a vector resulting form the concatenation of
 * coding and payload vectors.
 */
coded_packet_t* cp_add(coded_packet_t *this, coded_packet_t* other) ;

/* Creates a new coded packet that the scalar multiplication of a given coded
 * packets. This function sees the packet as a vector resulting form the
 * concatenation of coding and payload vectors.
 */
coded_packet_t* cp_scalar_multiply(coded_packet_t *this, int c) ;

/* Writes to buf a textual representation of the coded packet. The maximal length
 * of the representation must be smaller than len. */
void cp_to_string(coded_packet_t *this, char* buf, int len) ;

/* Creates a new coding vector decoder that operates on coding vectors of length
 * max_packets defined over the field ff
 */
coding_vector_decoder_t* create_coding_vector_decoder(int max_packets, finite_field_t* ff) ;

/* Destroys the specified coding vector decoder and releases all the associated
 * resources.
 */
void destroy_coding_vector_decoder(coding_vector_decoder_t *this) ;

/*
 * Destroys the decoded_coordinates structure and the releases the associated
 * resources
 */
void destroy_decoded_coordinates(decoded_coordinates_t *this) ;

/*
 * Adds a coding vector to a specified coding vector decoder. If the vector
 * specified is lineraly dependent from the vectors already added the function
 * returns 0. Otherwise it returns a struct decoded_coordinates_t that contains
 * all the new blocks that can be decode. The returned struct must be destroyed
 * by the caller of this function using destroy_decoded_coordinates().
 */
decoded_coordinates_t* coding_vector_decoder_add_vector(coding_vector_decoder_t *this, vector_t *v ) ;

/* Creates a new extension field with base prime q and cardinality q^m */
finite_field_t *create_extension_field(int q, int m) ;

/* Destroys a finite field structure and releases all the associated resources */
void destroy_finite_field(finite_field_t *this) ;

/* Fills the vector dest using the contents of the buffer src of length len. The
 * length of the vector dest must be compatible with the length of the buffer src
 * and vector has to be defined over the specified finite field */
void ff_bytes_to_vector(finite_field_t *this, char* src, int len, vector_t* dest) ;

/* Fills the buffer dest of length len using the contents of the vector src. The
 * length of the vector src must be compatible with the length of the buffer dest
 * and vector has to be defined over the specified finite field */
void ff_vector_to_bytes(finite_field_t *this, vector_t *vector, char *dest, int len) ;

/* Returns the number of bytes required to store a vector of length coordinatesCount
 * defined over the field this.*/
int ff_coordinates_to_bytes(finite_field_t* this, int coordinatesCount) ;

/* Returns the length of a vector defined over the field this required to store
 *  a buffer of length bytesLength.
 */
int ff_bytes_to_coordinates(finite_field_t* this, int bytesLength) ;

/* Creates a packet decoder that operates with coded packets defined over the finite
 * field ff, with coding coefficient vectors of length max_packets and with payloads
 * of lenght payload_length_bytes bytes */
packet_decoder_t *create_packet_decoder(finite_field_t *ff, int max_packets, int payload_length_bytes) ;

/* Destroys a packet decoder and releases the associated resources */
void destroy_packet_decoder(packet_decoder_t *this) ;

/* Destroys a decoded_packets_t structure and releases all the associated
 * resources */
void destroy_decoded_packets(decoded_packets_t *this) ;

/*
 * Adds a new coded packet to the specified packet decoder. It the packet is a linear
 * combination of the packets previously added the function will return 0 otherwise
 * it will return a decoded_packets_t structure containing the new packets that could
 * be decoded. This structure has to be destroyed by the caller of packet_decoder_add_packet
 * with the destroy_decoded_packets function */
decoded_packets_t *packet_decoder_add_packet(packet_decoder_t* this, coded_packet_t *p) ;

/* Creates a new uncoded packet with the specified block id and the payload set to
 * the content of the buffer payload of length len
 */
uncoded_packet_t * create_uncoded_packet(int id, char* payload, int len) ;

/* Creates a new uncoded packet with the specified block id and the payload set to
 * the content of the specified vector.
 */
uncoded_packet_t * create_uncoded_packet_from_vector(int id, vector_t* payload) ;

/* Destroys the specified uncoded packet and releases the associated resources */
void destroy_uncoded_packet(uncoded_packet_t *this) ;

/* Writes to buffer a textual representation of the uncoded packet this. The
 * maximal lenght of the representation is len */
void uncoded_packet_to_string(uncoded_packet_t* this, char* buffer, int len) ;

/* Creates a vector with length coordinates belonging to the specified field */
vector_t* create_vector(int length, finite_field_t* ff) ;

/* Destroys the specified vector and releases the associated resources */
void destroy_vector(vector_t *this) ;

/* Sets the coordinates of the specified vector to 0 */
void vector_set_to_zero(vector_t* this) ;

/* Creates a copy of the vector this */
vector_t *vector_copy(vector_t* this) ;

/* Creates a new vector that is the addition the two specified vectors */
vector_t *vector_add(vector_t* this, vector_t* other) ;

/* Creates a new vector that is the scalar multiplication of the specified vector */
vector_t *vector_scalar_multiply(vector_t* this, int c) ;

/* Copies a textual reprensentation of the vector this in the buffer len. The
 * representation will be of at most len bytes.
 */
void vector_to_string(vector_t* this, char* buffer, int len) ;

/* Return the number of coordinates of this vector */
int vector_get_length(vector_t *this);

/* Returns the specified coordinate in the vector */
int vector_get_coordinate(vector_t *this, int coordinate) ;

/* Sets a specified coordinate in the vector */
void vector_set_coordinate(vector_t *this, int coordinate, int value) ;

/* Returns the finite field over which the vector is defined */
finite_field_t *vector_get_finite_field(vector_t *this) ;



#ifdef	__cplusplus
}
#endif

#endif	/* _NCUTILS_H */
