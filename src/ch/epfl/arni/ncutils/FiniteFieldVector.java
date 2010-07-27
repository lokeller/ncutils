package ch.epfl.arni.ncutils;

import java.util.Arrays;

public class FiniteFieldVector {

    private int[] coefficients;
    private FiniteField ff ;

    public FiniteFieldVector(int length, FiniteField ff) {
        this.ff = ff;
        coefficients = new int[length];
    }

    public int getLength() {
        return coefficients.length;
    }

    public FiniteField getFiniteField() {
        return ff;
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

    public void setToZero() {
        Arrays.fill(coefficients, 0);
    }

    public FiniteFieldVector copy() {

        FiniteFieldVector vector = new FiniteFieldVector(coefficients.length, ff);
        System.arraycopy(coefficients, 0, vector.coefficients, 0, coefficients.length);

        return vector;
    }

    public FiniteFieldVector add(FiniteFieldVector vector) {

        assert(vector.getFiniteField() == ff);
        assert(vector.coefficients.length == coefficients.length);

        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coefficients.length ; i++ ) {
            out.coefficients[i] = ff.sum[coefficients[i]][vector.coefficients[i]];
        }

        return out;
    }

    public FiniteFieldVector scalarMultiply(int c) {

        assert(c < ff.getCardinality() && c >= 0);

        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coefficients.length ; i++ ) {
            out.coefficients[i] = ff.mul[coefficients[i]][c];
        }

        return out;

    }

    @Override
    public String toString() {
            String ret = "";
            for (int c : coefficients) {
                    ret += (ret.length() != 0 ? " " : "") + String.format("%02d ", c); ;
            }
            return ret;
    }


}
