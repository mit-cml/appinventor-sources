// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.widgets.properties;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Property editor for float values intended to represent the offset for the
 * center of rotation.
 *
 * @author gabrielj@mit.edu (Gabriel Jimenez)
 */
public class CenterOffsetFloatPropertyEditor extends NonNegativeFloatPropertyEditor {

	@Override
	protected void validate(String text) throws InvalidTextException {
		super.validate(text);

		// Make sure it falls within [0-100]
		float value = Float.parseFloat(text);
		if (value < 0 || value > 100) {
			throw new InvalidTextException(MESSAGES.notABoundedFloat(text));
		}
	}
}
