// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import gnu.math.DFloNum;
import gnu.math.IntNum;
import junit.framework.Assert;
import junit.framework.TestCase;
import kawa.standard.Scheme;

/**
 * Tests the evaluation of various YAIL code.
 *
 * TODO(markf): More tests needed!
 *
 * @author markf@google.com (Mark Friedman)
 */

public class YailEvalTest extends TestCase {
  Scheme scheme;

  private static final String YAIL_SCHEME_TESTS = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/buildserver/tests/com/google/appinventor/buildserver/YailEvalTest.scm";

  private static final String OPEN = "<<";
  private static final String ID = ":";
  private static final String TAG = "@@";
  private static final String RESULT = "==";
  private static final String CLOSE = ">>";
  private static final String SUCCESS = "Success";
  private static final String FAILURE = "Failure";

  private static final String ESCAPE = "&";
  private static final String ENCODED_ESCAPE = "&0";
  private static final String ENCODED_OPEN = "&1";
  private static final String ENCODED_CLOSE = "&2";

  @Override
  public void setUp() throws Exception {
    scheme = new Scheme();
    String yailRuntimeLibrary = Compiler.getResource(Compiler.YAIL_RUNTIME);
    String yailSchemeTests = YAIL_SCHEME_TESTS;
    try {
      scheme.eval("(load \"" + yailRuntimeLibrary + "\")");
      scheme.eval("(load \"" + yailSchemeTests + "\")");
      scheme.eval("(set! *testing* #t)");
    } catch (Exception e) {
      throw e;
    } catch (Throwable throwable) {
      throw new RuntimeException(throwable);
    }
  }

  public void testBasicKawaEval() throws Throwable {
    Scheme scheme = new Scheme();
    assertEquals("foobar", scheme.eval("(string-append \"foo\" \"bar\")").toString());
  }

  public void testRandomIntegerSameArgs() throws Throwable {
    assertEquals(1, ((IntNum) scheme.eval("(random-integer 1 1)")).intValue());
  }


  public void testFormatAsDecimal() throws Throwable {
    assertEquals("1.234", (scheme.eval("(format-as-decimal 1.233875 3)")).toString());
  }

  public void testFormatAsDecimalError() throws Throwable {
    String schemeString =
        " (try-catch " +
        "  (format-as-decimal 1.234456 -4) " +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"runtime-error calling format-as-decimal\" " +
        "))";
    assertEquals("runtime-error calling format-as-decimal",
                 scheme.eval(thunkify(schemeString)).toString());
  }

  /**
   * This is mostly here as a workaround for some strange behavior with top-level try-catch
   * expressions in Kawa.
   * Kawa seems to have changed the way that Scheme.eval() treats top-level try-catch expressions
   * between versions 1.9.3 and 1.11. If you look at gnu.exp.TryExp.apply() in 1.9.3 and 1.11 you
   * can see that there were changes made.  Also note that mustCompile() is different in the two
   * versions and we have mucked around a little with when we allow compilation (because we can't
   * dynamically compile on the phone)
   *
   * The workaround appears to be to embed the try-catch into a lambda() (i.e. to 'thunkify' it)
   * and then immediately apply the lambda().
   */
  private String thunkify(String schemeExp) {
    return "(let ((thunk (lambda ()" + schemeExp + "))) (thunk))";
  }

  public void testDefOfVariable() throws Throwable {
    assertTrue((Boolean) scheme.eval("(testDefOfVariable)"));
  }

   public void testDefOfProcedure() throws Throwable {
     assertTrue((Boolean) scheme.eval("(testDefOfProcedure)"));
   }

   public void testTailRecursion() throws Throwable {
     assertTrue((Boolean) scheme.eval("(testTailRecursion)"));
   }

   public void testLookupInPairs1() throws Throwable {
     assertTrue((Boolean) scheme.eval("(testLookupInPairs1)"));
   }

   public void testLookupInPairs2() throws Throwable {
     assertTrue((Boolean) scheme.eval("(testLookupInPairs2)"));
   }


  public void testIsNumber() throws Throwable {
    assertTrue((Boolean) scheme.eval(
        "(call-yail-primitive is-number? (*list-for-runtime* \"1.01\") '(any) \"is a number?\")"));
    assertFalse((Boolean) scheme.eval(
    "(call-yail-primitive is-number? (*list-for-runtime* \"1.01a\") '(any) \"is a number?\")"));
    assertTrue((Boolean) scheme.eval(
    "(call-yail-primitive is-number? (*list-for-runtime* 100.01) '(any) \"is a number?\")"));
  }

  public void testCallCoercions() throws Throwable {
    String schemeString =
        "(call-yail-primitive string-append (*list-for-runtime* 1 2 3) '(text text text) \"join\")";
    assertEquals("123", scheme.eval(schemeString).toString());
    schemeString = "(call-yail-primitive +  " +
        "(*list-for-runtime* \"1\" \"2\" \"3\") '(number number number) \"+\")";
    assertEquals(6, ((IntNum) scheme.eval(schemeString)).intValue());
    schemeString = "(call-yail-primitive string-append (*list-for-runtime* (list 1) \"2\" 3) " +
        "'(text text text) \"join\")";
    assertEquals("(1)23", scheme.eval(schemeString).toString());
  }

