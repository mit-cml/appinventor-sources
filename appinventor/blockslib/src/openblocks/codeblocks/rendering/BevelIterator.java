// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblocks.rendering;

import java.awt.Shape;
import java.awt.geom.PathIterator;

/**
 * The <code>BevelIterator</code> class handles the geometry calculations involved in
 * creating a bevel effect around the edge of a shape. Like a <code>FlatteningPathIterator</code>,
 * it iterates over a set of line segments that follows the shape's boundary. For each segment
 * it provides four points, of which the 2nd and 3rd are the endpoints of the current segment,
 * and the 1st and 4th are the endpoints of the neighboring segments. From these four points
 * relevant vectors for drawing a bevel are automatically calculated and provided.
 * <p>
 * Points and vectors are specified by (x,y) coordinates in an array.
 *
 * @see java.awt.geom.PathIterator
 */

public class BevelIterator {

    /** the point that comes before the current segment's start-point */
    public float pt1[] = {0,0};
    /** the start-point of the current segment */
    public float pt2[] = {0,0};
    /** the end-point of the current segment */
    public float pt3[] = {0,0};
    /** the point that comes after the current segment's end-point */
    public float pt4[] = {0,0};
    /** a vector pointing inwards from <code>pt2</code>, bisecting the angle at that point,
      * scaled to have a length appropriate for a bevel of thickness 1.0 (in other words, lying
      * on the line that is parallel to the current segment and a distance of 1.0 inwards). */
    public float inset2[] = {0,0};
    /** a vector pointing inwards from <code>pt3</code>, bisecting the angle at that point,
      * scaled to have a length appropriate for a bevel of thickness 1.0 (in other words, lying
      * on the line that is parallel to the current segment and a distance of 1.0 inwards). */
    public float inset3[] = {0,0};
    /** a unit vector perpendicular to the current segment, pointing outwards */
    public float perpVec[] = {0,0};

    private Shape area;
    private double flatness;
    private PathIterator iter;
    private int progress;
    private float currentPoint[] = {0,0};

    /**
     * Constructs a BevelIterator for a given shape. The shape should be simply-connected and
     * have a boundary that does not intersect itself. The <code>flatness</code> parameter has
     * the same meaning as in the constructor of <code>FlatteningPathIterator</code>. The fields of this class
     * are undefined until <code>nextSegment</code> is first called.
     *
     * @param area the region whose boundary is to be beveled
     * @param flatness the maximum allowable distance by which the segments can deviate from the actual boundary
     */
    public BevelIterator( Shape area, double flatness ) {
        this.area = area;
        this.flatness = flatness;
        iter = area.getPathIterator(null,flatness);
        progress = -2;
        doGetPoint();
        pt1[0] = currentPoint[0]; pt1[1] = currentPoint[1];
        pathIterAdvance();
        pt2[0] = currentPoint[0]; pt2[1] = currentPoint[1];
        pathIterAdvance();
        pt3[0] = currentPoint[0]; pt3[1] = currentPoint[1];
        pathIterAdvance();
        pt4[0] = currentPoint[0]; pt4[1] = currentPoint[1];
        getInsetVector(pt1,pt2,pt3,inset2);
        getInsetVector(pt2,pt3,pt4,inset3);
        getPerpVec(pt2,pt3,perpVec);
    }

    /**
     * Fills in this class's fields with information about the next segment. Has no effect
     * if all segments have been encountered.
     */
    public void nextSegment() {
        if (!isDone()) {
            pathIterAdvance();
            pt1[0] = pt2[0]; pt1[1] = pt2[1];
            pt2[0] = pt3[0]; pt2[1] = pt3[1];
            pt3[0] = pt4[0]; pt3[1] = pt4[1];
            pt4[0] = currentPoint[0]; pt4[1] = currentPoint[1];
            inset2[0] = inset3[0]; inset2[1] = inset3[1];
            getInsetVector(pt2,pt3,pt4,inset3);
            getPerpVec(pt2,pt3,perpVec);
        }
    }

    /**
     * Returns <code>true</code> if a call to <code>nextSegment</code> will move to a new segment.
     *
     * @return whether all segments have been encountered
     */
    public boolean isDone() {
        return progress >= 2;
    }

