package ch.epfl.arni.ncutils;

import java.util.HashMap;
import java.util.Map;

public class SparseFiniteFieldVector implements FiniteFieldVector {

    private HashMap<Integer, Integer> coefficients = new HashMap<Integer, Integer>();
    private FiniteField ff = FiniteField.getDefaultFiniteField();

    @Override
    public String toString() {
            String ret = "";
            for (Map.Entry<Integer, Integer> entry : coefficients.entrySet()) {
                    ret += (ret.length() != 0 ? " + " : "") + entry.getValue() + " * b" + entry.getKey() ;
            }
            return ret;
    }

    public void setToZero() {
        coefficients.clear();
    }

    @Override
    public void copyTo(FiniteFieldVector c) {
            c.setToZero();

            for (Map.Entry<Integer, Integer> e : coefficients.entrySet()) {
                    c.setCoefficient(e.getKey(), e.getValue() + 0);
            }            
    }

    public void setCoefficient(int index, int value) {
        if (value == 0) coefficients.remove(index);
        else coefficients.put(index, value);
    }

    public int getCoefficient(int index) {
        Integer value = coefficients.get(index);

        if (value == null) return 0;
        else return value;
        
    }

    public int getHammingWeight() {
        return coefficients.size();
    }

    public Iterable<Integer> getNonZeroCoefficients() {
        return coefficients.keySet();
    }

    public FiniteField getFiniteField() {
        return ff;
    }

}
