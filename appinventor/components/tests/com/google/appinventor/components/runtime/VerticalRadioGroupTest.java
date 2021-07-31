// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import com.google.appinventor.components.common.ComponentConstants;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for VerticalRadioGroup component.
 *
 * @author thamihardik8@gmail.com (Hardik Thami)
 */
public class VerticalRadioGroupTest extends RadioGroupTestBase<VerticalRadioGroup> {

    @Before
    public void SetUp() {
        super.setUp();
        super.aRadioGroup = new VerticalRadioGroup(getForm());
    }

    @Test
    public void testRadioGroupProperties() {
        super.testRadioGroupProperties();
        super.aRadioGroup.AlignHorizontal(ComponentConstants.GRAVITY_RIGHT);
        assertEquals(ComponentConstants.GRAVITY_RIGHT, super.aRadioGroup.AlignHorizontal());
    }

}