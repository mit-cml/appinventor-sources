package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;
import java.util.logging.Logger;

public class GalleryApp implements IsSerializable {
	

	/**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private GalleryApp() {
  }
	
	public static final String GALLERYURL = "http://gallery.appinventor.mit.edu/rpc?";
//    private static final Logger LOG = Logger.getLogger(GalleryApp.class.getName());
	
	public GalleryApp(String title, String developerName, String description,
			String creationDate, String updateDate, String imageURL, String sourceFileName,
			int downloads, int views, int likes, int comments, 
			String imageBlobId, String sourceBlobId, String galleryAppId, 
			ArrayList<String> tags ) {
		super();
		this.title = title;
		this.developerName = developerName;
		this.description = description;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.imageURL = imageURL;
        // the name we get from gallery can have some bad characters in it...
        //   e.g., name  (2).zip. We need to cleanse this and probably deal with url as
        //   well.
//        LOG.info("HERE:"+sourceFileName);
        if (sourceFileName.contains(".")) {
          String[] splitName = sourceFileName.split("\\.");
          projectName = splitName[0];
        } else {
          projectName = sourceFileName;
        }
		this.downloads = downloads;
		this.views = views;
		this.likes = likes;
		this.comments = comments;
		this.imageBlobId= imageBlobId;
		this.sourceBlobId= sourceBlobId;
		this.galleryAppId= galleryAppId;
		this.tags = tags;
	}
	private String title;
	private String developerName;
	private String description;
	private String creationDate;
	private String updateDate;
	// creation date and update date?
	
	// an image, icon, zip file 
	private String projectName;
	private String imageURL;
	private int downloads;
	private int views;
	private int likes;
	private int comments;
  private String imageBlobId;
  private String sourceBlobId;
  private String galleryAppId;
  private ArrayList<String> tags;
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDeveloperName() {
		return developerName;
	}
	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public int getDownloads() {
		return downloads;
	}
	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}
	public int getViews() {
		return views;
	}
	public void setViews(int views) {
		this.views = views;
	}
	public int getLikes() {
		return likes;
	}
	public void setLikes(int likes) {
		this.likes = likes;
	}
	public int getComments() {
		return comments;
	}
	public void setComments(int comments) {
		this.comments = comments;
	}
	public void setImageBlobId(String imageBlobId) {
		this.imageBlobId = imageBlobId;
	}
	public String getImageBlobId() {
      return this.imageBlobId;
    }
	public void setSourceBlobId(String sourceBlobId) {
		this.sourceBlobId = sourceBlobId;
	}
  public String getSourceBlobId() {
    return this.sourceBlobId;
  }
	public void setGalleryAppId(String galleryAppId) {
		this.galleryAppId = galleryAppId;
	}
  public String getGalleryAppId() {
    return this.galleryAppId;
  }
  
  // url is of form:
  //   gallery.appinventor.mit.edu/rpc?getblob=<sourceBlob>:<appid>
  // http://usf-appinventor-gallery.appspot.com/rpc?getblob=AMIfv96uvxoFUHj_Tsv671z66_Iu9HCsUgGad4_py4oWu2INlFgtvW6M5lUPKZwjBAT6Pi_-31MYIGF2aNji_qGZFxTwHH5ryPToMPumbajW0_I4Pf9XY2INsR-o7h_1z8jou1Ey9dS2ES1KjicqOebmCLMYKRrU5tAANrjTj1Bn3n0uipbWvsQ:48002
  public String getSourceURL() {
     return GALLERYURL+"getblob="+getSourceBlobId()+":"+getGalleryAppId();
  }

  public ArrayList<String> getTags() {
    return this.tags;
  }
  public void setTags(ArrayList<String> tags) {
    this.tags = tags;
  }
  
	@Override
	public String toString() {
		return title + " ||| " + description + " ||| " +  imageURL; 
	}

}
