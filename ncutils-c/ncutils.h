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

/* type declarations */
typedef struct _uncoded_packet_t * p_uncoded_packet_t;
typedef struct _packet_decoder_t* p_packet_decoder_t;
typedef struct _decoded_packets_t* p_decoded_packets_t;
typedef struct _decoded_coordinates_t* p_decoded_coordinates_t;
typedef struct _coding_vector_decoder_t* p_coding_vector_decoder_t;
typedef struct _coded_packet_t* p_coded_packet_t;
typedef struct _vector_t* p_vector_t;
typedef struct _finite_field_t* p_finite_field_t;


/*******************************************************************************
 * 
 * Finite field object: provides methods to perform computations over finite 
 * fields and to convert arrays of bytes to/from finite field vectors.
 * 
 * ****************************************************************************/

/* Creates a new extension field with base prime q and cardinality q^m */
p_finite_field_t create_extension_field(int q, int m) ;

/* Destroys a finite field structure and releases all the associated resources */
void destroy_finite_field(p_finite_field_t this) ;

/* Fills the vector dest using the contents of the buffer src of length len. The
 * length of the vector dest must be compatible with the length of the buffer src
 * and vector has to be defined over the specified finite field */
void ff_bytes_to_vector(p_finite_field_t this, char* src, int len, p_vector_t dest) ;

/* Fills the buffer dest of length len using the contents of the vector src. The
 * length of the vector src must be compatible with the length of the buffer dest
 * and vector has to be defined over the specified finite field */
void ff_vector_to_bytes(p_finite_field_t this, p_vector_t vector, char *dest, int len) ;

/* Returns the number of bytes required to store a vector of length coordinatesCount
 * defined over the field this.*/
int ff_coordinates_to_bytes(p_finite_field_t this, int coordinatesCount) ;

/* Returns the length of a vector defined over the field this required to store
 *  a buffer of length bytesLength.
 */
int ff_bytes_to_coordinates(p_finite_field_t this, int bytesLength) ;

/* Returns the result of the specified operation over the specified field */
int ff_sum(p_finite_field_t this, int a , int b);
int ff_sub(p_finite_field_t this, int a, int b);
int ff_div(p_finite_field_t this, int a, int b);
int ff_mul(p_finite_field_t this, int a, int b);

/* returns the cardinality of the finite field */
int ff_get_cardinality(p_finite_field_t this);



/*******************************************************************************
 *
 * Vector object: represents a vector of finite field elements.
 *
 * ****************************************************************************/

/* Creates a vector with length coordinates belonging to the specified field */
p_vector_t create_vector(int length, p_finite_field_t ff) ;

/* Destroys the specified vector and releases the associated resources */
void destroy_vector(p_vector_t this) ;

/* Sets the coordinates of the specified vector to 0 */
void vector_set_to_zero(p_vector_t this) ;

/* Creates a copy of the vector this */
p_vector_t vector_copy(p_vector_t this) ;

/* Creates a new vector that is the addition the two specified vectors */
p_vector_t vector_add(p_vector_t this, p_vector_t other) ;

/* Creates a new vector that is the scalar multiplication of the specified vector */
p_vector_t vector_scalar_multiply(p_vector_t this, int c) ;

/* Copies a textual reprensentation of the vector this in the buffer len. The
 * representation will be of at most len bytes.
 */
void vector_to_string(p_vector_t this, char* buffer, int len) ;

/* Return the number of coordinates of this vector */
int vector_get_length(p_vector_t this);

/* Returns the specified coordinate in the vector */
int vector_get_coordinate(p_vector_t this, int coordinate) ;

/* Sets a specified coordinate in the vector */
void vector_set_coordinate(p_vector_t this, int coordinate, int value) ;

/* Returns the finite field over which the vector is defined */
p_finite_field_t vector_get_finite_field(p_vector_t this) ;



/*******************************************************************************
 *
 * Uncoded packet object: stores a block of data along with its id in an uncoded
 * form. It is used to create coded packets and it is the output of the packet
 * decoder.
 *
 * ****************************************************************************/

/* Creates a new uncoded packet with the specified block id and the payload set to
 * the content of the specified vector.
 */
p_uncoded_packet_t create_uncoded_packet_from_vector(int id, p_vector_t payload) ;

/* Destroys the specified uncoded packet and releases the associated resources */
void destroy_uncoded_packet(p_uncoded_packet_t this) ;

/* Returns a pointer to the uncoded packet payload */
char *uncoded_packet_get_payload(p_uncoded_packet_t this);

/* Returns the size of the uncoded packet payload */
int uncoded_packet_get_payload_length(p_uncoded_packet_t this);

/* Return the ID of the block stored in this uncoded packet */
int uncoded_packet_get_id(p_uncoded_packet_t this);




/*******************************************************************************
 *
 * Coded packet object: stores a coding coefficient vector and the correspoding
 * linear combination of data blocks.
 *
 * ****************************************************************************/

/*
 * creates a new coded packet from the specified uncoded packet. The coding vector
 * is initialized with a vector of length max_packets with coordinate data->id set
 * to 1. The payload vector is initialized using the payload of the data packet.
 * The ff parameter specifies the finite field to be used to create coding and
 * payload vectors
 */
p_coded_packet_t create_coded_packet_from_uncoded(p_uncoded_packet_t data, int max_packets, p_finite_field_t ff) ;

/*
 * Creates a new zero coded packet with a coding vector of length max_packets and
 * a payload vector big enough to contain payload_bytes_len bytes of data. The vectors
 * are create using the finite field ff.
 */
