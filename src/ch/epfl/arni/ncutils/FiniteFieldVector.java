package ch.epfl.arni.ncutils;

import java.util.Arrays;

public class FiniteFieldVector {

    private int[] coordinates;
    private FiniteField ff ;

    public FiniteFieldVector(int length, FiniteField ff) {
        this.ff = ff;
        coordinates = new int[length];
    }

    public int getLength() {
        return coordinates.length;
    }

    public FiniteField getFiniteField() {
        return ff;
    }


    public void setCoordinate(int index, int value) {
        assert(index >= 0);

        assert(value < ff.getCardinality() && value >= 0);

        coordinates[index] = value;
    }

    public int getCoordinate(int index) {
        assert(index >= 0);
        
        return coordinates[index];
        
    }

    public void setToZero() {
        Arrays.fill(coordinates, 0);
    }

    public FiniteFieldVector copy() {

        FiniteFieldVector vector = new FiniteFieldVector(coordinates.length, ff);
        System.arraycopy(coordinates, 0, vector.coordinates, 0, coordinates.length);

        return vector;
    }

    public FiniteFieldVector add(FiniteFieldVector vector) {

        assert(vector.getFiniteField() == ff);
        assert(vector.coordinates.length == coordinates.length);

        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.sum[coordinates[i]][vector.coordinates[i]];
        }

        return out;
    }

    public FiniteFieldVector scalarMultiply(int c) {

        assert(c < ff.getCardinality() && c >= 0);

        FiniteFieldVector out = new FiniteFieldVector(getLength(), ff);

        for ( int i = 0 ; i < coordinates.length ; i++ ) {
            out.coordinates[i] = ff.mul[coordinates[i]][c];
        }

        return out;

    }

    @Override
    public String toString() {
            String ret = "";
            for (int c : coordinates) {
                    ret += (ret.length() != 0 ? " " : "") + String.format("%02d ", c); ;
            }
            return ret;
    }


}
