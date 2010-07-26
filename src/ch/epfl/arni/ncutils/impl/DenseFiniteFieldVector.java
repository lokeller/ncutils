package ch.epfl.arni.ncutils.impl;

import ch.epfl.arni.ncutils.FiniteField;
import ch.epfl.arni.ncutils.FiniteFieldVector;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DenseFiniteFieldVector implements FiniteFieldVector {

    private int[] coefficients;
    private FiniteField ff ;

    public DenseFiniteFieldVector(int size, FiniteField ff) {
        this.ff = ff;
        coefficients = new int[size];
    }

    @Override
    public String toString() {
            String ret = "";
            for (int c : coefficients) {
                    ret += (ret.length() != 0 ? " " : "") + c ;
            }
            return ret;
    }

    public void setToZero() {
        Arrays.fill(coefficients, 0);
    }

    @Override
    public void copyTo(FiniteFieldVector c) {

        assert(c.getFiniteField() == ff);

        /* need to make sure the other vector is zero ( maybe it is longer
         * than this vector)
         */
        c.setToZero();

        for (int i = 0 ; i < coefficients.length ; i ++) {
                c.setCoefficient(i, coefficients[i]);
        }
    }

    public void copyTo(DenseFiniteFieldVector c) {

        assert(c.getFiniteField() == ff);
        assert(c.coefficients.length == coefficients.length);

        for (int i = 0 ; i < coefficients.length ; i ++) {
                c.coefficients[i] = coefficients[i];
        }
    }

    public void setCoefficient(int index, int value) {
        assert(index >= 0);

        assert(value < ff.getCardinality() && value >= 0);

        coefficients[index] = value;
    }

    public int getCoefficient(int index) {
        assert(index >= 0);
        
        return coefficients[index];
        
    }

    public int getHammingWeight() {

        int count = 0;
        for (int i = 0 ; i < coefficients.length ; i ++) {
                if (coefficients[i] != 0) {
                    count++;
                }
        }         
        return count;

    }

    public Iterable<Integer> getNonZeroCoefficients() {
        Vector<Integer> coeffs = new Vector<Integer>();

        for (int i = 0 ; i < coefficients.length ; i ++) {
                if (coefficients[i] != 0) {
                    coeffs.add(i);
                }
        }
        return coeffs;
    }

    public FiniteField getFiniteField() {
        return ff;
    }

    public void add(DenseFiniteFieldVector vector) {

        assert(vector.getFiniteField() == ff);
        assert(vector.coefficients.length == coefficients.length);

        for ( int i = 0 ; i < coefficients.length ; i++ ) {
            coefficients[i] = ff.sum[coefficients[i]][vector.coefficients[i]];
        }

    }


    public void add(FiniteFieldVector vector) {

        assert(vector.getFiniteField() == ff);
      
        for ( Integer i : vector.getNonZeroCoefficients()) {
            coefficients[i] = ff.sum[getCoefficient(i)][vector.getCoefficient(i)];
        }

    }

    public void scalarMultiply(int c) {

        assert(c < ff.getCardinality() && c >= 0);
                
        for ( int i = 0 ; i < coefficients.length ; i++ ) {
            coefficients[i] = ff.mul[coefficients[i]][c];
        }

    }

}
