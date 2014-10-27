// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * GalleryAppListResults is a wrapper class to store both
 * list of gallery apps (sliced results) and number of results.
 * @author blu2@dons.usfca.edu (Bin Lu)
 */
public class GalleryAppListResult implements IsSerializable{
  List<GalleryApp> apps;    //sliced results
  int totalCount;           //total number of all suitable results
  String keyword;           //search keyword, only for searching
  /**
   * default constructor
   */
  public GalleryAppListResult(){

  }
  /**
   * constructor based on given parameters
   * @param apps list of GalleryApp
   * @param totalCount total num of results
   */
  public GalleryAppListResult(List<GalleryApp> apps, int totalCount){
    this.apps = apps;
    this.totalCount = totalCount;
  }

  /**
   * constructor based on given parameters
   * @param apps list of GalleryApp
   * @param totalCount total num of results
   * @param keyword the search keyword
   */
  public GalleryAppListResult(List<GalleryApp> apps, int totalCount, String keyword){
    this.apps = apps;
    this.totalCount = totalCount;
    this.keyword = keyword;
  }

  /**
   * setApps based on given list of apps
   * @param apps list of GalleryApp
   */
  public void setApps(List<GalleryApp> apps){
    this.apps = apps;
  }

  /**
   * setTotalCount based on given num
   * @param totalCount num of results
   */
  public void setTotalCount(int totalCount){
    this.totalCount = totalCount;
  }

  /**
   * setKeyword based on given keyword
   * @param keyword search key word
   */
  public void setKeyword(String keyword){
    this.keyword = keyword;
  }

  /**
   * @return apps list of GalleryApp
   */
  public List<GalleryApp> getApps(){
    return apps;
  }

  /**
   * @return totalCount num of results
   */
  public int getTotalCount(){
    return totalCount;
  }

  /**
   * @return keyword search key word
   */
  public String getKeyword(){
    return keyword;
  }
}
