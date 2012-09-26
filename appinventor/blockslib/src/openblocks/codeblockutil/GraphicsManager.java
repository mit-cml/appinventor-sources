// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;


/**
 * GraphicsManager maintains graphics context information that maybe useful for many aspects of
 * graphics rendering.  This means that many of the fields are computer dependent and are
 * placed here so they are only calculated once.
 */

public class GraphicsManager {
	
	/** get GraphicConfiguration for default screen - this should only be done once at startup **/
	private static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	private static GraphicsDevice gs = ge.getDefaultScreenDevice();
	public static GraphicsConfiguration gc = gs.getDefaultConfiguration();

    private static final int MAX_RECYCLED_IMAGES = 50;
    private static int numRecycledImages = 0;
    private static HashMap<Dimension, List<BufferedImage>> recycledImages = new HashMap<Dimension, List<BufferedImage>>();
    /** 
     * Functionally equivalent to
     *   gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT)
     * but allows reusing released images.
     */
    public static BufferedImage getGCCompatibleImage(int width, int height)
    {
        List<BufferedImage> imgList = recycledImages.get(new Dimension(width, height));
        if (imgList == null || imgList.isEmpty())
            return gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        numRecycledImages--;
        BufferedImage img = imgList.remove(imgList.size() - 1);
        // Clear the image
        Graphics2D g2D = img.createGraphics();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = new Rectangle2D.Double(0,0,width,height); 
        g2D.fill(rect);
        return img;
    }
    
    /** 
     * Add an image to the recycled images list (or if img = null, does nothing).
     * Note: the passed variable should be immediately set to null to avoid aliasing bugs.
     */ 
    public static void recycleGCCompatibleImage(BufferedImage img) {
        if (img == null) 
            return;
        // Make sure we don't waste too much memory
        if (numRecycledImages >= MAX_RECYCLED_IMAGES) {
            recycledImages.clear();
        }
        Dimension dim = new Dimension(img.getWidth(), img.getHeight());
        List<BufferedImage> imgList = recycledImages.get(dim);
        if (imgList == null)
        {
            imgList = new ArrayList<BufferedImage>();
            recycledImages.put(dim, imgList);
        }
        imgList.add(img);
    }
}
