// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblocks.rendering;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import openblocks.codeblockutil.GraphicsManager;
import openblocks.codeblockutil.LRUCache;

public class BlockShapeUtil {



    /**
	 * Draws a line segment relative to the current point of the GeneralPath.
	 */
    public static void lineToRelative(GeneralPath gp, float x, float y) {
    		Point2D currentPoint = gp.getCurrentPoint();
    		gp.lineTo( (float) currentPoint.getX() + x, (float) currentPoint.getY() + y);
    }

    /**
	 * Draws a curve segment relative to the current point of the GeneralPath.
	 *
	 * Adds a curved segment, defined by three new points, to the path by
	 * drawing a Bezier curve that intersects both the current coordinates and
	 * the coordinates (x3, y3), using the specified points (x1, y1) and (x2,
	 * y2) as Bezier control points.
	 */
    public static void curveTo(GeneralPath gp, float x1, float y1, float x2, float y2, float x3, float y3)  {
    		Point2D currentPoint = gp.getCurrentPoint();
    		gp.curveTo(
    				 x1 + (float) currentPoint.getX(), y1 + (float) currentPoint.getY(),
				 x2 + (float) currentPoint.getX(), y2 + (float) currentPoint.getY(),
				 x3 + (float) currentPoint.getX(), y3 + (float) currentPoint.getY());
    }





    /**
	 * Draws a corner relative to the current point of the GeneralPath.
	 * @param gp is the general path to which the corner is being added
	 * @param cornerPoint is where the intersection of the two sides would be if there was no curve
	 * @param nextCornerPoint is the location where the corner is curving to
	 * @param radius is the radius size of the corner
	 */
    public static void cornerTo(GeneralPath gp, Point2D cornerPoint, Point2D nextCornerPoint, float radius) {
        if (radius < 0.001f) {
        		//if a small radius, just draw a line
        		gp.lineTo((float) cornerPoint.getX(), (float) cornerPoint.getY());
        } else {
        		makeCornerTo(gp, cornerPoint, nextCornerPoint, radius);
        }
    }




    /**
	 * Draws a corner relative to the current point of the GeneralPath.  Note the radius denotes the
	 * distance from the cornerPoint to where the curve starts on the line formed from the cornerPoint to the
	 * current point on the gp and where the curve ends on the line from the cornerPoint to the nextCornerPoint.
	 * In other words,
	 */
    private static void makeCornerTo(GeneralPath gp, Point2D cornerPoint, Point2D nextCornerPoint, float radius)  {
    		Point2D currentPoint = gp.getCurrentPoint();

    		//get fractional to the corner where the line first starts to curve
    		double distance = currentPoint.distance(cornerPoint);
    		double fraction = (distance - radius) / distance;

    		//calculate these distance from the current point
    		double xDistance = (cornerPoint.getX() - currentPoint.getX()) * fraction;
    		double yDistance = (cornerPoint.getY() - currentPoint.getY()) * fraction;

    		//draw a line to the point where the line first starts to curve
    		lineToRelative(gp, (float) xDistance, (float) yDistance);

    		Point2D startCurvePoint = gp.getCurrentPoint();


    		//get fractional to the corner where the line first starts to curve
    		double distanceFromCornerToNextCorner = cornerPoint.distance(nextCornerPoint);
    		double fractionToNextCorner = radius / distanceFromCornerToNextCorner;

    		//calculate these distance from the current point
    		double xDistanceFromCornerToEndCurve = (nextCornerPoint.getX() - cornerPoint.getX()) * fractionToNextCorner;
    		double yDistanceFromCornerToEndCurve = (nextCornerPoint.getY() - cornerPoint.getY()) * fractionToNextCorner;


    		Point2D endCurvePoint = new Point2D.Double(cornerPoint.getX() + xDistanceFromCornerToEndCurve,
    												cornerPoint.getY() + yDistanceFromCornerToEndCurve);


    		//finally draw the cornerShape
    		cornerShape(gp,
    						//start at:
    						(float) startCurvePoint.getX(), (float) startCurvePoint.getY(),
    						//corner at:
    						(float) cornerPoint.getX(), (float) cornerPoint.getY(),
    						//end at:
    						(float) endCurvePoint.getX(), (float) endCurvePoint.getY());

//    		System.out.println("StartCurve at: " + startCurvePoint);
//    		System.out.println("Corner at: " + cornerPoint);
//    		System.out.println("EndCurve at: " + endCurvePoint);
//    		System.out.println("NextCorner at: " + nextCornerPoint);
    }




    /** Assumes we are at (x1,y1), the corner point is (x2,y2), and we end at (x3, y3) */
    public static void cornerShape(GeneralPath gp, float x1, float y1, float x2, float y2, float x3, float y3) {
        gp.curveTo((x1 + x2) / 2, (y1 + y2) / 2, (x2 + x3) / 2, (y2 + y3) / 2, x3, y3);
    }




