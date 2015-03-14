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

package ch.epfl.arni.ncutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.arni.ncutils.impl.JavaDecoder;
import ch.epfl.arni.ncutils.impl.JavaEncoder;
import ch.epfl.arni.ncutils.impl.NativeDecoder;
import ch.epfl.arni.ncutils.impl.NativeEncoder;

/**
 * 
 * Factory class for encoders and decoders
 *
 */

public class CodecFactory {

	private static boolean nativeLibraryLoaded; 
	private static boolean triedLoadingNativeLibrary;
	
	/**
	 * 
	 * Creates a decoder. This function uses native implementation if available
	 * 
	 * @param segmentLength the length of the segment
	 * @param packetsPerSegment number of packets that form a segment
	 * 
	 * @return a Decoder that can be used to decoded packets
	 */
	public static Decoder createDecoder(int segmentLength, int packetsPerSegment) {
		
		if ( isNativeLibraryAvailable() ) {
			return new NativeDecoder(segmentLength, packetsPerSegment);
		} else {
			return new JavaDecoder(segmentLength, packetsPerSegment);
		}
		
	}
	
	/**
	 * 
	 * Creates an encoder that creates coded packets from a segment. The function
	 * uses the native implementation if available. 
	 * 
	 * @param segment a buffer holding the segment that must be encoded
	 * @param offset the offset in the buffer where the segment starts
	 * @param length the length of the segment
	 * @param packetsPerSegment the number of packets in which the segment is split
	 * 
	 * @return an Encoder that can be used to encode packets for the specified segment
	 */
	public static Encoder createEncoder(byte[] segment, int offset, int length, int packetsPerSegment) {
		
		if ( isNativeLibraryAvailable() ) {
			return new NativeEncoder(segment, offset, length, packetsPerSegment);
		} else {
			return new JavaEncoder(segment, offset, length, packetsPerSegment);
		}		
		
	}

	/**
	 * 
	 * Returns true if the native library for the current platform is available.
	 * 
	 * @return true if the native library is available, false otherwise
	 */
	public static boolean isNativeLibraryAvailable() {
		
		if (!triedLoadingNativeLibrary) {
			
			triedLoadingNativeLibrary = true;
			
			// try to load the library from the jar			
			String os = System.getProperty("os.name");
			String arch = System.getProperty("os.arch");
			
			InputStream in = CodecFactory.class.getResourceAsStream("/NATIVE/" + os + "/" + arch + "/libncutils.so");
			
			if ( in != null ) {
				
				try {
					
					// save the library to an external file
					File tempFile = File.createTempFile("libncutils", "so");					
					FileOutputStream fo = new FileOutputStream(tempFile);
					
					int ch;
					
					while ( (ch = in.read()) != -1) {
						fo.write(ch);
					}
					
					fo.close();
					in.close();
					
					// try to load the library
					System.load(tempFile.getAbsolutePath());
			
					nativeLibraryLoaded = true;
					
					return true;
					
				} catch (IOException e) {
				} catch (UnsatisfiedLinkError e) {}
			}
			
			// if the previous code failed try to load from the library path			
			try {
				System.loadLibrary("ncutils");
				nativeLibraryLoaded = true;
			} catch (UnsatisfiedLinkError e) {}
		
		}
		
		return nativeLibraryLoaded;
		
	}
	
	
	public static void main(String[] args) {
		
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
			
		System.out.println("The operating system name is: " + os);
		System.out.println("The operating system name is: " + arch);
		
		System.out.println("Native library is available: " + isNativeLibraryAvailable());
	}
	
}
