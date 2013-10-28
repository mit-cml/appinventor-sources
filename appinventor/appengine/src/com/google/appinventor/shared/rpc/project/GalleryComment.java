package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.logging.Logger;

public class GalleryComment implements IsSerializable{
	
	/**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryComment() {
  }
  
	
  public GalleryComment(String appID, String timeStamp, String text,
			int numCurFlags, String author, String treeId, int numChildren) {
		super();
		this.appID = appID;
		this.timeStamp = timeStamp;
		this.text = text;
		this.numCurFlags = numCurFlags;
		this.author = author;
		this.treeId = treeId;
		this.numChildren = numChildren;
	}


public String getAppID() {
		return appID;
	}







	public void setAppID(String appID) {
		this.appID = appID;
	}


	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getNumCurFlags() {
		return numCurFlags;
	}

	public void setNumCurFlags(int numCurFlags) {
		this.numCurFlags = numCurFlags;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTreeId() {
		return treeId;
	}

	public void setTreeId(String treeId) {
		this.treeId = treeId;
	}

	public int getNumChildren() {
		return numChildren;
	}

	public void setNumChildren(int numChildren) {
		this.numChildren = numChildren;
	}

  private String appID;
  private String timeStamp;
  private String text;
  private int numCurFlags;
  private String author;
  private String treeId;
  private int numChildren;
  
  
  
 
	
	
	@Override
	public String toString()
	{
		return text; 
	}
	
 

}