  public void testDecimalReaderRoundoff() throws Throwable {
    assertTrue((Boolean) scheme.eval(
    "(call-yail-primitive yail-equal? (*list-for-runtime* 1.00000 \"1\") '(any any) \"=\")"));
    assertTrue((Boolean) scheme.eval(
    "(call-yail-primitive yail-equal? (*list-for-runtime* 1.00000 1) '(any any) \"=\")"));
    assertFalse((Boolean) scheme.eval(
    "(call-yail-primitive yail-equal? (*list-for-runtime* 1.000001 \"1\") '(any any) \"=\")"));
    // This test is here as a comment -- it's what Kawa does -- but we don't have to
    // assert it as a test requirement
    //  assertTrue((Boolean) scheme.eval(
    //  "(call-yail-primitive yail-equal? " +
    //  "(*list-for-runtime* 1.000000000000000001 \"1\") '(any any) \"=\")"));
  }

  public void testTypedCoercions() throws Throwable {
    String schemeString = "(coerce-arg (Float 5) 'text)";
    assertEquals("5", scheme.eval(schemeString).toString());
    schemeString = "(coerce-arg (Double 3.33333) 'number)";
    assertEquals("3.33333", scheme.eval(schemeString).toString());
    schemeString = "(coerce-arg (Integer 10453) 'text)";
    assertEquals("10453", scheme.eval(schemeString).toString());
    schemeString = "(coerce-arg (Long 0341234123412343423) 'text)";
    assertEquals("341234123412343423", scheme.eval(schemeString).toString());
    schemeString = "(coerce-arg (Short 12) 'text)";
    assertEquals("12", scheme.eval(schemeString).toString());
  }

  public void testNonCoercibleValues() throws Throwable {
    String schemeString = "(coerce-arg (Short 12) 'boolean)";
    assertEquals("(non-coercible)", scheme.eval(schemeString).toString());
    schemeString = "(coerce-arg (Float 5.45) 'list)";
    assertEquals("(non-coercible)", scheme.eval(schemeString).toString());
    schemeString = "(coerce-arg \"false\" 'boolean)";
    assertEquals("(non-coercible)", scheme.eval(schemeString).toString());
  }

  public void testCoercionFailureOnProcedureCall() throws Throwable {
    String schemeString =
        " (try-catch " +
        "  (call-yail-primitive + (*list-for-runtime* \"foo\" 4) '(number number) \"+\") " +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"runtime-error\" " +
        "))";
    assertEquals("runtime-error", scheme.eval(thunkify(schemeString)).toString());
  }


  public void testCoercionFailureOnPropertySet() throws Throwable {
    String schemeString =
        "(def Button3 'dontcare) " +
        "(set-and-coerce-property! 'Button3 'FontSize \"foo\" 'number) ";
    try {
      scheme.eval(schemeString);
      fail();
    } catch (YailRuntimeError e) {
      // expected
    }
  }


  public void testYailEqual() throws Throwable {
    assertTrue((Boolean) scheme.eval(
    "(yail-equal? \"1\" 1)"));
    assertTrue((Boolean) scheme.eval(
    "(yail-equal? \"1\" 1.0)"));
    assertTrue((Boolean) scheme.eval(
    "(yail-equal? \"0\" \"00\")"));
    assertFalse((Boolean) scheme.eval(
    "(yail-equal? \"1\" 1.01)"));
    assertFalse((Boolean) scheme.eval(
    "(yail-equal? 1 #t)"));
    assertTrue((Boolean) scheme.eval(
        "(yail-equal? (list #t 3.0 (list \"money\" 000.005)) " +
    " (list #t 3   (list \"money\" .005)))"));
    assertFalse((Boolean) scheme.eval(
    "(yail-equal? (list (list (list '()))) (list (list (list #f))))"));
    assertTrue((Boolean) scheme.eval(
    "(yail-equal? ((Integer 5):doubleValue) 5)"));
  }


  public void testYailNotEqual() throws Throwable {
    assertFalse((Boolean) scheme.eval(
    "(yail-not-equal? \"1\" 1)"));
    assertFalse((Boolean) scheme.eval(
    "(yail-not-equal? \"1\" 1.0)"));
    assertTrue((Boolean) scheme.eval(
    "(yail-not-equal? \"1\" 1.01)"));
    assertTrue((Boolean) scheme.eval(
    "(yail-not-equal? 1 #t)"));
    assertFalse((Boolean) scheme.eval(
        "(yail-not-equal? (list #t 3.0 (list \"money\" 000.005)) " +
    " (list #t 3   (list \"money\" .005)))"));
    assertTrue((Boolean) scheme.eval(
    "(yail-not-equal? (list (list (list '()))) (list (list (list #f))))"));
    assertFalse((Boolean) scheme.eval(
    "(yail-not-equal? ((Integer 5):doubleValue) 5)"));
  }


  public void testYailIsInList() throws Throwable {
    assertTrue((Boolean) scheme.eval(
    "(yail-list-member? \"1\" '(3 2 1))"));
    assertTrue((Boolean) scheme.eval(
    "(yail-list-member? 1 '(3 2 \"1\"))"));
  }


  public void testYailWhile() throws Throwable {
    String schemeString = "(define theList (list)) (define foo 5) (while (< foo 10) " +
        "(set! theList (append theList (list foo))) (set! foo (+ foo 1))) theList";
    assertEquals("null, (5 6 7 8 9)", scheme.eval(schemeString).toString());

  }

  public void testYailAnd() throws Throwable {
    assertEquals(true, scheme.eval("(and-delayed)"));
    assertEquals(true, scheme.eval("(and-delayed #t)"));
    assertEquals(false, scheme.eval("(and-delayed #f)"));
    assertEquals(true, scheme.eval("(and-delayed #t #t)"));
    assertEquals(false, scheme.eval("(and-delayed #t #f)"));
    assertEquals(true, scheme.eval("(and-delayed #t #t #t)"));
    assertEquals(false, scheme.eval("(and-delayed #t #f #t)"));
  }


