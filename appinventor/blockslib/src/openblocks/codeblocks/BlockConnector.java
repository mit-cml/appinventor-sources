package openblocks.codeblocks;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;

import openblocks.renderable.BlockUtilities;

import openblocks.workspace.ISupportMemento;
import openblocks.yacodeblocks.Escapers;

/**
 * <code>BlockConnector</code> is a light class that describes the socket/plug information for each socket or plug of a particular Block.
 * Each socket has a kind (i.e. number, String, boolean, etc.), a label, and the block id of the block at that socket (not to be
 * confused with the block that hold the socket information - socket does not have a reference to that parent
 * block).
 */
public class BlockConnector implements ISupportMemento {

    //TODO need some sort of indication if there is a block in this socket, i.e. -1 value or boolean flag

    //Connector properties
    private String kind;
    private String initKind;
    private PositionType positionType;
    private String label;
    private Long connBlockID = Block.NULL;
    private DefArgument arg = null;
    private boolean hasDefArg = false;
    private boolean isExpandable = false;
    private boolean isIndented = false;
    private boolean isLabelEditable = false;
    private String expandGroup = "";

    //Specifies the PositionType of connector:
    //Single is the default connector that appears on only one side (left/right) of a block.
    //Mirror is creates a connectors with locations mirrored on both left and right side of a block.
    //Bottom is a double sided enclosure within the bottom side of a block, or the next command.
    //Top is the previous command.
    public enum PositionType { SINGLE, MIRROR, BOTTOM, TOP };

    /**
     * Constructs a new <code>BlockConnector</code>
     * @param kind the kind of this socket
     * @param positionType the PositionType of connector
     * @param label the String label of this socket
	 * @param isLabelEditable is true iff this BlockConnector can have its labels edited.
     * @param isIndented is true iff this BlockConnected should be indented far to the left
     * @param isExpandable whether this socket can expand into another connector when a block is connected
     * @param expandGroup the expand socket group of this connector
     * @param connBlockID the ID of the block connected to this
     */
    public BlockConnector(String kind, PositionType positionType, String label, boolean isLabelEditable,
        boolean isIndented, boolean isExpandable, String expandGroup, Long connBlockID) {
        this(kind, positionType, label, isLabelEditable, isIndented, isExpandable, connBlockID);
        this.expandGroup = expandGroup == null ? "" : expandGroup;
    }

    /**
     * Constructs a new <code>BlockConnector</code>
     * @param label the String label of this socket
     * @param kind the kind of this socket
	 * @param isLabelEditable is true iff this BlockConnector can have its labels edited.
     * @param isIndented is true iff this BlockConnected should be indented far to the left
     * @param isExpandable true if this socket can expand into another connector when a block is connected to this
     * @param positionType specifies the PositionType of connector
     */
    public BlockConnector(String kind, PositionType positionType, String label, boolean isLabelEditable,
        boolean isIndented, boolean isExpandable, Long connBlockID){
        this.kind = kind;
        this.positionType = positionType;
        this.label = label;
        this.isLabelEditable = isLabelEditable;
        this.connBlockID = connBlockID;
        this.isIndented = isIndented;
        this.isExpandable = isExpandable;
        this.initKind = kind;
    }

    /**
     * Constructs a new <code>BlockConnector</code> with a single position
     * @param label the String label of this socket
     * @param kind the kind of this socket
     * @param socketBlockID the block id attached to this socket
     */
    public BlockConnector(String kind, String label, Long socketBlockID){
    		this(kind, PositionType.SINGLE, label, false, false, false, socketBlockID);
    }

    /**
     * Constructs a new <code>BlockConnector</code> with the specified label and kind.
     * This new socket does not have an attached block.
     * @param label the String label of this socket
	 * @param isLabelEditable is true iff this BlockConnector can have its labels edited.
     * @param kind the kind of this socket
     * @param isIndented is true iff this BlockConnected should be indented far to the left
     */
    public BlockConnector(String label, String kind, boolean isLabelEditable, boolean isIndented,
        boolean isExpandable){
    	this(kind, PositionType.SINGLE, label, isLabelEditable, isIndented, isExpandable, Block.NULL);
    }

    /**
     * Constucts a new <code>BlockConnector</code> by copying the connector information
     * from the specified con.  Copies the con's connector label and kind.
     * @param con the BlockConnector to copy from
     */
    public BlockConnector(BlockConnector con){
        this(con.kind, con.positionType, con.label, con.isLabelEditable, con.isIndented,
            con.isExpandable, con.connBlockID);
        this.hasDefArg = con.hasDefArg;
        this.arg = con.arg;
        this.isLabelEditable = con.isLabelEditable;
        this.expandGroup = con.expandGroup;
    }

