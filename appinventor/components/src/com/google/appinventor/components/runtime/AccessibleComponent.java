package com.google.appinventor.components.runtime;

public interface AccessibleComponent {

    //Sets the high contrast field of a component to be either True or False
    void setHighContrast(boolean isHighContrast);

    //Returns whether a component is in high contrast mode or not
    boolean getHighContrast();

    //Sets the large font field of a component to be either True or False
    void setLargeFont(boolean isLargeFont);

    //Returns whether a component is in large font mode or not
    boolean getLargeFont();
}
