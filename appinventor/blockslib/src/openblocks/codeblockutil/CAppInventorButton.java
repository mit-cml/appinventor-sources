package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public abstract class CAppInventorButton extends CButton {

  protected static final Color TEXT_COLOR = CGraphite.darkgray;
  
  protected Color borderColor = Color.black;
  protected float textAlign = CENTER_ALIGNMENT;
  protected int leftMargin = 0;
  protected boolean hoverFeedback = true;

  public CAppInventorButton(Color c1, Color c2, String text){
    super(c1, c2, text);
    this.setFont(new Font("Arial", Font.PLAIN, 13));
    this.setPreferredSize(new Dimension(80, 27));
  }

  // draw inside of button
  protected abstract void drawNormal(Graphics2D g2, int buttonWidth, int buttonHeight);

  // draw inside of button when pressed or selected
  protected abstract void drawSelected(Graphics2D g2, int buttonWidth, int buttonHeight);

  protected void drawNormalBorder(Graphics2D g2, int buttonWidth, int buttonHeight) {
    if (hoverFeedback && this.focus && isEnabled()){
      g2.setColor(CGraphite.button_hoverborder);
    } else {
      g2.setColor(this.borderColor);
    }
    g2.drawRect(0, 0, buttonWidth - 1, buttonHeight - 1);
  }

  protected void drawSelectedBorder(Graphics2D g2, int buttonWidth, int buttonHeight) {
    g2.setColor(this.borderColor);
    g2.drawRect(0, 0, buttonWidth - 1, buttonHeight - 1);
  }

  // change font if conditions warrant
  protected void updateFont() {}

  public void paint(Graphics g){
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int buttonHeight = this.getHeight();
    int buttonWidth = this.getWidth();

    if (this.pressed || this.selected) {
      drawSelected(g2, buttonWidth, buttonHeight);
      drawSelectedBorder(g2, buttonWidth, buttonHeight);
    } else {
      drawNormal(g2, buttonWidth, buttonHeight);
      drawNormalBorder(g2, buttonWidth, buttonHeight);
    }

    // draw label
    String text = this.getText();
    if (text != null) {
      updateFont();
      g2.setFont(this.getFont());
      FontMetrics metrics = g2.getFontMetrics();

      float x;
      if (textAlign == LEFT_ALIGNMENT) {
        x = (float)leftMargin;
      } else {
        double stringWidth = metrics.getStringBounds(text, g2).getWidth();
        x = (float)((buttonWidth - stringWidth) / 2);
      }

      float y = (float)((buttonHeight + metrics.getAscent()) / 2) - 1;

      if (hoverFeedback && this.focus && isEnabled()) {
        g2.setColor(CGraphite.button_hovertext);
      } else {
        g2.setColor(foregroundColor);
      }
      g2.drawString(text, x, y);
    }
  }
}
