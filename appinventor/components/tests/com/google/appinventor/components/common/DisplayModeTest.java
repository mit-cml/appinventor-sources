package com.google.appinventor.components.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DisplayModeTest {

  @Test
  public void testEnumCount() {
    // We expect exactly 3 modes now (Safe, EdgeToEdge, BackgroundEdgeToEdge)
    assertEquals(3, DisplayMode.values().length);
  }

  @Test
  public void testToUnderlyingValue() {
    assertEquals("safe", DisplayMode.Safe.toUnderlyingValue());
    assertEquals("edge-to-edge", DisplayMode.EdgeToEdge.toUnderlyingValue());
    assertEquals("background-edge-to-edge", DisplayMode.BackgroundEdgeToEdge.toUnderlyingValue());
  }

  @Test
  public void testFromUnderlyingValue() {
    assertEquals(DisplayMode.Safe, DisplayMode.fromUnderlyingValue("safe"));
    assertEquals(DisplayMode.EdgeToEdge, DisplayMode.fromUnderlyingValue("edge-to-edge"));
    assertEquals(DisplayMode.BackgroundEdgeToEdge, DisplayMode.fromUnderlyingValue("background-edge-to-edge"));
    assertNull(DisplayMode.fromUnderlyingValue("invalid"));
  }
}