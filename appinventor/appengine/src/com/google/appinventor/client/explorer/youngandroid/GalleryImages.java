package com.google.appinventor.client.explorer.youngandroid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public class GalleryImages {
  public interface Images extends ClientBundle {
    @Source("genericApp.png")
    ImageResource genericApp();

    @Source("androidIcon.png")
    ImageResource androidIcon();
  }

  private static final Images INSTANCE = GWT.create(Images.class);

  public static Images get() {
    return INSTANCE;
  }
}
