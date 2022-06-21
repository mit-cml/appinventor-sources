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
}
