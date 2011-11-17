package openblocks.renderable;

import java.awt.Color;
import java.awt.geom.Point2D;

import openblocks.codeblocks.BlockConnector;
import openblocks.codeblocks.BlockConnectorShape;

class SocketLabel extends BlockLabel{
	private BlockConnector socket;
	
	SocketLabel(BlockConnector socket, String initLabelText, BlockLabel.Type labelType, boolean isEditable, long blockID){
		super(initLabelText, labelType, isEditable, blockID, false, new Color(190, 250, 125));
		this.socket = socket;
	}
    /**
     * Returns true if the socket label should not be added to this.  Conditions for ignoring socket labels are:
     * 1.  the specified socket is a bottom socket
     * 2.  the specified socket has an empty label
     * @param socket the BlockConnector to test
     * @return true if the specified socket should have a corresponding Blocklabel instance added to this.
     */
    static boolean ignoreSocket(BlockConnector socket){
        return (socket.getPositionType() == BlockConnector.PositionType.BOTTOM) || socket.getLabel().equals("");
    }
	void update(Point2D socketPoint){
        if(ignoreSocket(socket)) return;
        //abstarct location so we need to tranform it
        double x;
        double y;
        if (BlockConnectorShape.isCommandConnector(socket)) {
            //command socket
            x = -8  - BlockConnectorShape.COMMAND_INPUT_BAR_WIDTH + socketPoint.getX();
            y = -4 + socketPoint.getY();
        }else{
            //data socket
            x = -4  - BlockConnectorShape.getConnectorDimensions(socket).width + socketPoint.getX();
            y = -10 + socketPoint.getY();
        }
        x = - this.getAbstractWidth() + x;
        this.setPixelLocation(rescale(x), rescale(y));
	}
	
	@Override
	protected void textChanged(String text) {
		// Prevents running this when sockets are null.
		// Sockets can be null during loading.
		if (socket != null){
			socket.setLabel(text);
			super.textChanged(text);
		}
	}
}