    private static class BevelCacheKey
    {
        public final int width, height;
        public final Area area;
        private final int hashCode;

        public BevelCacheKey(int width, int height, Area area)
        {
            this.width = width;
            this.height = height;
            this.area = area;
            this.hashCode = computeHashCode();
        }

        private int computeHashCode()
        {
            int hash = width * 1313 + height * 71;
            // Area.hashCode() is not implemented, so we have to do it ourselves..
            PathIterator pi = area.getPathIterator(null);
            double[] arg = new double[6];
            while (!pi.isDone())
            {
                for (int i = 0; i < 6; i++) arg[i] = 0;

                int val = pi.getWindingRule();
                val += 3 * pi.currentSegment(arg);
                for (int i = 0; i < 6; i++)
                    val = (val * 5) + (int) Math.floor(arg[i] * 1000);

                hash = hash * 7 + val;
                pi.next();
            }

            return hash;
        }

        @Override public boolean equals(Object o)
        {
            if (!(o instanceof BevelCacheKey) || o.hashCode() != hashCode)
                return false;
            BevelCacheKey b = (BevelCacheKey) o;
            return width == b.width && height == b.height && area.equals(b.area);
        }

        @Override public int hashCode()
        {
            return hashCode;
        }
    }
    static private final int BEVEL_CACHE_SIZE = 200;
    static private final LRUCache<BevelCacheKey, BufferedImage> bevelCache = new LRUCache<BevelCacheKey, BufferedImage>(BEVEL_CACHE_SIZE);

