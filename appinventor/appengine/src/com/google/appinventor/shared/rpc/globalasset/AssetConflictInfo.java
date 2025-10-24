package com.google.appinventor.shared.rpc.globalasset;

import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.List;

/**
 * Data transfer object containing information about asset conflicts
 * and their potential impact on existing projects.
 */
public class AssetConflictInfo implements IsSerializable {
    
    private GlobalAsset existingAsset;
    private List<ProjectInfo> affectedProjects;
    private int totalProjectCount;
    private long lastModifiedTime;
    
    // Required for GWT serialization
    public AssetConflictInfo() {}
    
    public AssetConflictInfo(GlobalAsset existingAsset, List<ProjectInfo> affectedProjects, 
                           int totalProjectCount, long lastModifiedTime) {
        this.existingAsset = existingAsset;
        this.affectedProjects = affectedProjects;
        this.totalProjectCount = totalProjectCount;
        this.lastModifiedTime = lastModifiedTime;
    }
    
    public GlobalAsset getExistingAsset() {
        return existingAsset;
    }
    
    public void setExistingAsset(GlobalAsset existingAsset) {
        this.existingAsset = existingAsset;
    }
    
    public List<ProjectInfo> getAffectedProjects() {
        return affectedProjects;
    }
    
    public void setAffectedProjects(List<ProjectInfo> affectedProjects) {
        this.affectedProjects = affectedProjects;
    }
    
    public int getTotalProjectCount() {
        return totalProjectCount;
    }
    
    public void setTotalProjectCount(int totalProjectCount) {
        this.totalProjectCount = totalProjectCount;
    }
    
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }
    
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }
    
    public boolean hasAffectedProjects() {
        return affectedProjects != null && !affectedProjects.isEmpty();
    }
    
    /**
     * Information about a project that uses the conflicting asset.
     */
    public static class ProjectInfo implements IsSerializable {
        private long projectId;
        private String projectName;
        private boolean isTracked;
        private long lastSyncTime;
        
        // Required for GWT serialization
        public ProjectInfo() {}
        
        public ProjectInfo(long projectId, String projectName, boolean isTracked, long lastSyncTime) {
            this.projectId = projectId;
            this.projectName = projectName;
            this.isTracked = isTracked;
            this.lastSyncTime = lastSyncTime;
        }
        
        public long getProjectId() {
            return projectId;
        }
        
        public void setProjectId(long projectId) {
            this.projectId = projectId;
        }
        
        public String getProjectName() {
            return projectName;
        }
        
        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }
        
        public boolean isTracked() {
            return isTracked;
        }
        
        public void setTracked(boolean tracked) {
            isTracked = tracked;
        }
        
        public long getLastSyncTime() {
            return lastSyncTime;
        }
        
        public void setLastSyncTime(long lastSyncTime) {
            this.lastSyncTime = lastSyncTime;
        }
        
        public boolean willReceiveUpdate() {
            return isTracked;
        }
    }
}