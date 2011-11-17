// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 */
public class WorkspaceUtilsTest extends TestCase {

  public void testParseFormProperties() throws Exception {
    final String scmProperties =
        "#|\n" +
        "$JSON\n" +
        "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
        "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
        "{\"$Name\":\"Button1\",\"$Type\":\"Button\",\"Uuid\":\"123\",\"Text\":\"Button1\"," +
        "\"Width\":\"80\"}," +
        "{\"$Name\":\"Label1\",\"$Type\":\"Label\",\"Uuid\":\"-456\",\"Text\":\"Label1\"}" +
        "]}}\n" +
        "|#";

    JSONObject o = WorkspaceUtils.parseFormProperties(scmProperties);
    assertEquals("Form", o.getString("Source"));
    JSONObject properties = o.getJSONObject("Properties");
    assertEquals("Screen1", properties.getString("$Name"));
    assertEquals("Form", properties.getString("$Type"));
    assertEquals("0", properties.getString("Uuid"));
    assertEquals("Screen1", properties.getString("Title"));
    assertTrue(properties.has("$Components"));
    JSONArray components = properties.getJSONArray("$Components");
    assertEquals(2, components.length());
    JSONObject component0 = components.getJSONObject(0);
    assertEquals("Button1", component0.getString("$Name"));
    assertEquals("Button", component0.getString("$Type"));
    assertEquals("123", component0.getString("Uuid"));
    assertEquals("Button1", component0.getString("Text"));
    assertEquals("80", component0.getString("Width"));
    JSONObject component1 = components.getJSONObject(1);
    assertEquals("Label1", component1.getString("$Name"));
    assertEquals("Label", component1.getString("$Type"));
    assertEquals("-456", component1.getString("Uuid"));
    assertEquals("Label1", component1.getString("Text"));
  }

  public void testParseFormPropertiesWithBraces() throws Exception {
    final String scmProperties =
        "#|\n" +
        "$JSON\n" +
        "{\"YaVersion\":\"39\",\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\"," +
        "\"$Type\":\"Form\",\"$Version\":\"5\",\"Uuid\":\"0\",\"Title\":\"{Screen1}\"}}\n" +
        "|#";
    JSONObject o = WorkspaceUtils.parseFormProperties(scmProperties);
    assertEquals("39", o.getString("YaVersion"));
    assertEquals("Form", o.getString("Source"));
    JSONObject properties = o.getJSONObject("Properties");
    assertEquals("Screen1", properties.getString("$Name"));
    assertEquals("Form", properties.getString("$Type"));
    assertEquals("5", properties.getString("$Version"));
    assertEquals("0", properties.getString("Uuid"));
    assertEquals("{Screen1}", properties.getString("Title"));
  }

  public void testParseFormPropertiesWithBrackets() throws Exception {
    final String scmProperties =
        "#|\n" +
        "$JSON\n" +
        "{\"YaVersion\":\"39\",\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\"," +
        "\"$Type\":\"Form\",\"$Version\":\"5\",\"Uuid\":\"0\",\"Title\":\"[Screen1]\"}}\n" +
        "|#";
    JSONObject o = WorkspaceUtils.parseFormProperties(scmProperties);
    assertEquals("39", o.getString("YaVersion"));
    assertEquals("Form", o.getString("Source"));
    JSONObject properties = o.getJSONObject("Properties");
    assertEquals("Screen1", properties.getString("$Name"));
    assertEquals("Form", properties.getString("$Type"));
    assertEquals("5", properties.getString("$Version"));
    assertEquals("0", properties.getString("Uuid"));
    assertEquals("[Screen1]", properties.getString("Title"));
  }

  public void testParseFormPropertiesWithNumberSignWithinBrackets() throws Exception {
    final String scmProperties =
        "#|\n" +
        "$JSON\n" +
        "{\"YaVersion\":\"39\",\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\"," +
        "\"$Type\":\"Form\",\"$Version\":\"5\",\"Uuid\":\"0\",\"Title\":\"[Game #1]\"}}\n" +
        "|#";
    JSONObject o = WorkspaceUtils.parseFormProperties(scmProperties);
    assertEquals("39", o.getString("YaVersion"));
    assertEquals("Form", o.getString("Source"));
    JSONObject properties = o.getJSONObject("Properties");
    assertEquals("Screen1", properties.getString("$Name"));
    assertEquals("Form", properties.getString("$Type"));
    assertEquals("5", properties.getString("$Version"));
    assertEquals("0", properties.getString("Uuid"));
    assertEquals("[Game #1]", properties.getString("Title"));
  }
}
