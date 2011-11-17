package openblocks.codeblocks;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import openblocks.renderable.RenderableBlock;

import openblocks.codeblocks.rendering.BlockShapeUtil;



/**
 * This class separates block shape from the RenderableBlock class.  BlockShape uses information
 * from RenderableBlock to determine shape and dimensions of the block being drawn.
 * 
 * BlockShape does not know where it is in the world -- all coords are local.
 */

public class BlockShape {

        /** Draws the individual connectors.  Shouldn't need more than one of these */
        protected static BlockConnectorShape BCS = new BlockConnectorShape();
        /** Tools to draw block shape.  Shouldn't need more than one of these */
        protected static BlockShapeUtil BSU = new BlockShapeUtil();
        
        /** The RenderableBlock associated to this BlockShape */
        protected RenderableBlock rb;
        /** The Block associated to this BlockShape */
        protected Block block;
        
        /** ArrayList of the CustonBlockShapeSets that are checked */
        private static ArrayList<CustomBlockShapeSet> customBlockShapeSets = new ArrayList<CustomBlockShapeSet>();
        
    /** left alignment buffer for command ports */
    public static final float COMMAND_PORT_OFFSET = 15f;
        
    /** radius of rounded corners */
    public static final float CORNER_RADIUS = 3.0f;
    /** variable declaration spacer */
    public static final float VARIABLE_DECLARATION_SPACER = 10f;
    /** spacer for bottom sockets to block sides and other bottom sockets */
    public static final float BOTTOM_SOCKET_SIDE_SPACER = 10f;
    /** spacer for in between bottom sockets */
    public static final float BOTTOM_SOCKET_MIDDLE_SPACER = 16f;
    /** spacer on top of bottom sockets to give continuous top */
    public static final float BOTTOM_SOCKET_UPPER_SPACER = 4f;
    
    /** Outline of top-left corner, top edge, and top-right corner */
    private GeneralPath gpTop;
    /** Outline of right edge */
    private GeneralPath gpRight;
    /** Outline of bottom-left conrner, bottom edge, and bottom-right corner (Counter-Clockwise)*/
    protected GeneralPath gpBottom;
    /** Bottom Clockwise */
    private GeneralPath gpBottomClockwise;
    /** Outline of left edge (Counter-Clockwise)*/
    private GeneralPath gpLeft;
    /** Bottom Clockwise */
    private GeneralPath gpLeftClockwise;
    
    /** Final area of the block */
    private Area blockArea;
    
    /** Body of the block */
    protected Rectangle blockBody;
    protected Point2D topLeftCorner;
    protected Point2D topRightCorner;
    protected Point2D botLeftCorner;
    protected Point2D botRightCorner;

        /** block properties determining shape */
    protected boolean hasCurvedCorners;
    protected float blockCornerRadius;
    
   

    /**
     * BlockShape constructor
     * @param rb
     * @param block which make not be rb.getBlock() if we're computing collapsed
     *        shape.
     */
        public BlockShape(RenderableBlock rb, Block block) {
                if (rb != null) { 
                        this.rb = rb;
                        this.block = block;
                } else {
                        System.out.println("Cannot create shape of null RenderableBlock.");
                }

                //initialize general path segments around the block shape
                gpTop = new GeneralPath();
                gpRight = new GeneralPath();
                gpBottom = new GeneralPath();
                gpLeft = new GeneralPath();
                

                setupProperties();
     }
        
    
    
    /**
     * Determine characteristics of the block shape depending on properties of the block
     */
        private void setupProperties() {
          //if isCommandBlock than it has curved corners, else sharp corners
          //note: it won't actually waste time drawing sharp corners,
          //              but cornering method will know to draw a line instead

          hasCurvedCorners = block.hasBeforeConnector() || block.hasAfterConnector() || block.isCommandBlock();
          blockCornerRadius = (hasCurvedCorners ? CORNER_RADIUS : 0f);
        }

    
    
