package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicToolTipUI;

public class CToolTip extends JToolTip{
  private static final long serialVersionUID = 328149080249L;
  private Color background = new Color(255,255,225);
  private CToolTip(){
    this(new Color(255,255,225));
  }
  public CToolTip(Color background){
    this.background=background;
    updateUI();
  }
  public void updateUI() {
    setUI(new CToolTipUI(background));
  }
}
class CToolTipUI extends BasicToolTipUI {
  private static final int WIDTH = 150;
  private static final int PAD_HEIGHT = 20;
  private CellRendererPane renderer;
  private JTextArea textArea;
  private Color background;
  public CToolTipUI(Color background) {
    super();
    this.background = background;
    renderer = new CellRendererPane();
    textArea = new JTextArea();
    textArea.setMargin(new Insets(0,3,0,0));
    renderer.removeAll();
    renderer.add(textArea );
    textArea.setFont(new Font("Ariel", Font.PLAIN, 11));
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
  }
  public void paint(Graphics g, JComponent c) {
    Dimension size = c.getSize();
    textArea.setBackground(background);
    renderer.paintComponent(g, textArea, c,1, 1,
        size.width-1, size.height-1, true);
  }
  public Dimension getPreferredSize(JComponent c) {
    String tipText = ((JToolTip)c).getTipText();
    if (tipText == null)
      return new Dimension(0,0);
    textArea.setText(tipText);
    Dimension d = textArea.getPreferredSize();
    d.width = Math.min(d.width, WIDTH);
    // We need to pad the height, or else the last line
    // doesn't show in some tooltips.  Is this a Swing bug or
    // is getPreferred size the wrong thing to use here?
    // Sadly, this typically is too much padding.  But there are
    // some blocks where we need this much, for example, the
    // tooltip on the '=' block.
    d.height += PAD_HEIGHT;
    textArea.setSize(d);
    return d;
    //return textArea.getPreferredSize();
  }
}
