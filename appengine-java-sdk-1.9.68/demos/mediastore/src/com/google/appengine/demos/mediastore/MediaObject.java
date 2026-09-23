/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.appengine.demos.mediastore;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.users.User;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class MediaObject {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

  @Persistent
  private User owner;

  @Persistent
  private BlobKey blob;

  @Persistent
  private Date creation;

  @Persistent
  private String contentType;

  @Persistent
  private String filename;

  @Persistent
  private long size;

  @Persistent
  private String title;

  @Persistent
  private String description;

  @Persistent
  private boolean isPublic;

  private static final List<String> IMAGE_TYPES = Arrays.asList("image/png",
    "image/jpeg", "image/tiff", "image/gif", "image/bmp");

  public MediaObject(User owner, BlobKey blob, Date creationTime,
      String contentType, String filename, long size, String title,
      String description, boolean isPublic) {

    this.blob = blob;
    this.owner = owner;
    this.creation = creationTime;
    this.contentType = contentType;
    this.filename = filename;
    this.size = size;
    this.title = title;
    this.description = description;
    this.isPublic = isPublic;
  }

  public Key getKey() {
    return key;
  }

  public User getOwner() {
    return owner;
  }

  public Date getCreationTime() {
    return creation;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public String getDescription() {
    return description;
  }

  public String getTitle() {
    return title;
  }

  public String getFilename() {
    return filename;
  }

  public long getSize() {
    return size;
  }

  public String getContentType() {
    if (contentType ==  null) {
      return "text/plain";
    }
    return contentType;
  }

  public String getURLPath() {
    String key = blob.getKeyString();
    return "/resource?key=" + key;
  }

  public String getDisplayURL() {
    String key = blob.getKeyString();
    return "/display?key=" + key;
  }

  public boolean isImage() {
    return IMAGE_TYPES.contains(getContentType());
  }
}