    /**
     * Returns the label of this
     * @return the label of this
     */
    public String getLabel(){
        return label;
    }

    /**
     * Returns the kind of this
     * @return the kind of this
     */
    public String getKind(){
        return kind;
    }

    /**
     * Returns the initial kind of this
     * @return the initial kind of this
     */
    public String initKind(){
        return initKind;
    }

    /**
     * Returns the PositionType of this
     * @return the PositionType of this
     */
    public PositionType getPositionType(){
        return positionType;
    }

    /**
     * Returns the block id attached (in) this socket
     * @return the block id attached (in) this socket
     */
    public Long getBlockID(){
        return connBlockID;
    }

    /**
     * Returns true iff a block is attached to this socket; false otherwise
     * @return true iff a block is attached to this socket; false otherwise
     */
    public boolean hasBlock(){
        return !connBlockID.equals(Block.NULL);
    }

    /**
     * Returns true iff this connector is expandable, meaning if a block is connected to it, it may
     * cause another empty connector just like this to appear.  Whether or not a block actually appears
     * depends on this connector's parent block.  Technically only sockets can expand.  Each block
     * can only have one plug (meaning return one value).
     * @return true iff this connector is expandable; false otherwise
     */
    public boolean isExpandable(){
        return isExpandable;
    }

    /**
     * Returns true iff this connector is indented, meaning that it appears under the heading of
     * the block, indented to the left as far as the labels will accommodate.  This is generally
     * used for command sockets that define a scope, such as the "do" socket in a loop, or
     * the "then" socket in a conditional.
     * @return true iff this connector is indented; false otherwise
     */
    public boolean isIndented(){
        return isIndented;
    }

    /**
     * Returns the expand group of this connector, or an empty string ("") if
     * the connector is not part of a group.
     */
    public String getExpandGroup() {
        return expandGroup;
    }

    /**
     * Sets the socket label of this to specified label
     * @param label the desired label
     */
    public void setLabel(String label){
        this.label = label;
    }

    /**
     * Returns true iff this socket's label is editable.
     * @return true iff this socket's label is editable; false otherwise
     */
    public boolean isLabelEditable(){
    	return isLabelEditable;
    }

    /**
     * Sets the socket kind of this to the specified kind
     * @param kind the desired kind
     */
    public void setKind(String kind){
    	this.kind = kind;
    }

    /**
     * Sets the socket block attached to this connector
     * @param id the block id of the desired block to attach
     */
    public void setConnectorBlockID(Long id){
    	connBlockID = id;
    }

    /**
     * Sets the position type of this connector
     * @param pos the desired PositionType for this
     */
    public void setPositionType(PositionType pos){
        this.positionType = pos;
    }

    /**
     * Returns true is this connector has a default argument; false otherwise
     * @return true is this connector has a default argument; false otherwise
     */
    public boolean hasDefArg(){
        return hasDefArg;
    }

    /**
     * Sets this connector's default argument to the specified genus and initial label.
     * @param genusName the desired BLockGenus name of the default agrument
     * @param label the initial label of the default argument
     */
    public void setDefaultArgument(String genusName, String label){
        hasDefArg = true;
        arg = new DefArgument(genusName, label);
    }

    /**
     * Connects this connector with its default argument, if it has any, and
     * returns the block ID of the connected default argument or Block.NULL if there is none.
     * @return the block ID of the connected default argument or Block.NULL if there is none.
     */
    public Long linkDefArgument(){
        //checks if connector has a def arg or if connector already has a block
        if(hasDefArg && connBlockID == Block.NULL){
            String argGenusName = arg.getGenusName();
            String argLabel = BlockUtilities.makeBlockLabel(arg.label, argGenusName);
            Block block = new Block(argGenusName, argLabel);
            connBlockID = block.getBlockID();
            return connBlockID;
        }
        return Block.NULL;
    }

    public String toString(){
        StringBuffer out = new StringBuffer();
        out.append("Connector label: ");
        out.append(label);
        out.append(", Connector kind: ");
        out.append(kind);
        out.append(", blockID: ");
        out.append(connBlockID);
        out.append(" with pos type: ");
        out.append(getPositionType());
        return out.toString();
    }

    /**
     * <code>DefArgument</code> is a lightweight class that stores Default Argument information of this
     * connector if it has any, particular the name of the argument's genus and its initial label.
     * Each connector has at most 1 default argument.
     */
    private class DefArgument{

        private String genusName;
        private String label;

