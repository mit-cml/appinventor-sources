// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.appinventor.yailgenerator;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.ComplaintDepartment;
import openblocks.codeblockutil.AIDirectory;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.Workspace;
import openblocks.yacodeblocks.AndroidController;
import openblocks.yacodeblocks.BlockSaveFile;
import openblocks.yacodeblocks.ComponentBlockManager;
import openblocks.yacodeblocks.IWorkspaceController;
import openblocks.yacodeblocks.PhoneCommManager;
import openblocks.yacodeblocks.ProcedureBlockManager;
import openblocks.yacodeblocks.NoProjectException;
import openblocks.yacodeblocks.WorkspaceControllerHolder;
import openblocks.yacodeblocks.WorkspaceUtils;
import openblocks.yacodeblocks.YABlockCompiler;
import openblocks.yacodeblocks.YailGenerationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * YailGenerator is used to generate YAIL without having to open codeblocks.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class YailGenerator implements IWorkspaceController {
  // Default character encoding
  private static final String DEFAULT_CHARSET = "Cp1252";

  private static final Object generateYailLock = new Object();

  private final Workspace workspace;
  private final ComponentBlockManager cbm;
  private final ProcedureBlockManager pbm;

  private volatile Element langDefRoot;

  private volatile boolean loadingBlocks;
  private volatile boolean projectLoaded;

  /**
   * Entry point for YailGenerator binary.
   * Command-line argumesnt:
   * <ol>
   * <li>the path of a file containing the form properties source</li>
   * <li>the path of a file containing the codeblocks source</li>
   * <li>the yail path</li>
   * </ol>
   *
   * <p>The generated YAIL is printed to stdout.</p>
   */
  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println("YailGenerator error - expected exactly 3 command line arguments");
      System.exit(-1);
    }

    // Save the original System.out and System.err and redirect output from codeblocks.
    PrintStream saveSystemOut = System.out;
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    PrintStream saveSystemErr = System.err;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));

    try {
      String formPropertiesSource = Files.toString(new File(args[0]),
          Charset.forName(DEFAULT_CHARSET));
      String codeblocksSource = Files.toString(new File(args[1]),
          Charset.forName(DEFAULT_CHARSET));
      String yailPath = args[2];

      try {
        String yail = generateYail(formPropertiesSource, codeblocksSource,
            yailPath);
        saveSystemOut.print(yail);
        System.exit(0);
      } catch (YailGenerationException e) {
        saveSystemErr.println(e.getMessage());
        System.exit(1);
      }

    } catch (Throwable e) {
      e.printStackTrace(saveSystemErr);
      System.exit(-1);
    }
  }

  public static String generateYail(
      String formPropertiesSource, String codeblocksSource, String yailPath)
      throws YailGenerationException {
    // Currently when YailGenerator is executed as a separate process, this method is called only
    // once. However, in tests (see YailGeneratorTest.java), it may called multiple times in
    // parallel.

    // Give the WorkspaceControllerHolder a factory that will create a YailGenerator.
    // This ensures that only one workspace controller will be created and that it will be the
    // appropriate implementation: YailGenerator.
    IWorkspaceController.Factory factory = new IWorkspaceController.Factory() {
      @Override
      public IWorkspaceController create() {
        return new YailGenerator();
      }
    };
    WorkspaceControllerHolder.setFactory(factory, true);  // headless
    YailGenerator yailGenerator = (YailGenerator) WorkspaceControllerHolder.get();

    return yailGenerator.loadBlocksAndGenerateYail(formPropertiesSource, codeblocksSource,
        yailPath);
  }

  private YailGenerator() {
    workspace = Workspace.getInstance();
    cbm = new ComponentBlockManager(workspace, this);
    pbm = new ProcedureBlockManager(workspace);
  }

  private String loadBlocksAndGenerateYail(String formPropertiesSource, String codeblocksSource,
      String yailPath) throws YailGenerationException {
    // Currently when YailGenerator is executed as a separate process, this method is called only
    // once. However, in tests (see YailGeneratorTest.java), it may called multiple times in
    // parallel. We use synchronized here to handle (prevent) concurreny.
    synchronized (generateYailLock) {
      try {

        // The first time this method is called, langDefRoot will be null.
        if (langDefRoot == null) {
          try {
            langDefRoot = WorkspaceUtils.loadLangDef();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          WorkspaceUtils.resetLanguage();
          WorkspaceUtils.loadLanguage(langDefRoot);
          // The following is required to load the "My Definitions" page/drawer.
          loadBlocks(null);
        }

        JSONObject formProperties = WorkspaceUtils.parseFormProperties(formPropertiesSource);
        if (formProperties == null) {
          throw new YailGenerationException("Unable to generate code.");
        }

        String formName;
        try {
          formName = formProperties.getJSONObject("Properties").getString("$Name");
        } catch (JSONException e) {
          throw new YailGenerationException("Unable to generate code.");
        }

        workspace.reset();

        if (!codeblocksSource.isEmpty()) {
          BlockSaveFile blockSaveFile = new BlockSaveFile(langDefRoot, codeblocksSource);
          loadBlocks(blockSaveFile.getRoot());
          if (!cbm.loadComponents(blockSaveFile)) {
            throw new YailGenerationException("Unable to generate code for " + formName + ".");
          }
        }

        if (!cbm.syncFromJson(formProperties)) {
          throw new YailGenerationException("Unable to generate code for " + formName + ".");
        }

        projectLoaded = true;

        if (containsBadBlocks()) {
          throw new YailGenerationException("There are bad blocks in " + formName + ".");
        }

        ComplaintDepartment.clearComplaints();

        HashMap<String, ArrayList<RenderableBlock>> componentMap =
            new HashMap<String, ArrayList<RenderableBlock>>();
        Map<Block, String> warnings = new HashMap<Block, String>();
        List<String> errors = new ArrayList<String>();
        WorkspaceUtils.populateComponentMap(componentMap, warnings, errors,
            false, // not for REPL
            false, // don't compile unattached blocks
            cbm);

        String code = YABlockCompiler.generateYailForProject(formProperties, componentMap,
            false); // not for REPL
        String[] compileErrors = ComplaintDepartment.getCompileErrors();
        if (compileErrors.length > 0) {
          String errorMessages = Joiner.on("\n").join(compileErrors);
          throw new YailGenerationException(
              "There are errors that must be fixed in " + formName + ".\n" +
              errorMessages);
        }

        StringBuilder yail = new StringBuilder();
        yail.append(YABlockCompiler.getYailPrelude(yailPath, formName));
        yail.append(code).append("\n");
        return yail.toString();

      } finally {
        projectLoaded = false;
        workspace.reset();
        cbm.reset();
        pbm.reset();
      }
    }
  }

  private void loadBlocks(Element blocksRoot) {
    loadingBlocks = true;
    try {
      workspace.loadWorkspaceFrom(blocksRoot, langDefRoot);
    } finally {
      loadingBlocks = false;
    }
  }

  private boolean containsBadBlocks() {
    for (Block block : Block.getAllBlocks()) {
      if (block.isBad()) {
        return true;
      }
    }
    return false;
  }

  // IWorkspaceController implementation

  @Override
  public boolean isLoadingBlocks() {
    return loadingBlocks;
  }

  @Override
  public boolean haveProject() {
    return projectLoaded;
  }

  @Override
  public ComponentBlockManager getComponentBlockManager() {
    return cbm;
  }

  @Override
  public void componentRenamed(String oldName, String newName) {
    // Nothing to do.
  }

  @Override
  public void componentRemoved(String name) {
    // Nothing to do.
  }

  @Override
  public void mouseMovedOnCanvas(MouseEvent e) {
    // Nothing to do.
  }

  @Override
  public PhoneCommManager getPhoneCommManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AndroidController getAndroidController() {
    throw new UnsupportedOperationException();
  }

  @Override
  public AIDirectory getAIDir() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getProjectDefinitionsForRepl()
      throws IOException, YailGenerationException, NoProjectException {
    throw new UnsupportedOperationException();
  }
}