    /**
     * Determine the dimensions of the block shape so there is enough space to
     * draw all of the shape's features.
     */
        private void setupDimensions() {

          //if it has a plug, then offset the start of drawing the block
          int initX = block.hasPlug()
                        ? BlockConnectorShape.getConnectorDimensions(block.getPlug()).width
                        : 0; 
          int initY = 0; 

          ///CHECK FOR CUSTOM SHAPES///
          //for every customBlockShapes
          Point2D[] cornerPoints = new Point2D[4];
          //System.out.println(block.getGenusName() + " custom size: " + customBlockShapeSets.size());
          for(CustomBlockShapeSet customBlockShapeSet : customBlockShapeSets) {
            //returns if custom shape is within this set, break for first one found
            if (customBlockShapeSet.checkCustomShapes(block, cornerPoints, rb.accommodateLabelsWidth(), getTotalHeightOfSockets())) {
              topLeftCorner = new Point2D.Double(cornerPoints[0].getX() + initX, cornerPoints[0].getY() + initY);
              topRightCorner = new Point2D.Double(cornerPoints[1].getX() + initX, cornerPoints[1].getY() + initY);
              botLeftCorner = new Point2D.Double(cornerPoints[2].getX() + initX, cornerPoints[2].getY() + initY);
              botRightCorner = new Point2D.Double(cornerPoints[3].getX() + initX, cornerPoints[3].getY() + initY);
              return;
            }
          }


          //if custom shape not found, then continue to derive it
          //as determined by the renderable block
          //POSITIONING FACTORS:
          //initial x: plug on left side
          //initial y: should always be 0 so top is at highest point
          //SIZE FACTORS:
          //width: default size of block, size of text
          //height: how many sockets

          int width = 0; int height = 0;

          width = determineBlockWidth();
          height = determineBlockHeight();

          blockBody = new Rectangle( initX, initY, width, height);        

          //derived fields
          topLeftCorner = new Point2D.Double(blockBody.getX(), blockBody.getY());
          topRightCorner = new Point2D.Double(blockBody.getX() + blockBody.width, blockBody.getY());
          botLeftCorner = new Point2D.Double(blockBody.getX(), blockBody.getY() + blockBody.getHeight());
          botRightCorner = new Point2D.Double(blockBody.getX() + blockBody.getWidth(), blockBody.getY() + blockBody.getHeight());         
        }
    
    
    
    /**
     * Determines the width of the block by checking for numerous block characteristics
     * TODO: this contains a lot of starlogo specific checks - should be refactored into slcodeblocks?
     */
        protected int determineBlockWidth() {

          int width = 0; 

          //add width for labels
          width += rb.accommodateLabelsWidth();
          //add width for sockets
          width += rb.getMaxSocketShapeWidth();

          if (block.isCommandBlock()) {
            width +=  10;
          } else if (block.isDataBlock() || block.isFunctionBlock()) {
            width += 8;
          } else if (block.isDeclaration()) {
            width += 20;
          } else {
            //assert false : "Block type not found." + block;
            //treat like a command block
            width += 10;
          }
          //add some width for the drop down triangle if it has siblings
          if(block.hasSiblings()){
            width +=5;
          }
          //add image width if the width calculated so far is less than the image width
          if(width < rb.accommodateImagesWidth())
            width += (rb.accommodateImagesWidth() - width) + 10 ;

          // Just in case the BlockLabel is very short...
          width = Math.max(width,
                           rb.getBlockNoteLabelsWidth() + rb.getDecoratorLabelWidth()); 

          //This forces the block to be an even number of pixels wide
          // Doing so ensures that command ports don't occur at half-pixel locations
          if (width % 2 == 1) width++;

          width = Math.max(width, rb.getBlockWidgetDimension().width);

          return width;
        }
    
