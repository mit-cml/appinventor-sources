// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2012-2017 Massachusetts Institute of Technology. All Rights Reserved.

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