  public void testYailAndShortCircuit() throws Throwable {
    String schemeString =
        "(define foo 0) " +
        "(begin (and-delayed #f (begin (set! foo 1) #f)) foo)";
    assertEquals("0", scheme.eval(schemeString).toString());
  }


  public void testYailOr() throws Throwable {
    assertEquals(false, scheme.eval("(or-delayed)"));
    assertEquals(true, scheme.eval("(or-delayed #t)"));
    assertEquals(false, scheme.eval("(or-delayed #f)"));
    assertEquals(false, scheme.eval("(or-delayed #f #f)"));
    assertEquals(true, scheme.eval("(or-delayed #t #f)"));
    assertEquals(true, scheme.eval("(or-delayed #t #t #t)"));
    assertEquals(true, scheme.eval("(or-delayed #t #f #t)"));
    assertEquals(false, scheme.eval("(or-delayed #f #f #f)"));
  }


  public void testYailOrShortCircuit() throws Throwable {
    String schemeString =
        "(define foo 0) " +
        "(begin (or-delayed #t (begin (set! foo 1) #f)) foo)";
    assertEquals("0", scheme.eval(schemeString).toString());
  }


  /* Tests for the implementation of Yail lists (runtime.scm) */

  /* simplest list operations */
  public void testListGroup1() throws Throwable {

//     // yail-list-first was removed
//     String schemeInputString = "(begin " +
//         "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
//         "(yail-list-first list1)" +
//         ")";
//     String schemeResultString = "a";
//     assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

//     // yail-list-rest was removed
//     String schemeInputString = "(begin " +
//         "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
//         "(yail-list->string (yail-list-rest list1))" +
//         ")";
//     String schemeResultString = "(b c d)";
//     assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-length list1)" +
        ")";
    String schemeResultString = "4";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-empty? list1)" +
        ")";
    schemeResultString = "false";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(yail-list-empty? (make-yail-list))";
    schemeResultString = "true";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(make-yail-list)";
    schemeResultString = "()";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-index \"c\" list1)" +
        ")";
    schemeResultString = "3";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-index (make-yail-list \"c\") list1)" +
        ")";
    schemeResultString = "0";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }


  public void deepCopyTest() throws Throwable {
    // check that yail-list-copy does a deep copy
    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list (make-yail-list \"a\" \"b\") \"c\" \"d\" ))" +
        "(define x (yail-list-copy list1))" +
        "(define y list1)" +
        "(yail-list-set-item! (yail-list-get-item list1 1) 2 \"foo\")" +
        "(define res (make-yail-list (yail-list-get-item (yail-list-get-item x 1) 2)" +
        "(yail-list-get-item (yail-list-get-item y 1) 2)))" +
        "(yail-list->kawa-list res)" +
        ")";
    String schemeResultString = "(b foo)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  /* trees  */
     public void testListGroup2() throws Throwable {

//        // yail-list-first was removed
//     String schemeInputString = "(begin " +
//         "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
//         "(define list2 (make-yail-list \"w\" \"x\" \"y\" \"z\" ))" +
//         "(define list3 (make-yail-list list1 list2))" +
//         "(yail-list->string (yail-list-first (yail-list-rest list3)))" +
//         ")";
//     String schemeResultString = "(w x y z)";
//     assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(define list2 (make-yail-list \"w\" \"x\" \"y\" \"z\" ))" +
        "(define list3 (make-yail-list list1 list2))" +
        "(yail-list-get-item (yail-list-get-item list3 2) 3)" +
        ")";
    String schemeResultString = "y";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(define list2 (make-yail-list \"w\" \"x\" \"y\" \"z\" ))" +
        "(define list3 (make-yail-list list1 list2))" +
        "(yail-list-member? \"y\" (yail-list-get-item list3 2))" +
        ")";
    schemeResultString = "true";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

     }

  /* more trees */
    public void testListGroup3() throws Throwable {
    String schemeInputString =
        "(make-yail-list \"a\" \"b\" (make-yail-list \"c\"))";
    String schemeResultString = "(a b (c))";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(yail-list->kawa-list " +
                             " (make-yail-list \"a\" \"b\" (make-yail-list \"c\")))";
    schemeResultString = "(a b (c))";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

     schemeInputString = "(yail-list->kawa-list " +
         "(kawa-list->yail-list '(a (b c ((d) e) f) g)))";
     schemeResultString = "(a (b c ((d) e) f) g)";
     assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

//      // yail-list-first was removed
//      schemeInputString = "(yail-list->kawa-list " +
//          "(yail-list-rest (kawa-list->yail-list '(a (b c ((d) e) f) g))))";
//      schemeResultString = "((b c ((d) e) f) g)";
//      assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

     schemeInputString = "(begin " +
         "(define the-list (make-yail-list 1 2 3 4 5 6 7))" +
         " (define x (yail-list-pick-random the-list)) " +
         "(yail-list-member? x the-list) " +
         ")";
     schemeResultString = "true";
     assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
 }

  /* side-effects */
  public void testListGroup4() throws Throwable {
    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(define list2 (make-yail-list \"w\" \"x\" \"y\" \"z\" ))" +
        "(yail-list-set-item! list1 3 list2)" +
        "list1" +
        ")";
    String schemeResultString = "(a b (w x y z) d)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-remove-item! list1 3)" +
        "list1" +
        ")";
    schemeResultString = "(a b d)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(define list2 (make-yail-list \"w\" \"x\" \"y\" \"z\" ))" +
        "(yail-list-append! list1 list2)" +
        "list1" +
        ")";
    schemeResultString = "(a b c d w x y z)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-add-to-list! list1 1 2 3 4 5)" +
        "list1" +
        ")";
    schemeResultString = "(a b c d 1 2 3 4 5)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-add-to-list! list1)" +
        "list1" +
        ")";
    schemeResultString = "(a b c d)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" \"b\" \"c\" \"d\" ))" +
        "(yail-list-add-to-list! list1)" +
        "(coerce-to-string list1)" +
        ")";
    schemeResultString = "(a b c d)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define list1 (make-yail-list \"a\" ))" +
        "(yail-list-add-to-list! list1 1)" +
        "(coerce-to-string list1)" +
        ")";
    schemeResultString = "(a 1)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define x (make-yail-list \"a\" ))" +
        " (call-yail-primitive yail-list-add-to-list! " +
        " (*list-for-runtime* x  \"hi\" ) '(list any)  \"add items to list\") " +
        "(coerce-to-string x)" +
        ")";
    schemeResultString = "(a hi)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define x (make-yail-list \"a\" ))" +
        "(define y (make-yail-list \"ho\" ))" +
        " (call-yail-primitive yail-list-append! " +
        " (*list-for-runtime* x  y ) '(list any)  \"append to list\") " +
        "(coerce-to-string x)" +
        ")";
    schemeResultString = "(a ho)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define x (make-yail-list \"a\" ))" +
        "(define y (make-yail-list \"ho\" ))" +
        "(define z (make-yail-list \"hum\" ))" +
        " (call-yail-primitive yail-list-append! " +
        " (*list-for-runtime* x  y ) '(list any)  \"append to list\") " +
        " (call-yail-primitive yail-list-append! " +
        " (*list-for-runtime* x  z ) '(list any)  \"append to list\") " +
        "(coerce-to-string x)" +
        ")";
    schemeResultString = "(a ho hum)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    schemeInputString = "(begin " +
        "(define x (make-yail-list \"a\" ))" +
        "(define y (make-yail-list \"ho\" ))" +
        "(define z (make-yail-list \"hum\" ))" +
        " (call-yail-primitive yail-list-append! " +
        " (*list-for-runtime* x  y ) '(list any)  \"append to list\") " +
        " (call-yail-primitive yail-list-append! " +
        " (*list-for-runtime* x  z ) '(list any)  \"append to list\") " +
        "(coerce-to-string y)" +
        ")";
    schemeResultString = "(ho)";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

  }



  /* foreach */
  public void testListGroup5() throws Throwable {
    /* test the underlying yail-for-each call */
    /* note that yail-for-each is what the macro expands to */
    String schemeInputString = "(begin " +
        "(define x 0)" +
        "(yail-for-each (lambda (y) (set! x (+ x y ))) (make-yail-list 1 2 3))" +
        "x" +
        ")";
    String schemeResultString = "6";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    /* test that yail-for-each works with nested lists */
    schemeInputString = "(begin " +
        "(define the-list (make-yail-list (make-yail-list 1 2 3) (make-yail-list 4 5 6))) " +
        "(define total 0 )" +
        "(yail-for-each (lambda (y) (set! total (+ total (yail-list-get-item  y 1)))) " +
                             " the-list) " +
        "total" +
        ")";
    schemeResultString = "5";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());


    /* test the call to the foreach macro. */
    schemeInputString = "(begin " +
        "(define x 0)" +
        "(foreach item (begin (set! x (* 2 item))) (make-yail-list 1 2 3))" +
        "x" +
        ")";
    schemeResultString = "6";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    /* test evaluation of the Blocks compiler output */
    schemeInputString = "(begin " +
        "(def x 0)" +
        "(foreach y " +
                  "(begin " +
                      "(set-var! x " +
                         "(call-yail-primitive " +
                            "+" +
                            "(*list-for-runtime* (get-var x)  y)" +
                            "'( number number) \"+\")))" +
                 "(make-yail-list  1 2 3 ))" +
         "(get-var x)" +
        ")";
    schemeResultString = "6";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());

    /* test that a non-list for the second argument will be caught as a YailRuntimeError */
    schemeInputString = "(begin " +
        "(define x 0) " +
        "(define badlist 100)" +
        "(foreach item (begin (set! x (+ x v))) badlist)" +
        ")";
    try {
      scheme.eval(schemeInputString);
      fail();
    } catch (YailRuntimeError e) {
      // this is expected
    }
  }

  public void testForRange() throws Throwable {
    /* test forrange */
    String schemeInputString = "(begin " +
        "(def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " 1  100 1) "  +
        " (get-var x) " +
         "  )";
    String schemeResultString = "5050";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testForRangeDecreasing() throws Throwable {
    /* test forrange */
    String schemeInputString = " (begin " +
        "(def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " -1 -100 -1) "  +
        " (get-var x) " +
         "  )";
    String schemeResultString = "-5050";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }


  public void testForRangeConversion() throws Throwable {
    /* test that we do runtime type checking on the range bounds */
    String schemeInputString = "(begin " +
        "(def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " \"1\"  \"100\"  \"1\") "  +
        " (get-var x) " +
         "  )";
    String schemeResultString = "5050";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  // replace this by testForRangeErrorNonterminating if we make that an error
  public void testForRangeEmpty() throws Throwable {
    /* test forrange giving error in nonterminating case */
    String schemeInputString =
        "(begin (def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " 1 100 -1) "  +
        " (get-var x) " +
        ")";
    String schemeResultString = "0";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  // (Hal) I removed this test because I eliminated the error
  // public void testForRangeErrorNonterminating() throws Throwable {
  //   /* test forrange giving error in nonterminating case */
  //   String schemeInputString =
  //       "(begin (def x 0)" +
  //       "(forrange i " +
  //       " (begin "      +
  //       "   (set-var! x "      +
  //       "    (call-yail-primitive "      +
  //       "     + "      +
  //       "     (*list-for-runtime* (get-var x) "      +
  //       "          (lexical-value i) ) "      +
  //       "     '( number number)  \"+\") )  ) "      +
  //       " 1 100 -1) "  +
  //       ")";
  //   try {
  //     scheme.eval(schemeInputString);
  //     fail();
  //   } catch (YailRuntimeError e) {
  //     // expected
  //   }
  // }

  public void testForRangeConversionErrorOnStartArg() throws Throwable {
    /* test forrange signaling a converison error for the start argument */
    String schemeInputString =
        "(begin (def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " \"foo\" 100 -1) "  +
        ")";
    try {
      scheme.eval(schemeInputString);
      fail();
    } catch (YailRuntimeError e) {
      // expected
    }
  }

  public void testForRangeConversionErrorOnEndArg() throws Throwable {
    /* test forrange signaling a converison error for the end argument */
    String schemeInputString =
        "(begin (def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " 1 \"foo\" -1) "  +
        ")";
    try {
      scheme.eval(schemeInputString);
      fail();
    } catch (YailRuntimeError e) {
      // expected
    }
  }

  public void testForRangeConversionErrorOnStepArg() throws Throwable {
    /* test forrange signaling a converison error for the step argument */
    String schemeInputString =
        "(begin (def x 0)" +
        "(forrange i " +
        " (begin "      +
        "   (set-var! x "      +
        "    (call-yail-primitive "      +
        "     + "      +
        "     (*list-for-runtime* (get-var x) "      +
        "          (lexical-value i) ) "      +
        "     '( number number)  \"+\") )  ) "      +
        " 1 100 \"foo\" ) "  +
        ")";
    try {
      scheme.eval(schemeInputString);
      fail();
    } catch (YailRuntimeError e) {
      // expected
    }
  }

  /* a-list lookup */
  public void testAListLookup1() throws Throwable {
    String schemeInputString = "(begin " +
      "(define pair1 (make-yail-list \"a\" \"b\")) " +
      "(define pair2 (make-yail-list \"aa\" \"bb\")) " +
      "(define pairs (make-yail-list pair1 pair2)) " +
      "(yail-alist-lookup \"aa\" pairs \"nothing\") " +
      ")";
    String schemeResultString = "bb";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testAListLookup2() throws Throwable {
    String schemeInputString = "(begin " +
      "(define pair1 (make-yail-list \"a\" \"b\")) " +
      "(define pair2 (make-yail-list (make-yail-list 1 2) \"bb\")) " +
      "(define pairs (make-yail-list pair1 pair2)) " +
      "(yail-alist-lookup (make-yail-list 1  (+ 1 1)) pairs \"nothing\") " +
      ")";
    String schemeResultString = "bb";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testAListLookup3() throws Throwable {
    String schemeInputString = "(begin " +
      "(define pair1 (make-yail-list \"a\" \"b\")) " +
      "(define pair2 (make-yail-list (make-yail-list 1 2) \"bb\")) " +
      "(define pairs (make-yail-list pair1 pair2)) " +
      "(yail-alist-lookup (make-yail-list \"foo\"  (+ 1 1)) pairs \"nothing\") " +
      ")";
    String schemeResultString = "nothing";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testAListLookup4() throws Throwable {
    /* check that this signals a runtime error for a bad pair  */
    String schemeInnerInputString = "(begin " +
      "(define pair1 (make-yail-list \"a\" \"b\")) " +
      "(define pair2 (make-yail-list (make-yail-list 1 2) \"bb\")) " +
      "(define badpair 100) " +
      "(define pairs (make-yail-list pair1 pair2 badpair)) " +
      "(yail-alist-lookup (make-yail-list \"foo\"  (+ 1 1)) pairs \"nothing\") " +
      ")";
    String schemeInputString = "(try-catch " +
          schemeInnerInputString +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"bad pair\" " +
        "))";
    assertEquals("bad pair", scheme.eval(thunkify(schemeInputString)).toString());
  }

  public void testListInsertionMiddle() throws Throwable {
    /* test list insertion in middle */
    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list (make-yail-list \"a\" \"b\") \"c\" \"d\"))" +
        "(yail-list-insert-item! list1 3 \"foo\")" +
        "(yail-list-get-item list1 3)" +
        ")";
    String schemeResultString = "foo";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testListInsertionBeginning() throws Throwable {
    /* test list insertion at beginning */
    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list (make-yail-list \"a\" \"b\") \"c\" \"d\"))" +
        "(yail-list-insert-item! list1 1 \"foo\")" +
        "(yail-list-get-item list1 1)" +
        ")";
    String schemeResultString = "foo";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testListInsertionEnd() throws Throwable {
    /* test list insertion at end */
    String schemeInputString = "(begin " +
        "(define list1 (make-yail-list (make-yail-list \"a\" \"b\") \"c\" \"d\"))" +
        "(yail-list-insert-item! list1 4 \"foo\")" +
        "(yail-list-get-item list1 4)" +
        ")";
    String schemeResultString = "foo";
    assertEquals(schemeResultString, scheme.eval(schemeInputString).toString());
  }

  public void testListInsertionError() throws Throwable {
    /* test list insertion error */
   String schemeInputString =
       "(begin  " +
       "(define list1 (make-yail-list (make-yail-list \"a\" \"b\") \"c\" \"d\"))" +
       thunkify(
         "(try-catch " +
         "(yail-list-insert-item! list1 5 \"foo\")" +
         " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
          " \"runtime-error\" " +
          "))") +
       ")";
    assertEquals("runtime-error", scheme.eval(schemeInputString).toString());
  }

  /* error conditions */

  public void testListGroupErrorConditions() throws Throwable {
    String schemeString =
        " (try-catch " +
        " (yail-list-get-item (make-yail-list 0 1 2 3) 5) " +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"runtime-error\" " +
        "))";
    assertEquals("runtime-error", scheme.eval(thunkify(schemeString)).toString());

    schemeString =
        " (try-catch " +
        " (yail-list-get-item (make-yail-list 0 1 2 3) 0) " +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"runtime-error\" " +
        "))";
    assertEquals("runtime-error", scheme.eval(thunkify(schemeString)).toString());

    schemeString =
        " (try-catch " +
        " (yail-list-set-item! (make-yail-list 0 1 2 3) -3 \"hello\") " +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"runtime-error\" " +
        "))";
    assertEquals("runtime-error", scheme.eval(thunkify(schemeString)).toString());

    schemeString =
        " (try-catch " +
        " (yail-list-set-item! (make-yail-list 0 1 2 3) 8 \"hello\") " +
        " (exception com.google.appinventor.components.runtime.errors.YailRuntimeError " +
        " \"runtime-error\" " +
        "))";
    assertEquals("runtime-error", scheme.eval(thunkify(schemeString)).toString());
  }

  /* Text operations */

  public void testStringAppend() throws Throwable {
    assertEquals("abcdef", scheme.eval("(string-append \"abc\" \"def\")").toString());
  }

  public void testStringStartsAt() throws Throwable {
    assertEquals("3", scheme.eval("(string-starts-at \"abc\" \"c\")").toString());
    assertEquals("0", scheme.eval("(string-starts-at \"abc\" \"x\")").toString());
  }

  public void testStringContains() throws Throwable {
    assertEquals("true", scheme.eval("(string-contains \"abc\" \"b\")").toString());
    assertEquals("false", scheme.eval("(string-contains \"abc\" \"x\")").toString());
  }

  public void testStringSplit() throws Throwable {
    assertEquals("(ab cd ef)", scheme.eval("(string-split \"ab&cd&ef\" \"&\")").toString());

    assertEquals("(ab cd ef)", scheme.eval(
        "(string-split-at-any \"ab&cd-ef\" (make-yail-list \"&\" \"-\" ))").toString());

    assertEquals("(ab cd ef)", scheme.eval("(string-split-at-spaces \"ab   cd    ef\" )")
        .toString());

    // These have no match

    assertEquals("(ab&cd&ef)", scheme.eval("(string-split \"ab&cd&ef\" \"blah\")").toString());

    assertEquals("(ab&cd-ef)", scheme.eval(
        "(string-split-at-any \"ab&cd-ef\" (make-yail-list \"*\" \"%\" ))").toString());

    assertEquals("(ab&&cd&ef)", scheme.eval("(string-split-at-spaces \"ab&&cd&ef\" )").toString());



    // Corner cases
    String schemeString = "(clarify (string-split \"&&ab&cd&ef&\" \"&\")) ";

    // There should not be a trailing <empty> here
    assertEquals("(<empty> <empty> ab cd ef)", scheme.eval(schemeString).toString());

    //assertEquals("(<empty> <empty> ab cd ef <empty>)", scheme.eval(schemeString).toString());

    schemeString = "(clarify (string-split-at-any \"&ab&-cd-ef&\" (make-yail-list \"&\" \"-\" )))";

    assertEquals("(<empty> ab <empty> cd ef <empty>)", scheme.eval(schemeString).toString());

    schemeString = "(clarify (string-split-at-any \"&ab&+cd+ef&\" (make-yail-list \"&\" \"+\" )))";

    assertEquals("(<empty> ab <empty> cd ef <empty>)", scheme.eval(schemeString).toString());


    schemeString = "(clarify (string-split-at-spaces \" ab  cd ef  \" ))";

    assertEquals("(ab cd ef)", scheme.eval(schemeString).toString());

    // Happy cases
    assertEquals("(ab cd&ef)",
        scheme.eval("(string-split-at-first \"ab&cd&ef\" \"&\")").toString());

    assertEquals("(ab cd-ef)", scheme.eval(
        "(string-split-at-first-of-any \"ab-cd-ef\" (make-yail-list \"&\" \"-\" ))").toString());

    // No-match cases
    assertEquals("(ab&cd&ef)",
        scheme.eval("(string-split-at-first \"ab&cd&ef\" \"blah\")").toString());

    schemeString = "(string-split-at-first-of-any \"ab&cd-ef\" (make-yail-list))";
    try {
      scheme.eval(schemeString);
      fail();
    } catch (YailRuntimeError e) {
      // this is expected
    }
  }

  public void testStringSubstring() throws Throwable {
    assertEquals("b&&c", scheme.eval("(string-substring \"ab&&cd&ef\" 2 4)").toString());

    try {
      scheme.eval("(string-substring \"abcd\" 3 3)");
      fail();
    } catch (YailRuntimeError e) {
      // this is expected
    }
  }

  public void testStringTest1() throws Throwable {
    assertEquals("true", scheme.eval("(stringTest1)").toString());
  }

  public void testStringToUpperCaseForConstant() throws Throwable {
    assertEquals("ABCDEFG", scheme.eval("(string-to-upper-case \"aBcDeFg\")").toString());
    // Lowercase sharp s (Unicode 00DF) is replaced with SS. This behavior follow the current
    // spelling rules. However, beginning with Unicode 5.1 (released April 4, 2008), a capital
    // sharp s character was added as Unicode 1E9E. It is possible that spelling rules will change
    // in the future to use the new capital sharp s character when uppercasing a lowercase sharp s.
    // So, if this test begins failing (perhaps after we switch to a future version of Java), that
    // may be the cause and the test should be updated.
    assertEquals("STRASSE", scheme.eval("(string-to-upper-case \"Stra\u00dfe\")").toString());
  }

  public void testStringToUpperCaseForExpression() throws Throwable {
    assertEquals("STRASSE", scheme.eval("(string-to-upper-case "
                                        + "(string-append \"Stra\" \"\u00dfe\"))").toString());
  }

  public void testStringToLowerCaseForConstant() throws Throwable {
    assertEquals("abcdefg", scheme.eval("(string-to-lower-case \"aBcDeFg\")").toString());
    // An uppercase sigma (Unicode 03A3) in the middle of a word is replaced with lowercase sigma
    // (Unicode 03C3) but an uppercase sigma the end of a word is replaced with lowercase final
    // sigma (Unicode 03C2).
    // Chi Alpha Omicron Sigma Sigma
    assertEquals("\u03c7\u03b1\u03bf\u03c3\u03c2",
        scheme.eval("(string-to-lower-case \"\u03a7\u0391\u039f\u03a3\u03a3\")").toString());
  }

  public void testStringToLowerCaseForExpression() throws Throwable {
    assertEquals("\u03c7\u03b1\u03bf\u03c3\u03c2",
        scheme.eval("(string-to-lower-case "
                    + "(string-append \"\u03a7\u0391\u039f\" \"\u03a3\u03a3\"))").toString());
  }

  public void testStringReplace() throws Throwable {
    /* this tests that we've quoted the dot so it's not special as a regexp */
    assertEquals("12x34x56x", scheme.eval("(string-replace-all \"12.34.56.\" \".\" \"x\")")
        .toString());
  }

  public void testStringIsEmpty() throws Throwable {
    assertTrue((Boolean) scheme.eval("(string-empty? \"\")"));
    assertFalse((Boolean) scheme.eval("(string-empty? \" \")"));
    assertFalse((Boolean) scheme.eval("(string-empty? \"foo\")"));
  }

  public void testMathsConvert() throws Throwable {
    // we have to make the test inputs strings, because in the running system,
    // the convert blocks force a conversion to string before calling the
    // Yail procedures
    assertEquals("3E8", scheme.eval("(math-convert-dec-hex \"1000\")").toString());
    assertEquals("1000", scheme.eval("(math-convert-hex-dec \"3E8\")").toString());
    assertEquals("1010", scheme.eval("(math-convert-dec-bin \"10\")").toString());
    assertEquals("10", scheme.eval("(math-convert-bin-dec \"1010\")").toString());
  }

  public void testMathsConvert2() throws Throwable {
    // test that we've patched around the Kawa bug in conversion to binary
     assertTrue((Boolean) scheme.eval("(testMathsConvert2)"));
   }

  public void testRoundToIntegerGroup() throws Throwable {
    assertEquals("10", scheme.eval("(yail-round 10.48)").toString());
    assertEquals("10", scheme.eval("(yail-floor 10.48)").toString());
    assertEquals("11", scheme.eval("(yail-ceiling 10.48)").toString());
    assertEquals("10", scheme.eval("(format-as-decimal 10.48 0)").toString());
    assertEquals("10.5", scheme.eval("(format-as-decimal 10.48 1)").toString());
    try {
      scheme.eval("(format-as-decimal 10.48 -2)");
      fail();
    } catch (YailRuntimeError e) {
      // expected
    }
  }

  public void testRandomIntegerAvoidError() throws Throwable {
    // demonstrate that this does not fail with large arguments
    scheme.eval("(random-integer 10 (- (expt 2 40)))");
    scheme.eval("(random-integer (- (expt 2 40)) 10)");
  }

  private static final double DELTA = .0001;

  private void testUnaryDoubleFunction(String funName, double[] args, double[] vals)
      throws Throwable {
    for (int i = 0; i < args.length; i++) {
      String expression = "(" + funName + " " + args[i] + ")";
      Object result = scheme.eval(expression);
      if (result instanceof DFloNum) {
        Assert.assertEquals(expression,
                            vals[i],
                            ((DFloNum) result).doubleValue(),
                            DELTA);
      } else {
        Assert.assertEquals(expression,
                            vals[i],
                            (Double) result,
                            DELTA);
      }
    }
  }

   public void testSine() throws Throwable {
     double[] args = { 0, 90, 180, 270 };
     double[] vals = { 0,  1,  0,   -1 };
     testUnaryDoubleFunction("sin-degrees", args, vals);
   }

   public void testCosine() throws Throwable {
     double[] args = { 0, 90, 180, 270 };
     double[] vals = { 1,  0,  -1,   0 };
     testUnaryDoubleFunction("cos-degrees", args, vals);
   }

   public void testTangent() throws Throwable {
     double[] args = { 0, 45, 135, 180, 225, 315, 30 };
     double[] vals = { 0,  1,  -1,   0,   1,  -1, .57735 };
     testUnaryDoubleFunction("tan-degrees", args, vals);
   }

   public void testAsin() throws Throwable {
     // Results should be in the range (-90, +90].
     double[] args = { 0, .5,  1,  -1 };
     double[] vals = { 0, 30, 90, -90 };
     testUnaryDoubleFunction("asin-degrees", args, vals);
   }

   public void testAcos() throws Throwable {
     // Results should be in the range [0, 180).
     double[] args = {  0, .5, 1,  -1 };
     double[] vals = { 90, 60, 0, 180 };
     testUnaryDoubleFunction("acos-degrees", args, vals);
   }

   public void testAtan() throws Throwable {
     // Results should be in the range (-90, +90).
     double[] args = { 0,  1,  -1, .57735 };
     double[] vals = { 0, 45, -45, 30 };
     testUnaryDoubleFunction("atan-degrees", args, vals);
   }

  // These constant definitions make the below tests more readable.
  static final double PI = Math.PI;
  static final double PI_2 = PI / 2;
  static final double PI_4 = PI / 4;

  public void testDegreesToRadians() throws Throwable {
    double[] args = {   -45, 0,   45,   90,   270, 360, 720 };
    double[] vals = { -PI_4, 0, PI_4, PI_2, -PI_2,   0,   0 };
    testUnaryDoubleFunction("degrees->radians", args, vals);
  }

  public void testRadiansToDegrees() throws Throwable {
    double[] args = { -PI_4, 0, PI_4, PI_2, -PI, -PI_2, 8 * Math.PI };
    double[] vals = {   315, 0, 45,     90, 180,   270, 0 };
    testUnaryDoubleFunction("radians->degrees", args, vals);
  }

  private void testBinaryDoubleFunction(String funName,
                                        double[] args1,
                                        double[] args2,
                                        double[] vals)
      throws Throwable {
    for (int i = 0; i < args1.length; i++) {
      String expression = "(" + funName + " " + args1[i] + " " + args2[i] + ")";
      Assert.assertEquals(vals[i],
                          ((DFloNum) scheme.eval(expression)).doubleValue(),
                          DELTA);
    }
  }

   public void testAtan2() throws Throwable {
     // Results should be in the range (-180, +180].
     double[] args1 = { 0,  1,  -1, 0   };
     double[] args2 = { 1,  0,   0, -1  };
     double[] vals =  { 0, 90, -90, 180 };
     testBinaryDoubleFunction("atan2-degrees", args1, args2, vals);
   }

   public void testIntegerDivison() throws Throwable {
     // Check that integer division does not produce rationals
     assertFalse((Boolean) scheme.eval("(exact? (yail-divide 2 3))"));
   }


   public void testConvertToStrings() throws Throwable {
     String schemeInputString = "(convert-to-strings (make-yail-list (/ 10 5) 2.0 \"abc\" 123 (list 4 5 6)))";
     String schemeExpectedResultString = "(2 2 abc 123 (4 5 6))";
     String schemeActualResult = scheme.eval(schemeInputString).toString();
     assertEquals(schemeExpectedResultString, schemeActualResult);
   }

  private void testMakeColorCase(int color, String red, String green, String blue, String alpha)
      throws Throwable {
    assertEquals(color,
        ((IntNum) scheme.eval(
            String.format(
                "(make-color (make-yail-list %s %s %s %s))",
                red, green, blue, alpha))).intValue());
  }

  public void testMakeColor() throws Throwable {
    // Normal case with 4 arguments in the range 0 to 255.
    testMakeColorCase(0x01020304, "2", "3", "4", "1");
    testMakeColorCase(0x0f0a0b0c, "10", "11", "12", "15");
    testMakeColorCase(0xffff0000, "255", "0", "0", "255");  // red

    // Four arguments, some out of range.
    testMakeColorCase(0xff020304, "258", "-253", "4", "-1");

    // Omit alpha.
    testMakeColorCase(0xff020304, "2", "3", "4", "");
    testMakeColorCase(0xff0a0b0c, "10", "11", "12", "");
    testMakeColorCase(0xffff0000, "255", "0", "0", "");  // red
    testMakeColorCase(0xff020304, "258", "-253", "4", "");
  }

  // The input to split-color is the actual parameter color.
  // The expected outputs are the actual parameters red, green, blue, and alpha.
  private void testSplitColorCase(int red, int green, int blue, int alpha, int color)
      throws Throwable {
    assertTrue((Boolean) scheme.eval(
        String.format("(let ((components (split-color %d))) " +
            "  (and (= (yail-list-get-item components 1) %d)" +
            "       (= (yail-list-get-item components 2) %d)" +
            "       (= (yail-list-get-item components 3) %d)" +
            "       (= (yail-list-get-item components 4) %d)" +
            "       (= (yail-list-length components) 4)))",
            color, red, green, blue, alpha)));
  }

  public void testSplitColor() throws Throwable {
    testSplitColorCase(0, 0, 0, 0, 0);
    testSplitColorCase(1, 2, 3, 4, 0x04010203);
    testSplitColorCase(0x1a, 0x2b, 0xff, 0xf0, 0xf01a2bff);
    testSplitColorCase(0xff, 0xff, 0xff, 0xff, 0xffffffff);
  }

  public void testExptWithSanitizedDouble() throws Throwable {
    // This tests the bug where passing a double property value to expt caused
    // java.lang.ClassCastException: java.lang.Double cannot be cast to gnu.math.Numeric
    String schemeString = "(define five :: java.lang.Double (java.lang.Double 5)) " +
        "(coerce-to-string (expt (sanitize-component-data five) 2))";
    assertEquals("25", scheme.eval(schemeString).toString());
  }

  public void testCoerceToStringWithSanitizedFloatZero() throws Throwable {
    // This tests the bug where coercing a float property value of 0.0 to a string gave the
    // incorrect result "OEO".
    String schemeString = "(define zero :: java.lang.Float (java.lang.Float 0)) " +
        "(coerce-to-string (sanitize-component-data zero))";
    assertEquals("0", scheme.eval(schemeString).toString());
  }

  public void testCoerceToStringWithSanitizedDoubleZero() throws Throwable {
    // This tests the bug where coercing a double property value of 0.0 to a string gave the
    // incorrect result "OEO".
    String schemeString = "(define zero :: java.lang.Double (java.lang.Double 0)) " +
        "(coerce-to-string (sanitize-component-data zero))";
    assertEquals("0", scheme.eval(schemeString).toString());
  }
}