    /**
     * Returns the total height of this blocks sockets based on connector shape and position type; 0 if this block has no sockets
     * @return the total height of this blocks sockets based on connector shape and position type; 0 if this block has no sockets
     */
    protected int getTotalHeightOfSockets(){
        int heightSum = 0; 
        
        //note if we find a bottom socket
        boolean hasBottomSocket = false;
        int maxBottomSocketHeight = 0;
        
        for(BlockConnector socket : block.getSockets()) {


            Dimension socketDimension = rb.getSocketSpaceDimension(socket);
            //bottom connector stuff
            if (socket.getPositionType() == BlockConnector.PositionType.BOTTOM ) {
                if(socketDimension != null && socketDimension.height > maxBottomSocketHeight){
                    maxBottomSocketHeight = socketDimension.height;
                }
                hasBottomSocket = true;
                continue;
            }
                
                
            //if the socket has been assigned a dimension...
                if(socketDimension != null) {
                    heightSum += socketDimension.height;
                    
                    //if command, then add other command parts
                    if(BlockConnectorShape.isCommandConnector(socket)) {
                        heightSum += BlockConnectorShape.COMMAND_INPUT_BAR_HEIGHT + 2 * CORNER_RADIUS;
                    }
                    continue;
                }
                
                //else use default dimension
                if(BlockConnectorShape.isCommandConnector(socket)) {
                    heightSum += BlockConnectorShape.DEFAULT_COMMAND_INPUT_HEIGHT + BlockConnectorShape.COMMAND_INPUT_BAR_HEIGHT + 2 * CORNER_RADIUS;
                    
                    //else normal data connector, so add height
                } else {
                    heightSum += BlockConnectorShape.DATA_PLUG_HEIGHT;
                }
            }
        

        //if it has bottom sockets, add extra height
        if(hasBottomSocket){
            heightSum += maxBottomSocketHeight;
        }
        
        
        return heightSum;
    }

    /**
     * Returns true if any of the sockets are indented
     */
    public boolean isIndented() {
      return firstIndentedSocket() != -1;
   }
    
    public int firstIndentedSocket() {
      int index = 0;
      for(BlockConnector curSocket : block.getSockets()) {
        if (curSocket.isIndented())
          return index;
        index++;
      }
      return -1;
    }
    
    /**
     * Determines the height of a block by summing it's socket heights OR plug height if no sockets
     * TODO: this contains a lot of starlogo specific checks - should be refactored into slcodeblocks?
     */
    protected int determineBlockHeight() {
      int heightSum = 0;

      //has cornered edges?
      heightSum += (int) (hasCurvedCorners ? 2 * CORNER_RADIUS : 0f);

      //determine and add socket heights
      heightSum += getTotalHeightOfSockets();
      // System.out.println("height sum after getting total height of sockets: "+heightSum);

      //ensure at least height of one data plug
      if(heightSum < BlockConnectorShape.DATA_PLUG_HEIGHT) {
        heightSum = (int) BlockConnectorShape.DATA_PLUG_HEIGHT;
      }

      //get any height of labels other than sockets
      //page label height
      heightSum += rb.accommodatePageLabelHeight();

      //total image height if height so far is less than the total height of images
      if(heightSum < rb.accommodateImagesHeight())
        heightSum += (rb.accommodateImagesHeight() - heightSum) + 10;

      if(block.isInfix())
        heightSum += BOTTOM_SOCKET_UPPER_SPACER;

      heightSum = Math.max(heightSum, rb.getBlockWidgetDimension().height);

      if (this.firstIndentedSocket() == 0) {
        // If this is a block with first socket indented, we need to make room for
        // the (undented) top of the block that comes above the socket.  So we add the
        // height of the labels.   We also add a little padding (4) to allow for the
        // thin "lower tongue" of the block, which comes below the indented sockets.
        heightSum += rb.accommodateBlockLabelsHeight() + 4;
      } else {
        heightSum += 4;
      }
      return heightSum;
    }

    
    //=====================================================================
    // OUTLINE SHAPE: path will proceed clockwise, starting at the top left
    //=====================================================================

