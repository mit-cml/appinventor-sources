package openblocks.codeblocks;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public abstract class CustomBlockShapeSet {


  /**
   * Contains all of the custom block shapes that are being used in a project.
   */
  public ArrayList<CustomBlockShape> customBlockShapes = new ArrayList<CustomBlockShape>();


  /**
   * Add CustomBlockShape to the array of customBlockShapes.
   */
  protected void addCustomBlockShape(CustomBlockShape customBlockShape) {
    customBlockShapes.add(customBlockShape);
  }



  /**
   * Internal CustomBlockShape class.
   *
   */
  public class CustomBlockShape {

    protected String genusName;

    protected Point2D topLeftCorner;
    protected Point2D topRightCorner;
    protected Point2D botLeftCorner;
    protected Point2D botRightCorner;
  }



  /**
   * Checks if the given block is a "special shape" that has custom dimensions.
   * CornerPoints is an array with array order Point2D topLeftCorner, Point2D topRightCorner,
   * Point2D botLeftCorner, Point2D botRightCorner.
   *
   * @return if a matching customBlockShape was found that corresponds to the given block.
   * @modifies the array of cornerPoints if method returns true
   */
  public boolean checkCustomShapes(Block b, Point2D[] cornerPoints, int labelsWidth, int totalSocketHeight) {

    //for every customBlockShape
    for(CustomBlockShape cbs : customBlockShapes) {

      //check genus for a match
      if(b.getGenusName().equals(cbs.genusName)) {

        //set corner points, making room for the labelWidth
        cornerPoints[0] = (Point2D) cbs.topLeftCorner.clone();
        cornerPoints[1] = new Point2D.Double(cbs.topRightCorner.getX() + labelsWidth,
            cbs.topRightCorner.getY());
        cornerPoints[2] = new Point2D.Double(cbs.botLeftCorner.getX(), cbs.botLeftCorner.getY() +totalSocketHeight);
        cornerPoints[3] = new Point2D.Double(cbs.botRightCorner.getX() + labelsWidth,
            cbs.botRightCorner.getY() + totalSocketHeight);
        return true;
      }
    }

    //else no match was found
    return false;
  }






}