    /**
     * Scales the vector <code>inset2</code> by the factor <code>scalar</code> and adds it to <code>pt2</code>.
     * The returned point is <code>pt2</code> inset by an amount appropriate for a bevel of
     * thickness <code>scalar</code>.
     *
     * @param scalar amount to scale <code>inset2</code> by
     * @param f receives the new point
     * @return the argument <code>f</code>
     */
    public float[] insetPoint2(float scalar,float[] f) {
        f[0] = pt2[0]+inset2[0]*scalar;
        f[1] = pt2[1]+inset2[1]*scalar;
        return f;
    }

    /**
     * Scales the vector <code>inset3</code> by the factor <code>scalar</code> and adds it to <code>pt3</code>.
     * The returned point is <code>pt3</code> inset by an amount appropriate for a bevel of
     * thickness <code>scalar</code>.
     *
     * @param scalar amount to scale <code>inset3</code> by
     * @param f receives the new point
     * @return the argument <code>f</code>
     */
    public float[] insetPoint3(float scalar,float[] f) {
        f[0] = pt3[0]+inset3[0]*scalar;
        f[1] = pt3[1]+inset3[1]*scalar;
        return f;
    }

    ///// private stuff

    private float p[] = {0,0,0,0,0,0};

    private void pathIterAdvance() {
        if (progress < -1) {
            progress++;
            return;
        }
        //float p[] = {0,0,0,0,0,0};
        iter.next();
        if ((!iter.isDone())&&(iter.currentSegment(p)==PathIterator.SEG_CLOSE)) iter.next();
        if (iter.isDone()) {
            iter = area.getPathIterator(null,flatness);
            progress++;
            // if this is the first time done, pathIterProgress now says 0 for the first time
        }
        else {
            if (progress >= 0) {
                progress++; // this isn't the first time around, counting now
            }
        }
        p[0] = currentPoint[0];
        p[1] = currentPoint[1];
        doGetPoint();
        if (((p[0]-currentPoint[0])*(p[0]-currentPoint[0])+(p[1]-currentPoint[1])*(p[1]-currentPoint[1])
            < 0.001)&&(!isDone())) {
            pathIterAdvance();
        }
    }

    private float array[] = {0,0,0,0,0,0};
    private int doGetPoint() {
        int kind = iter.currentSegment(array);
        currentPoint[0] = array[0];
        currentPoint[1] = array[1];
        return kind;
    }

    private float v1[] = {0,0}; // vector 2_1
    private float v2[] = {0,0}; // vector 2_3
    /**
     * a vector parallel to the angle bisector of angle 123,
     * pointing into the block area
     * that assumes a bevel size of 1
     */
    private void getInsetVector( float pt1[], float pt2[], float pt3[], float out[] ) {
        v1[0] = pt1[0]-pt2[0];
        v1[1] = pt1[1]-pt2[1];
        v2[0] = pt3[0]-pt2[0];
        v2[1] = pt3[1]-pt2[1];

        float len;
        // normalize them
        len = (float)Math.sqrt(v1[0]*v1[0]+v1[1]*v1[1]);
        v1[0] /= len; v1[1] /= len;
        len = (float)Math.sqrt(v2[0]*v2[0]+v2[1]*v2[1]);
        v2[0] /= len; v2[1] /= len;

        out[0] = v1[0]+v2[0]; out[1] = v1[1]+v2[1]; // add vectors
        if (out[0] == 0 && out[1] == 0) {
            out[0] = -v1[1];
            out[1] = v1[0];
        }
        else {
            // fix length
            float scale = 1f/(-v1[1]*v2[0]+v1[0]*v2[1]);
            out[0] *= scale; out[1] *= scale;
        }
    }

    /** returns unit vector perpendicular to the line joining the points */
    private void getPerpVec( float pt1[], float pt2[], float vec[] ) {
        // perpendicular vector
        vec[0] = -(pt2[1]-pt1[1]);
        vec[1] = (pt2[0]-pt1[0]);
        // normalize
        float len = (float)Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]);
        vec[0] /= len; vec[1] /= len;
    }

}
