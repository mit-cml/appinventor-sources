package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * it's like a neon sign, only cooler
 *
 */
public class CSignLabel extends JPanel{
	private static final long serialVersionUID = 328149080428L;
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
	private String[] charSet;
	private int FONT_SIZE = 12;
	public CSignLabel(){
		super();
		this.charSet = new String[0];
		this.setOpaque(false);
		this.setFont(new Font("Ariel", Font.BOLD, FONT_SIZE));
	}
	public void setText(String text){
		if(text != null){
			text = text.toUpperCase();
			List<String> characters = new ArrayList<String>();
			for (int i = 0; i<text.length(); i++){
				characters.add(text.substring(i, i+1));
			}
			charSet = characters.toArray(charSet);
		}
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		for (int j = 0; j<charSet.length; j++){
			/*String c = charSet[i];
			Font font = g2.getFont().deriveFont((float)(((float)this.getHeight()) * .8));
			g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics();
            Rectangle2D textBounds = metrics.getStringBounds(c,g2);
            double textHeight = textBounds.getHeight();
            double textWidth = textBounds.getWidth() > this.getWidth() ? this.getWidth()/2 : textBounds.getWidth();
            */
			String c = charSet[j];
			System.out.println(c);
			int x = 5;
			int y = (j+1) * (FONT_SIZE+3);
			g.setColor(Color.black);
			for (int i = 0; i < shadowPositionArray.length; i++) {
				int dx = shadowPositionArray[i][0];
				int dy = shadowPositionArray[i][1];
				g2.setColor(new Color(0,0,0, shadowColorArray[i]));
				g2.drawString(c, x+(int)((dx)*offsetSize), y+(int)((dy)*offsetSize));
			}
			g2.setColor(Color.white);
			g2.drawString(c, x, y);
		}
	}
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setSize(500, 75);
		CSignLabel c = new CSignLabel();
		c.setText("hello there");
		c.setPreferredSize(new Dimension(400,50));

		f.add(c);
		f.setVisible(true);
		f.repaint();

	}
}
