package openblocks.codeblocks;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import openblocks.codeblocks.rendering.BlockShapeUtil;


/**
 * BlockConnectionShape defines and draws the connectors between the blocks.  This includes
 * the different (command/data) (sockets/plugs).  The standard sockets/plugs have varying styles.
 *
 * BlockConnectionShape doesn't care about the layout of the connectors on the blocks, that is the
 * job of BlockShape.
 *
 * BlockConnectionShape is extendable to allow for 3rd party (command/data) (sockets/plugs) shapes.
 *
 */
public class BlockConnectorShape {

    /** height of horizontal-plug/socket */
    public static final float DATA_PLUG_HEIGHT = 24.0f;
    /** Width of most plug shapes */
    public static final float NORMAL_DATA_PLUG_WIDTH = 8.0f;
    /** Width of polymorphic plug shape */
    public static final float POLYMORPHIC_DATA_PLUG_WIDTH = 8.0f;
    /** width of vertical control connection */
    public static final float CONTROL_PLUG_WIDTH = 14.0f;
    /** height of vertical control connection */
    public static final float CONTROL_PLUG_HEIGHT = 4.0f;


    /** width of command input bar */
    public static final float COMMAND_INPUT_BAR_WIDTH = BlockShape.COMMAND_PORT_OFFSET + 2f;
    /** height of command input bar */
    public static final float COMMAND_INPUT_BAR_HEIGHT = 5f;
    /** default height of command input */
    public static final float DEFAULT_COMMAND_INPUT_HEIGHT = DATA_PLUG_HEIGHT;



    /** The starting point of the current connection begin drawn */
    private Point2D startPoint;
    /** The path of the current connection begin drawn */
    private GeneralPath currentConnectorPath;


    /** A hashmap mapping the application specific string of connection shapes to a shape identifying integer */
    private static HashMap<String, Integer> SHAPE_MAPPINGS = new HashMap<String, Integer>();

    /** The shape name that maps to BlockConnectorShape.COMMAND*/
    private static String COMMAND_SHAPE_NAME;

    /**Different styles of SocketShapes:
     * 1 is the normal shape
     * 2 is the double stacked shape
     * 3 is the double inversion
     */

    public static final int TRIANGLE_1 = 1;
    public static final int TRIANGLE_2 = 2;
    public static final int TRIANGLE_3 = 3;

    public static final int CIRCLE_1 = 4;
    public static final int CIRCLE_2 = 5;
    public static final int CIRCLE_3 = 6;

    public static final int SQUARE_1 = 7;
    public static final int SQUARE_2 = 8;
    public static final int SQUARE_3 = 9;

    public static final int POLYMORPHIC_1 = 10;
    public static final int POLYMORPHIC_2 = 11;
    public static final int POLYMORPHIC_3 = 12;

    public static final int PROC_PARAM = 13;

    public static final int COMMAND = 14;

  public static final int INFIX_RHS = 15;

    public static final boolean DEBUG_MODE = false;


    public BlockConnectorShape() {

    		if (DEBUG_MODE) {
    			addDebugConnectionShapeMappings();
    		}
    }


    private void addDebugConnectionShapeMappings() {
      addConnenctionShapeMapping("number", 1);
      addConnenctionShapeMapping("number-list", 2);
      addConnenctionShapeMapping("number-inv", 3);
      addConnenctionShapeMapping("boolean", 4);
      addConnenctionShapeMapping("boolean-list", 5);
      addConnenctionShapeMapping("boolean-inv", 6);
      addConnenctionShapeMapping("string", 7);
      addConnenctionShapeMapping("string-list", 8);
      addConnenctionShapeMapping("string-inv", 9);
      addConnenctionShapeMapping("poly", 10);
      addConnenctionShapeMapping("poly-list", 11);
      addConnenctionShapeMapping("poly-inv", 12);
      addConnenctionShapeMapping("proc-param", 13);
      addConnenctionShapeMapping("cmd", 14);
      addConnenctionShapeMapping("infix-rhs", 15);
    }

    ///////////////////////////////////////////
    ///// STATIC CONNECTOR SHAPE METHODS /////
    ///////////////////////////////////////////

