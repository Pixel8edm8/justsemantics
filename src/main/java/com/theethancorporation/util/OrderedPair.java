package com.theethancorporation.util;

/**
 * A container that specifically takes two, and only two, elements. Made for the WebPage object, so that the HashMap returns both the accumulated values
 * and the frequency table.
 * OrderedPair
 */
public class OrderedPair<X, Y> {
    private X xObject;
    private Y yObject;
    public OrderedPair(X xObject, Y yObject) {
        this.xObject = xObject;
        this.yObject = yObject;
    }

    public X getX() {
        return xObject;
    }

    public Y getY() {
        return yObject;
    }

    public void setxObject(X xObject) {
        this.xObject = xObject;
    }

    public void setyObject(Y yObject) {
        this.yObject = yObject;
    }
}
