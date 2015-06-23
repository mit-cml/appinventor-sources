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
public class GalleryReportListResult implements IsSerializable{
  List<GalleryAppReport> reports;    //sliced results
  int totalCount;           //total number of all suitable results
  /**
   * default constructor
   */
  public GalleryReportListResult(){

  }
  /**
   * constructor based on given parameters
   * @param reports list of GalleryAppReport
   * @param totalCount total num of results
   */
  public GalleryReportListResult(List<GalleryAppReport> reports, int totalCount){
    this.reports = reports;
    this.totalCount = totalCount;
  }

  /**
   * setReports based on given list of reports
   * @param report list of GalleryAppReport
   */
  public void setReports(List<GalleryAppReport> reports){
    this.reports = reports;
  }

  /**
   * setTotalCount based on given num
   * @param totalCount num of results
   */
  public void setTotalCount(int totalCount){
    this.totalCount = totalCount;
  }

  /**
   * @return reports list of GalleryAppReport
   */
  public List<GalleryAppReport> getReports(){
    return reports;
  }

  /**
   * @return totalCount num of results
   */
  public int getTotalCount(){
    return totalCount;
  }
}
