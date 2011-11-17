package openblocks.codeblockutil;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class CLabel extends JLabel{
	private static final long serialVersionUID = 328149080212L;
	//To get the shadow effect the text must be displayed multiple times at
	//multiple locations.  x represents the center, white label.
	// o is color values (0,0,0,0.5f) and Y is black.
	//			  o o
	//			o x Y o
	//			o Y o
	//			  o
	//offsetArrays representing the translation movement needed to get from
	// the center location to a specific offset location given in {{x,y},{x,y}....}
	//..........................................grey points.............................................black points
	private final int[][] shadowPositionArray = {{0,-1},{1,-1}, {-1,0}, {2,0},	{-1,1}, {1,1},  {0,2}, 	{1,0},  {0,1}};
	private final float[] shadowColorArray =	{0.5f,	0.5f,	0.5f, 	0.5f, 	0.5f, 	0.5f,	0.5f,	0,		0};
	private double offsetSize = 1;

	public CLabel(String text){
		super(text);
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		String text = this.getText();
		if(text != null){
			Font font = g2.getFont().deriveFont((float)(((float)this.getHeight()) * .8));
			g2.setFont(font);

            FontMetrics metrics = g2.getFontMetrics();
            Rectangle2D textBounds = metrics.getStringBounds(this.getText(),g2);
            double textHeight = textBounds.getHeight();
            double textWidth = textBounds.getWidth() > this.getWidth() ? this.getWidth()/2 : textBounds.getWidth();
            float x = (float)((this.getWidth() / 2) - (textWidth / 2));
            float y = (float)((this.getHeight() / 2) + (textHeight / 2)) - metrics.getDescent();
			g.setColor(Color.black);
			for (int i = 0; i < shadowPositionArray.length; i++) {
				int dx = shadowPositionArray[i][0];
				int dy = shadowPositionArray[i][1];
				g2.setColor(new Color(0,0,0, shadowColorArray[i]));
				g2.drawString(text, x+(int)((dx)*offsetSize), y+(int)((dy)*offsetSize));
			}
			g2.setColor(Color.white);
			g2.drawString(text, x, y);
		}
	}
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new FlowLayout());
		f.setSize(500, 75);
		CLabel c = new CLabel("hi");
		c.setPreferredSize(new Dimension(400,50));

		f.add(c);
		f.setVisible(true);
		f.repaint();

	}
}