    //---------------------
    //DRAW TOP OF THE BLOCK
    //---------------------
    // drawn clockwise
    // top-left corner, top edge, and top-right corner
    
    protected void makeTopSide() {
        
                //starting point of the block
                //gpTop.moveTo((float) topLeftCorner.getX(), (float) topLeftCorner.getY() + blockCornerRadius);
                setEndPoint(gpTop, topLeftCorner, botLeftCorner, true);
        
        
                //curve up and right
                BlockShapeUtil.cornerTo(gpTop, topLeftCorner, topRightCorner, blockCornerRadius);

                
                //command socket if necessary   
                if (block.isCommandBlock() && block.hasBeforeConnector()) {
                        //params: path, distance to center of block, going right
                // Old center-aligned ports
                 //Point2D p = BCS.addControlConnectorShape(gpTop, (float) topLeftCorner.distance(topRightCorner) / 2 - blockCornerRadius, true);
                 // Trying left-aligned ports for now
                        Point2D p = BCS.addControlConnectorShape(gpTop, COMMAND_PORT_OFFSET + blockCornerRadius, true);
    
                        rb.updateSocketPoint(block.getBeforeConnector(), p);
                }
                
                
                //curve down
                BlockShapeUtil.cornerTo(gpTop, topRightCorner, botRightCorner, blockCornerRadius);
                
                //end topside
                //gpTop.lineTo(blockBody.x + blockBody.width, blockBody.y + blockCornerRadius);
                //gpTop.lineTo((float) topRightCorner.getX(), (float) topRightCorner.getY() + blockCornerRadius);
                setEndPoint(gpTop, topRightCorner, botRightCorner, false);
    }
    
    
    
    
    
    
    //------------------------
    //DRAW RIGHT SIDE OF BLOCK
    //------------------------
    // drawn clockwise
    
