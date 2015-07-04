// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0
package com.google.appinventor.components.runtime.util;

import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimplePropertyCopier;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.HandlesEventDispatching;
import com.google.common.testing.junit4.JUnitAsserts;

import junit.framework.TestCase;


/**
 * @author markf@google.com (Mark Friedman)
 */
public class PropertyUtilTest extends TestCase {

  public void testOneStringProperty() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private String prop1;

      @SimpleProperty
      public String Prop1() {
        return prop1;
      }

      @SimpleProperty
      public void Prop1(String prop1) {
        this.prop1 = prop1;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1("idontcare");
    assertEquals("idontcare", sourceObj.Prop1());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals(sourceObj.Prop1(), targetObj.Prop1());
  }

  public void testOneIntProperty() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int prop1;

      @SimpleProperty
      public int Prop1() {
        return prop1;
      }

      @SimpleProperty
      public void Prop1(int prop1) {
        this.prop1 = prop1;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1(1234);
    assertEquals(1234, sourceObj.Prop1());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals(sourceObj.Prop1(), targetObj.Prop1());
  }

  public void testMultipleProperties() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int prop1;
      private String prop2;

      @SimpleProperty
      public int Prop1() {
        return prop1;
      }

      @SimpleProperty
      public void Prop1(int prop1) {
        this.prop1 = prop1;
      }

      @SimpleProperty
      public String Prop2() {
        return prop2;
      }

      @SimpleProperty
      public void Prop2(String prop2) {
        this.prop2 = prop2;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1(1234);
    assertEquals(1234, sourceObj.Prop1());
    sourceObj.Prop2("idontcare");
    assertEquals("idontcare", sourceObj.Prop2());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals(sourceObj.Prop1(), targetObj.Prop1());
    assertEquals(sourceObj.Prop2(), targetObj.Prop2());
  }

  public void testOnlySimplePropertiesAreCopied() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int prop;
      private String nonProp;

      @SimpleProperty
      public int Prop() {
        return prop;
      }

      @SimpleProperty
      public void Prop(int prop) {
        this.prop = prop;
      }

      public String NonProp() {
        return nonProp;
      }

      public void NonProp(String nonProp) {
        this.nonProp = nonProp;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop(1234);
    assertEquals(1234, sourceObj.Prop());
    sourceObj.NonProp("idontcare");
    assertEquals("idontcare", sourceObj.NonProp());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals(sourceObj.Prop(), targetObj.Prop());
    JUnitAsserts.assertNotEqual(sourceObj.NonProp(), targetObj.NonProp());
  }

  public void testNoSetterDoesntCopy() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int prop;

      @SimpleProperty
      public int Prop() {
        return prop;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.prop = 1234;
    assertEquals(1234, sourceObj.Prop());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    JUnitAsserts.assertNotEqual(sourceObj.Prop(), targetObj.Prop());
  }

  public void testSetterNotSimplePropertyDoesntCopy() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int prop;

      @SimpleProperty
      public int Prop() {
        return prop;
      }

      public void Prop(int prop) {
        this.prop = prop;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop(1234);
    assertEquals(1234, sourceObj.Prop());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    JUnitAsserts.assertNotEqual(sourceObj.Prop(), targetObj.Prop());
  }
  public void testNoGetterDoesntCopy() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      public int prop;

      @SimpleProperty
      public void Prop(int prop) {
        this.prop = prop;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop(1234);
    assertEquals(1234, sourceObj.prop);

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    JUnitAsserts.assertNotEqual(sourceObj.prop, targetObj.prop);
  }

  public void testGetterNotSimplePropertyDoesntCopy() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int prop;

      public int Prop() {
        return prop;
      }

      @SimpleProperty
      public void Prop(int prop) {
        this.prop = prop;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop(1234);
    assertEquals(1234, sourceObj.Prop());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    JUnitAsserts.assertNotEqual(sourceObj.Prop(), targetObj.Prop());
  }

  public void testGetterSetterTypeMismatchDoesntCopy() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private int intProp;
      private String stringProp;

      @SimpleProperty
      public int Prop() {
        return intProp;
      }

      @SimpleProperty
      public void Prop(String stringProp) {
        this.stringProp = stringProp;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop("idontcare");
    assertEquals("idontcare", sourceObj.stringProp);
    sourceObj.intProp = 1234;

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    JUnitAsserts.assertNotEqual(sourceObj.Prop(), targetObj.Prop());
    JUnitAsserts.assertNotEqual(sourceObj.intProp, targetObj.intProp);
  }

  public void testCopyMethod() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private String prop1;

      @SimpleProperty
      public String Prop1() {
        return prop1;
      }

      @SimplePropertyCopier
      public void CopyProp1(TestClass comp) {
        prop1 = "foo";
      }

      @SimpleProperty
      public void Prop1(String prop1) {
        this.prop1 = prop1;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1("idontcare");
    assertEquals("idontcare", sourceObj.Prop1());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals("foo", targetObj.Prop1());
  }

