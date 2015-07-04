// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.annotations.SimpleObject;

/**
 * Runtime error indicating a failed attempt of converting a value of a type
 * into a value of another type, e.g. the String "foo" into an Integer, but
 * also converting from a base type to a derived type where there is no
 * relationship.
 *
 */
@SimpleObject
public class ConversionError extends RuntimeError {
}
