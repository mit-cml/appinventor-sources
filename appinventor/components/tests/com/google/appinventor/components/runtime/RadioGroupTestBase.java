// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.common.ComponentConstants;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

/**
 * Base Test class for RadioGroup component.
 *
 * @author thamihardik8@gmail.com (Hardik Thami)
 */
public abstract class RadioGroupTestBase<T extends RadioGroup> extends RobolectricTestBase {

    protected T aRadioGroup;

    @Test
    public void testRadioGroupDefaults() {
        assertEquals(ComponentConstants.HORIZONTAL_ALIGNMENT_DEFAULT, aRadioGroup.AlignHorizontal());
        assertEquals(ComponentConstants.VERTICAL_ALIGNMENT_DEFAULT, aRadioGroup.AlignVertical());
        assertEquals(Component.COLOR_DEFAULT, aRadioGroup.BackgroundColor());
        assertEquals("", aRadioGroup.Image());
    }

    @Test
    public void testRadioGroupProperties() {
        aRadioGroup.BackgroundColor(Component.COLOR_ORANGE);
        assertEquals(Component.COLOR_ORANGE, aRadioGroup.BackgroundColor());
    }

    @Test
    public void testRadioGroupMainFunctionality() {
        RadioButton button = null, temp = null;
        int numberOfChildRadioButtons = 5;

        for (; numberOfChildRadioButtons > 0; numberOfChildRadioButtons--){
            temp = new RadioButton(aRadioGroup.container());
        }
        
        for (Component child: aRadioGroup.getChildren()){
            if (child instanceof RadioButton) {
                if (button != null){
                    temp = button;
                    button = (RadioButton) child;
                    button.Checked(true);
                    assertFalse(temp.Checked());
                } else {
                    button = (RadioButton) child;
                    button.Checked(true);
                }
            }
        } 
    }
}