    protected void makeRightSide() {
        
                //move to the end of the TopSide
                //gpRight.moveTo(blockBody.x + blockBody.width, blockBody.y + blockCornerRadius);
                //gpRight.moveTo((float) topRightCorner.getX(), (float) topRightCorner.getY() + blockCornerRadius);
                setEndPoint(gpRight, topRightCorner, botRightCorner, true);
                
                
                //if page label enabled, extra height
                if(block.hasPageLabel()) {
                  BlockShapeUtil.lineToRelative(gpRight, 0, rb.accommodatePageLabelHeight()/2);
                }                       
                
                //// ADD MIRRORED PLUG ////
                //if it has a mirrored plug, then add it
                if ((block.getPlug() != null) && 
                    (block.getPlug().getPositionType().equals(BlockConnector.PositionType.MIRROR))) {
                  //add the plug to the gpRight
                  BCS.addDataPlug(gpRight, block.getPlug().getKind(), true);
                        
                }       
                
                //// ADD SOCKETS ////
        boolean currentlyIndented = false; // as we go through the sockets, keep track of whether we are already indented
        boolean firstSocket = true;  // to permit special handling for the first socket
        int labelsWidth = 15 + rb.accommodateSocketTagsWidth();
        //for each socket in the iterator
        for(BlockConnector curSocket : block.getSockets()) {
            if (curSocket.isIndented() && !currentlyIndented) {  // add indentation
                    if (firstSocket) {
                      // draw the right side of the upper piece of the block that sits above
                      // the indented part
                      BlockShapeUtil.lineToRelative(gpRight, 0, rb.accommodateBlockLabelsHeight());
                    }
                BlockShapeUtil.lineToRelative(gpRight, (float) (topLeftCorner.getX()+labelsWidth
                                                                - topRightCorner.getX()), 0);
                currentlyIndented = true;
            } else if (currentlyIndented && !curSocket.isIndented()) {  // remove indentation
                BlockShapeUtil.lineToRelative(gpRight, 
                    (float) - (botLeftCorner.getX()+labelsWidth - botRightCorner.getX()), 0); 
                currentlyIndented = false;
            }
                        
            //if it is a single socket (there are no mirrored sockets)
            if (curSocket.getPositionType().equals(BlockConnector.PositionType.SINGLE)) {
                //add the socket shape to the gpRight   
                          
                //if it's a command socket
                if (BlockConnectorShape.isCommandConnector(curSocket)) {
                    int spacerHeight = getSocketSpacerHeight(curSocket, BlockConnectorShape.DEFAULT_COMMAND_INPUT_HEIGHT);
                    //draw the command socket bar and such
                    Point2D p = BCS.addCommandSocket(gpRight, spacerHeight);
                    rb.updateSocketPoint(curSocket, p);
                } else {
                    appendConnectorOffset(gpRight, topRightCorner, botRightCorner, curSocket, true);
                                        
                    //it's a data socket
                    Point2D p = BCS.addDataSocket(gpRight, curSocket.getKind(), true);
                    rb.updateSocketPoint(curSocket, p);

                    int spacerHeight = getSocketSpacerHeight(curSocket, BlockConnectorShape.DATA_PLUG_HEIGHT);
                    int socketHeight = BlockConnectorShape.getConnectorDimensions(curSocket).height;
                    BlockShapeUtil.lineToRelative(gpRight, 0, spacerHeight - socketHeight);

                    appendConnectorOffset(gpRight, topRightCorner, botRightCorner,
                                          curSocket, false);
                }
            }
            firstSocket = false;
        }
                
        //if (block.getPlug() != null) System.out.println(block.getPlug().getPositionType());
        
        // FINISH INDENTATION
        if (currentlyIndented) {
            BlockShapeUtil.lineToRelative(gpRight, 
                (float) - (botLeftCorner.getX()+labelsWidth - botRightCorner.getX()), 0); 
            BlockShapeUtil.lineToRelative(gpRight, 0, 4); 
        }
                
        //line to the bottom right
        //gpRight.lineTo(blockBody.x + blockBody.width, blockBody.y + blockBody.height - blockCornerRadius);
        //gpRight.lineTo((float) botRightCorner.getX(), (float) botRightCorner.getY() - blockCornerRadius);
        setEndPoint(gpRight, botRightCorner, topRightCorner, false);
    }
    
    //---------------------------
        //DRAW LEFT SIDE OF THE BLOCK
        //---------------------------
        //drawn counter-clockwise
        
        protected void makeLeftSide() {
                        
                //starting point of the block
                //gpLeft.moveTo(blockBody.x, blockBody.y + blockCornerRadius);
                //gpLeft.moveTo((float) topLeftCorner.getX(), (float) topLeftCorner.getY() + blockCornerRadius);
                setEndPoint(gpLeft, topLeftCorner, botLeftCorner, true);
                        
                
                //// ADD PLUG ////
                if (block.getPlug() != null){
                        
                        appendConnectorOffset(gpLeft, topLeftCorner, botLeftCorner, block.getPlug(), true);
                        
                        //add the plug shape to the gpLeft
                        Point2D p = BCS.addDataPlug(gpLeft, block.getPlug().getKind(), false);
                        rb.updateSocketPoint(block.getPlug(), p);
                        
                        appendConnectorOffset(gpLeft, topLeftCorner, botLeftCorner, block.getPlug(), false);
                }
                
                        
            // end left side
                // gpLeft.lineTo(blockBody.x, blockBody.y + blockBody.height - blockCornerRadius);
        
                setEndPoint(gpLeft, botLeftCorner, topLeftCorner, false);
        }

        
        
        
        
    
    //------------------------
    //DRAW BOTTOM OF THE BLOCK
    //------------------------
    // drawn counter-clockwise
        // bottom-left conrner, bottom edge, and bottom-right corner

