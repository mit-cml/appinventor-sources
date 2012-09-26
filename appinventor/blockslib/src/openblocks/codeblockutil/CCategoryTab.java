// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class CCategoryTab extends CCategoryButton {

  public CCategoryTab(Color buttonColor, String text){
    // -1 indicates use default center alignment, not left alignment
    super(buttonColor, text, -1);
  }

  public void drawNormalBorder(Graphics2D g2, int buttonWidth, int buttonHeight) {
    g2.setColor(this.borderColor);
    g2.drawRect(0, 0, buttonWidth - 1, buttonHeight - 1);
    g2.setColor(CGraphite.button_hoverborder);
    g2.drawLine(0, buttonHeight - 1, buttonWidth - 1, buttonHeight - 1);
  }

  public void drawSelectedBorder(Graphics2D g2, int buttonWidth, int buttonHeight) {
    g2.setColor(CGraphite.button_hoverborder);
    g2.drawRect(0, 0, buttonWidth - 1, buttonHeight - 1);
    g2.setColor(buttonColor);
    g2.drawLine(0, buttonHeight - 1, buttonWidth - 1, buttonHeight - 1);
  }
}
