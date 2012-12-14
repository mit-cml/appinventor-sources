// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.codeblocks;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import openblocks.renderable.RenderableBlock;

import openblocks.codeblocks.rendering.BlockShapeUtil;


/**
 * Specifies the BlockShape of infix blocks.  Infix blocks have two bottom sockets.  
 */
public class InfixBlockShape extends BlockShape{
        /**
         * In order to properly determine where the pen should be after drawing all the blocks, 
         * the variable
         * maxX is used. This stores the maximum x-coordinate visited by the pen and is updated
         * after each connected block is drawn. After the update, the pen is moved to the maximum 
         * x-coordinate visited. When finished drawing all connected blocks, maxX is reset to 0 to 
         * allow for resizing if some of the connected blocks are removed.
         */
        private float maxX=0; //Stores the maximum X-coordinate visited by the infix block

    public InfixBlockShape(RenderableBlock rb) {
        super(rb, rb.getBlock());
    }
    
    /**
     * Overrided from BlockShape.
     * Takes into account the need to resize the dimensions of an infix block for various cases.
     */
    @Override
    protected void makeBottomSide(){
        // Reset the maximum X-coordinate so the infix block can resize if you remove blocks 
        // within it
        maxX = 0;
        
        //start bottom-right
        setEndPoint(gpBottom, botLeftCorner, topLeftCorner, true);        
        
        //curve down and right
        BlockShapeUtil.cornerTo(gpBottom, botLeftCorner, botRightCorner, blockCornerRadius);
        
          
        
        /// BOTTOM SOCKETS
        //for each socket in the iterator
        int socketCounter = 0; //need to use this to determine which socket we're on
        for(BlockConnector curSocket : block.getSockets()) {
            
            //if bottom socket
            if (curSocket.getPositionType() == BlockConnector.PositionType.BOTTOM) {
                
                //move away from bottom left corner
                if(socketCounter > 0){
                    gpBottom.lineTo( 
                            (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_MIDDLE_SPACER, 
                            (float) gpBottom.getCurrentPoint().getY());
                }else{
                    gpBottom.lineTo( 
                            (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_SIDE_SPACER, 
                            (float) gpBottom.getCurrentPoint().getY());
                }
                
                
                //move down so bevel doesn't screw up from connecting infinitely sharp corner
                // as occurs from a curved port
                BlockShapeUtil.lineToRelative(gpBottom, 0, -0.1f);

                //////////////////////
                //begin drawing socket
                //////////////////////
                
                if(curSocket.getBlockID() == Block.NULL){
                    //draw first socket - up left side
                    Point2D leftSocket = BCS.addDataSocketUp(gpBottom, curSocket.getKind(), true);
                    rb.updateSocketPoint(curSocket, leftSocket);
                    //System.out.println("socket poitn: "+rb.getSocketPoint(curSocket));

                    //System.out.println("socket poitn leftsocket: "+leftSocket);
                    
                    //draw left standard empty socket space - top side
                    gpBottom.lineTo( 
                            (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_SIDE_SPACER, 
                            (float) gpBottom.getCurrentPoint().getY());
                    
                    
                    // draw first socket - down right side
                    // old version used the block kind to make something symmertrical 
                    // to the left side using this code:
                    // BCS.addDataSocket(gpBottom, curSocket.getKind(), false);
                    // rb.updateSocketPoint(curSocket, rightSocket);
                    // In this version, don't make a real socket on
                    // the right side and use this code instead.
                    BCS.addDataSocket(gpBottom, "infix-rhs", false);
                } else { //there is a connected block
                    Block connectedBlock = Block.getBlock(curSocket.getBlockID());
                    RenderableBlock connectedRBlock = 
                      RenderableBlock.getRenderableBlock(curSocket.getBlockID());
                    
                    //calculate and update the new socket point
                    //update the socket point of this cursocket which should now adopt the plug
                    // socket point of its
                    //connected block since we're also adopting the left side of its shape
                    
                    //Use coordinates when the zoom level is 1.0 to calculate socket point
                    double unzoomX = 
                      connectedRBlock.getSocketPixelPoint(connectedBlock.getPlug()).getX() / 
                      connectedRBlock.getZoom();
                    double unzoomY = 
                      connectedRBlock.getSocketPixelPoint(connectedBlock.getPlug()).getY() /
                      connectedRBlock.getZoom();
                    Point2D connectedBlockSocketPoint = new Point2D.Double(unzoomX, unzoomY);
                    Point2D currentPoint = gpBottom.getCurrentPoint();
                    double newX = 
                      connectedBlockSocketPoint.getX() + 
                      Math.abs(connectedBlockSocketPoint.getX() - currentPoint.getX());
                    double newY = 
                      connectedBlockSocketPoint.getY() + 
                      Math.abs(connectedRBlock.getBlockHeight() / 
                          connectedRBlock.getZoom() - currentPoint.getY());
                    rb.updateSocketPoint(curSocket, new Point2D.Double(newX, newY));
                    
                    
                    BlockShape connectedBlockShape = 
                      RenderableBlock.getRenderableBlock(curSocket.getBlockID()).getBlockShape();
                    //append left side of connected block
                    appendPath(gpBottom, connectedBlockShape.getLeftSide(), false);
                    
                    
                    
                    
                    //append right side of connected block (more complicated)
                    if(connectedBlock.getNumSockets() == 0 || connectedBlock.isInfix()){
//                      append top side of connected block
                        appendPath(gpBottom, connectedBlockShape.getTopSide(), false);
                        appendPath(gpBottom, connectedBlockShape.getRightSide(), false);
                    }else{
                        //iterate through the sockets of the connected block, checking if 
                        //it has blocks connected to them
                        appendRightSidePath(gpBottom, connectedBlock, connectedBlockShape);
                    }
                    
                    // Updates the maximum X-coordinate and sets the current point to maxX 
                    if(maxX < (float) gpBottom.getCurrentPoint().getX()){
                        maxX = (float) gpBottom.getCurrentPoint().getX();
                    }
                        gpBottom.lineTo(maxX, (float) gpBottom.getCurrentPoint().getY());
                    
                   
                }
                
                
                //bump down so bevel doesn't screw up
                BlockShapeUtil.lineToRelative(gpBottom, 0, 0.1f);
                
                //System.out.println("gpbottom starting point: "+gpBottom.getCurrentPoint());
                
                //// draw RIGHT to create divider ////
                if(socketCounter < block.getNumSockets()-1){
                    gpBottom.lineTo( 
                        //need to add the width of the block label.  
                        // warning: this assumes that there is only one block label
                            (float) gpBottom.getCurrentPoint().getX() + 
                            BOTTOM_SOCKET_MIDDLE_SPACER + rb.accommodateLabelsWidth(), 
                            (float) gpBottom.getCurrentPoint().getY());
                }else{
                    gpBottom.lineTo( 
                            (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_SIDE_SPACER, 
                            (float) gpBottom.getCurrentPoint().getY());
                }
                
                socketCounter++;
            }
        }
        
        
        //curve right and up
        BlockShapeUtil.cornerTo(gpBottom, botRightCorner, topRightCorner, blockCornerRadius);
        
        //end bottom
        setEndPoint(gpBottom, botRightCorner, topRightCorner, false);
    }
    
    /**
     * Appends the right side path of the stack of blocks connected to the specified 
     * connectedBlock.  If there are 
     * some empty sockets, this method will append empty placeholders.  
     * @param gpBottom the GeneralPath to append the new path to
     * @param connectedBlock the Block instance whose right side of its stack of connected blocks 
     * will be appened to the 
     * specified gpBottom
     * @param connectedBlockShape the BlockShape of the specified connectedBlock
     */
    private void appendRightSidePath(GeneralPath gpBottom, Block connectedBlock,
        BlockShape connectedBlockShape){
        
        //int lastBottomPathWidth;
        

        //append top side of connected block
        appendPath(gpBottom, connectedBlockShape.getTopSide(), false);
        
        float startX = (float) gpBottom.getCurrentPoint().getX();
        for(BlockConnector socket : connectedBlock.getSockets()){
                // Sets the current x-coordinate to the start x-coordinate
                // Makes it so path movements created by previous blocks don't affect
                // the subsequent blocks.
                gpBottom.lineTo(startX, (float) gpBottom.getCurrentPoint().getY());
            if(socket.getBlockID() == Block.NULL){                
                //just draw an empty socket placeholder
                //if its the first socket, draw a top side
                gpBottom.lineTo( 
                                (float) gpBottom.getCurrentPoint().getX() + BOTTOM_SOCKET_SIDE_SPACER, 
                                (float) gpBottom.getCurrentPoint().getY());
                
                // now draw the empty right socket side
                // draw first socket - down right side
                // old version used the block kind to make something symmertrical to the left side
                // using this code:
                // In this version, don't make a real socket on
                // the right side and use this code instead.
                // This is the same change as commented above, except to deal with the case where
                // a connected block has been placed in infix argument socket
                BCS.addDataSocket(gpBottom, "infix-rhs", false);
                //BCS.addDataSocket(gpBottom, socket.getKind(), false);
                //TODO:lastBottomPathWidth = (int)BOTTOM_SOCKET_SIDE_SPACER;
            }else{
              //a block is connected to this socket, check if that block has sockets
              //OR if the block is an infix block - if it is infix, 
              // then just wrap around the infix block
                Block block = Block.getBlock(socket.getBlockID());
                BlockShape shape = 
                  RenderableBlock.getRenderableBlock(socket.getBlockID()).getBlockShape();
                if(block.getNumSockets() == 0 || block.isInfix()){
                    //append this block's top and right side
                    //TODO instead of just appending the right side...draw line to
                    appendPath(gpBottom, shape.getTopSide(), false);
                    appendPath(gpBottom, shape.getRightSide(), false);
                }else{
                    appendRightSidePath(gpBottom, block, shape);
                }
            }
            
            // Updates the maximum X-coordinate and sets the current point to maxX 
            if(maxX < (float) gpBottom.getCurrentPoint().getX()){
                maxX = (float) gpBottom.getCurrentPoint().getX();
            }
                gpBottom.lineTo(maxX, (float) gpBottom.getCurrentPoint().getY());
        }
    }
    
    /**
     * Overrided from BlockShape.
     * Determines the width of the sum of the bottom sockets and uses it if it is
     * greater than the width determined by the determineBlockWidth in BlockShape.
     * Else, it returns the sum of these two values.
     */
    @Override
    protected int determineBlockWidth(){

        //System.out.println("determining block width");
        
        int width = super.determineBlockWidth();
        
        //if the sum of bottom sockets is greater than the calculated width, then use it
        int bottomSocketWidth = 0;
        for(BlockConnector socket : block.getSockets()) {
            if (socket.getPositionType() == BlockConnector.PositionType.BOTTOM) {
                if(socket.getBlockID() == Block.NULL){
                    //3 socket spacers = left of socket, between connectors, right of socket
                    bottomSocketWidth += BOTTOM_SOCKET_SIDE_SPACER ;
                }else{ //a block is connected to socket
                    //TODO get their assigned width from rb
                        if (rb.getSocketSpaceDimension(socket) != null)
                                bottomSocketWidth += rb.getSocketSpaceDimension(socket).width;
                    bottomSocketWidth -= BlockConnectorShape.NORMAL_DATA_PLUG_WIDTH;
                    // if it's a mirror plug, subtract for the other side, too.
                    if (Block.getBlock(socket.getBlockID()).getPlug().getPositionType() == 
                      BlockConnector.PositionType.MIRROR) {
                      bottomSocketWidth -= BlockConnectorShape.NORMAL_DATA_PLUG_WIDTH;
                    }
               }
            }
        }
        

        bottomSocketWidth += 2 * BOTTOM_SOCKET_MIDDLE_SPACER;  
        //TODO need to decide for a size of the middle spacer and how to place them
        bottomSocketWidth += 2 * BOTTOM_SOCKET_SIDE_SPACER;
        
        if (bottomSocketWidth > width) return (bottomSocketWidth + rb.accommodateLabelsWidth());
        
        width += bottomSocketWidth;

        //make sure its even
        if(width % 2 == 1) width++;
        
        return width;
    }
    
    /** Append gp2 to gp1.  If reversed == true, then add the segments in reverse order 
     * NOTE: copied and pasted from starlogoblocks/blockengine/BlockShape.java */
    private void appendPath(GeneralPath gp1, GeneralPath gp2, boolean reversed)
    {
        ArrayList<Number[]> points = new ArrayList<Number[]>();
        // Each element is an array consisting of one Integer and six Floats

        PathIterator i = gp2.getPathIterator(new AffineTransform());

        float[] segment = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

        float leftmost = Float.POSITIVE_INFINITY;

        while(!i.isDone())
        {
            int type = i.currentSegment(segment);
            i.next();

            points.add(new Number[]
            {
                new Integer(type),
                new Float(segment[0]),
                new Float(segment[1]),
                new Float(segment[2]),
                new Float(segment[3]),
                new Float(segment[4]),
                new Float(segment[5])
            });
        }

        if(!reversed)
        {
            float deltaX = 
              (float)gp1.getCurrentPoint().getX() - ((Float)points.get(0)[1]).floatValue();
            float deltaY = 
              (float)gp1.getCurrentPoint().getY() - ((Float)points.get(0)[2]).floatValue();

            for(int j = 1; j < points.size(); j++)
            {
                Object[] typeAndPoints = points.get(j);

                int type = ((Integer)typeAndPoints[0]).intValue();
                float x1 = ((Float)typeAndPoints[1]).floatValue();
                float y1 = ((Float)typeAndPoints[2]).floatValue();
                float x2 = ((Float)typeAndPoints[3]).floatValue();
                float y2 = ((Float)typeAndPoints[4]).floatValue();
                float x3 = ((Float)typeAndPoints[5]).floatValue();
                float y3 = ((Float)typeAndPoints[6]).floatValue();

                if(type == PathIterator.SEG_MOVETO)
                {
                }
                else if(type == PathIterator.SEG_LINETO)
                {
                    gp1.lineTo(x1 + deltaX, y1 + deltaY);

                    leftmost = Math.min(leftmost, x1 + deltaX);
                }
                else if(type == PathIterator.SEG_QUADTO)
                {
                    gp1.quadTo(x1 + deltaX, y1 + deltaY, x2 + deltaX, y2 + deltaY);

                    leftmost = Math.min(leftmost, x2 + deltaX);
                }
                else if(type == PathIterator.SEG_CUBICTO)
                {
                    gp1.curveTo(x1 + deltaX, 
                        y1 + deltaY, 
                        x2 + deltaX, 
                        y2 + deltaY, 
                        x3 + deltaX, 
                        y3 + deltaY);

                    leftmost = Math.min(leftmost, x3 + deltaX);
                }
                else
                    assert false : type;
            }
        }
    }
    
}
