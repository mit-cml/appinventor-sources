// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.net.URL;

public class ImageManager {
    
    private Component comp;
    
    public ImageManager(Component comp) {
	assert(comp != null);
        this.comp = comp;
    }
    
    public BufferedImage createImage(String file) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
	URL u = ImageManager.class.getResource(file);
	if (u == null) {
		System.out.println("Could not find resource " + file);
		return null;
	}
        Image img = toolkit.createImage(u);
        if (img == null) {
            System.out.println("Couldn't load image " + file);
            return null;
        }

	
	MediaTracker mt = new MediaTracker(comp);
	try {
	    mt.addImage(img,0);
	    mt.waitForAll();
	}
	catch(Exception e) {
            System.out.println("Couldn't load image "+file);
            System.out.println(e);
            return null;
        }
        if (mt.isErrorAny()) {
            System.out.println("Couldn't load image "+file);
            return null;
        }
        
//         ImageObserver observer = new ImageObserver() {
//             public boolean imageUpdate(Image img,int flags,int x,int y,int w,int h) {
//                 if ((flags & (ALLBITS | FRAMEBITS | ABORT)) != 0) {
//                     synchronized (this) { notify(); }
//                     return false;
//                 }
//                 return true;
//             }
//         };
//         try {
//             synchronized (observer) {
//                 while (!toolkit.prepareImage(img,-1,-1,observer)) { observer.wait(); }
//             }
//         }
//         catch (InterruptedException e) {
//             System.out.println("Couldn't load image "+file);
//             return null;
//         }
	//System.out.println("image width "+ img.getWidth(comp) + 
	//		   " height " + img.getHeight(comp));
        BufferedImage bimg =
            comp.getGraphicsConfiguration()
            .createCompatibleImage(img.getWidth(comp),
				   img.getHeight(comp),
				   Transparency.TRANSLUCENT);
        bimg.getGraphics().drawImage(img,0,0,comp);
        return bimg;
    }
        
    public static boolean hitTest(BufferedImage img,int x,int y) {
        return ((img.getRGB(x,y) >> 24) & 0xFF) >= 0xFF/2;
    }
    
}
