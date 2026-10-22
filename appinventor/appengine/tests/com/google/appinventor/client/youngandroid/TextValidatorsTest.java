// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.youngandroid;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class TextValidatorsTest extends TestCase {

  private final List<String> legalIdentifierNames = Arrays.asList("a", "aB", "a_b", "a_B", "Ab",
                                                                  "A9", "A9b_c");

  private final List<String> illegalIdentifierNames = Arrays.asList("", "_a", "9A", "ab0-", "-aB",
                                                                    "A b", " ", "foo bar", "foo,bar");

  private final List<String> legalComponentIdentifierNames = Arrays.asList("你好吗", "按钮1", "图片2",
		  "图_片2", "图3片_2", "botón1", "botón_2");

  private final List<String> illegalComponentIdentifierNames = Arrays.asList("", "!你好吗", "2你好吗",
		  "123按_钮", "1按2钮 ", "1botón", "!botón2", "Clock`");

	private final List<String> invisibleCharacterNames = Arrays.asList("Label\u00091",   // tab
      "Label\u007F1",   // DEL
      "Label\u00A01",   // no-break space
      "Label\u00AD1",   // soft hyphen
      "Label\u16801",   // ogham space mark
      "Label\u20031",   // em space
      "Label\u200B1",   // zero-width space
      "Label\u200C1",   // ZWNJ
      "Label\u200D1",   // ZWJ
      "Label\u200F1",   // right-to-left mark
      "Label\u20281",   // line separator
      "Label\u202E1",   // right-to-left override
      "Label\u20601",   // word joiner
      "Label\u20661",   // bidi isolate
      "Label\u30001",   // ideographic space
      "Label\uFEFF1",   // BOM
      "\u200BLabel1",   // at the start
      "Label1\u200B",   // at the end
      "\u200B");        // only invisible

  private final List<String> reservedWordNames = Arrays.asList("YailList", "YailNumberToString", "YailRuntimeError",
          "final", "interface", "static");

  public void testIdentifierFilter(){
    for (String legalIdentifier : legalIdentifierNames) {
      assertTrue(TextValidators.isValidIdentifier(legalIdentifier));
    }
    for (String illegalIdentifier : illegalIdentifierNames) {
      assertFalse(TextValidators.isValidIdentifier(illegalIdentifier));
    }
  }

  public void testComponentIdentifierFilter() {
    for (String legalComponentIdentifier : legalComponentIdentifierNames) {
      assertTrue(TextValidators.isValidComponentIdentifier(legalComponentIdentifier));
    }
    for (String illegalComponentIdentifier : illegalComponentIdentifierNames) {
      assertFalse(TextValidators.isValidComponentIdentifier(illegalComponentIdentifier));
    }
    for (String invisibleCharacter : invisibleCharacterNames) {
      assertFalse(TextValidators.isValidComponentIdentifier(invisibleCharacter));
    }
  }

  public void testReservedNameFilter() {
    for (String reservedWord : reservedWordNames) {
      assertTrue(TextValidators.isReservedName(reservedWord));
    }
    for (String notReservedWord : legalIdentifierNames) {
      assertFalse(TextValidators.isReservedName(notReservedWord));
    }
  }
}
