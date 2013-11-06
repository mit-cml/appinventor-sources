// Copyright 2012 Massachusetts Institute of Technology. All Rights Reserved.

package com.google.appinventor.client.editor.youngandroid;

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
