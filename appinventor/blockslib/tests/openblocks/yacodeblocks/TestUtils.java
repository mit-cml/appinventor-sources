package openblocks.yacodeblocks;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility methods for testing Codeblocks.
 * Maintains static state since Codeblocks has static state.
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class TestUtils extends com.google.appinventor.common.testutils.TestUtils {
  public static final String TESTING_SOURCE_PATH = TestUtils.APP_INVENTOR_ROOT_DIR +
      "/blockslib/tests/openblocks/yacodeblocks/testing_source_files/";

  private static WorkspaceController controller;
  private static Element langDefRoot;
  private static ComponentBlockManager cbm;



  private static final String langDefPrefix =
      // note: the lang def path will not be used, but it needs to match "*lang_def.dtd"
      "<!DOCTYPE BlockLangDef SYSTEM \"ignore_lang_def.dtd\">" +
      "<BlockLangDef>" +
      "  <BlockConnectorShapes>" +
      "    <BlockConnectorShape shape-type=\"poly\" shape-number=\"10\"/>" +
      "    <BlockConnectorShape shape-type=\"cmd\" shape-number=\"14\"/>" +
      "  </BlockConnectorShapes>" +
      "  <BlockColors>" +
      "    <BlockColor name=\"tan\" rgb-value=\"255 200 150\"/>\n" +
      "    <BlockColor name=\"blue\" rgb-value=\"107 144 218\"/>\n" +
      "    <BlockColor name=\"red\" rgb-value=\"153 0 0\"/>\n" +
      "    <BlockColor name=\"dark-blue\" rgb-value=\"0 0 136\"/>\n" +
      "    <BlockColor name=\"green\" rgb-value=\"0 128 0\"/>\n" +
      "    <BlockColor name=\"purple\" rgb-value=\"102 51 153\"/>\n" +
      "    <BlockColor name=\"brown\" rgb-value=\"153 102 51\"/>\n" +
      "    <BlockColor name=\"orange\" rgb-value=\"192 128 64\"/>\n" +
      "    <BlockColor name=\"grey\" rgb-value=\"187 187 187\"/>\n" +
      "  </BlockColors>" +
      "  <BlockGenuses>";
  private static final String langDefSuffix =
      "  </BlockGenuses>" +
      "</BlockLangDef>";

  private TestUtils() {}

  public static WorkspaceController getController() {
    return controller;
  }

  public static ComponentBlockManager getComponentBlockManager() {
    return cbm;
  }

  /**
   * Set up a codeblocks workspace. Resets the controller and creates a new
   * ComponentBlockManager, assigning it to cbm.
   * @param genusString The section of the lang def file defining genuses
   * @param props  If non-null, a JSON properties section from the form properties file.
   */
  public static void setupWorkspace(String genusString, String props) throws LoadException,
      IOException, ParserConfigurationException, SAXException {
    FeedbackReporter.testingMode = true;
    AutoSaver.testingMode = true;

    // Give the WorkspaceControllerHolder a factory that will create a WorkspaceController.
    // This ensures that only one workspace controller will be created and that it will be the
    // appropriate implementation: WorkspaceController.
    IWorkspaceController.Factory factory = new IWorkspaceController.Factory() {
      @Override
      public IWorkspaceController create() {
        return new WorkspaceController();
      }
    };
    WorkspaceControllerHolder.setFactory(factory, true);  // headless so tests can run on forge
    controller = (WorkspaceController) WorkspaceControllerHolder.get();

    controller.setPhoneCommManager(new PhoneCommManager());  // in case a previous test set it
    cbm = controller.getComponentBlockManager();
    if (genusString == null) {
      langDefRoot = WorkspaceUtils.loadLangDef();
    } else {
      String langDef = langDefPrefix + genusString + langDefSuffix;
      InputStream is = new ByteArrayInputStream(langDef.getBytes());
      langDefRoot = WorkspaceUtils.loadLangDef(is);
    }

    controller.loadFreshWorkspace("", langDefRoot);
    if (props != null) {
      controller.loadProperties(props);
    }
  }

  public static Element getLangDefRoot() {
    return langDefRoot;
  }

  /**
   * Read the contents of a File as a String
   * @param path the path to the File
   */
  public static String getFileAsString(String path) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(path));
      StringBuilder sb = new StringBuilder();
      String line;
      try {
        while ((line = reader.readLine()) != null) {
          sb.append(line + "\n");
        }
        return sb.toString();
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          reader.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

}
