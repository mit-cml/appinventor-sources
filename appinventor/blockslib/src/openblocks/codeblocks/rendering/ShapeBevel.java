package openblocks.codeblocks.rendering;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;



public class ShapeBevel {
    
    public static void createShapeBevel(Graphics2D g2,Shape theShape,double flatness,int numBands,float bevelSize,float light[]) {
        // draw bands from inside of shape to outside (important)
        for(int i=numBands-1;i>=0;i--) {
            // get the path around the block area, flattening any curves
            // 0.2 is flattening tolerance, big == faster, small == smoother
            BevelIterator bi = new BevelIterator(theShape,flatness);
            
            // cos and sin of angle between surface normal and screen plane
            float theCos = 1f-((float)i+0.5f)/numBands; // center of band
            float theSin = (float)Math.sqrt(1-theCos*theCos);
            
            float from = 0; // draw strips from outer edge,
            float to = 1f/numBands*(i+1)*bevelSize; // to this distance
            // the overlap makes sure there is no tiny space between the bands
            
            g2.setStroke(new BasicStroke(to-from));
            
            float[] p = {0,0};
            float norm[] = {0,0,0};
            float grayAlpha[] = {0,0}; // receives gray and alpha
            
            while (!bi.isDone()) {
                //count++;
                bi.nextSegment();
                
                norm[0] = bi.perpVec[0]*theCos;
                norm[1] = bi.perpVec[1]*theCos;
                norm[2] = theSin;
                getLightingOverlay(norm,light,grayAlpha);
                g2.setColor(new Color(grayAlpha[0],grayAlpha[0],grayAlpha[0],grayAlpha[1]));
                g2.setComposite(AlphaComposite.Src);
                
                GeneralPath gp = new GeneralPath();
                bi.insetPoint2(from,p); gp.moveTo(p[0],p[1]);
                bi.insetPoint3(from,p); gp.lineTo(p[0],p[1]);
                bi.insetPoint3(to,p); gp.lineTo(p[0],p[1]);
                bi.insetPoint2(to,p); gp.lineTo(p[0],p[1]);
                gp.closePath();
                g2.fill(gp);
                
            }
        }
    }
    
    public static Color getFrontFaceOverlay(float light[]) {
        float frontNorm[] = {0,0,1};
        float frontGrayAlpha[] = {0,0}; // receives gray,alpha
        getLightingOverlay(frontNorm,light,frontGrayAlpha);
        return new Color(frontGrayAlpha[0],frontGrayAlpha[0],frontGrayAlpha[0],frontGrayAlpha[1]);
    }
        
    
    public static float[] getLightVector(float x,float y,float z) {
        // normalized light vector
        float light[] = {x,y,z};
        float lightLen = (float)Math.sqrt(light[0]*light[0]+light[1]*light[1]+light[2]*light[2]);
        light[0] /= lightLen; light[1] /= lightLen; light[2] /= lightLen;
        return light;
    }
        
    /**
     * takes a normalized surface normal and normalized light vector and returns
     * a (gray,alpha) color value to be applied to the block
     */
    private static void getLightingOverlay( float norm[], float light[], float grayAlpha[] ) {
        // dot product with light produces diffuse shading
        float lum = norm[0]*light[0] + norm[1]*light[1] + norm[2]*light[2];
        // amount of white to apply (float const is an arbitrary strength)
        float shine = (2*norm[2]*lum - light[2]);
        if (shine < 0) shine = 0;
        shine = shine*shine*shine*shine*0.9f;
        if (lum < 0) lum = 0;
        // amount of black to apply (float const is an arbitrary strength)
        float shad = (1f-lum);
        shad = shad*shad;
        shad *= 0.8f;
        // combine the effects of overlaying shad black and shine white
        grayAlpha[1] = shad+shine-shad*shine;
        if (shine > 0) grayAlpha[0] = shine/grayAlpha[1];
        else grayAlpha[0] = 0;
    }
}
