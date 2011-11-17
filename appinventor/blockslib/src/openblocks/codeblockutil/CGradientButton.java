package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public class CGradientButton extends CAppInventorButton {

  protected static final Color BUTTON_COLOR_TOP = CGraphite.button_white;
  protected static final Color BUTTON_COLOR_BOTTOM = CGraphite.button_lightgray;
  protected static final Color BUTTON_GRAYED_COLOR = CGraphite.button_grayedout;
  protected static final Color TEXT_GRAYED_COLOR = CGraphite.button_textgrayedout;

  public CGradientButton(Color topColor, Color bottomColor, String text){
    super(topColor, bottomColor, text);
    this.borderColor = CGraphite.category_border;
    this.foregroundColor = TEXT_COLOR;
  }

  protected void drawNormal(Graphics2D g2, int buttonWidth, int buttonHeight){
    g2.setPaint(new GradientPaint(0, 0, buttonColor, 0, buttonHeight, selectedColor));
    g2.fillRect(0, 0, buttonWidth, buttonHeight);
  }

  protected void drawSelected(Graphics2D g2, int buttonWidth, int buttonHeight){
    g2.setPaint(new GradientPaint(0, 0, selectedColor, 0, buttonHeight, buttonColor));
    g2.fillRect(0, 0, buttonWidth, buttonHeight);
  }
}
