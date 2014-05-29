package com.google.appinventor.shared.rpc.project;

import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * GalleryAppListResults is a wrapper class to store both
 * list of gallery apps (sliced results) and number of results.
 * @author blu2@dons.usfca.edu (Bin Lu)
 */
public class GalleryAppListResult implements IsSerializable{
  List<GalleryApp> apps;    //sliced results
  int totalCount;           //total number of all suitable results
  public GalleryAppListResult(){

  }
  public GalleryAppListResult(List<GalleryApp> apps, int totalCount){
    this.apps = apps;
    this.totalCount = totalCount;
  }

  public void setApps(List<GalleryApp> apps){
    this.apps = apps;
  }
  public void setTotalCount(int totalCount){
    this.totalCount = totalCount;
  }
  public List<GalleryApp> getApps(){
    return apps;
  }
  public int getTotalCount(){
    return totalCount;
  }

}
