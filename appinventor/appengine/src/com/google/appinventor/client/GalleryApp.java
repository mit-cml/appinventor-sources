package com.google.appinventor.client;

public class GalleryApp {
	
	
	public GalleryApp(String title, String developerName, String description,
			String creationDate, String updateDate, String imageURL, String zipURL,
			int downloads, int views) {
		super();
		this.title = title;
		this.developerName = developerName;
		this.description = description;
		this.creationDate = creationDate;
		this.updateDate = updateDate;
		this.imageURL = imageURL;
		this.zipURL = zipURL;
		this.downloads = downloads;
		this.views = views;
	}
	private String title;
	private String developerName;
	private String description;
	private String creationDate;
	private String updateDate;
	// creation date and update date?
	
	// an image, icon, zip file 
	private String zipURL;
	private String imageURL;
	private int downloads;
	private int views;
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
	public String getZipURL() {
		return zipURL;
	}
	public void setZipURL(String zipURL) {
		this.zipURL = zipURL;
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
	
	

	
	

}
