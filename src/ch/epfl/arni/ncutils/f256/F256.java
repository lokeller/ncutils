package ch.epfl.arni.ncutils.f256;

import ch.epfl.arni.ncutils.FiniteField;

/**
 * This class represents the finite field F_2^8
 */

public class F256 {

	private static FiniteField ff = new FiniteField(2, 8);
	
	public static FiniteField getF256() {
		return ff;
	}

	
	
	
}