    /**
     * Add a mapping of an application specific string of a connection shape to a shape identifying integer
     */
    public static void addConnenctionShapeMapping(String shapeName, int integer) {
    		//System.out.println("adding con shape map: "+shapeName+", "+integer);
        SHAPE_MAPPINGS.put(shapeName, integer);

        if(integer == BlockConnectorShape.COMMAND)
            BlockConnectorShape.COMMAND_SHAPE_NAME = shapeName;
    }

    /**
     * Resets all the connector shape names to connector shape mappings.
     *
     */
    public static void resetConnectorShapeMappings(){
        SHAPE_MAPPINGS.clear();
    }

    /**
     * Get a shape identifying integer mapped to an application specific string of a connection shape
     */
    public static int getConnenctionShapeMapping(String shapeName) {
    		if (SHAPE_MAPPINGS.get(shapeName) == null) {
    			assert false : ("Unknown Connection Type: " + shapeName);
    			return -1;
    		} else {
    			return SHAPE_MAPPINGS.get(shapeName);
    		}
    }





    /**
     * Gets the dimension of a given BlockConnector.  Mapping for the connector to a shape must already exist.
     */
    public static Dimension getConnectorDimensions(BlockConnector blockConnector) {
    		int mappedValue = getConnenctionShapeMapping(blockConnector.getKind());

    		//if shaped not yet mapped
    		assert (mappedValue != -1) : "Block Connector is not mapped: "+blockConnector;

    		//TODO: add proc param dimensions
    		if(mappedValue == POLYMORPHIC_1 || mappedValue == POLYMORPHIC_2 || mappedValue == POLYMORPHIC_3) {
    			return new Dimension((int) POLYMORPHIC_DATA_PLUG_WIDTH, (int) DATA_PLUG_HEIGHT);
    		} else {
    			return new Dimension((int) NORMAL_DATA_PLUG_WIDTH, (int) DATA_PLUG_HEIGHT);
    		}
    }






    ////////////////////////////////////
    ///// CONTROL PLUG AND SOCKETS /////
    ////////////////////////////////////
    public Point2D addControlConnectorShape(GeneralPath blockPath, boolean appendRight) {

    		return addControlConnectorShape(blockPath, CONTROL_PLUG_WIDTH / 2, appendRight);

    }


    public Point2D addControlConnectorShape(GeneralPath blockPath, float distanceToCenter, boolean appendRight) {

		//get the initial point info and set the currentConnectorPath to use the _lineTo _curveTo methods
		startPoint = blockPath.getCurrentPoint();
		Point2D socketPoint =
		  new Point2D.Float((float)startPoint.getX() + (appendRight ? distanceToCenter : -distanceToCenter),
		      (float)startPoint.getY());
		currentConnectorPath = blockPath;

		if (appendRight) {
			//then the centerPoint is to the right of the current location on the generalPath
			_lineTo(distanceToCenter - CONTROL_PLUG_WIDTH / 2, 0);
			//update starting point for _curveTo
			startPoint = blockPath.getCurrentPoint();

			_curveTo( CONTROL_PLUG_WIDTH / 2, CONTROL_PLUG_HEIGHT * 4/3,
					  CONTROL_PLUG_WIDTH / 2, CONTROL_PLUG_HEIGHT * 4/3,
					  CONTROL_PLUG_WIDTH, 0);
		} else {
			// then the centerPoint is to the left of the current location on the generalPath
			_lineTo(- distanceToCenter + CONTROL_PLUG_WIDTH / 2, 0);
			//update starting point for _curveTo
			startPoint = blockPath.getCurrentPoint();

			_curveTo( - CONTROL_PLUG_WIDTH / 2, CONTROL_PLUG_HEIGHT * 4/3,
					 - CONTROL_PLUG_WIDTH / 2, CONTROL_PLUG_HEIGHT * 4/3,
					 - CONTROL_PLUG_WIDTH, 0);
		}




		//to catch bugs
		currentConnectorPath = null;

		return socketPoint;
    }






    ///////////////////////////////////////////////////
    /////   COMMAND SOCKET (OVERHANG AND  AREA)   /////
    ///////////////////////////////////////////////////