        public DefArgument(String genusName, String label){
            this.genusName = genusName;
            this.label = label;
        }

        public String getGenusName(){
            return genusName;
        }

        public String getLabel(){
            return label;
        }
    }

    ////////////////////////
    // SAVING AND LOADING //
    ////////////////////////
    /**
     * Loads information for a single BlockConnector and returns an instance
     * of BlockConnector with the loaded information
     * @param node the Node containing the desired information
     * @return BlockConnector instance with the loaded information
     */
    public static BlockConnector loadBlockConnector(Node node, HashMap<Long, Long> idMapping){
        Pattern attrExtractor=Pattern.compile("\"(.*)\"");
        Matcher nameMatcher;

        BlockConnector con = null;

        String initKind = null;
        String kind = null;
        Long idConnected = Block.NULL;
        String label = "";
        boolean isExpandable = false;
        boolean isIndented = false;
        boolean isLabelEditable = false;
        String expandGroup = "";
        String positionType = "single";

        if(node.getNodeName().equals("BlockConnector")){
            //load attributes
            nameMatcher=attrExtractor.matcher(node.getAttributes().getNamedItem("init-type").toString());
            if (nameMatcher.find()) //will be true
                initKind = nameMatcher.group(1);
            nameMatcher=attrExtractor.matcher(node.getAttributes().getNamedItem("connector-type").toString());
            if (nameMatcher.find()) //will be true
                kind = nameMatcher.group(1);
            nameMatcher=attrExtractor.matcher(node.getAttributes().getNamedItem("label").toString());
            if (nameMatcher.find()) //will be true
                label = nameMatcher.group(1);
            //load optional items
            Node opt_item = node.getAttributes().getNamedItem("con-block-id");
            if(opt_item != null){
                nameMatcher=attrExtractor.matcher(opt_item.toString());
                if (nameMatcher.find()) //will be true
                    idConnected= Block.translateLong(Long.parseLong(nameMatcher.group(1)), idMapping);
            }
            opt_item = node.getAttributes().getNamedItem("label-editable");
            if (opt_item != null) {
            	nameMatcher=attrExtractor.matcher(opt_item.toString());
            	if (nameMatcher.find()) //will be true
            		isLabelEditable = nameMatcher.group(1).equals("true");
            }
            opt_item = node.getAttributes().getNamedItem("is-expandable");
            if(opt_item != null){
                nameMatcher=attrExtractor.matcher(opt_item.toString());
                if (nameMatcher.find()) //will be true
                    isExpandable = nameMatcher.group(1).equals("yes") ? true : false;
            }
            opt_item = node.getAttributes().getNamedItem("is-indented");
            if(opt_item != null){
                nameMatcher=attrExtractor.matcher(opt_item.toString());
                if (nameMatcher.find()) //will be true
                    isIndented = nameMatcher.group(1).equals("yes") ? true : false;
            }
            opt_item = node.getAttributes().getNamedItem("expand-group");
            if(opt_item != null){
                nameMatcher=attrExtractor.matcher(opt_item.toString());
                if (nameMatcher.find()) //will be true
                    expandGroup = nameMatcher.group(1);
            }
            opt_item = node.getAttributes().getNamedItem("position-type");
            if(opt_item != null){
                nameMatcher=attrExtractor.matcher(opt_item.toString());
                if (nameMatcher.find()) //will be true
                    positionType= nameMatcher.group(1);
            }

            assert initKind != null : "BlockConnector was not specified a initial connection kind";

            if(positionType.equals("single"))
                con = new BlockConnector(initKind, PositionType.SINGLE, label, isLabelEditable,
                    isIndented, isExpandable, idConnected);
            else if(positionType.equals("bottom"))
                con = new BlockConnector(initKind, PositionType.BOTTOM, label, isLabelEditable,
                    isIndented,  isExpandable, idConnected);
            else if(positionType.equals("mirror"))
                con = new BlockConnector(initKind, PositionType.MIRROR, label, isLabelEditable,
                    isIndented, isExpandable, idConnected);
            else if(positionType.endsWith("top"))
                con = new BlockConnector(initKind, PositionType.TOP, label, isLabelEditable,
                    isIndented, isExpandable, idConnected);

            con.expandGroup = expandGroup;
            if(!initKind.equals(kind))
                con.setKind(kind);
        }

        assert con != null : "BlockConnector was not loaded "+node;

        return con;
    }

