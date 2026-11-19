// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.util;

import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;

/** Tests compatibility and canonical output for anonymous account revisit codes. */
public class AccountUtilTest extends TestCase {
  // Legacy word, canonical word, and index in the original production word list.
  private static final String[][] LEGACY_WORDS = {
    {"BAWD", "BATS", "151"},
    {"BONG", "BONES", "232"},
    {"BUM", "BUMP", "271"},
    {"COCK", "COCCI", "354"},
    {"COKE", "COIR", "364"},
    {"COON", "COOP", "376"},
    {"DUMB", "DULY", "521"},
    {"GYP", "GYRO", "791"},
    {"HEBE", "HEAVE", "833"},
    {"HELL", "HELLO", "839"},
    {"HICK", "HICCUP", "854"},
    {"HO", "HOAX", "870"},
    {"JERK", "JELL", "973"},
    {"KILL", "KILN", "1033"},
    {"KLAN", "KIWI", "1042"},
    {"KNOB", "KNOLL", "1046"},
    {"LAME", "LAMP", "1070"},
    {"LUST", "LUTE", "1183"},
    {"MUFF", "MUDS", "1302"},
    {"NIP", "NINTH", "1355"},
    {"NUDE", "NUB", "1378"},
    {"ORGY", "ORES", "1423"},
    {"SHAG", "SHAD", "1649"},
    {"WANG", "WANED", "1951"},
  };

  public void testLegacyWordsPreserveAccountsAndDisplayCanonicalWords() {
    for (String[] entry : LEGACY_WORDS) {
      long index = Long.parseLong(entry[2]);
      for (int position = 0; position < 4; position++) {
        String[] oldCode = {"A", "A", "A", "A"};
        String[] newCode = {"A", "A", "A", "A"};
        oldCode[position] = entry[0];
        newCode[position] = entry[1];
        String expectedAccount = "anon-" + (index << (11 * position));
        String account = AccountUtil.codeToAccount(joinCode(oldCode));
        assertEquals(entry[0], expectedAccount, account);
        assertEquals(joinCode(newCode), AccountUtil.accountToCode(account));
        assertEquals(account, AccountUtil.codeToAccount(joinCode(newCode)));
      }
    }
  }

  public void testMixedLegacyAndCanonicalWords() {
    long accountId = 354L | (833L << 11) | (1042L << 22) | (1951L << 33);
    String expectedAccount = "anon-" + accountId;
    assertEquals(expectedAccount, AccountUtil.codeToAccount("COCK-HEBE-KLAN-WANG"));
    assertEquals(expectedAccount, AccountUtil.codeToAccount("COCCI-HEBE-KIWI-WANG"));
    assertEquals("COCCI-HEAVE-KIWI-WANED", AccountUtil.accountToCode(expectedAccount));
    assertEquals("COCCI-COCCI-COCCI-COCCI", AccountUtil.accountToCode(
        AccountUtil.codeToAccount("COCK-COCK-COCK-COCK")));
  }

  public void testEveryCanonicalWordRoundTripsInEveryPosition() {
    Set<String> legacyWords = new HashSet<>();
    for (String[] entry : LEGACY_WORDS) {
      legacyWords.add(entry[0]);
    }
    Set<String> canonicalWords = new HashSet<>();
    String previousWord = null;
    for (int index = 0; index < 2048; index++) {
      String word = AccountUtil.accountToCode("anon-" + index).split("-")[0];
      assertFalse(legacyWords.contains(word));
      assertTrue(canonicalWords.add(word));
      if (previousWord != null) {
        assertTrue(previousWord.compareTo(word) < 0);
      }
      previousWord = word;
      for (int position = 0; position < 4; position++) {
        String account = "anon-" + ((long) index << (11 * position));
        String code = AccountUtil.accountToCode(account);
        assertEquals(word, code.split("-")[position]);
        assertEquals(account, AccountUtil.codeToAccount(code));
      }
    }
    assertEquals(2048, canonicalWords.size());
  }

  public void testUnknownWordsRemainInvalid() {
    try {
      AccountUtil.codeToAccount("COCK-NOTINLIST-A-A");
      fail("Unknown words must not be accepted alongside legacy aliases");
    } catch (IllegalArgumentException expected) {
      // Expected.
    }
  }

  private static String joinCode(String[] words) {
    return words[0] + "-" + words[1] + "-" + words[2] + "-" + words[3];
  }
}
