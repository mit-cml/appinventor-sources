package openblocks.workspace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * RedoUndoManager manges the redoing and undoing for the WorkspaceController.
 */
public class ReundoManager implements WorkspaceListener
{
	// Member Variables
	private List<ISupportMemento> managedClasses;
	private List<Object> currentStateMemento;
	private Stack<List<Object>> undoMementoStack;
	private Stack<List<Object>> redoMementoStack;
	//This lock object prevents events raised during undoing/redoing
	//from being registered as user events. Set it to true to lock
	//out event registering
	private boolean lock = false;
	// Member Variables

    public ReundoManager(ISupportMemento managedClass)
    {
    	this.managedClasses = new ArrayList<ISupportMemento>();
    	this.managedClasses.add(managedClass);

    	this.reset();
    }

    public void reset()
    {
    	undoMementoStack = new Stack<List<Object>>();
    	redoMementoStack = new Stack<List<Object>>();

    	currentStateMemento = null;
    	//Initial state, nothing in the undo stack, and no current state
    	//There is a workspace completed loading event that fires so that
    	//the current state will be valid. (hence the is null test below.
    }

    public void addManagedClass(ISupportMemento anotherManagedClass)
    {
    	managedClasses.add(anotherManagedClass);
    }

    public void workspaceEventOccurred(WorkspaceEvent event)
    {
    	if(!lock)
    	{
	    	if(event.isUserEvent())
	    	{
	    		if(currentStateMemento != null)
	    		{
	    			undoMementoStack.add(currentStateMemento);
	    		}
	    		currentStateMemento = new ArrayList<Object>();

	    		for(ISupportMemento managedClass : managedClasses)
	    		{
	    			currentStateMemento.add(managedClass.getState());
	    		}
	    	}
    	}
    }

	public void undo()
    {
    	if(canUndo() && !lock)
    	{
    		lock = true;
    		{
	    		//Get the undo state
	    		List<Object> olderStates = undoMementoStack.pop();
	    		//Get an iterator
	    		Iterator<Object> olderStatesIterator = olderStates.iterator();
	    		//Load it
	    		for(ISupportMemento managedClass : managedClasses)
	    		{
	    			managedClass.loadState(olderStatesIterator.next());
	    		}

	    		//Put the newer current state on the redo
	    		redoMementoStack.push(currentStateMemento);
	    		//And make the new current state the one we just loaded
	    		currentStateMemento = olderStates;
    		}
    		lock = false;
    	}
    }

    public void redo()
    {
    	if(canRedo() && !lock)
    	{
    		lock = true;
    		{
//				//Get the redo state
//	    		Object newerState = redoMementoStack.pop();
//	    		//Load it
//	    		managedClass.loadState(newerState);
//	    		//Put the older current state onto the undo stack
//	    		undoMementoStack.push(currentStateMemento);
//	    		//And make the new current state the the one we jsut loaded
//	    		currentStateMemento = newerState;
    		}
    		lock = false;
    	}
    }

	public boolean canUndo()
	{
		return (undoMementoStack.size() > 0);
	}

	public boolean canRedo()
	{
		return (redoMementoStack.size() > 0);
	}

	public String getUndoText()
	{
		return "";
	}

	public String getRedoText()
	{
		return "";
	}
}