    /**
     * Returns the save string of this.  save string only includes
     * information that was modifiable and modified
     * @param conKind String containing if this is a socket or plug
     * @return the save string of this
     */
    public String getSaveString(String conKind){
        StringBuffer saveString = new StringBuffer();

        saveString.append("<BlockConnector ");
        appendAttribute("connector-kind", conKind, saveString);
        appendAttribute("connector-type", kind, saveString);
        appendAttribute("init-type", initKind, saveString);
        appendAttribute("label", label, saveString);
        if (expandGroup.length() > 0)
            appendAttribute("expand-group", expandGroup, saveString);
        if(isExpandable)
            appendAttribute("is-expandable", "yes", saveString);
        if (isIndented)
          appendAttribute("is-indented", "yes", saveString);
        if(this.positionType.equals(PositionType.SINGLE))
            appendAttribute("position-type", "single", saveString);
        else if(this.positionType.equals(PositionType.MIRROR))
            appendAttribute("position-type", "mirror", saveString);
        else if(this.positionType.equals(PositionType.BOTTOM))
            appendAttribute("position-type", "bottom", saveString);
        else if(this.positionType.equals(PositionType.TOP))
            appendAttribute("position-type", "top", saveString);

        if (this.isLabelEditable) appendAttribute("label-editable", "true", saveString);

        if(!this.connBlockID.equals(Block.NULL))
            appendAttribute("con-block-id", this.connBlockID.toString(), saveString);

        saveString.append("></BlockConnector>");

        return saveString.toString();
    }

    private final String EQ_OPEN_QUOTE = "=\"";
    private final String CLOSE_QUOTE ="\" ";

    private void appendAttribute(String att, String value, StringBuffer buf){
        buf.append(att);
        buf.append(EQ_OPEN_QUOTE);
        buf.append(Escapers.escapeForXml(value));
        buf.append(CLOSE_QUOTE);
    }

    /***********************************
    * State Saving Stuff for Undo/Redo *
    ***********************************/

    private class BlockConnectorState
    {
    	public String kind;
        public String initKind;
        public PositionType positionType;
        public String label;
        public Long connBlockID = Block.NULL;
        //DefaultArg Stuff
        public boolean hasDefArg;
        public String defArgGenusName;
        public String defArgLabel;

        public boolean isIndented;
        public boolean isExpandable;
        public String expandGroup;
        public boolean isLabelEditable;
    }

	public Object getState()
	{
		BlockConnectorState state = new BlockConnectorState();

		state.kind = this.getKind();
		state.initKind = this.initKind;
		state.positionType = this.getPositionType();
		state.label = this.getLabel();
		state.connBlockID = this.getBlockID();
		//Default Args stuff
		if(this.hasDefArg())
		{
			state.defArgGenusName = this.arg.getGenusName();
			state.defArgLabel = this.arg.getLabel();
			state.hasDefArg = true;
		}
		else
		{
			state.defArgGenusName = null;
			state.defArgLabel = null;
			state.hasDefArg = false;
		}
	    state.isIndented = this.isIndented();
		state.isExpandable = this.isExpandable();
		state.isLabelEditable = this.isLabelEditable;
		state.expandGroup = this.expandGroup;

		return state;
	}

	public void loadState(Object memento)
	{
		if(memento instanceof BlockConnectorState)
		{
			BlockConnectorState state = (BlockConnectorState) memento;

			this.setKind(state.kind);
			this.setPositionType(state.positionType);
			this.setLabel(state.label);
			this.setConnectorBlockID(state.connBlockID);

			if(state.hasDefArg)
			{
				this.setDefaultArgument(state.defArgGenusName, state.defArgLabel);
			}
			else
			{
				this.arg = null;
			}

			this.isExpandable = state.isExpandable;
			this.isLabelEditable = state.isLabelEditable;
			this.expandGroup = state.expandGroup;
		}
	}

	/**
	 * This is a way of generating a BlockConnector from a memento. It's a bit
	 * weird since other objects don't have this method, however, it makes the
	 * best sense here since BlockConnector is essentially a struct.
	 * @param memento The state to load
	 * @return An instance of BlockConnector
	 */
	public static BlockConnector instantiateFromState(Object memento)
	{
		if(memento instanceof BlockConnectorState)
		{
			BlockConnectorState state = (BlockConnectorState) memento;

			BlockConnector instance = new BlockConnector(state.kind, state.positionType,
			    state.label, state.isLabelEditable, state.isIndented, state.isExpandable,
			    state.connBlockID);
			instance.isLabelEditable = state.isLabelEditable;

			if(state.hasDefArg)
			{
				instance.setDefaultArgument(state.defArgGenusName, state.defArgLabel);
			}
			return instance;
		}
		return null;
	}
}
