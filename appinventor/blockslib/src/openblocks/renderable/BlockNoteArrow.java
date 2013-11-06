// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.renderable;

import openblocks.workspace.RBParent;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.JComponent;

/**
 * Draws an arrow (triangle to make the blockNote look like a speech bubble)
 *  between a blockNote and its source.
 *
 *
 *
 */
public class BlockNoteArrow {
	BlockNote blockNote;

	/**Internal Timer*/
	// BlockNote reaches in here to remove arrow from its parent when deleting.
	final Triangle triangle;
	private boolean active = false;

	/**Constructs this*/
	public BlockNoteArrow(BlockNote blockNote) {
		this.blockNote = blockNote;
		triangle = new Triangle();
	}

	/**
     * Set new location for triangle
     */
   public void setLocation(int x, int y){
   	triangle.setLocation(x, y);
   }

   /**
    * set new location for triangle
    * @param p
    */
   public void setLocation(Point p){
	   setLocation(p.x, p.y);
   }

   public void update() {
     Container parentContainer = blockNote.getParent();

     if (blockNote.getRenderableBlock() != null && parentContainer != null){
       RenderableBlock blockNoteSource = blockNote.getRenderableBlock();
       Point start =  blockNoteSource.getArrowStartLocation(
                                                      blockNote.getBlockNoteLabel());
       Point end = blockNote.getLocation();
       end.translate(blockNote.getWidth() / 2, blockNote.getHeight() / 2);

       double dx = (end.x - start.x);
       double dy = (end.y - start.y);
       double length = Math.sqrt(dx * dx + dy * dy);

       if (length > 0) {
         dx = dx / length;
         dy = dy / length;
       }

       triangle.setXpoints((int) (end.x - dy * 10), start.x, (int) (end.x + dy * 10));
       triangle.setYpoints((int) (end.y + dx * 10), start.y, (int) (end.y - dx * 10));
       triangle.update();

       if (triangle.getParent() != null) {
         triangle.getParent().remove(triangle);
       }
      ((RBParent) parentContainer).addToBlockArrowLayer(triangle);

       parentContainer.validate();
       parentContainer.repaint();
       active = true;
     }
   }

   /**
    * Returns whether this  animation is active
    * @return the active
    */
   boolean isActive() {
     return active;
   }

   /**
    * Sets the visibility of the triangle component
    *
    */
   public void setVisible(boolean b) {
     if (triangle != null) {
       triangle.setVisible(b);
     }
   }

	/**
	 * Class in charge of drawing actual triangular arrow between the blockNote
	 * and the RenderableBlock
	 * @author joshua
	 *
	 */
	private class Triangle extends JComponent {
		public int[] xpoints;
		public int[] ypoints;
		int minx, miny;

		private static final long serialVersionUID = 328149080427L;
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING,
			    RenderingHints.VALUE_ANTIALIAS_ON));
	 		g2.setColor(blockNote.getBorderColor());
			g2.fillPolygon(xpoints, ypoints, 3);
		}

		/**
		 * Set the three x coordinates of the triangle (in the coordinate system
		 * containing the blockNote and blockNoteSource)
		 * @param x1
		 * @param x2
		 * @param x3
		 */
		void setXpoints(int x1, int x2, int x3) {
			xpoints = new int[] {x1, x2, x3};
			minx = Math.min(xpoints[0], Math.min(xpoints[1], xpoints[2]));
			xpoints[0] = xpoints[0] - minx;
			xpoints[1] = xpoints[1] - minx;
			xpoints[2] = xpoints[2] - minx;
		}

		/**
		 * Set the three y coordinates of the triangle (in the coordinate system
		 * containing the blockNote and blockNoteSource)
		 * @param y1
		 * @param y2
		 * @param y3
		 */
		void setYpoints(int y1, int y2, int y3) {
			ypoints = new int[] {y1, y2, y3};
			miny = Math.min(ypoints[0], Math.min(ypoints[1], ypoints[2]));
			ypoints[0] = ypoints[0] - miny;
			ypoints[1] = ypoints[1] - miny;
			ypoints[2] = ypoints[2] - miny;
		}

		/**
		 * should be called after points are changed
		 */
		void update() {
			int w = Math.max(xpoints[0], Math.max(xpoints[1], xpoints[2]));
			int h = Math.max(ypoints[0], Math.max(ypoints[1], ypoints[2]));
    		setBounds(minx, miny, w, h);
		}
	}

	/**
	 * This parent for this triangle
	 * @return This parent for this triangle
	 */
	public Container getParent() {
		return triangle.getParent();
	}

}
