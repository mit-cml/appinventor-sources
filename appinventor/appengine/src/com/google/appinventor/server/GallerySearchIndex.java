package com.google.appinventor.server;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.StatusCode;

import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;

import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;

import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.GalleryApp;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;


// see sample at https://developers.google.com/appengine/docs/java/search/

public class GallerySearchIndex {

  private static final Logger LOG = Logger.getLogger(GallerySearchIndex.class.getName());
  private static String GALLERYINDEX="GalleryIndex";

  private final transient GalleryStorageIo galleryStorageIo = 
      GalleryStorageIoInstanceHolder.INSTANCE;

  private static volatile GallerySearchIndex  instance= null;
  private GallerySearchIndex () {
  }
  public static GallerySearchIndex getInstance () {
    if (instance == null) {
      synchronized (GallerySearchIndex.class) {
        instance = new GallerySearchIndex();
      }
    }
    return instance;
  }

  public void indexApp (GalleryApp app) {
    // take the title, description, and the user name and index it
    // need to build up a string with all meta data
    String indexWords = app.getTitle()+" "+app.getDescription();
    // now create the doc
    Document doc = Document.newBuilder()
      .setId(String.valueOf(app.getGalleryAppId()))
      .addField(Field.newBuilder().setName("content").setText(indexWords))
      .build();
     
    Index index = getIndex();
    
    try {
        index.put(doc);
    } catch (PutException e) {
        if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
            // retry putting the document
        }
    }

  }
  
  public List<GalleryApp> find (String searchWords) {
    List<GalleryApp> apps = new ArrayList<GalleryApp>();
    try {
      Query query = Query.newBuilder()
          .setOptions(QueryOptions.newBuilder()
              .setLimit(10).
              // for deployed apps, uncomment the line below to demo snippeting.
              // This will not work on the dev_appserver.
              // setFieldsToSnippet("content").
              build())
          .build(searchWords);
      LOG.info("Sending query " + query);
      Results<ScoredDocument> results = getIndex().search(query);

      // Iterate over the documents in the results
      for (ScoredDocument document : results) {
        GalleryApp app = galleryStorageIo.getGalleryApp(Long.parseLong(document.getId()));
        apps.add(app);
      }
    } catch (SearchException e) {
      if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
        // retry
      }

    }
    return apps;
  }

  private Index getIndex() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(GALLERYINDEX).build(); 
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    return index;
  } 
  

}