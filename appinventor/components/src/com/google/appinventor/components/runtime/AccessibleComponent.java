// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

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