    public Point2D addCommandSocket(GeneralPath blockPath, int commandSocketHeight) {

		//float xInitial = (float) blockPath.getCurrentPoint().getX();
		//float yInitial = (float) blockPath.getCurrentPoint().getY();

		//draw bar
		BlockShapeUtil.lineToRelative(blockPath, COMMAND_INPUT_BAR_WIDTH, 0);
		BlockShapeUtil.lineToRelative(blockPath, 0, COMMAND_INPUT_BAR_HEIGHT);
		Point2D socketPoint = addControlConnectorShape(blockPath, false);

		//first corner inside command input
		BlockShapeUtil.cornerTo(blockPath,
				new Point2D.Float(
						(float) blockPath.getCurrentPoint().getX() - COMMAND_INPUT_BAR_WIDTH +  BlockShape.CORNER_RADIUS,
						(float) blockPath.getCurrentPoint().getY()),
				new Point2D.Float(
						(float) blockPath.getCurrentPoint().getX() - COMMAND_INPUT_BAR_WIDTH +  BlockShape.CORNER_RADIUS,
						(float) blockPath.getCurrentPoint().getY() + BlockShape.CORNER_RADIUS),
				BlockShape.CORNER_RADIUS);



		//insert dynamic command input height between these two methods
		BlockShapeUtil.lineToRelative(blockPath, 0, commandSocketHeight);

    	//second corner at bottom of command input
		BlockShapeUtil.cornerTo(blockPath,
				new Point2D.Float(
						(float) blockPath.getCurrentPoint().getX(),
						(float) blockPath.getCurrentPoint().getY() + BlockShape.CORNER_RADIUS),
				new Point2D.Float(
						(float) blockPath.getCurrentPoint().getX() + BlockShape.CORNER_RADIUS,
						(float) blockPath.getCurrentPoint().getY() + BlockShape.CORNER_RADIUS),
				BlockShape.CORNER_RADIUS);


		//extend left to match y coordinate of initial point
		BlockShapeUtil.lineToRelative(blockPath, CONTROL_PLUG_WIDTH/2, 0);

		return socketPoint;
    }

    //////////////////////////////////
    ///// DATA PLUGS AND SOCKETS /////
    //////////////////////////////////

 /**
  * Appends a dataPlug to the blockPath given.  Starts drawing from the top.
  *
  * @param blockPath the current GeneralPath of the block being drawn
  * @param connectionShape specifies the socket shape to be added
  * @param onRightSide (note this method assumes startingFromTop)
  */
    public Point2D addDataSocket(GeneralPath blockPath, String connectionShape, boolean onRightSide) {
    			//if onRightSide, socket is convex-left
    		return addDataConnection(blockPath, connectionShape, true, !onRightSide);
    }

    //assosiated method to draw starting from the bottom of the socket
    public Point2D addDataSocketUp(GeneralPath blockPath, String connectionShape, boolean onRightSide) {
		//if onRightSide, socket is convex-left
    		return addDataConnection(blockPath, connectionShape, false, !onRightSide);
    }



    /**
     * Appends a dataSocket to the blockPath given.  Starts drawing from the top.
     *
     * @param blockPath the current GeneralPath of the block being drawn
     * @param connectionShape specifies the socket shape to be added
     * @param onRightSide (note this method assumes startingFromTop)
     */
    public Point2D addDataPlug(GeneralPath blockPath, String connectionShape, boolean onRightSide) {
			//if onRightSide, plug is convex-right
		return addDataConnection(blockPath, connectionShape, true, onRightSide);
    }

//  assosiated method to draw starting from the bottom of the socket
    public Point2D addDataPlugUp(GeneralPath blockPath, String connectionShape, boolean onRightSide) {
		//if onRightSide, plug is convex-right
    		return addDataConnection(blockPath, connectionShape, false, onRightSide);
    }