p_coded_packet_t create_coded_packet(int max_packets, int payload_bytes_len, p_finite_field_t ff) ;

/*
 * Destroys the specified coded packet and releases the associated memory
 */
void destroy_coded_packet(p_coded_packet_t this) ;

/*
 * Sets the index-th coordinate of the packet to value. This function sees the
 * packet as a vector resulting form the concatenation of coding and payload
 * vectors.
 */
void cp_set_coordinate(p_coded_packet_t this, int index, int value) ;

/*
 * Gets the index-th coordinate of the packet. This function sees the
 * packet as a vector resulting form the concatenation of coding and payload
 * vectors.
 */
int cp_get_coordinate(p_coded_packet_t this, int index) ;

/* Returns the coding vector of the specified coded packet */
p_vector_t cp_get_coding_vector(p_coded_packet_t this);

/* Returns the coded payload of the specified coded packet */
p_vector_t cp_get_payload_vector(p_coded_packet_t this);

/* Creates a copy the specified coded packet */
p_coded_packet_t cp_copy(p_coded_packet_t this ) ;

/* Creates a new coded packet that is the sum of two coded packets. This
 * function sees the packet as a vector resulting form the concatenation of
 * coding and payload vectors.
 */
p_coded_packet_t cp_add(p_coded_packet_t this, p_coded_packet_t other) ;

/* Creates a new coded packet that the scalar multiplication of a given coded
 * packets. This function sees the packet as a vector resulting form the
 * concatenation of coding and payload vectors.
 */
p_coded_packet_t cp_scalar_multiply(p_coded_packet_t this, int c) ;

/* Writes to buf a textual representation of the coded packet. The maximal length
 * of the representation must be smaller than len. */
void cp_to_string(p_coded_packet_t this, char* buf, int len) ;




/*******************************************************************************
 *
 * Packet decoder: this object receives as input coded packets and as soon
 * as it is possible it outputs the original uncoded packets.
 *
 * ****************************************************************************/

/* Creates a packet decoder that operates with coded packets defined over the finite
 * field ff, with coding coefficient vectors of length max_packets and with payloads
 * of lenght payload_length_bytes bytes */
p_packet_decoder_t create_packet_decoder(p_finite_field_t ff, int max_packets, int payload_length_bytes) ;

/* Destroys a packet decoder and releases the associated resources */
void destroy_packet_decoder(p_packet_decoder_t this) ;

/* Destroys a decoded_packets_t structure and releases all the associated
 * resources */
void destroy_decoded_packets(p_decoded_packets_t this) ;

/* Returns the number of packets that were decoded */
int decoded_packets_get_count(p_decoded_packets_t this);

/* Returns the specified packet in the list of packets that were decoded */
p_uncoded_packet_t decoded_packets_get_packet(p_decoded_packets_t this, int pos);

/*
 * Adds a new coded packet to the specified packet decoder. It the packet is a linear
 * combination of the packets previously added the function will return 0 otherwise
 * it will return a decoded_packets_t structure containing the new packets that could
 * be decoded. This structure has to be destroyed by the caller of packet_decoder_add_packet
 * with the destroy_decoded_packets function */
p_decoded_packets_t packet_decoder_add_packet(p_packet_decoder_t this, p_coded_packet_t p) ;

/* Creates a new uncoded packet with the specified block id and the payload set to
 * the content of the buffer payload of length len
 */
p_uncoded_packet_t create_uncoded_packet(int id, char* payload, int len) ;



/*******************************************************************************
 *
 * Coding vector decoder: this object receives as input coding vectors and as soon
 * as it is possible it outputs the coefficients that must be used to obtain from
 * the packets corresponding the the received coding vector uncoded packets by
 * linear combination.
 *
 * ****************************************************************************/

/* Creates a new coding vector decoder that operates on coding vectors of length
 * max_packets defined over the field ff
 */
p_coding_vector_decoder_t create_coding_vector_decoder(int max_packets, p_finite_field_t ff) ;

/* Destroys the specified coding vector decoder and releases all the associated
 * resources.
 */
void destroy_coding_vector_decoder(p_coding_vector_decoder_t this) ;

/*
 * Destroys the decoded_coordinates structure and the releases the associated
 * resources
 */
void destroy_decoded_coordinates(p_decoded_coordinates_t this) ;

/*
 * Adds a coding vector to a specified coding vector decoder. If the vector
 * specified is lineraly dependent from the vectors already added the function
 * returns 0. Otherwise it returns a struct decoded_coordinates_t that contains
 * all the new blocks that can be decode. The returned struct must be destroyed
 * by the caller of this function using destroy_decoded_coordinates().
 */
p_decoded_coordinates_t coding_vector_decoder_add_vector(p_coding_vector_decoder_t this, p_vector_t v ) ;

/* Returns the number of decoded coordinates contained */
int decoded_coordinates_get_count(p_decoded_coordinates_t this);

/* Returns the index of the block at the specified position */
int decoded_coordinates_get_coordinate(p_decoded_coordinates_t this, int pos);

/* Returns the coefficients that have to be used to decode the block at the specified position */
p_vector_t decoded_coordinates_get_coefficients(p_decoded_coordinates_t this, int pos);


/* Writes to buffer a textual representation of the uncoded packet this. The
 * maximal lenght of the representation is len */
void uncoded_packet_to_string(p_uncoded_packet_t this, char* buffer, int len) ;



#ifdef	__cplusplus
}
#endif

#endif	/* _NCUTILS_H */
