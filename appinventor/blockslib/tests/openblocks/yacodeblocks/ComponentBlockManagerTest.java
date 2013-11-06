// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
// All Rights Reserved.

package openblocks.yacodeblocks;

import openblocks.codeblocks.Block;

import junit.framework.TestCase;

import openblocks.renderable.RenderableBlock;

import openblocks.workspace.Workspace;

import java.util.Set;


public class ComponentBlockManagerTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    FeedbackReporter.testingMode = true;
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testInitComponentNames() throws Exception {
    final String myGenuses =
      "<BlockGenus name=\"comp1\" initlabel=\"comp1\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "<LangSpecProperty key=\"ya-kind\" value=\"component\" />" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"noncomp1\" initlabel=\"noncomp1\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"comp2\" initlabel=\"comp2\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "<LangSpecProperty key=\"ya-kind\" value=\"component\" />" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"comp3\" initlabel=\"comp3\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "<LangSpecProperty key=\"ya-kind\" value=\"component\" />" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"comp4\" initlabel=\"comp4\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "<LangSpecProperty key=\"ya-kind\" value=\"component\" />" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"comp5\" initlabel=\"comp5\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "<LangSpecProperty key=\"ya-kind\" value=\"component\" />" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"noncomp2\" initlabel=\"noncomp2\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "</LangSpecProperties>" +
      "</BlockGenus>" +
      "<BlockGenus name=\"noncomp3\" initlabel=\"noncomp3\" kind=\"procedure\" color=\"\">" +
      "<LangSpecProperties>" +
      "</LangSpecProperties>" +
      "</BlockGenus>";

    TestUtils.setupWorkspace(myGenuses, null);
    Set<String> genuses = TestUtils.getComponentBlockManager().getComponentGenuses();
    assertEquals(5, genuses.size());
    assertTrue(genuses.contains("comp1"));
    assertTrue(genuses.contains("comp2"));
    assertTrue(genuses.contains("comp3"));
    assertTrue(genuses.contains("comp4"));
    assertTrue(genuses.contains("comp5"));
    assertFalse(genuses.contains("noncomp1"));
    assertFalse(genuses.contains("noncomp2"));
    assertFalse(genuses.contains("noncomp3"));
  }

  // Test the following case:
  // User has a button named MyComponent. With codeblocks window closed,
  // the user removes the MyComponent button and then adds another component
  // named MyComponent. The next time codeblocks opens and tries to sync
  // the properties it should remove the old MyComponent and properly create
  // the new MyComponent
  public void testReuseComponentNameNewComponent() throws Exception {
    final String initProperties =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
      "{\"$Name\":\"MyComponent\",\"$Type\":\"Button\",\"Uuid\":\"123\"," +
      "\"Text\":\"Text for Button1\"}" +
      "]}}\n" +
      "|#";
    final String initBlocks =
      "<!DOCTYPE YACodeBlocks SYSTEM \"/yacodeblocks/support/save_format.dtd\">\n" +
      "<YACodeBlocks ya-version=\"1\" lang-version=\"1\">\n" +
      "<VersionSpecs>\n" +
      "<VersionSpec key=\"providesCoercion\" value=\"true\"></VersionSpec>\n" +
      "<VersionSpec key=\"explicitResults\" value=\"true\"></VersionSpec>\n" +
      "<VersionSpec key=\"noComponentBlocks\" value=\"true\"></VersionSpec>\n" +
      "</VersionSpecs>\n" +
      "<Pages><Page page-name=\" \" page-color=\"255 255 255\" page-width=\"3840\" " +
      "page-infullview=\"yes\" page-drawer=\"My Definitions\" >\n" +
      "</Page>\n" +
      "\n" +
      "</Pages><YoungAndroidMaps><YoungAndroidUuidMap>\n" +
      "<YoungAndroidUuidEntry uuid=\"123\" component-id=\"MyComponent\" " +
      "component-genus=\"Button\" ></YoungAndroidUuidEntry>\n" +
      "</YoungAndroidUuidMap>\n" +
      "</YoungAndroidMaps>\n" +
      "</YACodeBlocks>\n" +
      "\n";
    final String changedProperties =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
      "{\"$Name\":\"MyComponent\",\"$Type\":\"Label\",\"Uuid\":\"456\"," +
      "\"Text\":\"Text for Label1\"}" +
      "]}}\n" +
      "|#";

    TestUtils.setupWorkspace(null, null);
    ComponentBlockManager cbm = TestUtils.getComponentBlockManager();

    assertTrue(cbm.syncFromJson(WorkspaceUtils.parseFormProperties(initProperties)));

    Workspace.getInstance().reset();
    cbm.reset();

    BlockSaveFile blockSaveFile = new BlockSaveFile(TestUtils.getLangDefRoot(), initBlocks);
    assertTrue(cbm.loadComponents(blockSaveFile));
    assertTrue(cbm.syncFromJson(WorkspaceUtils.parseFormProperties(changedProperties)));
  }

  // Test the case where existing component X is deleted and existing component
  // Y is renamed to X.
  public void testReuseComponentNameOldComponent() throws Exception {
    final String initProperties =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
      "{\"$Name\":\"X\",\"$Type\":\"Button\",\"Uuid\":\"123\",\"Text\":\"Text for Button1\"}," +
      "{\"$Name\":\"Y\",\"$Type\":\"Button\",\"Uuid\":\"456\",\"Text\":\"Text for Button2\"}" +
      "]}}\n" +
      "|#";
    final String initBlocks =
      "<!DOCTYPE YACodeBlocks SYSTEM \"/yacodeblocks/support/save_format.dtd\">\n" +
      "<YACodeBlocks ya-version=\"1\" lang-version=\"1\">\n" +
      "<VersionSpecs>\n" +
      "<VersionSpec key=\"providesCoercion\" value=\"true\"></VersionSpec>\n" +
      "<VersionSpec key=\"explicitResults\" value=\"true\"></VersionSpec>\n" +
      "<VersionSpec key=\"noComponentBlocks\" value=\"true\"></VersionSpec>\n" +
      "</VersionSpecs>\n" +
      "<Pages><Page page-name=\" \" page-color=\"255 255 255\" page-width=\"3840\" " +
      "page-infullview=\"yes\" page-drawer=\"My Definitions\" >\n" +
      "</Page>\n" +
      "\n" +
      "</Pages><YoungAndroidMaps><YoungAndroidUuidMap>\n" +
      "<YoungAndroidUuidEntry uuid=\"123\" component-id=\"X\" " +
      "component-genus=\"Button\" ></YoungAndroidUuidEntry>\n" +
      "<YoungAndroidUuidEntry uuid=\"456\" component-id=\"Y\" " +
      "component-genus=\"Button\" ></YoungAndroidUuidEntry>\n" +
      "</YoungAndroidUuidMap>\n" +
      "</YoungAndroidMaps>\n" +
      "</YACodeBlocks>\n" +
      "\n";
    final String changedProperties =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
      "{\"$Name\":\"X\",\"$Type\":\"Button\",\"Uuid\":\"456\",\"Text\":\"Text for Button2\"}" +
      "]}}\n" +
      "|#";

    TestUtils.setupWorkspace(null, null);
    ComponentBlockManager cbm = TestUtils.getComponentBlockManager();

    assertTrue(cbm.syncFromJson(WorkspaceUtils.parseFormProperties(initProperties)));

    Workspace.getInstance().reset();
    cbm.reset();

    BlockSaveFile blockSaveFile = new BlockSaveFile(TestUtils.getLangDefRoot(), initBlocks);
    assertTrue(cbm.loadComponents(blockSaveFile));
    assertTrue(cbm.syncFromJson(WorkspaceUtils.parseFormProperties(changedProperties)));
  }

  // Test the case where two components swap names after the initial load
  public void testSwapComponentNames() throws Exception {
    final String initProperties =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
      "{\"$Name\":\"MyComponent\"," +
      "\"$Type\":\"Button\",\"Uuid\":\"123\",\"Text\":\"Text for Button1\"}," +
      "{\"$Name\":\"YourComponent\",\"$Type\":\"Button\",\"Uuid\":\"456\"," +
      "\"Text\":\"Text for Button2\"}" +
      "]}}\n" +
      "|#";
    final String initBlocks =
      "<!DOCTYPE YACodeBlocks SYSTEM \"/yacodeblocks/support/save_format.dtd\">\n" +
      "<YACodeBlocks ya-version=\"1\" lang-version=\"1\">\n" +
      "<VersionSpecs>\n" +
      "<VersionSpec key=\"providesCoercion\" value=\"true\"></VersionSpec>\n" +
      "<VersionSpec key=\"explicitResults\" value=\"true\"></VersionSpec>\n" +
      "<VersionSpec key=\"noComponentBlocks\" value=\"true\"></VersionSpec>\n" +
      "</VersionSpecs>\n" +
      "<Pages><Page page-name=\" \" page-color=\"255 255 255\" page-width=\"3840\" " +
      "page-infullview=\"yes\" page-drawer=\"My Definitions\" >\n" +
      "</Page>\n" +
      "\n" +
      "</Pages><YoungAndroidMaps><YoungAndroidUuidMap>\n" +
      "<YoungAndroidUuidEntry uuid=\"123\" component-id=\"MyComponent\" " +
      "component-genus=\"Button\" ></YoungAndroidUuidEntry>\n" +
      "<YoungAndroidUuidEntry uuid=\"456\" component-id=\"YourComponent\" " +
      "component-genus=\"Button\" ></YoungAndroidUuidEntry>\n" +
      "</YoungAndroidUuidMap>\n" +
      "</YoungAndroidMaps>\n" +
      "</YACodeBlocks>\n" +
      "\n";
    final String changedProperties =
      "#|\n" +
      "$JSON\n" +
      "{\"Source\":\"Form\",\"Properties\":{\"$Name\":\"Screen1\",\"$Type\":\"Form\"," +
      "\"Uuid\":\"0\",\"Title\":\"Screen1\",\"$Components\":[" +
      "{\"$Name\":\"YourComponent\"," +
      "\"$Type\":\"Button\",\"Uuid\":\"123\",\"Text\":\"Text for Button1\"}," +
      "{\"$Name\":\"MyComponent\",\"$Type\":\"Button\",\"Uuid\":\"456\"," +
      "\"Text\":\"Text for Button2\"}" +
      "]}}\n" +
      "|#";

    TestUtils.setupWorkspace(null, null);
    ComponentBlockManager cbm = TestUtils.getComponentBlockManager();
    assertTrue(cbm.syncFromJson(WorkspaceUtils.parseFormProperties(initProperties)));
    Workspace.getInstance().reset();
    cbm.reset();

    BlockSaveFile blockSaveFile = new BlockSaveFile(TestUtils.getLangDefRoot(), initBlocks);
    assertTrue(cbm.loadComponents(blockSaveFile));
    assertTrue(cbm.syncFromJson(WorkspaceUtils.parseFormProperties(changedProperties)));
  }

}
