// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class CCategoryButton extends CAppInventorButton {

  private Font plainFont;
  private Font boldFont;

  public CCategoryButton(Color buttonColor, String text, int leftMargin){
    super(buttonColor, Color.WHITE, text);
    if (false && leftMargin >= 0) {
      this.leftMargin = leftMargin;
      this.textAlign = LEFT_ALIGNMENT;
    }
    this.borderColor = CGraphite.category_border;
    this.foregroundColor = TEXT_COLOR;
    this.hoverFeedback = false;
    this.plainFont = this.getFont().deriveFont(Font.PLAIN);
    this.boldFont = this.plainFont.deriveFont(Font.BOLD);
  }

  protected void drawNormal(Graphics2D g2, int buttonWidth, int buttonHeight){
    g2.setPaint(new Color(buttonColor.getRed(), buttonColor.getGreen(),
                          buttonColor.getBlue(), 160));
    g2.fillRect(0, 0, buttonWidth, buttonHeight);
  }

  protected void drawSelected(Graphics2D g2, int buttonWidth, int buttonHeight){
    drawNormal(g2, buttonWidth, buttonHeight);
  }

  protected void updateFont() {
    if (this.pressed || this.selected) {
      setFont(boldFont);
    } else {
      setFont(plainFont);
    }
  }
}