        protected void makeBottomSide() {
        
                //start bottom-right
                //gpBottom.moveTo(blockBody.x, blockBody.y + blockBody.height - blockCornerRadius);
                //gpBottom.moveTo((float) botLeftCorner.getX(), (float) botLeftCorner.getY() - blockCornerRadius);
                setEndPoint(gpBottom, botLeftCorner, topLeftCorner, true);
                
                //BlockShapeUtil.lineToRelative(gpLeft, 0, 10);
                
                
                //curve down and right
                BlockShapeUtil.cornerTo(gpBottom, botLeftCorner, botRightCorner, blockCornerRadius);

                
                /// CONTROL CONNECTOR
                // Removing the isCommandBlock requirement for now because procedure block has
                // an after connector
                //if (block.isCommandBlock() && block.hasAfterConnector()) {
                if (block.hasAfterConnector() && !rb.isCollapsed()) {
                  //control connector if necessary        
                  // Old center-aligned port
                  //Point2D p = BCS.addControlConnectorShape(gpBottom,
                  //        (float) botLeftCorner.distance(botRightCorner) / 2
                  //         - blockCornerRadius, true);
                  // Trying left-aligned ports
                  Point2D p =
                      BCS.addControlConnectorShape(gpBottom,
                                                   COMMAND_PORT_OFFSET +
                                                   blockCornerRadius, true);
                  rb.updateSocketPoint(block.getAfterConnector(), p);
                }
                
/*              
                
                /// BOTTOM SOCKETS
                //for each socket in the iterator
                for(BlockConnector curSocket : block.getSockets()) {
                        
                        //if bottom socket
                        if (curSocket.getPositionType() == BlockConnector.PositionType.BOTTOM) {
                                
                                //move away from bottom left corner
                                gpBottom.lineTo( 
                                  (float) gpBottom.getCurrentPoint().getX() +
                                          BOTTOM_SOCKET_SIDE_SPACER, 
                                  (float) gpBottom.getCurrentPoint().getY());
                                
                                
                                //move down so bevel doesn't screw up from connecting infinitely
                                // sharp corner
                                // as occurs from a curved port
                                BlockShapeUtil.lineToRelative(gpBottom, 0, -1);
                    
                    
                   // int spacerHeight = getSocketSpacerHeight(curSocket,
                  //                       BlockConnectorShape.DATA_PLUG_HEIGHT);
                  // int socketHeight =
                  //               BlockConnectorShape.getConnectorDimensions(curSocket).height;
                  // BSU.lineToRelative(gpBottom, 0, -(spacerHeight - socketHeight));
                    
                  //draw first socket - up left side
                  Point2D leftSocket = BCS.addDataSocketUp(gpBottom, curSocket.getKind(), true);
                  rb.updateSocketPoint(curSocket, leftSocket);
                                
                  //draw left standard empty socket space
                  gpBottom.lineTo( 
                  (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_SIDE_SPACER, 
                  (float) gpBottom.getCurrentPoint().getY());
                                
                                
                  //TODO: ADD BOTTOM SOCKET STRETCHING HERE
                                
                                
                  //draw first socket - down right side
                  Point2D rightSocket = BCS.addDataSocket(gpBottom, curSocket.getKind(), false);
                  //rb.updateSocketPoint(curSocket, rightSocket);
                  
                  //bump down so bevel doesn't screw up
                  BlockShapeUtil.lineToRelative(gpBottom, 0, 1);
                  
                  
                  //// draw left to create divider ////
                  gpBottom.lineTo( 
                  (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_SIDE_SPACER, 
                  (float) gpBottom.getCurrentPoint().getY());
                                
//                              
//                              
//                              
//                              //bump up so bevel doesn't screw up
//                              BlockShapeUtil.lineToRelative(gpBottom, 0, -1);
//                              //draw second socket - up left side
//                              BCS.addDataSocketUp(gpBottom, curSocket.getKind(), true);
//              
//                              
//                              //draw left standard empty socket space
//                              gpBottom.lineTo( 
//                                              (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SPACER, 
//                                              (float) gpBottom.getCurrentPoint().getY());
//                              
//                              //draw second socket - down right side
//                              BCS.addDataSocket(gpBottom, curSocket.getKind(), false);
//                              //bump down so bevel doesn't screw up
//                              BlockShapeUtil.lineToRelative(gpBottom, 0, 1);
                                
                        }
                }*/
                
                
                
                
                //curve right and up
                BlockShapeUtil.cornerTo(gpBottom, botRightCorner, topRightCorner, blockCornerRadius);
                
                //end bottom
                //gpBottom.lineTo(blockBody.x + blockBody.width, blockBody.y + blockBody.height - blockCornerRadius);
                //gpBottom.lineTo((float) botRightCorner.getX(), (float) botRightCorner.getY() - blockCornerRadius);    
                setEndPoint(gpBottom, botRightCorner, topRightCorner, false);
        
        }
    
    
        
