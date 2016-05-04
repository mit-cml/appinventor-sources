// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.properties.json.ClientJsonParser;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Database holding property and event information of Simple components.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class SimpleComponentDatabase extends ComponentDatabase {

  private static final Map<Long, SimpleComponentDatabase> instances = new HashMap<Long, SimpleComponentDatabase>();

  /**
   * Returns the  component database instance for projectId
   *
   * @return  component database instance
   */
  public static SimpleComponentDatabase getInstance(long projectId) {
    if (instances.containsKey(projectId)) {
      return instances.get(projectId);
    }
    SimpleComponentDatabase newSimpleComponentDatabase = new SimpleComponentDatabase();
    instances.put(projectId, newSimpleComponentDatabase);
    return newSimpleComponentDatabase;
  }

  /**
   * Returns the component database for the current project
   *
   * @return component database instance
   */
  public static SimpleComponentDatabase getInstance() {
    return getInstance(Ode.getInstance().getCurrentYoungAndroidProjectId());
  }

  /**
   * Resource describing the standard components.
   *
   * Note: this interface should be private but the GWT compiler doesn't seem to be able to handle
   * that.
   */
  public interface ComponentResource extends ClientBundle {
    @Source("com/google/appinventor/simple_components.json")
    TextResource getSimpleComponents();
  }

  private static final ComponentResource componentResources = GWT.create(ComponentResource.class);

  private SimpleComponentDatabase() {
    super(new ClientJsonParser().parse(
        componentResources.getSimpleComponents().getText()).asArray());
  }
}
