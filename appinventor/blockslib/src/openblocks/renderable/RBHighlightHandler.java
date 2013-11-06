// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.renderable;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;


import openblocks.workspace.RBParent;
import openblocks.codeblockutil.GraphicsManager;

/**
 * The RBHighlightHandler class is responsible for maintaining the
 * highlight state of a RenderableBlock and rendering the highlight
 * properly.  It works by adding itself to a layer behind its RenderableBlock
 * and drawing the highlight there so as not to cover the block.
 */
public class RBHighlightHandler extends JComponent implements ComponentListener, HierarchyListener {
	private static final long serialVersionUID = 328149080427L;
	//highlight stroke and width specifications
    public static final int HIGHLIGHT_STROKE_WIDTH = 12;
	private static final float HIGHLIGHT_ALPHA = .75f;

	private static final Color HIGHLIGHT_COLOR = new Color(255,165,0);

	private Color hColor = null;
	private boolean isSearchResult = false, hasFocus = false;
	private Area blockArea = null;

    private RenderableBlock rb;

    private BufferedImage hImage;

    public RBHighlightHandler(RenderableBlock rb) {
    	super();
    	this.rb = rb;
    	setSize(HIGHLIGHT_STROKE_WIDTH/2,1);
    	this.setOpaque(false);
    	hColor = null;
    	this.blockArea = rb.getBlockArea();
		updateImage();
    	rb.addHierarchyListener(this);
    	rb.addComponentListener(this);
    }

	public void setHighlightColor(Color c) {
		hColor = c;
		updateImage();
		repaint();
	}

	public void setIsSearchResult(boolean isResult) {
		isSearchResult = isResult;
		updateImage();
		repaint();
	}

	public void resetHighlight() {
		hColor = null;
		updateImage();
		repaint();
	}

	public void repaint() {
		if (rb.isVisible()) {
			if (blockArea == null || blockArea != rb.getBlockArea() || (rb.getBlock() != null && rb.getBlock().hasFocus() != hasFocus)) {
				updateImage();
				blockArea = rb.getBlockArea();
			}

			// only update bounds on the Swing thread
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateBounds();
				}
			});
		}
		super.repaint();
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.setColor(new Color(0,0,0,0));
		g2.fillRect(0,0,getWidth(),getHeight());
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,HIGHLIGHT_ALPHA));
        if (hImage != null)
        	g2.drawImage(hImage,0,0,null);
	}

	public void setParent(RBParent newParent) {
		removeFromParent();
		newParent.addToHighlightLayer(this);
		updateImage();
		((Container)newParent).validate();
	}

	public void removeFromParent() {
		if (this.getParent() != null) {
			this.getParent().remove(this);
		}
	}

	public void updateImage() {
		// cache the focus so it'll know when it needs to redraw later.
        hasFocus = rb.getBlock().hasFocus();

        Color color = null;
        if (hColor != null) {
            color = hColor;
        }
        else if (rb.getBlock().hasFocus()) {
            color = HIGHLIGHT_COLOR;
        }
        else if (isSearchResult) {
            color = Color.YELLOW;
        }
        else if (rb.getBlock().isBad()) {
            color = Color.RED;
        }
        else {
            GraphicsManager.recycleGCCompatibleImage(hImage);
            hImage = null;
            return; // if we're not highlighting, destroy the image and just return
        }

        if (!rb.isVisible()) {
            GraphicsManager.recycleGCCompatibleImage(hImage);
            hImage = null;
            return; // if we're not highlighting, destroy the image and just return
        }

        hImage = GraphicsManager.getGCCompatibleImage(rb.getBlockWidth()+HIGHLIGHT_STROKE_WIDTH, rb.getBlockHeight()+HIGHLIGHT_STROKE_WIDTH);

        Graphics2D hg = (Graphics2D)hImage.getGraphics();
		hg.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        hg.setColor(color);
        hg.translate(HIGHLIGHT_STROKE_WIDTH/2,HIGHLIGHT_STROKE_WIDTH/2);
        hg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.2f));
        for (int i = 0; i < HIGHLIGHT_STROKE_WIDTH; i++) {
	        hg.setStroke(new BasicStroke(i));
	        hg.draw(rb.getBlockArea());
        }
        hg.setColor(new Color(0,0,0,0));
        hg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC,1));
        hg.fill(rb.getBlockArea());
	}

	/**
	 * Resizes and moves this highlight to match its RB's size and position
	 */
	private void updateBounds() {
		Point rbLoc = SwingUtilities.convertPoint(rb.getParent(), rb.getLocation(), this.getParent());
		this.setBounds(rbLoc.x-HIGHLIGHT_STROKE_WIDTH/2, rbLoc.y-HIGHLIGHT_STROKE_WIDTH/2, rb.getBlockWidth()+HIGHLIGHT_STROKE_WIDTH, rb.getBlockHeight()+HIGHLIGHT_STROKE_WIDTH);
	}


	/************************************************************
	 * ComponentListener methods for when the RB moves or resizes
	 ************************************************************/
	public void componentResized(ComponentEvent arg0) {
		repaint();
	}

	public void componentMoved(ComponentEvent arg0) {
		repaint();
	}

	public void componentShown(ComponentEvent arg0) {
		repaint();
	}

	public void componentHidden(ComponentEvent arg0) {
        GraphicsManager.recycleGCCompatibleImage(hImage);
		hImage = null;
	}

	/*************************************************************
	 * HierarchyListener method for the RB is added to or removed
	 * from a parent component
	 *************************************************************/
	public void hierarchyChanged(HierarchyEvent he) {
		if (rb.getParent() == null) {
			this.removeFromParent();
            GraphicsManager.recycleGCCompatibleImage(hImage);
			hImage = null;
		}
	}
}