        /**
         * Sets the end point for a particular side of a block. 
         * firstPointOnSide == true : Used to "moveTo" first point of a side,
         * firstPointOnSide == false : "lineTo" the last point of a side.
         */
        protected void setEndPoint(GeneralPath gp, Point2D currentCorner, Point2D otherCorner, boolean firstPointOnSide) {
        
                //save calculation time if cornerRadius is zero
                if(blockCornerRadius == 0) {
                        if(firstPointOnSide) {
                                gp.moveTo((float) currentCorner.getX(), (float) currentCorner.getY());
                        } else {
                                gp.lineTo((float) currentCorner.getX(), (float) currentCorner.getY());
                        }
                        
                } else {
                        //corner radius > 0
                        
            //find theta from line from current corner to other corner
                        double theta = Math.atan2(otherCorner.getX() - currentCorner.getX(),
                                                                        //negate since (0,0) at upper left
                                                                        -(otherCorner.getY() - currentCorner.getY()));
                        
                        double dx = blockCornerRadius * Math.cos(Math.PI/2 - theta);
                        double dy = blockCornerRadius * Math.sin(Math.PI/2 - theta);
                        
                        //System.out.println("theta: " + theta + "  xdiff: " + Xdiff + "  Ydiff: " + Ydiff);
                        
                        if(firstPointOnSide) {
                                gp.moveTo((float) (currentCorner.getX() + dx), (float) (currentCorner.getY() - dy));
                        } else {
                                gp.lineTo((float) (currentCorner.getX() + dx), (float) (currentCorner.getY() - dy));
                        }
                        
                }
                
                
                
                
                
                
        }
        
        
        /**
         * Appends an offset to a general path that makes up the side of a block.  
         */
        private void appendConnectorOffset(GeneralPath gp, Point2D topPoint, Point2D botPoint, 
                                                                                        BlockConnector blockConnector, boolean aboveConnector) {
                
                //if top and bottom are equal, then no offset necessary
                if(topPoint.getX() == botPoint.getX()) return;
                
                //if top further right than bottom, then Xdiff is positive
                double Xdiff = topPoint.getX() - botPoint.getX();
                //absolute distance
                double Ydiff = Math.abs(topPoint.getY() - botPoint.getY());
                
                
                //check to only offset correctly above or below the connector:
                //offset only above connectors on right slanting sides
                if(Xdiff > 0 && !aboveConnector) return;
                //offset only below connectors on left slanting sides
                if(Xdiff < 0 && aboveConnector) return;
                

                //get fraction by dividing connector height by total height of the side
                double fraction = BlockConnectorShape.getConnectorDimensions(blockConnector).getHeight()/Ydiff;
                double insetDist = Xdiff * fraction;                                    
                
                //if top further out, then inset left - else move right
                BlockShapeUtil.lineToRelative(gp, (float) -insetDist, 0);
                
        }
        
        
        
          
    /**
     * Returns the height of the spacer associated with a socket if it exists, else
     * it returns the given default height.
     */
    protected int getSocketSpacerHeight(BlockConnector socket, float defaultHeight) {
                //spacer for block connected to socket
                //default spacer height if no block connected
                int spacerHeight = (int) defaultHeight;
                //check for socket space from a connected block
                Dimension socketSpaceDimension = rb.getSocketSpaceDimension(socket);
                //if the socket has been assigned a dimension
                if(socketSpaceDimension != null) {
                        spacerHeight = socketSpaceDimension.height;
                }
                return spacerHeight;
    }
    
    
        ////////////////////////////
    // GENERAL PATH ACCESSORS //
    ////////////////////////////
    
