package com.google.appinventor.client;

import java.util.List;

/**
 * This interface abstracts the Gallery methods.
 *
 * @author wolberd@google.com (David Wolber)
 */
public interface GalleryInterface {

	void Publish(GalleryApp app);
	void Update(GalleryApp app);
	
	List<GalleryApp> GetApps(int start, int count,int sortOrder, String sortField);
	
	List<GalleryApp> FindApps(String keywords, int start, int count, int sortOrder);

	List<GalleryApp> FindByTag(String tag, int start, int count, int sortOrder); //   we may not have tags
	
	List<GalleryApp> GetFeatured(int start, int count, int sortOrder);
	
	List<GalleryApp> GetMostDownloaded(int start, int count);
	
	List<GalleryApp> GetMostViewed(int start, int count);
	
	List<GalleryApp> GetAppsByDeveloper(int start, int count, String userId);  
	
	GalleryApp GetProject(String id);
	
	List <String> GetCategories();  // we may not have this
	

	
}
