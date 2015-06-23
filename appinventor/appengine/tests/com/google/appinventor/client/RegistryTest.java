// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import junit.framework.TestCase;

/**
 * Unit tests for {@link Registry}.
 *
 */
public class RegistryTest extends TestCase {
  private static class A {}
  private static class B extends A {}
  private static class C extends B {}
  private static class D extends C {}
  private static class E extends A {}
  private static class F extends E {}
  private static class G extends F {}

  private static class TestRegistry extends Registry<A, String> {
    public TestRegistry() {
      super(A.class);

      register(B.class, "B");
      register(F.class, "F");
    }
  }

  private TestRegistry registry;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registry = new TestRegistry();
  }

  public void testClasses() throws Exception {
    assertNull(registry.get(A.class));
    assertEquals("B", registry.get(B.class));
    assertEquals("B", registry.get(C.class));
    assertEquals("B", registry.get(D.class));
    assertNull(registry.get(E.class));
    assertEquals("F", registry.get(F.class));
    assertEquals("F", registry.get(G.class));
  }

  public void testObjects() throws Exception {
    assertNull(registry.get(new A()));
    assertEquals("B", registry.get(new B()));
    assertEquals("B", registry.get(new C()));
    assertEquals("B", registry.get(new D()));
    assertNull(registry.get(new E()));
    assertEquals("F", registry.get(new F()));
    assertEquals("F", registry.get(new G()));
  }
}
