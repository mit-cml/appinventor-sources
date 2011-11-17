package openblocks.workspace;

import openblocks.renderable.BlockNote;
import openblocks.renderable.RenderableBlock;
import openblocks.renderable.Report;
import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockLink;

public class WorkspaceEvent {

    //workspace-wide events
    //affects layout and content of workspace and at least two or more blocks
    public static final int PAGE_ADDED = 1;
    public static final int PAGE_REMOVED = 2;
    public static final int BLOCK_ADDED = 3;
    public static final int BLOCK_REMOVED = 4;
    public static final int BLOCKS_CONNECTED = 5;
    public static final int BLOCKS_DISCONNECTED = 6;

    public static final int WORKSPACE_FINISHED_LOADING = 7;
    
    //page specific events
    public static final int PAGE_RENAMED = 8;
    public static final int PAGE_RESIZED = 9;
    
    //block specific events
    public static final int BLOCK_RENAMED = 10;
    public static final int BLOCK_MOVED = 11;
    public static final int BLOCK_GENUS_CHANGED = 12;
    public static final int BLOCK_NOTE_ADDED = 13;
    public static final int BLOCK_NOTE_REMOVED = 14;
    public static final int BLOCK_NOTE_MOVED = 15;
    public static final int BLOCK_NOTE_RESIZED = 16;
    public static final int BLOCK_NOTE_VISIBILITY_CHANGE = 17;
    public static final int BLOCK_NOTE_CHANGED= 18;
    public static final int BLOCK_DEACTIVATED = 19;
    public static final int BLOCK_ACTIVATED= 20;
    public static final int BLOCK_COLLAPSE_CHANGE = 21;
    
   // Events below here don't require the workspace to be saved.

    private static final int DO_NOT_SAVE = 100; 
    
    public static final int BLOCK_REPORT_CHANGE = 140;
    public static final int BLOCK_DO_IT = 141;
    public static final int BLOCK_DOUBLE_CLICKED = 149;
    
    private Long blockID = Block.NULL;
    private int eventType;
    private WorkspaceWidget widget = null;
    private BlockLink link = null;
    private BlockNote blockNote = null;
    
    //If this is a user spawned event or not
    private boolean userSpawned = false;
    
