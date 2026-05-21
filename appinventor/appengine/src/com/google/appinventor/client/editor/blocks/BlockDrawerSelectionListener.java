// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2025 Massachusetts Institute of Technology. All Rights Reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.blocks;

/**
 * Interface for receiving notifications about clicks on items in
 * a BlockSelectorBox.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public interface BlockDrawerSelectionListener {

  void onBuiltinDrawerSelected(String drawerName);

  void onGenericDrawerSelected(String drawerName);

}
