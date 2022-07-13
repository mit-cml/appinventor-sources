// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.communityhelp;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(ServerLayout.SUBMIT_POST_SERVICE)
public interface SubmitPostService extends RemoteService
{
    /**
     * Submits a post and returns a response
     */
    public String submitPost(String userId, String username, String title, String description, int categoryId, boolean attachProject, String projectId);

    /**
     * uploads a file to Community Server
     */
    public String uploadFile(String userId, String username, String title, String description, int categoryId, boolean attachProject, String projectId);

    /**
     * Fetches all public categories from discourse
     */
    public String getDiscourseCategories();

    /**
     * Fetch all similar topics from discourse 
     */
    public String getSimilarTopics(String query);

}
