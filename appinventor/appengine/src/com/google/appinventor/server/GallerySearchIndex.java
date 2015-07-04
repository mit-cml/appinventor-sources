// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.search.DeleteException;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;
import com.google.appinventor.server.storage.GalleryStorageIo;
import com.google.appinventor.server.storage.GalleryStorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.googlecode.objectify.NotFoundException;


// see sample at https://developers.google.com/appengine/docs/java/search/

public class GallerySearchIndex {

  private static final Logger LOG = Logger.getLogger(GallerySearchIndex.class.getName());
  private static String GALLERYINDEX="GalleryIndex";
  private final transient GalleryStorageIo galleryStorageIo =
      GalleryStorageIoInstanceHolder.INSTANCE;
  private static volatile GallerySearchIndex  instance= null;

  /**
   * The default constructor of GallerySearchIndex
   */
  private GallerySearchIndex () {

  }

  /**
   * @return instance of gallery app
   */
  public static GallerySearchIndex getInstance () {
    if (instance == null) {
      synchronized (GallerySearchIndex.class) {
        instance = new GallerySearchIndex();
      }
    }
    return instance;
  }

  /**
   * index gallery app into search index
   * @param app galleryapp
   */
  public void indexApp (GalleryApp app) {
    // take the title, description, and the user name and index it
    // need to build up a string with all meta data
    String indexWords = app.getTitle()+" "+app.getDescription() + " " + app.getDeveloperName();
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

  /**
   * unindex gallery app from search index
   * @param galleryId gallery id
   */
  public void unIndexApp(long galleryId) {
    Index index = getIndex();
    try {
      index.delete(String.valueOf(galleryId));
    } catch (DeleteException e) {
      LOG.info("error deleting index entry");
    }

  }

  /**
   * Return a wrapped class which contains a list of matched results based on
   * keywords and total number of results in database
   * @param searchWords search words
   * @param start start index
   * @param count count number
   * @return GalleryAppListResult gallery applist result
   */
  public GalleryAppListResult find (String searchWords, int start, int count) {
    //TODO page sliced has not implemented yet
    final List<GalleryApp> apps = new ArrayList<GalleryApp>();
    final Result<Integer> size = new Result<Integer>();
    try {
      Query query = Query.newBuilder()
          .setOptions(QueryOptions.newBuilder().
              //setLimit(10).
              // for deployed apps, uncomment the line below to demo snippeting.
              // This will not work on the dev_appserver.
              // setFieldsToSnippet("content").
              build())
          .build(searchWords);
      LOG.info("Sending query " + query);
      Results<ScoredDocument> results = getIndex().search(query);

      // Iterate over the documents in the results
      for (ScoredDocument document : results) {
        LOG.info("Find:" + document.getId());
      }

      // Iterate over the documents in the results
      int index = 0;
      for (ScoredDocument document : results) {
        if((index++) < start) continue;
        if(count == 0) break;
        count--;
        try{
          GalleryApp app = galleryStorageIo.getGalleryApp(Long.parseLong(document.getId()));
          apps.add(app);
        }catch(NotFoundException e){
          LOG.log(Level.SEVERE, "Didn't Find GalleryAppData.id: " + document.getId());
          unIndexApp(Long.parseLong(document.getId()));
        }
      }
      size.t = (int) results.getNumberFound();
    } catch (SearchException e) {
      if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult().getCode())) {
        // retry
      }
    }
    return new GalleryAppListResult(apps, size.t, searchWords);
  }

  /**
   * @return the search index
   */
  private Index getIndex() {
    IndexSpec indexSpec = IndexSpec.newBuilder().setName(GALLERYINDEX).build();
    Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);
    return index;
  }
  // Create a final object of this class to hold a modifiable result value that
  // can be used in a method of an inner class.
  private class Result<T> {
    T t;
  }
}