  public void testCopyMethodWithoutAnnotationUsesSetterAndGetter() throws Throwable {
    @SimpleObject
    class TestClass implements Component {
      private String prop1;

      @SimpleProperty
      public String Prop1() {
        return prop1;
      }

      public void CopyProp1(TestClass comp) {
        prop1 = "foo";
      }

      @SimpleProperty
      public void Prop1(String prop1) {
        this.prop1 = prop1;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1("idontcare");
    assertEquals("idontcare", sourceObj.Prop1());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals(sourceObj.Prop1(), targetObj.Prop1());
  }

  public void testOnePropertyWithCopyMethodAndManyPropertiesWithoutCopytMethod() throws Throwable {
    // NOTE(lizlooney) - This test attempts to test whether having a copy method for one property
    // prevents other properties from being copied.
    // Since reflection provides the methods in no particular order, this test could pass if
    // the order were such that the property with copy method was last. To make it more likely that
    // that won't happen, there are nine properties here and the fifth one has a copy method.
    @SimpleObject
    class TestClass implements Component {
      private String prop1;
      private String prop2;
      private String prop3;
      private String prop4;
      private String prop5;
      private String prop6;
      private String prop7;
      private String prop8;
      private String prop9;

      @SimpleProperty
      public String Prop1() {
        return prop1;
      }

      @SimpleProperty
      public void Prop1(String prop1) {
        this.prop1 = prop1;
      }

      @SimpleProperty
      public String Prop2() {
        return prop2;
      }

      @SimpleProperty
      public void Prop2(String prop2) {
        this.prop2 = prop2;
      }

      @SimpleProperty
      public String Prop3() {
        return prop3;
      }

      @SimpleProperty
      public void Prop3(String prop3) {
        this.prop3 = prop3;
      }

      @SimpleProperty
      public String Prop4() {
        return prop4;
      }

      @SimpleProperty
      public void Prop4(String prop4) {
        this.prop4 = prop4;
      }

      @SimpleProperty
      public String Prop5() {
        return prop5;
      }

      @SimplePropertyCopier
      public void CopyProp5(TestClass comp) {
        prop5 = "foo";
      }

      @SimpleProperty
      public void Prop5(String prop5) {
        this.prop5 = prop5;
      }

      @SimpleProperty
      public String Prop6() {
        return prop6;
      }

      @SimpleProperty
      public void Prop6(String prop6) {
        this.prop6 = prop6;
      }

      @SimpleProperty
      public String Prop7() {
        return prop7;
      }

      @SimpleProperty
      public void Prop7(String prop7) {
        this.prop7 = prop7;
      }

      @SimpleProperty
      public String Prop8() {
        return prop8;
      }

      @SimpleProperty
      public void Prop8(String prop8) {
        this.prop8 = prop8;
      }

      @SimpleProperty
      public String Prop9() {
        return prop9;
      }

      @SimpleProperty
      public void Prop9(String prop9) {
        this.prop9 = prop9;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1("idontcare1");
    sourceObj.Prop2("idontcare2");
    sourceObj.Prop3("idontcare3");
    sourceObj.Prop4("idontcare4");
    sourceObj.Prop5("idontcare5");
    sourceObj.Prop6("idontcare6");
    sourceObj.Prop7("idontcare7");
    sourceObj.Prop8("idontcare8");
    sourceObj.Prop9("idontcare9");
    assertEquals("idontcare1", sourceObj.Prop1());
    assertEquals("idontcare2", sourceObj.Prop2());
    assertEquals("idontcare3", sourceObj.Prop3());
    assertEquals("idontcare4", sourceObj.Prop4());
    assertEquals("idontcare5", sourceObj.Prop5());
    assertEquals("idontcare6", sourceObj.Prop6());
    assertEquals("idontcare7", sourceObj.Prop7());
    assertEquals("idontcare8", sourceObj.Prop8());
    assertEquals("idontcare9", sourceObj.Prop9());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals(sourceObj.Prop1(), targetObj.Prop1());
    assertEquals(sourceObj.Prop2(), targetObj.Prop2());
    assertEquals(sourceObj.Prop3(), targetObj.Prop3());
    assertEquals(sourceObj.Prop4(), targetObj.Prop4());
    assertEquals("foo", targetObj.Prop5());
    assertEquals(sourceObj.Prop6(), targetObj.Prop6());
    assertEquals(sourceObj.Prop7(), targetObj.Prop7());
    assertEquals(sourceObj.Prop8(), targetObj.Prop8());
    assertEquals(sourceObj.Prop9(), targetObj.Prop9());
  }

  public void testCopyMethodInSuperClass() throws Throwable {
    // Tests the situation where the property getter, setter, and copy method, are declared in a
    // superclass.
    @SimpleObject
    class SuperTestClass implements Component {
      private String prop1;

      @SimpleProperty
      public String Prop1() {
        return prop1;
      }

      @SimplePropertyCopier
      public void CopyProp1(SuperTestClass comp) {
        prop1 = "foo";
      }

      @SimpleProperty
      public void Prop1(String prop1) {
        this.prop1 = prop1;
      }

      @Override
      public HandlesEventDispatching getDispatchDelegate() {
        throw new UnsupportedOperationException();
      }
    }
    @SimpleObject
    class TestClass extends SuperTestClass {
    }

    TestClass sourceObj = new TestClass();
    sourceObj.Prop1("idontcare");
    assertEquals("idontcare", sourceObj.Prop1());

    TestClass targetObj = new TestClass();
    PropertyUtil.copyComponentProperties(sourceObj, targetObj);
    assertEquals("foo", targetObj.Prop1());
  }
}