    // Only valid for phone hardware connected/disconnected events
    private String deviceSerialNum = null;

    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * page added, removed events.  The WorkspaceWidget page parameter should
     * be an instance of Page.
     * @param page
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget page,int eventType){
      widget = page;
      this.eventType = eventType;
      blockID = Block.NULL;
    }

    public WorkspaceEvent(WorkspaceWidget page,int eventType, boolean userSpawned){
      widget = page;
      this.eventType = eventType;
      blockID = Block.NULL;
      this.userSpawned = userSpawned;        
    }

    /**
     * Constructs a new WorkspaceEvent. Use this constructor to report
     * phone-related events. 
     * @param eventType PHONE_SOFTWARE_CONNECTED or PHONE_SOFTWARE_DISCONNECTED
     */
    public WorkspaceEvent(int eventType){
      widget = null;
      this.eventType = eventType;
      blockID = Block.NULL;
    }
    /**
     * Constructs a new WorkspaceEvent. Use this constructor to report
     * phone-related events. 
     * @param eventType PHONE_HARDWARE_CONNECTED or PHONE_HARDWARE_DISCONNECTED
     */
    public WorkspaceEvent(int eventType, String serialNum){
      widget = null;
      this.eventType = eventType;
      blockID = Block.NULL;
      deviceSerialNum = serialNum;
    }

    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * page renamed events.  The WorkspaceWidget page parameter should
     * be an instance of Page.
     * @param page
     * @param oldName the old String name of this page
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget page, String oldName, int eventType){
      widget = page;
      this.eventType = eventType;
      blockID = Block.NULL;
    }

    public WorkspaceEvent(WorkspaceWidget page, String oldName, int eventType, boolean userSpawned){
      widget = page;
      this.eventType = eventType;
      blockID = Block.NULL;
      this.userSpawned = userSpawned;
    }

    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * the following: block added, removed, renamed, compiled, moved, de/activated.
     * @param widget
     * @param blockID
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget widget, Long blockID, int eventType){
      this.widget = widget;
      this.eventType = eventType;
      this.blockID = blockID;
    }

    public WorkspaceEvent(WorkspaceWidget widget, Long blockID, int eventType, boolean userSpawned){
      this.widget = widget;
      this.eventType = eventType;
      this.blockID = blockID;
      this.userSpawned = userSpawned;
    }

    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * block connected/disconnected events.  The specified link contains the connection 
     * information.
     * @param widget
     * @param link
     * @param eventType
     */
    public WorkspaceEvent(WorkspaceWidget widget, BlockLink link, int eventType){
      this.widget = widget;
      this.link = link;
      this.eventType = eventType;
      blockID = Block.NULL;
    }

    public WorkspaceEvent(WorkspaceWidget widget, BlockLink link, int eventType, boolean userSpawned){
      this.widget = widget;
      this.link = link;
      this.eventType = eventType;
      this.userSpawned = userSpawned;
    }

    /**
     * Constructs a new WorkspaceEvent.  This constructor should be used to report
     * changes to block notes.  
     * @param eventType
     * @param BlockID
     * @param bn the block note
     */
    public WorkspaceEvent(int eventType, long BlockID, BlockNote bn){
      this.eventType = eventType;
      this.blockID = BlockID;
      blockNote = bn;
    }

    /**
     * Tells if this event is a user spawned event or not
     * @return true if this event was spawned by a user
     */
    public boolean isUserEvent()
    {
      return userSpawned;
    }

    /**
     * Returns the WorkspaceWidget where this event occured. 
     * @return the WorkspaceWidget where this event occured.
     */
    public WorkspaceWidget getSourceWidget(){
      return widget;
    }
    /**
     * Returns the Long ID of the Block where this event occured.  For 
     * block connection events, this id is Block.NULL since the event occurred
     * from two blocks.
     */
    public Long getSourceBlockID(){
      return blockID;
    }
    /**
     * Returns the int event type of this
     * @return the int event type of this
     */
    public int getEventType(){
      return eventType;
    }

    /**
     * Returns the BlockLink where this event originated, or null if the event type
     * of this is not block connected or disconnected.
     * @return the BlockLink where this event originated, or null if the event type
     * of this is not block connected or disconnected.
     */
    public BlockLink getSourceLink(){
      return link;
    }

    /**
     * Returns the device serial number
     * @return the device serial number
     */
    public String getDeviceSerialNum(){
      return deviceSerialNum;
    }

    /**
     * Returns the original name of the source widget; null if the source widget's 
     * name did not change.
     * @return the original name of the source widget; null if the source widget's 
     * name did not change.
     */
    public BlockNote getBlockNote(){
      return blockNote;
    }

    public boolean shouldSaveChanges() {
      // We don't save reports since they come from the Repl.
      return eventType <  DO_NOT_SAVE && !(getBlockNote() instanceof Report);
    }

    @Override
    public String toString() {
      switch (eventType) {
        case PAGE_ADDED:
          return "WorkspaceEvent(PAGE_ADDED: " + widget + ")";
        case PAGE_REMOVED:
          return "WorkspaceEvent(PAGE_REMOVED: " + widget +")";
        case BLOCK_ADDED:
          return "WorkspaceEvent(BLOCK_ADDED: " + Block.getBlock(blockID) + ")";
        case BLOCK_REMOVED:
          return "WorkspaceEvent(BLOCK_REMOVED: " + Block.getBlock(blockID) + ")";
        case BLOCKS_CONNECTED:
          return "WorkspaceEvent(BLOCKS_CONNECTED: " + link + ")";
        case BLOCKS_DISCONNECTED:
          return "WorkspaceEvent(BLOCKS_DISCONNECTED: " + link + ")";
        case BLOCK_DOUBLE_CLICKED:
          return "WorkspaceEvent(BLOCK_DOUBLE_CLICKED: " + Block.getBlock(blockID) + ")";
        case BLOCK_REPORT_CHANGE:
          return "WorkspaceEvent(BLOCK_REPORT_CHANGE: " + Block.getBlock(blockID) +
          (RenderableBlock.getRenderableBlock(blockID).hasReport() ? " gained" : " lost") + ")";
        case PAGE_RENAMED:
          return "WorkspaceEvent(PAGE_RENAMED: " + widget + ")";
        case PAGE_RESIZED:
          return "WorkspaceEvent(PAGE_RESIZED: " + widget + ")";
        case BLOCK_RENAMED:
          return "WorkspaceEvent(BLOCK_RENAMED: " + Block.getBlock(blockID) + ")";
        case BLOCK_MOVED:
          if (link == null)
            return "WorkspaceEvent(BLOCK_MOVED: " + Block.getBlock(blockID) + ")";
          else
            return "WorkspaceEvent(BLOCK_MOVED: " + link + ")";
        case BLOCK_GENUS_CHANGED:
          return "WorkspaceEvent(BLOCK_GENUS_CHANGED: " + Block.getBlock(blockID) + ")";
        case WORKSPACE_FINISHED_LOADING:
          return "WorkspaceEvent(WORKSPACE_FINISHED_LOADING)";
        default:
          return "WorkspaceEvent(" + eventType + ")";    
      }
    }

}
