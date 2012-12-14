// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

public class GlassExplorerEvent implements ExplorerEvent{
	public final static int SLIDING_CONTAINER_FINISHED_OPEN = 1;
	public final static int SLIDING_CONTAINER_FINISHED_CLOSED = 2;
	
	private int eventType;
	private GlassExplorer ge=null;
	
	public GlassExplorerEvent(GlassExplorer ge, int event){
		this.ge =ge;
		this.eventType = event;
	}
		
	public int getEventType(){
		return this.eventType;
	}
	
	public Explorer getSource(){
		return this.ge;
	}
	
}
