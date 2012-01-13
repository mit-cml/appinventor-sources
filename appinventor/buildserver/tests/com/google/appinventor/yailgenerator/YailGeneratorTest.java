// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.yailgenerator;

import com.google.appinventor.common.testutils.TestUtils;
import com.google.common.io.Files;

import openblocks.codeblocks.Block;
import openblocks.workspace.Workspace;
import openblocks.yacodeblocks.YailGenerationException;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Tests for YailGenerator.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YailGeneratorTest extends TestCase {
  private static final String TESTING_SOURCE_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/buildserver/tests/com/google/appinventor/yailgenerator/testing_files/";
  private static final Charset DEFAULT_CHARSET = Charset.forName("Cp1252");

  public void testGenerateYailNewProject() throws Exception {
    // NewProject/Screen1.blk is empty.
    // The YailGenerator should be able to generate the YAIL.
    String yail = generateYail("NewProject", "Screen1");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Title \"Screen1\" 'text)\n" +
        ")\n");
  }

  public void testGenerateYailVariousBlocks() throws Exception {
    // VariousBlocks/Screen1.blk contains various blocks.
    // The YailGenerator should be able to generate the YAIL.
    String yail = generateYail("VariousBlocks", "Screen1");
    assertContainsYailFragment(yail,
        "(def StoredValue \"text\")\n");
    assertContainsYailFragment(yail,
        "(def MyFavoriteNumber 217)\n");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Title \"Screen1\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event Screen1 Initialize()\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Screen1 'Title \"My Application\" 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'Screen1 'BackgroundColor -16776961 'number)\n" +
        "\n" +
        "(set-and-coerce-property! 'Screen1 'Scrollable #t 'boolean)\n" +
        "\n" +
        "(set-var! StoredValue  (call-component-method 'TinyDB1 'GetValue (*list-for-runtime* " +
        "123)\n" +
        " '( text)\n" +
        ")\n" +
        ")\n" +
        "\n" +
        " (call-component-method 'TinyDB1 'GetValue (*list-for-runtime* \"text\")\n" +
        " '( text)\n" +
        ")\n" +
        "\n" +
        "(call-component-method 'TinyDB1 'StoreValue (*list-for-runtime* \"FavoriteNumber\" " +
        "(get-var MyFavoriteNumber)\n" +
        ")\n" +
        " '( text any)\n" +
        ")\n" +
        "\n" +
        "(call-component-method 'TinyDB1 'StoreValue (*list-for-runtime* \"product\" " +
        "(call-yail-primitive * (*list-for-runtime* 3 (get-var MyFavoriteNumber)\n" +
        ")\n" +
        " '( number number)\n" +
        " \"\\u00d7\")\n" +
        ")\n" +
        " '( text any)\n" +
        ")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event Screen1 ErrorOccurred( component  functionName  errorNumber  message )\n" +
        " (set-this-form)\n" +
        " (call-component-method 'TinyDB1 'StoreValue (*list-for-runtime* 123 (lexical-value " +
        "message)\n" +
        ")\n" +
        " '( text any)\n" +
        ")\n" +
        "\n" +
        "(call-component-method 'TinyDB1 'StoreValue (*list-for-runtime* \"text\" (get-property " +
        "'Screen1 'BackgroundImage)\n" +
        ")\n" +
        " '( text any)\n" +
        ")\n" +
        "\n" +
        "(set-and-coerce-property! 'Screen1 'Title -16776961 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'Screen1 'BackgroundColor \"123\" 'number)\n" +
        "\n" +
        "(set-and-coerce-property! 'Screen1 'Scrollable (get-var MyFavoriteNumber)\n" +
        " 'boolean)\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; TinyDB1\n" +
        "(add-component Screen1 TinyDB TinyDB1 )\n");
  }

  public void testAlternating() throws Exception {
    // This test ensures that we can generate yail for a new "empty" project as the first project
    // and also after we've generated yail for a non-empty project a few times.
    testGenerateYailNewProject();
    testGenerateYailVariousBlocks();
    testGenerateYailVariousBlocks();
    testGenerateYailVariousBlocks();
    testGenerateYailNewProject();
    testGenerateYailVariousBlocks();
  }

  public void testGenerateYailMultipleForms() throws Exception {
    String[] formNames = {
      "Screen1",
      "BlackCat",
      "CalicoCat",
      "OrangeCat",
      "PersianCat",
      "SiameseCat",
    };
    for (String formName : formNames) {
      String yail = generateYail("CatSurvey", formName);
      if (formName.equals("Screen1")) {
        checkCatSurveyScreen1Yail(yail);
      } else if (formName.equals("BlackCat")) {
        checkCatYail("Black", yail);
      } else if (formName.equals("CalicoCat")) {
        checkCatYail("Calico", yail);
      } else if (formName.equals("OrangeCat")) {
        checkCatYail("Orange", yail);
      } else if (formName.equals("PersianCat")) {
        checkCatYail("Persian", yail);
      } else if (formName.equals("SiameseCat")) {
        checkCatYail("Siamese", yail);
      } else {
        fail();
      }
    }
  }

  private void checkCatSurveyScreen1Yail(String yail) {
    assertContainsYailFragment(yail,
        "(def (IncrementVoteValue Tag )\n" +
        "(call-component-method 'TinyDB1 'StoreValue (*list-for-runtime* (lexical-value Tag)\n" +
        " (call-yail-primitive + (*list-for-runtime* (call-component-method 'TinyDB1 'GetValue " +
        "(*list-for-runtime* (lexical-value Tag)\n" +
        ")\n" +
        " '( text)\n" +
        ")\n" +
        " 1)\n" +
        " '( number number)\n" +
        " \"+\")\n" +
        ")\n" +
        " '( text any)\n" +
        ")\n" +
        "\n" +
        " )\n");
    assertContainsYailFragment(yail,
        "(def (InitializeDatabase)\n" +
        "(foreach  tag  (begin ((get-var InitializeVoteValues) (lexical-value tag)\n" +
        ")\n" +
        "\n" +
        ")\n" +
        "  (call-yail-primitive make-yail-list (*list-for-runtime* \"BlackCat\" \"CalicoCat\" " +
        "\"OrangeCat\" \"PersianCat\" \"SiameseCat\")\n" +
        " '( any any any any any)\n" +
        " \"make a list\")\n" +
        ")\n" +
        "\n" +
        " )\n");
    assertContainsYailFragment(yail,
        "(def (MakeTag TagPrefix  Vote )\n" +
        " (call-yail-primitive string-append (*list-for-runtime* (lexical-value TagPrefix)\n" +
        " \"_\" (lexical-value Vote)\n" +
        ")\n" +
        " '( text text text)\n" +
        " \"make text\")\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(def (InitializeVoteValues TagPrefix3 )\n" +
        "(foreach  Vote3  (begin (if (call-yail-primitive string=? (*list-for-runtime* " +
        "(call-component-method 'TinyDB1 'GetValue (*list-for-runtime* ((get-var MakeTag) " +
        "(lexical-value TagPrefix3)\n" +
        " (lexical-value Vote3)\n" +
        ")\n" +
        ")\n" +
        " '( text)\n" +
        ")\n" +
        " \"\")\n" +
        " '( text text)\n" +
        " \"text=\")\n" +
        " (begin (call-component-method 'TinyDB1 'StoreValue (*list-for-runtime* " +
        "((get-var MakeTag) (lexical-value TagPrefix3)\n" +
        " (lexical-value Vote3)\n" +
        ")\n" +
        " 0)\n" +
        " '( text any)\n" +
        ")\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        ")\n" +
        "  (call-yail-primitive make-yail-list (*list-for-runtime* \"Yes\" \"No\")\n" +
        " '( any any)\n" +
        " \"make a list\")\n" +
        ")\n" +
        "\n" +
        " )\n");
    assertContainsYailFragment(yail,
        "(def (GetScore TagPrefix2 )\n" +
        " (call-yail-primitive - (*list-for-runtime* (call-component-method 'TinyDB1 'GetValue " +
        "(*list-for-runtime* ((get-var MakeTag) (lexical-value TagPrefix2)\n" +
        " \"Yes\")\n" +
        ")\n" +
        " '( text)\n" +
        ")\n" +
        " (call-component-method 'TinyDB1 'GetValue (*list-for-runtime* ((get-var MakeTag) " +
        "(lexical-value TagPrefix2)\n" +
        " \"No\")\n" +
        ")\n" +
        " '( text)\n" +
        ")\n" +
        ")\n" +
        " '( number number)\n" +
        " \"\\u2212\")\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Scrollable #f 'boolean)\n" +
        "(set-and-coerce-property! 'Screen1 'Title \"Cat Survey\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event Screen1 Initialize()\n" +
        " (set-this-form)\n" +
        " ((get-var InitializeDatabase))\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelBlackCatScore 'Text ((get-var GetScore) \"BlackCat\")\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelCalicoCatScore 'Text ((get-var GetScore) " +
        "\"CalicoCat\")\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelOrangeCatScore 'Text ((get-var GetScore) " +
        "\"OrangeCat\")\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelPersianCatScore 'Text ((get-var GetScore) " +
        "\"PersianCat\")\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelSiameseCatScore 'Text ((get-var GetScore) " +
        "\"SiameseCat\")\n" +
        " 'text)\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event Screen1 OtherScreenClosed( screenName  result )\n" +
        " (set-this-form)\n" +
        " (if (call-yail-primitive > (*list-for-runtime* (call-yail-primitive string-length " +
        "(*list-for-runtime* (lexical-value result)\n" +
        ")\n" +
        " '( text)\n" +
        " \"length\")\n" +
        " 0)\n" +
        " '( number number)\n" +
        " \">\")\n" +
        " (begin ((get-var IncrementVoteValue) ((get-var MakeTag) (lexical-value screenName)\n" +
        " (lexical-value result)\n" +
        ")\n" +
        ")\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        "(if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value screenName)\n" +
        " \"BlackCat\")\n" +
        " '( any any)\n" +
        " \"=\")\n" +
        " (begin (set-and-coerce-property! 'LabelBlackCatResult 'Text (lexical-value result)\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelBlackCatScore 'Text ((get-var GetScore) " +
        "(lexical-value screenName)\n" +
        ")\n" +
        " 'text)\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        "(if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value screenName)\n" +
        " \"OrangeCat\")\n" +
        " '( any any)\n" +
        " \"=\")\n" +
        " (begin (set-and-coerce-property! 'LabelOrangeCatResult 'Text (lexical-value result)\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelOrangeCatScore 'Text ((get-var GetScore) " +
        "(lexical-value screenName)\n" +
        ")\n" +
        " 'text)\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        "(if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value screenName)\n" +
        " \"CalicoCat\")\n" +
        " '( any any)\n" +
        " \"=\")\n" +
        " (begin (set-and-coerce-property! 'LabelCalicoCatResult 'Text (lexical-value result)\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelCalicoCatScore 'Text ((get-var GetScore) " +
        "(lexical-value screenName)\n" +
        ")\n" +
        " 'text)\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        "(if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value screenName)\n" +
        " \"PersianCat\")\n" +
        " '( any any)\n" +
        " \"=\")\n" +
        " (begin (set-and-coerce-property! 'LabelPersianCatResult 'Text (lexical-value result)\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelPersianCatScore 'Text ((get-var GetScore) " +
        "(lexical-value screenName)\n" +
        ")\n" +
        " 'text)\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        "(if (call-yail-primitive yail-equal? (*list-for-runtime* (lexical-value screenName)\n" +
        " \"SiameseCat\")\n" +
        " '( any any)\n" +
        " \"=\")\n" +
        " (begin (set-and-coerce-property! 'LabelSiameseCatResult 'Text (lexical-value result)\n" +
        " 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'LabelSiameseCatScore 'Text ((get-var GetScore) " +
        "(lexical-value screenName)\n" +
        ")\n" +
        " 'text)\n" +
        "\n" +
        ")\n" +
        ")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; HorizontalArrangement4\n" +
        "(add-component Screen1 HorizontalArrangement HorizontalArrangement4 " +
        "(set-and-coerce-property! 'HorizontalArrangement4 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'HorizontalArrangement4 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonBlackCat\n" +
        "(add-component HorizontalArrangement4 Button ButtonBlackCat " +
        "(set-and-coerce-property! 'ButtonBlackCat 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'ButtonBlackCat 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'ButtonBlackCat 'Text \"Black Cat\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonBlackCat 'Width 200 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonBlackCat Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive open-screen (*list-for-runtime* \"BlackCat\")\n" +
        " '( text)\n" +
        " \"open another screen\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelBlackCatResult\n" +
        "(add-component HorizontalArrangement4 Label LabelBlackCatResult " +
        "(set-and-coerce-property! 'LabelBlackCatResult 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelBlackCatResult 'Text \"?\" 'text)\n" +
        "(set-and-coerce-property! 'LabelBlackCatResult 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelBlackCatScore\n" +
        "(add-component HorizontalArrangement4 Label LabelBlackCatScore " +
        "(set-and-coerce-property! 'LabelBlackCatScore 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelBlackCatScore 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; HorizontalArrangement2\n" +
        "(add-component Screen1 HorizontalArrangement HorizontalArrangement2 " +
        "(set-and-coerce-property! 'HorizontalArrangement2 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'HorizontalArrangement2 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonCalicoCat\n" +
        "(add-component HorizontalArrangement2 Button ButtonCalicoCat " +
        "(set-and-coerce-property! 'ButtonCalicoCat 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'ButtonCalicoCat 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'ButtonCalicoCat 'Text \"Calico Cat\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonCalicoCat 'Width 200 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonCalicoCat Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive open-screen (*list-for-runtime* \"CalicoCat\")\n" +
        " '( text)\n" +
        " \"open another screen\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelCalicoCatResult\n" +
        "(add-component HorizontalArrangement2 Label LabelCalicoCatResult " +
        "(set-and-coerce-property! 'LabelCalicoCatResult 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelCalicoCatResult 'Text \"?\" 'text)\n" +
        "(set-and-coerce-property! 'LabelCalicoCatResult 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelCalicoCatScore\n" +
        "(add-component HorizontalArrangement2 Label LabelCalicoCatScore " +
        "(set-and-coerce-property! 'LabelCalicoCatScore 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelCalicoCatScore 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; HorizontalArrangement1\n" +
        "(add-component Screen1 HorizontalArrangement HorizontalArrangement1 " +
        "(set-and-coerce-property! 'HorizontalArrangement1 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'HorizontalArrangement1 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonOrangeCat\n" +
        "(add-component HorizontalArrangement1 Button ButtonOrangeCat " +
        "(set-and-coerce-property! 'ButtonOrangeCat 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'ButtonOrangeCat 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'ButtonOrangeCat 'Text \"Orange Cat\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonOrangeCat 'Width 200 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonOrangeCat Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive open-screen (*list-for-runtime* \"OrangeCat\")\n" +
        " '( text)\n" +
        " \"open another screen\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelOrangeCatResult\n" +
        "(add-component HorizontalArrangement1 Label LabelOrangeCatResult " +
        "(set-and-coerce-property! 'LabelOrangeCatResult 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelOrangeCatResult 'Text \"?\" 'text)\n" +
        "(set-and-coerce-property! 'LabelOrangeCatResult 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelOrangeCatScore\n" +
        "(add-component HorizontalArrangement1 Label LabelOrangeCatScore " +
        "(set-and-coerce-property! 'LabelOrangeCatScore 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelOrangeCatScore 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; HorizontalArrangement5\n" +
        "(add-component Screen1 HorizontalArrangement HorizontalArrangement5 " +
        "(set-and-coerce-property! 'HorizontalArrangement5 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'HorizontalArrangement5 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonPersianCat\n" +
        "(add-component HorizontalArrangement5 Button ButtonPersianCat " +
        "(set-and-coerce-property! 'ButtonPersianCat 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'ButtonPersianCat 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'ButtonPersianCat 'Text \"Persian Cat\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonPersianCat 'Width 200 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonPersianCat Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive open-screen (*list-for-runtime* \"PersianCat\")\n" +
        " '( text)\n" +
        " \"open another screen\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelPersianCatResult\n" +
        "(add-component HorizontalArrangement5 Label LabelPersianCatResult " +
        "(set-and-coerce-property! 'LabelPersianCatResult 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelPersianCatResult 'Text \"?\" 'text)\n" +
        "(set-and-coerce-property! 'LabelPersianCatResult 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelPersianCatScore\n" +
        "(add-component HorizontalArrangement5 Label LabelPersianCatScore " +
        "(set-and-coerce-property! 'LabelPersianCatScore 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelPersianCatScore 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; HorizontalArrangement3\n" +
        "(add-component Screen1 HorizontalArrangement HorizontalArrangement3 " +
        "(set-and-coerce-property! 'HorizontalArrangement3 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'HorizontalArrangement3 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonSiameseCat\n" +
        "(add-component HorizontalArrangement3 Button ButtonSiameseCat " +
        "(set-and-coerce-property! 'ButtonSiameseCat 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'ButtonSiameseCat 'Height -2 'number)\n" +
        "(set-and-coerce-property! 'ButtonSiameseCat 'Text \"Siamese Cat\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonSiameseCat 'Width 200 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonSiameseCat Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive open-screen (*list-for-runtime* \"SiameseCat\")\n" +
        " '( text)\n" +
        " \"open another screen\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelSiameseCatResult\n" +
        "(add-component HorizontalArrangement3 Label LabelSiameseCatResult " +
        "(set-and-coerce-property! 'LabelSiameseCatResult 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelSiameseCatResult 'Text \"?\" 'text)\n" +
        "(set-and-coerce-property! 'LabelSiameseCatResult 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; LabelSiameseCatScore\n" +
        "(add-component HorizontalArrangement3 Label LabelSiameseCatScore " +
        "(set-and-coerce-property! 'LabelSiameseCatScore 'FontSize 24.0 'number)\n" +
        "(set-and-coerce-property! 'LabelSiameseCatScore 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; TinyDB1\n" +
        "(add-component Screen1 TinyDB TinyDB1 )\n");
  }

  private void checkCatYail(String catType, String yail) {
    assertContainsYailFragment(yail,
        ";;; " + catType + "Cat\n" +
        "(do-after-form-creation (set-and-coerce-property! '" + catType + "Cat 'Title \"" +
        catType + " Cat\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Label1\n" +
        "(add-component " + catType + "Cat Label Label1 (set-and-coerce-property! 'Label1 " +
        "'FontSize 30.0 'number)\n" +
        "(set-and-coerce-property! 'Label1 'Text \"Do you like this cat?\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Image1\n" +
        "(add-component " + catType + "Cat Image Image1 (set-and-coerce-property! 'Image1 " +
        "'Picture \"" + catType + "Cat.jpg\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; HorizontalArrangement1\n" +
        "(add-component " + catType + "Cat HorizontalArrangement HorizontalArrangement1 " +
        "(set-and-coerce-property! 'HorizontalArrangement1 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonYes\n" +
        "(add-component HorizontalArrangement1 Button ButtonYes " +
        "(set-and-coerce-property! 'ButtonYes 'Text \"Yes\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonYes 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonYes Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive close-screen-with-result (*list-for-runtime* \"Yes\")\n" +
        " '( text)\n" +
        " \"close screen with result\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonNo\n" +
        "(add-component HorizontalArrangement1 Button ButtonNo " +
        "(set-and-coerce-property! 'ButtonNo 'Text \"No\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonNo 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonNo Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive close-screen-with-result (*list-for-runtime* \"No\")\n" +
        " '( text)\n" +
        " \"close screen with result\")\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonNoComment\n" +
        "(add-component HorizontalArrangement1 Button ButtonNoComment " +
        "(set-and-coerce-property! 'ButtonNoComment 'Text \"No Comment\" 'text)\n" +
        "(set-and-coerce-property! 'ButtonNoComment 'Width -2 'number)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonNoComment Click()\n" +
        " (set-this-form)\n" +
        " (call-yail-primitive close-screen (*list-for-runtime*)\n" +
        " '() \"close screen\")\n" +
        "\n" +
        ")\n");
  }

  public void testGenerateYailComponentHasBeenRemoved() throws Exception {
    // RemovedButton/Screen1.blk contains a component (Button1) that has been removed from
    // RemovedButton/Screen1.scm. There are blocks for a Button1 event and property setters.
    // The YailGenerator should be able to remove the Button1 blocks and generate the YAIL. There
    // should be no YAIL generated for Button1 blocks.
    String yail = generateYail("RemovedButton", "Screen1");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Title \"Screen1\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonYes\n" +
        "(add-component Screen1 Button ButtonYes (set-and-coerce-property! 'ButtonYes 'Text " +
        "\"Yes\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonYes Click()\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Label1 'Text \"Yes\" 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'Label1 'BackgroundColor -16711936 'number)\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; ButtonNo\n" +
        "(add-component Screen1 Button ButtonNo (set-and-coerce-property! 'ButtonNo 'Text " +
        "\"No\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event ButtonNo Click()\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Label1 'Text \"No\" 'text)\n" +
        "\n" +
        "(set-and-coerce-property! 'Label1 'BackgroundColor -65536 'number)\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Label1\n" +
        "(add-component Screen1 Label Label1 (set-and-coerce-property! 'Label1 'Text " +
        "\".\" 'text)\n" +
        ")\n");

    assertDoesNotContainYailFragment(yail, "Button1");
  }

  public void testGenerateYailComponentHasBeenRemovedEventHadArguments() throws Exception {
    // RemovedImageSprite/Screen1.blk contains a component (ImageSprite1) that has been removed
    // from RemovedImageSprite/Screen1.scm. There is an ImageSprite1.Touched event block with
    // arguments x and y.
    // The YailGenerator should be able to remove the ImageSprite1 blocks, including the event and
    // arguments, and generate the YAIL. There should be no YAIL generated for ImageSprite1 blocks.
    String yail = generateYail("RemovedImageSprite", "Screen1");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Scrollable #f 'boolean)\n" +
        "(set-and-coerce-property! 'Screen1 'Title \"Screen1\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Canvas1\n" +
        "(add-component Screen1 Canvas Canvas1 (set-and-coerce-property! 'Canvas1 " +
        "'Height -2 'number)\n" +
        "(set-and-coerce-property! 'Canvas1 'Width -2 'number)\n" +
        ")\n");
    assertDoesNotContainYailFragment(yail, "ImageSprite1");
  }

  public void testGenerateYailComponentHasBeenRenamed() throws Exception {
    // RenamedButton/Screen1.blk contains a component (Button1) that has been renamed in
    // RenamedButton/Screen1.scm (Button2).
    // The YailGenerator should be able to generate the YAIL.
    String yail = generateYail("RenamedButton", "Screen1");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Title \"Screen1\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Button2\n" +
        "(add-component Screen1 Button Button2 (set-and-coerce-property! 'Button2 'Text " +
        "\"Text for Button\" 'text)\n" +
        ")\n" +
        "(define-event Button2 Click()\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Button2 'Text (call-yail-primitive string-append " +
        "(*list-for-runtime* (get-property 'Button2 'Text)\n" +
        " \".\")\n" +
        " '( text text)\n" +
        " \"join\")\n" +
        " 'text)\n" +
        "\n" +
        ")\n");
    assertDoesNotContainYailFragment(yail, "Button1");
  }

  public void testGenerateYailCollapsedBlocks() throws Exception {
    // CollapsedBlocks/Screen1.blk contains collapsed blocks.
    // The YailGenerator should be able to generate the YAIL.
    String yail = generateYail("CollapsedBlocks", "Screen1");
    assertContainsYailFragment(yail,
        ";;; Screen1\n" +
        "(do-after-form-creation (set-and-coerce-property! 'Screen1 'Title \"Screen1\" 'text)\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event Screen1 ErrorOccurred( component  functionName  errorNumber  message )\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Screen1 'Title (lexical-value message)\n" +
        " 'text)\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        "(define-event Screen1 Initialize()\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Screen1 'BackgroundColor -1 'number)\n" +
        "\n" +
        ")\n");
    assertContainsYailFragment(yail,
        ";;; Button1\n" +
        "(add-component Screen1 Button Button1 (set-and-coerce-property! 'Button1 'Text " +
        "\"Text for Button1\" 'text)\n" +
        ")\n" +
        "(define-event Button1 Click()\n" +
        " (set-this-form)\n" +
        " (set-and-coerce-property! 'Button1 'Text (call-yail-primitive string-append " +
        "(*list-for-runtime* (get-property 'Button1 'Text)\n" +
        " \".\")\n" +
        " '( text text)\n" +
        " \"join\")\n" +
        " 'text)\n" +
        "\n" +
        ")\n");
  }

  public void testGenerateYailSucceedsDespiteWarnings() throws Exception {
    String[] projectNames = {
      "UnboundVariable",
      "DuplicateHandler",
    };
    for (String projectName : projectNames) {
      generateYail(projectName, "Screen1");
    }
  }

  public void testGenerateYailExpectedFailures() throws Exception {
    String[] projectNames = {
      "GlobalReferencingComponent",
      "GlobalReferencingComponentProperty",
      "GlobalReferencingVariable",
      "EmptySocket",
      "RemovedButtonLeavesEmptySocket",
    };
    for (String projectName : projectNames) {
      try {
        generateYail(projectName, "Screen1");
        fail();
      } catch (YailGenerationException e) {
        // expected
      }
    }
  }

  private String generateYail(String projectName, String formName)
      throws IOException, YailGenerationException {
    // Load the .scm and .blk files.
    String formPropertiesSource = loadFile(projectName + "/" + formName + ".scm");
    String codeblocksSource = loadFile(projectName + "/" + formName + ".blk");
    String yailPath = "src/appinventor/ai_someone/" + projectName + "/" + formName + ".yail";

    try {
      String yail = YailGenerator.generateYail(formPropertiesSource, codeblocksSource, yailPath);
      checkCommonYail(yail, "appinventor.ai_someone." + projectName + "." + formName, formName);
      return yail;
    } finally { // Regardless of whether it could generate YAIL or not...
      // Check that no blocks remain in the system.
      checkNoBlocksRemain();
    }
  }

  private static String loadFile(String fileName) throws IOException {
    return Files.toString(new File(TESTING_SOURCE_PATH + fileName),
        DEFAULT_CHARSET);
  }

  private static void checkCommonYail(String yail, String qualifiedFormName, String formName) {
    assertNotNull(yail);
    assertContainsYailFragment(yail,
        "#|\n" +
        "$Source $Yail\n" +
        "|#\n" +
        "\n");
    assertContainsYailFragment(yail,
        "(define-form " + qualifiedFormName + " " + formName + ")\n" +
        "(require <com.google.youngandroid.runtime>)\n");
    assertContainsYailFragment(yail,
        "(init-runtime)\n");
  }

  private static void assertContainsYailFragment(String text, String term) {
    // Strip blanks and line breaks, then check for a match.
    String textStripped = text.replace(" ", "").replace("\n", "");
    String termStripped = term.replace(" ", "").replace("\n", "");
    if (!textStripped.contains(termStripped)) {
      fail("Expected <" + textStripped + "> to contain <" + termStripped  + ">");
    }
  }

  private static void assertDoesNotContainYailFragment(String text, String term) {
    // Strip blanks and line breaks, then check for a match.
    String textStripped = text.replace(" ", "").replace("\n", "");
    String termStripped = term.replace(" ", "").replace("\n", "");
    if (textStripped.contains(termStripped)) {
      fail("Expected <" + textStripped + "> to not contain <" + termStripped  + ">");
    }
  }

  private static void checkNoBlocksRemain() {
    // Check that the blocks system has been cleaned out.
    assertFalse(Block.getAllBlocks().iterator().hasNext());
    Workspace workspace = Workspace.getInstance();
    assertFalse(workspace.getRenderableBlocks().iterator().hasNext());
    assertFalse(workspace.getBlocks().iterator().hasNext());
  }
}
