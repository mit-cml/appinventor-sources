// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2017-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * BlocksLanguage encapsulates the categories of blocks used to compose a language in the blocks
 * editor.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class BlocksLanguage implements Iterable<BlocksCategory> {
  private final String name;
  private final int version;
  private final Set<BlocksCategory> categories;

  public BlocksLanguage(String name, int version, BlocksCategory... categories) {
    this.name = name;
    this.version = version;
    this.categories = new LinkedHashSet<>(Arrays.asList(categories));
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public Set<BlocksCategory> getCategories() {
    return Collections.unmodifiableSet(categories);
  }

  @Override
  public Iterator<BlocksCategory> iterator() {
    return categories.iterator();
  }
}