    /**
     * Returns the GeneralPath of the bottom side of this block shape.
     * @return the GeneralPath of the bottom side of this block shape.
     */
    protected GeneralPath getBottomSide(){
        return gpBottomClockwise;
    }
        
    /**
     * Returns the GeneralPath of the left side of this block shape.
     * @return the GeneralPath of the left side of this block shape.
     */
    protected GeneralPath getLeftSide(){
        return gpLeftClockwise;
    }
    /**
     * Returns the GeneralPath of the top side of this block shape.
     * @return the GeneralPath of the top side of this block shape.
     */
    protected GeneralPath getTopSide(){
        return gpTop;
    }
    /**
     * Returns the GeneralPath of the right side of this block shape.
     * @return the GeneralPath of the right side of this block shape.
     */
    protected GeneralPath getRightSide(){
        return gpRight;
    }
        
    /**
     * Reform the BlockShape area.  This is the major procedure that makes all of the sides
     * combines them in their correct directions, and connects them so they are all one direction.
     * @return reformed Area of the BlockShape
     */
    public Area reformArea() {
                //hopefully reseting is less costly than creating new ones
                gpTop.reset();
                gpRight.reset();
                gpBottom.reset();
                gpLeft.reset();
        
                setupDimensions();
        
                //make all of the sides
                makeTopSide();
                makeRightSide();
                makeBottomSide();
                makeLeftSide();
        
            
        //corrected (clockwise) left and bottom
        gpBottomClockwise = new GeneralPath();
        gpLeftClockwise = new GeneralPath();
        gpBottomClockwise.moveTo((float) gpBottom.getCurrentPoint().getX(), 
                                    (float) gpBottom.getCurrentPoint().getY());
        gpLeftClockwise.moveTo((float) gpLeft.getCurrentPoint().getX(), 
                                    (float) gpLeft.getCurrentPoint().getY());
        BlockShapeUtil.appendPath(gpBottomClockwise, gpBottom, true);
        BlockShapeUtil.appendPath(gpLeftClockwise, gpLeft, true);
        
        
                //create direction specific paths
                GeneralPath gpClockwise = new GeneralPath();
                GeneralPath gpCounterClockwise = new GeneralPath();
                
                //add to the direction specific paths
                gpCounterClockwise.append(gpLeft, true);
                gpCounterClockwise.append(gpBottom, true);
                gpClockwise.append(gpTop, true);
                gpClockwise.append(gpRight, true);
        
                //connect so gpCounterClockwise is the full path
                //it must be counter-clockwise for the bevel to be able to use it
                BlockShapeUtil.appendPath(gpCounterClockwise, gpClockwise, true);
                
                //convert it to an area
                blockArea = new Area(gpCounterClockwise); 
        
                return blockArea;
    }
    
    
    
    
    /////////////////////////////////
    // CUSTOM BLOCK SHAPE METHODS ///
    /////////////////////////////////
    
    /**
     * Add a new set of custom shapes
     */
    public static void addCustomShapes(CustomBlockShapeSet cbs) {
                customBlockShapeSets.add(cbs);
    }
}
