package com.google.appinventor.client;
import com.google.appinventor.shared.rpc.project.GalleryApp;

import java.util.List;

/**
 * This interface abstracts the Gallery methods.
 *
 * @author wolberd@google.com (David Wolber)
 */
public interface GalleryInterface {

	void Publish(GalleryApp app);
	void Update(GalleryApp app);
	
	public void GetApps(int start, int count,int sortOrder, String sortField);
	
	public void FindApps(String keywords, int start, int count, int sortOrder);

	public void FindByTag(String tag, int start, int count, int sortOrder); //   we may not have tags
	
	public void GetFeatured(int start, int count, int sortOrder);
	
	public void GetMostRecent(int start, int count);
	
	public void GetMostDownloaded(int start, int count);
	
	public void GetMostViewed(int start, int count);
	
	public void GetMostLiked(int start, int count);
	
	public void GetAppsByDeveloper(int start, int count, String userId);  
	
	public void GetProject(String id);
	
	public void GetCategories();  // we may not have this
	

	
}