	/**
	 * Static method to return bufferedImage of a Beveled outline of a block
	 */
    public static Image getBevelImage(int width, int height, Area s) {
        BevelCacheKey key = new BevelCacheKey(width, height, s);
        BufferedImage img;
        img = bevelCache.get(key);
        if (img != null)
        {
            //System.out.println("Found cached bevel!");
            return img;
        }
        //System.out.println("Not found cached bevel!");
        //generic light vector - "chosen to look good"
        float[] light = ShapeBevel.getLightVector(-1,-2,2);
        int bevelSize = 3;
    	//create image
        img = GraphicsManager.gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g2 = (Graphics2D)img.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ShapeBevel.getFrontFaceOverlay(light));
        g2.fill(s);
        ShapeBevel.createShapeBevel(g2, s, 0.1, bevelSize, bevelSize, light);
        // Make a copy of the Area to prevent aliasing.
        BevelCacheKey key2 = new BevelCacheKey(width, height, new Area(s));
        bevelCache.put(key2, img);
        return img;
    }







	/**
	 * Appends path gp2 to gp1.
	 * Taken from pre-redesign code.
	 * @param reversed is true if the segments are added in reverse order
	 */
	public static  void appendPath(GeneralPath gp1, GeneralPath gp2, boolean reversed) {
		ArrayList<Number[]> points = new ArrayList<Number[]>(); // Each element is an array consisting of one Integer and six Floats

		PathIterator i = gp2.getPathIterator(new AffineTransform());

		float[] segment = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

		float leftmost = Float.POSITIVE_INFINITY;

		while(!i.isDone()) {
			int type = i.currentSegment(segment);
			i.next();

			points.add(new Number[] {
					new Integer(type),
					new Float(segment[0]),
					new Float(segment[1]),
					new Float(segment[2]),
					new Float(segment[3]),
					new Float(segment[4]),
					new Float(segment[5])
			});
		}

		if(reversed) {
			float deltaX = (float)gp1.getCurrentPoint().getX();
			float deltaY = (float)gp1.getCurrentPoint().getY();

			Object[] typeAndPoints = points.get(points.size() - 1);

			int type = ((Integer)typeAndPoints[0]).intValue();

			if(type == PathIterator.SEG_LINETO) {
				deltaX -= ((Float)typeAndPoints[1]).floatValue();
				deltaY -= ((Float)typeAndPoints[2]).floatValue();
			}
			else if(type == PathIterator.SEG_QUADTO) {
				deltaX -= ((Float)typeAndPoints[3]).floatValue();
				deltaY -= ((Float)typeAndPoints[4]).floatValue();
			}
			else if(type == PathIterator.SEG_CUBICTO) {
				deltaX -= ((Float)typeAndPoints[5]).floatValue();
				deltaY -= ((Float)typeAndPoints[6]).floatValue();
			}
			else
				assert false : type;

			for(int j = points.size() - 1; j >= 1; j--) {
				typeAndPoints = points.get(j);

				type = ((Integer)typeAndPoints[0]).intValue();
				float x1 = ((Float)typeAndPoints[1]).floatValue();
				float y1 = ((Float)typeAndPoints[2]).floatValue();
				float x2 = ((Float)typeAndPoints[3]).floatValue();
				float y2 = ((Float)typeAndPoints[4]).floatValue();

				float prevX = 0.0f, prevY = 0.0f;

				int prevType = ((Integer)points.get(j - 1)[0]).intValue();

				if((prevType == PathIterator.SEG_MOVETO) || (prevType == PathIterator.SEG_LINETO)) {
					prevX = ((Float)points.get(j - 1)[1]).floatValue();
					prevY = ((Float)points.get(j - 1)[2]).floatValue();
				}
				else if(prevType == PathIterator.SEG_QUADTO) {
					prevX = ((Float)points.get(j - 1)[3]).floatValue();
					prevY = ((Float)points.get(j - 1)[4]).floatValue();
				}
				else if(prevType == PathIterator.SEG_CUBICTO) {
					prevX = ((Float)points.get(j - 1)[5]).floatValue();
					prevY = ((Float)points.get(j - 1)[6]).floatValue();
				}
				else
					assert false : prevType;

				leftmost = Math.min(leftmost, prevX + deltaX);

				if((type == PathIterator.SEG_MOVETO) || (type == PathIterator.SEG_LINETO)) {
					gp1.lineTo(prevX + deltaX, prevY + deltaY);
				}
				else if(type == PathIterator.SEG_QUADTO) {
					gp1.quadTo(x1 + deltaX, y1 + deltaY, prevX + deltaX, prevY + deltaY);
				}
				else if(type == PathIterator.SEG_CUBICTO) {
					gp1.curveTo(x2 + deltaX, y2 + deltaY, x1 + deltaX, y1 + deltaY, prevX + deltaX, prevY + deltaY);
				}
				else
					assert false : type;
			}
		}
		else // Not reversed
		{
			float deltaX = (float)gp1.getCurrentPoint().getX() - ((Float)points.get(0)[1]).floatValue();
			float deltaY = (float)gp1.getCurrentPoint().getY() - ((Float)points.get(0)[2]).floatValue();

			for(int j = 1; j < points.size(); j++) {
				Object[] typeAndPoints = points.get(j);

				int type = ((Integer)typeAndPoints[0]).intValue();
				float x1 = ((Float)typeAndPoints[1]).floatValue();
				float y1 = ((Float)typeAndPoints[2]).floatValue();
				float x2 = ((Float)typeAndPoints[3]).floatValue();
				float y2 = ((Float)typeAndPoints[4]).floatValue();
				float x3 = ((Float)typeAndPoints[5]).floatValue();
				float y3 = ((Float)typeAndPoints[6]).floatValue();

				if(type == PathIterator.SEG_MOVETO) {
				}
				else if(type == PathIterator.SEG_LINETO) {
					gp1.lineTo(x1 + deltaX, y1 + deltaY);

					leftmost = Math.min(leftmost, x1 + deltaX);
				}
				else if(type == PathIterator.SEG_QUADTO) {
					gp1.quadTo(x1 + deltaX, y1 + deltaY, x2 + deltaX, y2 + deltaY);

					leftmost = Math.min(leftmost, x2 + deltaX);
				}
				else if(type == PathIterator.SEG_CUBICTO) {
					gp1.curveTo(x1 + deltaX, y1 + deltaY, x2 + deltaX, y2 + deltaY, x3 + deltaX, y3 + deltaY);

					leftmost = Math.min(leftmost, x3 + deltaX);
				}
				else
					assert false : type;
			}
		}
	}






	  /** Prints out a GeneralPath.  Used for debugging only */
    public static void printPath(GeneralPath gp)
    {
        if(gp == null) {
            System.out.println("(null path)");

            return;
        }

        int type;
        float[] segment = new float[6];

        PathIterator i = gp.getPathIterator(new AffineTransform());

        while(!i.isDone()) {
            type = i.currentSegment(segment);

            if(type == PathIterator.SEG_MOVETO)
                System.out.println("m: (" + segment[0] + ", " + segment[1] + ")");
            else if(type == PathIterator.SEG_LINETO)
                System.out.println("l: (" + segment[0] + ", " + segment[1] + ")");
            else if(type == PathIterator.SEG_QUADTO)
                System.out.println("q: (" + segment[0] + ", " + segment[1] + "), (" + segment[2] + ", " + segment[3] + ")");
            else if(type == PathIterator.SEG_CUBICTO)
                System.out.println("c: (" + segment[0] + ", " + segment[1] + "), (" + segment[2] + ", " + segment[3] + "), (" + segment[4] + ", " + segment[5] + ")");

            i.next();
        }
    }



}
