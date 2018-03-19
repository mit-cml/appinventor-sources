package edu.colorado.lpc.blockytalkyble;

/**
 * Created by Abbie on 10/14/17.
 */
public enum ValueType {
    STRING("S"),
    NUMBER("N");

    private String indicator;

    public String getIndicator() {
        return indicator;
    }


    ValueType(String typeIndicator) {
        this.indicator = typeIndicator;
    }
}
