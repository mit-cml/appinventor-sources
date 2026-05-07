// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines a NxtMailbox type used by the NxtDirectCommands component.
 */
public enum NxtMailbox implements OptionList<Integer> {
  // Note from Paul Gyugyi during code review: we are only supporting mailboxes 1-10, but NXT can
  // use mailboxes above 10 as relays to other NXTs.  We've never needed it, but if you ever see
  // a feature request or bug report, all that might be required is just raising our upper limit
  // on the range.
  Box1(1, 0),
  Box2(2, 1),
  Box3(3, 2),
  Box4(4, 3),
  Box5(5, 4),
  Box6(6, 5),
  Box7(7, 6),
  Box8(8, 7),
  Box9(9, 8),
  Box10(10, 9);

  private Integer value;
  private int intValue;

  NxtMailbox(Integer box, int intBox) {
    this.value = box;
    this.intValue = intBox;
  }

  public Integer toUnderlyingValue() {
    return value;
  }

  public Integer toInt() {
    return intValue;
  }

  private static final Map<Integer, NxtMailbox> lookup = new HashMap<>();

  static {
    for (NxtMailbox box : NxtMailbox.values()) {
      lookup.put(box.toUnderlyingValue(), box);
    }
  }

  public static NxtMailbox fromUnderlyingValue(Integer box) {
    return lookup.get(box);
  }
}

