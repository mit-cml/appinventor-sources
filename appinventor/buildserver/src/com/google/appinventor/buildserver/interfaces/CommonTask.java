// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.interfaces;

import com.google.appinventor.buildserver.context.CompilerContext;

/**
 * The CommonTask interface is a marker interface for tasks common to all
 * target platforms.
 */
public interface CommonTask extends Task<CompilerContext<?>> {
}
