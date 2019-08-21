package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;

import java.util.Arrays;

public class MockChartData2D extends MockChartData {
  public static final String TYPE = "ChartData2D";

  /**
   * Creates a new MockChartData2D component.
   *
   * @param editor editor of source file the component belongs to
   */
  public MockChartData2D(SimpleEditor editor) {
    super(editor, TYPE, images.tinyDB());

    // Initialize dataFileColumns to default values.
    // Since ChartData2D consists of 2 entries,
    // the List is initialized with 2 entries.
    dataFileColumns = Arrays.asList("", "");
  }
}
