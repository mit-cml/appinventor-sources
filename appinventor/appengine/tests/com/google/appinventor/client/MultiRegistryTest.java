// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;

/**
 * Unit tests for {@link MultiRegistry}.
 *
 */
public class MultiRegistryTest extends TestCase {
  private static class A {}
  private static class B extends A {}
  private static class C extends B {}
  private static class D extends C {}

  private static class TestRegistry extends MultiRegistry<A, String> {
    public TestRegistry() {
      super(A.class);
      register(B.class, "B1");
      register(D.class, "D1");
      register(B.class, "B2");
      register(D.class, "D2");
    }
  }

  private TestRegistry testRegistry;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testRegistry = new TestRegistry();
  }

  public void testClasses() {
    assertEquals(Collections.emptyList(), testRegistry.get(A.class));
    assertEquals(Arrays.asList("B1", "B2"), testRegistry.get(B.class));
    assertEquals(Arrays.asList("B1", "B2"), testRegistry.get(C.class));
    assertEquals(Arrays.asList("D1", "D2", "B1", "B2"), testRegistry.get(D.class));
  }

  public void testObjects() {
    assertEquals(Collections.emptyList(), testRegistry.get(new A()));
    assertEquals(Arrays.asList("B1", "B2"), testRegistry.get(new B()));
    assertEquals(Arrays.asList("B1", "B2"), testRegistry.get(new C()));
    assertEquals(Arrays.asList("D1", "D2", "B1", "B2"), testRegistry.get(new D()));
  }
}
