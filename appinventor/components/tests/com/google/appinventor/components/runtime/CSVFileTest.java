package com.google.appinventor.components.runtime;

import org.junit.Before;

import static org.junit.Assert.*;

public class CSVFileTest extends FileTestBase {
  protected CSVFile csvFile;

  @Before
  public void setUp() {
    super.setUp();
    csvFile = new CSVFile(getForm());
  }
}