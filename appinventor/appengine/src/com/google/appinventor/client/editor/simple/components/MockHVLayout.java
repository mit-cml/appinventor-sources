// Copyright 2009 Google Inc. All Rights Reserved.

package com.google.appinventor.client.editor.simple.components;

/**
 * A Layout that arranges the children of a container in a single column or a
 * single row.
 * Note: Right now this doesn't do any more than the base class, but it is
 * here for clarity and in case it someday needs additional functionality.
 *
 * @author sharon@google.com (Sharon Perl)
 */
final class MockHVLayout extends MockHVLayoutBase {
  /**
   * Creates a new linear layout with the specified orientation.
   */
  MockHVLayout(int orientation) {
    super(orientation);
  }
}
