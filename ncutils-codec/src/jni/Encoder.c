#include <malloc.h>
#include <jni.h>
#include <ncutils.h>
#include <field_tables.h>

JNIEXPORT void JNICALL Java_ch_epfl_arni_ncutils_impl_NativeEncoder_createPacket (JNIEnv *env, jclass class, jbyteArray segment, jint offset, jint len, jbyteArray output, jint output_offset, jint coords) {


	int packet_len,i,j;

	jbyte *packet_a, *segment_a;

	// this is the lenght of the encoded packet
	packet_len = len / coords;	

	// check that we have enough space in the destination vector
	if ( (*env)->GetArrayLength(env, output) - output_offset < packet_len + coords) {
		jclass excCls = (*env)->FindClass(env, 
		    "java/lang/IllegalArgumentException");
		if (excCls != 0)
		    (*env)->ThrowNew(env, excCls, "The output buffer is too short");

		return;

	}
	
	// allocate some memory to store the segment
	segment_a = (jbyte *) malloc( len * sizeof(jbyte));

	if (!segment_a) {
		jclass excCls = (*env)->FindClass(env, 
		    "java/lang/IllegalStateException");
		if (excCls != 0)
		    (*env)->ThrowNew(env, excCls, "Cannot allocate memory for segment");
		return;
	}

	// get a copy of the segment
	(*env)->GetByteArrayRegion(env, segment, offset, len, segment_a);

	// create an arry that will contain the packet
	packet_a = (jbyte *) malloc( (coords + packet_len) * sizeof(jbyte));

	if (!packet_a) {
		jclass excCls = (*env)->FindClass(env, 
		    "java/lang/IllegalStateException");
		free(segment_a);
		if (excCls != 0)
		    (*env)->ThrowNew(env, excCls, "Cannot allocate memory for packet");
		return;
	}

	// clear the output vector
	for ( j = 0 ; j < coords + packet_len ; j++) {
		packet_a[j] = 0;
	}

	// create a random linear combination
	int coefficient, d;
	for ( i = 0 ; i < coords; i++) {
		
		coefficient = (lrand48() & 0xFF );

		packet_a[i] = coefficient;

		// pre-multiply by 256 the coefficient to do the table lookup
		coefficient = coefficient << 8;

		for ( j = 0 ; j < packet_len ; j++) {
			d = ((int) segment_a[i*packet_len + j]) & 0xFF;
			packet_a[coords + j] = packet_a[coords + j] ^ mul_table[coefficient  + d];
		}

	} 

	// copy the encoded packet to the appropriate buffer
	(*env)->SetByteArrayRegion(env, output, output_offset, packet_len+coords, packet_a);	

	// free allocated memory
	free(packet_a);
	free(segment_a);

}