    /**
     * Appends a specific data connection GeneralPath to a given GeneralPath.  startFromTop specifies
     * that the end of the current generalPath is at the top of the data connection to be drawn.
     * convexRight means that the curve drawn points to the right.
     *
     * The use of _lineTo and _curveTo are to reduce code clutter and to make it clear that
     * these are private methods and not the GeneralPath.lineTo or GeneralPath.curveTo.
     * _lineTo and _curveTo are relative to the starting point of the the data connection to be drawn.
     *
     * @param blockPath is the current GeneralPath to be appended
     * @param socketShape is the integer representing the kind and style of socket to be drawn
     * @param startFromTop true if end of the current generalPath is at the top of the data connection to be drawn
     * @param convexRight true if curve drawn points to the right
     */
    private Point2D addDataConnection(GeneralPath blockPath, String connectionShape, boolean startFromTop, boolean convexRight) {

    		//get the associated connection shape value
    		int connectionShapeInt = getConnenctionShapeMapping(connectionShape);

    		//get the initial point info and set the currentConnectorPath to use the _lineTo _curveTo methods
    		startPoint = blockPath.getCurrentPoint();
		float xStart = (float) startPoint.getX();
		float yStart = (float) startPoint.getY();
		currentConnectorPath = new GeneralPath();
		currentConnectorPath.moveTo(xStart, yStart);

		Point2D socketPoint = new Point2D.Float((float)startPoint.getX(), (startFromTop ? ((float)startPoint.getY() + ((int) DATA_PLUG_HEIGHT / 2)) : ((float)startPoint.getY() - ((int) DATA_PLUG_HEIGHT / 2))));

		switch (connectionShapeInt) {
			//Starlogo Number
		case TRIANGLE_1:
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT / 2);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;

		case TRIANGLE_2:
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT / 4);
			_lineTo( 0, DATA_PLUG_HEIGHT / 2);
			//shifted duplicate
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 3/4);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;

		case TRIANGLE_3:
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT / 4);
			_lineTo( 0, DATA_PLUG_HEIGHT / 2);
			//inversion
			_lineTo( -NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 3/4);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;



			//Starlogo Boolean
		case CIRCLE_1:
			_curveTo(
					(NORMAL_DATA_PLUG_WIDTH) * 4 / 3, 0,
					(NORMAL_DATA_PLUG_WIDTH) * 4 / 3, DATA_PLUG_HEIGHT,
					0, DATA_PLUG_HEIGHT);
			break;


		case CIRCLE_2:
			_curveTo(
					NORMAL_DATA_PLUG_WIDTH, 0,
					NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 1/4,
					NORMAL_DATA_PLUG_WIDTH * 1/2, DATA_PLUG_HEIGHT * 1/2);
			_curveTo(NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 3/4,
					NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT,
					0, DATA_PLUG_HEIGHT);
			break;

		case CIRCLE_3:
			_curveTo(
					NORMAL_DATA_PLUG_WIDTH, 0,
					NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 1/4,
					NORMAL_DATA_PLUG_WIDTH * 1/2, DATA_PLUG_HEIGHT * 1/2);
				//inversion
			_curveTo(-NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 3/4,
					-NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT,
					0, DATA_PLUG_HEIGHT);
			break;



			//Starlogo String
		case SQUARE_1:
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.15f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.15f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.85f);
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.85f);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;

		case SQUARE_2:
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.15f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.15f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.45f);
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.45f);
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.55f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.55f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.85f);
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.85f);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;

		case SQUARE_3:
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.15f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.15f);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.50f);
			_lineTo( -NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.50f);
			_lineTo( -NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 0.85f);
			_lineTo( 0, DATA_PLUG_HEIGHT * 0.85f);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;



		case POLYMORPHIC_1:
			_curveTo(0, DATA_PLUG_HEIGHT/3,
					POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT/3,
					POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT/4);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH *2/3, DATA_PLUG_HEIGHT/6,
					POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT/6,
					POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT/2);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT *5/6,
					POLYMORPHIC_DATA_PLUG_WIDTH *2/3, DATA_PLUG_HEIGHT *5/6,
					POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT *3/4);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT *2/3,
					0, DATA_PLUG_HEIGHT *2/3,
					0, DATA_PLUG_HEIGHT);
			break;

		case POLYMORPHIC_2:
			_curveTo(0, DATA_PLUG_HEIGHT/6,
					POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT/6,
					POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT/8);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH *2/3, DATA_PLUG_HEIGHT/12,
					POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT/12,
					POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT/4);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT *5/12,
					POLYMORPHIC_DATA_PLUG_WIDTH *2/3, DATA_PLUG_HEIGHT *5/12,
					POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT *3/8);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT *2/6,
					0, DATA_PLUG_HEIGHT *2/6,
					0, DATA_PLUG_HEIGHT/2);
				//shifted duplicate
			_curveTo(0, DATA_PLUG_HEIGHT *4/6,
					POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT *4/6,
					POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT *5/8);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH *2/3, DATA_PLUG_HEIGHT *7/12,
					POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT *7/12,
					POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT *3/4);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT *11/12,
					POLYMORPHIC_DATA_PLUG_WIDTH *2/3, DATA_PLUG_HEIGHT *11/12,
					POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT *7/8);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT *5/6,
					0, DATA_PLUG_HEIGHT *5/6,
					0, DATA_PLUG_HEIGHT);
			break;


		case POLYMORPHIC_3:
			_lineTo( POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT / 8);
			_lineTo( POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT * 0.025f);
			_lineTo( POLYMORPHIC_DATA_PLUG_WIDTH * 3/4, DATA_PLUG_HEIGHT/8);
			_curveTo(POLYMORPHIC_DATA_PLUG_WIDTH * 10 / 9, DATA_PLUG_HEIGHT * 0.15f,
					POLYMORPHIC_DATA_PLUG_WIDTH * 10 / 9, DATA_PLUG_HEIGHT * 0.35f,
					POLYMORPHIC_DATA_PLUG_WIDTH * 3 / 4, DATA_PLUG_HEIGHT * 3 / 8);
			_lineTo( POLYMORPHIC_DATA_PLUG_WIDTH / 2, DATA_PLUG_HEIGHT * 0.475f);
			_lineTo( POLYMORPHIC_DATA_PLUG_WIDTH / 3, DATA_PLUG_HEIGHT * 3 / 8);
			_lineTo( 0, DATA_PLUG_HEIGHT / 2);
				//inversion
			_lineTo( -POLYMORPHIC_DATA_PLUG_WIDTH/3, DATA_PLUG_HEIGHT / 8 + DATA_PLUG_HEIGHT / 2);
			_lineTo( -POLYMORPHIC_DATA_PLUG_WIDTH/2, DATA_PLUG_HEIGHT * 0.025f + DATA_PLUG_HEIGHT / 2);
			_lineTo( -POLYMORPHIC_DATA_PLUG_WIDTH * 3/4, DATA_PLUG_HEIGHT/8 + DATA_PLUG_HEIGHT / 2);
			_curveTo(-POLYMORPHIC_DATA_PLUG_WIDTH * 10 / 9, DATA_PLUG_HEIGHT * 0.15f + DATA_PLUG_HEIGHT / 2,
					-POLYMORPHIC_DATA_PLUG_WIDTH * 10 / 9, DATA_PLUG_HEIGHT * 0.35f + DATA_PLUG_HEIGHT / 2,
					-POLYMORPHIC_DATA_PLUG_WIDTH * 3 / 4, DATA_PLUG_HEIGHT * 3 / 8 + DATA_PLUG_HEIGHT / 2);
			_lineTo( -POLYMORPHIC_DATA_PLUG_WIDTH / 2, DATA_PLUG_HEIGHT * 0.475f + DATA_PLUG_HEIGHT / 2);
			_lineTo( -POLYMORPHIC_DATA_PLUG_WIDTH / 3, DATA_PLUG_HEIGHT * 3 / 8 + DATA_PLUG_HEIGHT / 2);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;

			// Used for right-hand side of an infix plug
		case INFIX_RHS:
		  _lineTo ( 0, DATA_PLUG_HEIGHT);

		  // TODO(halbelson:: Consider using this alternate version that draws
		  // a shallow curve instead, but the block that fits in would have to also
		  // be changed to match:
		  // _curveTo(
		  //      (NORMAL_DATA_PLUG_WIDTH) * 9 / 8, 0,
		  //      (NORMAL_DATA_PLUG_WIDTH) * 9 / 8, DATA_PLUG_HEIGHT,
		  //       0, DATA_PLUG_HEIGHT);
		  break;

		case 13:
			_lineTo( 0, DATA_PLUG_HEIGHT * 1 / 4);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 1 / 4);
			_lineTo( NORMAL_DATA_PLUG_WIDTH, DATA_PLUG_HEIGHT * 3 / 4);
			_lineTo( 0, DATA_PLUG_HEIGHT * 3 / 4);
			_lineTo( 0, DATA_PLUG_HEIGHT);
			break;

		//look for additional 3rd party shapes here...

		default:
			//System.out.println("Connection Type Not Identified: " + connectionShape);
			break;
		}


		//flip the path if starting from the bottom or changing convex direction
		if (!startFromTop || !convexRight) {
			transformGeneralPath(currentConnectorPath, !convexRight, !startFromTop);
		}

		//finally append the correctly oriented currentConnectorPath to the blockPath
		blockPath.append(currentConnectorPath, true);

		//to catch bugs
		currentConnectorPath = null;

		return socketPoint;
	}



    /**
	 * Draws a line segment relative to the starting point of the current
	 * connector.
	 */
    private void _lineTo(float x, float y) {
    		currentConnectorPath.lineTo( x + (float) startPoint.getX(), y + (float) startPoint.getY());
    }

    /**
	 * Draws a curve segment relative to the starting point of the current
	 * connector.
	 *
	 * Adds a curved segment, defined by three new points, to the path by
	 * drawing a Bezier curve that intersects both the current coordinates and
	 * the coordinates (x3, y3), using the specified points (x1, y1) and (x2,
	 * y2) as Bezier control points.
	 */
    private void _curveTo(float x1, float y1, float x2, float y2, float x3, float y3)  {
    		currentConnectorPath.curveTo(
    				 x1 + (float) startPoint.getX(), y1 + (float) startPoint.getY(),
				 x2 + (float) startPoint.getX(), y2 + (float) startPoint.getY(),
				 x3 + (float) startPoint.getX(), y3 + (float) startPoint.getY());
    }


    /**
     * Flips a GeneralPath and translates it so the starting point is in the correct place.
     *
     * @param gp the GeneralPath to be transformed
     * @param horzFlip true if flipped horizontally
     * @param vertFlip true if flipped vertically
     */
	private void transformGeneralPath(GeneralPath gp, boolean horzFlip, boolean vertFlip) {
		int xScale, yScale;
		double xTranslate, yTranslate;


		if (horzFlip) {
			xScale = -1;
			xTranslate = 2*startPoint.getX();
		} else {
			xScale = 1;
			xTranslate = 0;
		}


		if (vertFlip) {
			yScale = -1;
			yTranslate = 2*startPoint.getY();
		} else {
			yScale = 1;
			yTranslate = 0;
		}

		//scale (flip)
		AffineTransform scale = AffineTransform.getScaleInstance(xScale, yScale);
		//translate across the origin
		AffineTransform transform = AffineTransform.getTranslateInstance(xTranslate, yTranslate);

		//apply the transforms
		gp.transform(scale);
		gp.transform(transform);
	}

    /**
     * Loads the all the initial BlockConnectorShapes of this language
     * @param root the Element carrying the specifications of the BlockConnectorShapes
     */
    public static void loadBlockConnectorShapes(Element root){


            Pattern attrExtractor=Pattern.compile("\"(.*)\"");
            Matcher nameMatcher;
                NodeList drawerNodes=root.getElementsByTagName("BlockConnectorShape");
                Node drawerNode;

                for(int i=0; i<drawerNodes.getLength(); i++){
                    drawerNode = drawerNodes.item(i);
                    if(drawerNode.getNodeName().equals("BlockConnectorShape")){
                        String shapeType = null;
                        String shapeNumber = null;
                        nameMatcher=attrExtractor.matcher(drawerNode.getAttributes().getNamedItem("shape-type").toString());
                        if (nameMatcher.find()) //will be true
                            shapeType = nameMatcher.group(1);
                        nameMatcher=attrExtractor.matcher(drawerNode.getAttributes().getNamedItem("shape-number").toString());
                        if (nameMatcher.find()) //will be true
                            shapeNumber = nameMatcher.group(1);

                        if(shapeType != null && shapeNumber != null){

                            //create shape to number mapping here
                            BlockConnectorShape.addConnenctionShapeMapping(shapeType, Integer.parseInt(shapeNumber));
                        }
                    }
                }


    }



    ///////////////////////////
    // STATIC HELPER METHODS //
    ///////////////////////////

    static public boolean isCommandConnector(BlockConnector connector) {
    		return (BlockConnectorShape.getConnenctionShapeMapping(connector.getKind()) == BlockConnectorShape.COMMAND);
    }

    static public String getCommandShapeName(){
        return BlockConnectorShape.COMMAND_SHAPE_NAME;
    }
}
