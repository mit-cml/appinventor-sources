// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Element;

import openblocks.codeblocks.Block;
import openblocks.codeblocks.BlockStub;
import openblocks.codeblocks.ComplaintDepartment;
import openblocks.codeblockutil.AIDirectory;
import openblocks.codeblockutil.CDeviceSelector;
import openblocks.codeblockutil.CEmulatorButton;
import openblocks.codeblockutil.CHeader;
import openblocks.codeblockutil.CSaveButton;
import openblocks.codeblockutil.PhoneCommIndicator;
import openblocks.renderable.RenderableBlock;
import openblocks.workspace.Page;
import openblocks.workspace.TrashCan;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.workspace.WorkspaceListener;
import openblocks.workspace.ZoomSlider;
import openblocks.workspace.typeblocking.TypeBlockManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Modified from generic codeblocks WorkspaceController to be specific to Young Android
 * @author sharon@google.com (Sharon Perl)
 */
// TODO(user) Change this so that the methods are not synchronized with the synchronized
// keyword else other classes could cause deadlocks by synchronizing on the
// WorkspaceController instance.
public class WorkspaceController implements IWorkspaceController, WorkspaceListener {
  private static final boolean DEBUG = false;

  // Save button constants
  private static final int BUTTON_WIDTH = 70;
  private static final int BUTTON_HEIGHT = 24;

  // if an external program is controlling yacodeblocks (e.g., via JavaWebStart),
  // we give it a handle on the WorkspaceController. We use the externalController
  // to save source code and generated code as well.
  private ExternalController externalController = null;

  private String codeblocksSourceSavePath = null;

  private final SaverSynchronizer saverSynchronizer = new SaverSynchronizer();

  private final AutoSaver autoSaver;

  private Element langDefRoot;

  private static final String NO_SAVE_WARNING = "\nThe project will not be saved if you do not " +
                "modify any blocks.\nPlease backup your project before continuing.";

  //flags
  private boolean isWorkspacePanelInitialized = false;
  private volatile boolean loadingBlocks = false;  // This seems a long way from
                                                   // ComponentBlockMangager which
                                                   // uses it, so volatilize.
  private volatile boolean componentRemovedOrRenamedDuringLoad;

  protected JFrame topFrame;
  protected JPanel workspacePanel;
  private final CSaveButton saveButton;
  private final CSaveButton undoButton;
  private final CSaveButton redoButton;
  private final CEmulatorButton newEmulatorButton;
  private final CDeviceSelector deviceSelector;
  private final PhoneCommIndicator commIndicator;


  //flag to indicate if a workspace has been loaded/initialized
  private boolean workspaceLoaded = false;

  private ZoomSlider zoomSlider;
  private ComponentBlockManager cbm;
  private ProcedureBlockManager pbm;

  // This is the phone communications manager for this workspace
  private PhoneCommManager pcm;

  private AndroidController androidController;

  private final AIDirectory aiDir;

  public static final String REPL_DO_IT = "Do It";
  public static final String REPL_DEFINE_IT = "Define It";
  // A BlockParser that knows its working for the Repl
  private final BlockParser blockParser = new BlockParser(true);
  // Used by mouseMovedOnCanvas to determine wandering from Done block.
  private Block doItBlock = null; // the most recent block to execute a Do It
  private Rectangle doItBlockRegion = null;

  private CHeader headerPane;

  // projectLoaded is true iff we currently have a project successfully loaded.
  // affects state of the repl comm button. Should only be accessed from
  // the UI thread because access isn't synchronized. When we change this
  // we need to call pcm.updateStatusIndicators to adjust the repl button and
  // phone status icon.
  private boolean projectLoaded = false;
  private String currentProjectName = "";
  private String currentFormName = "";

  public WorkspaceController() {
    Workspace workspace = getWorkspaceInstance();
    initLookAndFeel();

    aiDir = new AIDirectory();
    cbm = new ComponentBlockManager(workspace, this);
    pcm = new PhoneCommManager();
    pbm = new ProcedureBlockManager(workspace);
    saveButton = new CSaveButton("Save", "Saved");
    undoButton = new CSaveButton("Undo", "Undo");
    redoButton = new CSaveButton("Redo", "Redo");
    newEmulatorButton = new CEmulatorButton();
    autoSaver = new AutoSaver(this, saveButton, undoButton, redoButton);
    commIndicator = new PhoneCommIndicator();
    deviceSelector = new CDeviceSelector();
    workspace.addWorkspaceListener(autoSaver);
    workspace.addWorkspaceListener(cbm);
    workspace.addWorkspaceListener(pbm);
    workspace.addWorkspaceListener(this);
  }

  /*
   * Sets up UI to use MetalLookAndFeel. Also, adds Mac key bindings for
   * cut/paste/etc.
   */
  private void initLookAndFeel() {
    // The Java UI on OS X using the default look and feel
    // does not let you change the background color on buttons.
    // Set the Look and Feel before creating UI components.
    // TODO(sharon): Note that we explicitly use the MetalLookAndFeel because that is
    // what UIManager.getCrossPlatformLookAndFeelClassName() returns at the
    // time this code was written. If that changed, we might want to consider
    // changing this code. Also, overriding initComponentDefaults is a bit
    // of a hack. It isn't clear what the "right way" is to get the additional
    // key bindings into all components that need them, but this works.
    LookAndFeel laf = new javax.swing.plaf.metal.MetalLookAndFeel() {
      @Override
      protected void initComponentDefaults(UIDefaults table) {
        super.initComponentDefaults(table);
        addMacKeyBindings(table);
      }
    };
    try {
      UIManager.setLookAndFeel(laf);
    } catch (UnsupportedLookAndFeelException e) {
      System.out.println("Setting look & feel got exception: " + e.getMessage());
    }
  }

  private static final Pattern ctrlPat = Pattern.compile("\\bctrl\\b");

  /*
   * Finds all Ctrl-key KeyStrokes found within InputMaps in uiDefaults and adds
   * Command-key equivalents (for benefit of Mac users). We leave both Ctrl-key
   * and Meta-key bindings on all platforms.
   *
   * This code is copied from
   * http://lists.apple.com/archives/Java-dev/2008/Apr/msg00209.html
   */
  public static void addMacKeyBindings(UIDefaults uiDefaults) {
    Object[] keys = uiDefaults.keySet().toArray(); // Copied to prevent concurrent modification issues.

    for (Object key : keys) {
      Object  value = uiDefaults.get(key);
      if (value instanceof InputMap) {
        InputMap map = (InputMap) value;
        KeyStroke[] keyStrokes = map.keys();
        if (keyStrokes != null) {
          for (KeyStroke keyStroke : keyStrokes) {
            String  keyString = keyStroke.toString();
            if (keyString.indexOf("ctrl ") >= 0) {
              Object  action = map.get(keyStroke);  // action for ctrl key stroke
              keyString = ctrlPat.matcher(keyString).replaceAll("meta");
              keyStroke = KeyStroke.getKeyStroke(keyString); // corresponding meta key stroke
              Object oldAction = map.get(keyStroke); // existing action for meta key stroke
              if (oldAction != null) {
                // just to be safe, don't replace existing bindings!
                System.out.println("  Warning: not replacing META key binding for "
                    + keyString + " which is already bound to action "
                    + getActionName(oldAction));
              } else {
                map.put(keyStroke, action);
              }
            }
          }
        }
      }
    }
  }

  private static String getActionName(Object o) {
    if (o instanceof Action) {
      return ((Action)o).getValue(Action.NAME).toString();
    }
    return o.toString();
  }

  /**
   * Returns the one <code>Workspace</code> instance
   * @return the one <code>Workspace</code> instance
   */
  private Workspace getWorkspaceInstance() {
    return Workspace.getInstance();
  }

  /**
   * returns the AIDirectory manager for this workspace
   * @return the AIDirectory manager for this workspace
   */
  @Override
  public AIDirectory getAIDir() {
    return aiDir;
  }

  @Override
  public AndroidController getAndroidController() {
    return androidController;
  }

  /**
   * Returns the <code>ComponentBlockManager</code> for this
   * <code>WorkspaceController</code>
   * @return the <code>ComponentBlockManager</code> for this
   * <code>WorkspaceController</code>
   */
  @Override
  public ComponentBlockManager getComponentBlockManager() {
    return cbm;
  }

  /**
   * Returns the <code>ProcedureBlockManager</code> for this
   * <code>WorkspaceController</code>
   * @return the <code>ProcedureBlockManager</code> for this
   * <code>WorkspaceController</code>
   */
  public ProcedureBlockManager getProcedureBlockManager() {
    return pbm;
  }

  /**
   * Returns the <code>PhoneCommManager</code> for this
   * <code>WorkspaceController</code>
   */
  @Override
  public PhoneCommManager getPhoneCommManager() {
    return pcm;
   }

  /**
   * Provides a way for us to give the workspace controller a handle on
   * a PhoneCommManager. Mainly for use in testing when startCodeblocks
   * is not being called
   */

  public synchronized void setPhoneCommManager(PhoneCommManager phoneMgr) {
    pcm = phoneMgr;
  }

  /*
   *@return loadingBlocks for benefit of Workspace listeners wondering if
   *         changes are incremental.
   */
  @Override
  public boolean isLoadingBlocks() {
    return loadingBlocks;
  }

  //////////////////////////
  //  LANGUAGE DEFINITION //
  //////////////////////////
  /**
   * Loads all the block genuses, properties, and link rules of
   * a language specified in the pre-defined language def file.
   * @param languageDefinition the root element of the lang def file.
   */
  private void loadNewBlockLanguage(Element languageDefinition){
    langDefRoot = languageDefinition;
    WorkspaceUtils.resetLanguage();
    WorkspaceUtils.loadLanguage(langDefRoot);
  }

  ////////////////////////
  // SAVING AND LOADING //
  ////////////////////////

  /**
   * All saving of the codeblocks source file should be done with this method.
   *
   * If the workspace has changed due to user action within Codeblocks, this
   * call will save the codeblocks source file to the source save path that is
   * associated with the currently loaded project.  If the source save path is
   * null or empty, an exception will be thrown.
   *
   * If a save is unnecessary this method will return a value of false, but
   * still return normally.
   *
   * @param synchronous if true, waits until the save has happened before returning
   * @throws SaveException if the save should have occurred, but it failed
   * for some reason.
   */
  public synchronized void persistCodeblocksSourceFile(boolean synchronous) throws SaveException {
    // There's no point trying to persist anything if there's no place to put it, unless we really
    // insist.
    if (codeblocksSourceSavePath == null || codeblocksSourceSavePath.length() == 0) {
      return;
    }
    if (autoSaver.getWorkspaceChanged()) {
      // This always happens on the Event Dispatch thread.
      final CodeblocksSourceOutput sourceOutputInfo = takeSnapshot(true /* tell autosaver */, false /* don't synchronize */);

      Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
          if (DEBUG) {
            System.out.println("********* Actually invoking save to server");
          }

          try {
            long startTime = 0;
            if (DEBUG) {
              startTime = System.nanoTime();
            }
            externalController.writeCodeblocksSourceToServer(sourceOutputInfo.getPath(),
                sourceOutputInfo.getContents());
            autoSaver.onSaveSucceeded();
            if (DEBUG) {
              System.out.println("Running on " + Thread.currentThread() + "...");
              System.out.println("Writing code blocks to server took " +
                  (System.nanoTime() - startTime) / 1000000 + " milliseconds.");
            }
          } catch (IOException e) {
            autoSaver.onSaveFailed();
          }
        }
      };

      try {
        if (synchronous) {
          saverSynchronizer.saveNow(saveRunnable);
        } else {
          saverSynchronizer.saveEventually(saveRunnable);
        }
      } catch (SaveException e) {
        autoSaver.onSaveFailed();
        throw new SaveException("Source failed to write to server.");
      }
    }
  }

  // Wraper around takeSnapshot(boolean) that may or may not be synchronized based
  // on the dosync argument. We call it with false (no synchronization) if we are calling
  // from a synchornized method in this class.

  protected CodeblocksSourceOutput takeSnapshot(boolean tellAutosaver, boolean dosync)
    throws SaveException {
    if (dosync) {
      synchronized(this) {
        return takeSnapshot(tellAutosaver);
      }
    } else {
      return takeSnapshot(tellAutosaver);
    }
  }

  // Snapshot the current blocks workspace and return the save string for it,
  // along with the path for saving it on the server. tellAutosaver should be
  // true if we should let the autosaver know that we're going to save
  private CodeblocksSourceOutput takeSnapshot(final boolean tellAutosaver)
    throws SaveException {
    final StringBuilder sourcePath = new StringBuilder();
    final StringBuilder saveString = new StringBuilder();


    class SaveStringRunnable implements Runnable {
      public void run() {

        if (codeblocksSourceSavePath != null) {
          sourcePath.append(codeblocksSourceSavePath);
        }
        // TODO(markf): It seems like there's no need to generate the saveString if there's no place
        // to save it, so maybe we should just return if codeblocksSourceSavePath is null.
        saveString.append("<!DOCTYPE YACodeBlocks SYSTEM \""
            + DTDResolver.SAVE_FORMAT_DTD_FILEPATH + "\">\n");
        // append root node
        saveString.append("<YACodeBlocks ya-version=\"" +
            WorkspaceUtils.getYoungAndroidVersion(langDefRoot) + "\" lang-version=\"" +
            WorkspaceUtils.getBlocksLanguageVersion(langDefRoot) + "\">\n");
        saveString.append(getWorkspaceInstance().getSaveString());
        saveString.append(cbm.getSaveString());
        saveString.append("</YACodeBlocks>");
        // note: need to call autoSaver.onSnapshot here to ensure that
        // it is synchronized with UI operations.
        if (tellAutosaver) {
          autoSaver.onSnapshot(saveString.toString());
        }
      }
    }

    if (SwingUtilities.isEventDispatchThread()) {
      new SaveStringRunnable().run();
    } else {
      try {
        SwingUtilities.invokeAndWait(new SaveStringRunnable());
      } catch (InterruptedException e) {
        throw new SaveException("Saving was interrupted.", e);
      } catch (InvocationTargetException e) {
        throw new SaveException("Saving failed.", e);
      }
    }

    if (sourcePath.toString().equals("")) {
      throw new SaveException("No known save location exists for the current project.");
    } else {
      // TODO(sharon): how inefficient is it to call saveString.toString()
      // twice?
      return new CodeblocksSourceOutput(sourcePath.toString(), saveString.toString());
    }
  }

  /**
   * Loads a fresh workspace based on the specified language
   * definition file.  If languageDefinition is null, no new language
   * definition will be loaded.  The block canvas will have no live blocks.
   *
   * Changes made to the workspace by executing this method will not
   * trigger the autosaver.
   *
   * @param newCodeblocksSourceSavePath The path that the codeblocks
   * source should be saved to for this project.  If null or the empty
   * string, this project will not be able to be saved.
   * @param languageDefinition
   */
  public synchronized void loadFreshWorkspace(final String newCodeblocksSourceSavePath,
      final Element languageDefinition) {

    class LoadFreshWorkspaceRunnable implements Runnable {
      public void run() {
        try {
          autoSaver.clearHistory();
          autoSaver.stopListening();
          doLoadFreshWorkspace(newCodeblocksSourceSavePath, languageDefinition, true, true);
        } finally {
          autoSaver.startListening();
        }
      }
    }

    if (SwingUtilities.isEventDispatchThread()) {
      new LoadFreshWorkspaceRunnable().run();
    } else {
      try {
        SwingUtilities.invokeAndWait(new LoadFreshWorkspaceRunnable());
      } catch (InterruptedException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
      } catch (InvocationTargetException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
      }
    }

  }

  // Reset the current workspace if necessary and then load an empty
  // workspace with newCodeblocksSourceSavePath as the save path.
  private void doLoadFreshWorkspace(final String newCodeblocksSourceSavePath,
      final Element languageDefinition, boolean clearAssets, boolean resetPhone) {
    // Reset the workspace if it has previously loaded.
    if (workspaceLoaded) {
      resetWorkspace();
    }
    if (languageDefinition != null) {
      loadNewBlockLanguage(languageDefinition);
    }

    if (langDefRoot == null) {
      FeedbackReporter.showSystemErrorMessage("Error: No block language found.");
      return;
    }
    loadingBlocks = true;

    try {
      getWorkspaceInstance().loadWorkspaceFrom(null, langDefRoot);
    } finally {
      loadingBlocks = false;
    }

    // show the factory
    Workspace.getInstance().getFactoryManager().viewStaticDrawers();
    codeblocksSourceSavePath = newCodeblocksSourceSavePath;

    pcm.prepareForNewProject(clearAssets, resetPhone);

    workspaceLoaded = true;
  }

  private void resetWorkspace(){
    codeblocksSourceSavePath = null;
    //clear all pages and their drawers
    //clear all drawers and their content
    //clear all block and renderable block instances
    getWorkspaceInstance().reset();
    if(zoomSlider != null) {
      zoomSlider.reset();  // Avoid firing property change.
    } else {
      System.out.println("zoomSlider was null during start up!");
    }
    cbm.reset();
    pbm.reset();
    doItBlock = null;
    setProjectName("");
    projectLoaded = false;
    pcm.updateStatusIndicators();
  }

  /**
   * @param yailPath points to the location of the output code
   * @return the Yail with a suitable preamble.
   */
  public String wrapProjectYailForAPK(String yailPath)
      throws YailGenerationException, IOException, NoProjectException, JSONException {
    StringBuilder yail =  new StringBuilder();
    final JSONObject formProperties =
      WorkspaceUtils.parseFormProperties(externalController.getFormPropertiesForProject());
    if (formProperties == null) {
      return "";
    }
    String formName = formProperties.getJSONObject("Properties").getString("$Name");
    // The prelude can't be sent to the phone, is only for .apk file.
    yail.append(YABlockCompiler.getYailPrelude(yailPath, formName))
        .append(getYailForProject(formProperties, true, false, false));
    //TODO(user): A disconnect and reconnect of the phone
    // triggers a complete Yail generation which triggers a complete reload of
    // properties which triggers a download of all the property settings to the
    // phone, a few microseconds before we download all the yail to the phone,
    // including the property settings. I guess the cable can handle it.
    return yail.toString();
  }

  /**
   * If we're connected to the phone, generates Yail for all the definitions and
   * downloads to the phone.
   * @param formProperties contents of the form properties file
   */
  public void sendCurrentProjectDefinitionsToRepl(String formProperties) {
    if (pcm.connectedToPhone()) {
      // Download all the definitions to the phone
      try {
        String yail = getProjectDefinitionsForRepl(formProperties);
        pcm.replControllerCreateAndSendAsync(yail, PhoneCommManager.REPL_PROJECT_LOADING,
            new Long(0), true);
      } catch (YailGenerationException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
      }
    }
  }

  /**
   * If we're connected to the phone, first gets the current form properties
   * from the server and then generates Yail for all the definitions and
   * downloads to the phone.
   */
  public void sendCurrentProjectDefinitionsToRepl() {
    if (pcm.connectedToPhone()) {
      try {
        sendCurrentProjectDefinitionsToRepl(externalController.getFormPropertiesForProject());
      } catch (IOException e) {
        FeedbackReporter.showSystemErrorMessage(
            "Error trying to talk to the server. Can't update the definitions on the device");
      } catch (NoProjectException e) {
        FeedbackReporter.showErrorMessage(e.getMessage());
      }
    }
  }

  /**
   * Gets the current form properties from the server and then generates and
   * returns the yail code for all the project definitions in a format suitable
   * for feeding to the phone REPL
   */
  @Override
  public String getProjectDefinitionsForRepl() throws IOException,
      YailGenerationException, NoProjectException {
    String formProperties = externalController.getFormPropertiesForProject();
    return getProjectDefinitionsForRepl(formProperties);
  }

  /**
   * @return yail code for all the definitions in a format suitable for
   *    feeding to the REPL on a phone
   * @throws YailGenerationException
   */
  public String getProjectDefinitionsForRepl(String formProperties)
      throws YailGenerationException {
    return getYailForProject(WorkspaceUtils.parseFormProperties(formProperties),
            false, false, true);

  }

  /**
   * Returns the YAIL code for this workspace given the current set of
   * form properties.
   *
   * Changes made to the workspace by executing this method will not
   * trigger the autosaver.
   *
   * @param reloadFirst whether or not the workspace should be reloaded
   *                    with the formProperties before generating the Yail.
   * @param compileUnattachedBlocks will compile clumps that aren't definitions
   *            true only for testing
   * @param forRepl instructs BlockParser to warn less about runtime errors.
   * @return Yail
   * @throws YailGenerationException if there are compile errors.
   */
  public synchronized String getYailForProject(final JSONObject formProperties,
      final boolean reloadFirst, final boolean compileUnattachedBlocks,
      final boolean forRepl)  throws YailGenerationException {
    // build a map of components -> top level blocks for that component
    // a special entry for GLOBALS maps to top-level global definitions
    final HashMap<String, ArrayList<RenderableBlock>> componentMap =
        new HashMap<String, ArrayList<RenderableBlock>>();

    final class PageMapPopulator implements Runnable {
      public void run(){
        if (reloadFirst) {
          try {
            // TODO(sharon): would prefer to allow undo across compilations.
            // Is that possible?
            autoSaver.clearHistory();
            autoSaver.stopListening();
            doLoadFormProperties(formProperties);
          } finally {
            autoSaver.startListening();
          }
        }

        ComplaintDepartment.clearComplaints();
        Map<Block, String> warnings = new HashMap<Block, String>();
        List<String> errors = new ArrayList<String>();
        WorkspaceUtils.populateComponentMap(componentMap, warnings, errors,
            forRepl, compileUnattachedBlocks, getComponentBlockManager());
        for (Map.Entry<Block, String> entry : warnings.entrySet()) {
          Block block = entry.getKey();
          String warning = entry.getValue();
          block.postWarning(warning);
        }
        for (String error : errors) {
          FeedbackReporter.showErrorMessage(error);
        }
      }
    }

    if (SwingUtilities.isEventDispatchThread()) {
      new PageMapPopulator().run();
    } else {
      try {
        SwingUtilities.invokeAndWait(new PageMapPopulator());
      } catch (InterruptedException e) {
        e.printStackTrace();
        return "";
      } catch (InvocationTargetException e) {
        e.printStackTrace();
        return "";
      }
    }
    String code = YABlockCompiler.generateYailForProject(formProperties,
        componentMap, forRepl) + "\n";
    int hardErrors = ComplaintDepartment.showCompiletimeComplaints();
    if (hardErrors == 0) {
      return code;
    }
    throw new YailGenerationException("Can't proceed. Please fix the " +
        hardErrors + " erroneous blocks.");
  }

  /**
   * Returns the YAIL code for testing. Allows certain illegal programs
   * fragments to generate code.
   *
   * @param formProperties a JSONObject representation of the components
   * on this form and their properties.
   */
   public String testGetYail(String formProperties) throws YailGenerationException {
    return getYailForProject(WorkspaceUtils.parseFormProperties(formProperties), true, true, false);
  }

  /**
   * Returns true iff we currently have a project loaded
   */
  @Override
  public boolean haveProject() {
    if (SwingUtilities.isEventDispatchThread()) {
      return projectLoaded;
    } else {
      final boolean loaded[] = new boolean[]{ false };
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          @Override
          public void run() {
            loaded[0] = projectLoaded;
          }
        });
        return loaded[0];
      } catch (InterruptedException e) {
        System.out.println("Got exception trying to check for project loaded");
        e.printStackTrace();
        return false;
      } catch (InvocationTargetException e) {
        System.out.println("Got exception trying to check for project loaded");
        e.printStackTrace();
        return false;
      }
    }
  }


  /**
   * Used to sync new form properties that change the components for a
   * particular project.
   *
   * Changes made to the workspace by executing this method will not
   * trigger the autosaver.
   *
   * Cannot be called from the EventDispatch (UI) thread.
   *
   * Throws LoadException if any errors are detected during the load
   *
   * @param formProperties a String representation of the JSON that represents
   * the components and their properties.
   */
  public synchronized void loadProperties(final String formProperties) throws LoadException {
    final class LoadRunnable implements Runnable {
      public void run() {
        try {
          autoSaver.stopListening();
          projectLoaded = false;
          pcm.updateStatusIndicators();
          doLoadFormProperties(WorkspaceUtils.parseFormProperties(formProperties));
          projectLoaded = true;
          pcm.updateStatusIndicators();
        } finally {
          autoSaver.reset();
          autoSaver.saveFormProperties(formProperties);
          try {
            takeSnapshot(true, false);
          } catch (SaveException e) {
            FeedbackReporter.showErrorMessageWithExit(AutoSaver.SAVE_FAILURE_MESSAGE);
          }
        }
      }
    }

    try {
      SwingUtilities.invokeAndWait(new LoadRunnable());
      sendCurrentProjectDefinitionsToRepl(formProperties);
    } catch (InterruptedException e) {
      e.printStackTrace();
      // TODO(sharon): should change FeedbackReporter to always invokeLater if we're not on the
      // UI thread.
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FeedbackReporter.showErrorMessage("An error occurred during the loading of components. " +
                NO_SAVE_WARNING);}});
      throw new LoadException("Load was interrupted");
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FeedbackReporter.showErrorMessage("An error occurred during the loading of components. " +
              NO_SAVE_WARNING);}});
      Throwable cause = e.getCause();
      throw new LoadException(cause != null ? cause : e);
    }
  }

  /**
   * Loads a new project, or a new form in the same project, from its form properties and
   * codeblocks source.
   *
   * After the project has loaded the autosaver will be reset.
   *
   * Cannot be called from the EventDispatch (UI) thread.
   *
   * Throws LoadException if any errors are detected during the load
   *
   * @param newCodeblocksSourceSavePath the path that the ExternalController
   * should save the CodeblocksSource to.
   * @param codeblocksSource can be empty but not null
   * @param formProperties
   * @param assetFiles a map containing names of asset files
   * @param projectName the name of the project
   */
  public synchronized void loadSourceAndProperties(final String newCodeblocksSourceSavePath,
      final String codeblocksSource, final String formProperties,
      final Map<String,String> assetFiles, final String projectName)
      throws LoadException {

    final boolean differentProject = !projectName.equals(currentProjectName);

    final class LoadRunnable implements Runnable {
      public void run() {
        boolean blocksWereUpgraded = false;
        try {
          autoSaver.stopListening();
          autoSaver.clearHistory();  // is this needed? we reset later
          projectLoaded = false;
          componentRemovedOrRenamedDuringLoad = false;
          pcm.updateStatusIndicators();
          System.out.println("WorkspaceController: starting reload of workspace");
          boolean clearAssets = differentProject;
          // We don't even need to reset the phone! How cool is that?
          boolean resetPhone = false;
          if (codeblocksSource.length() == 0) {
            doLoadFreshWorkspace(newCodeblocksSourceSavePath, null, clearAssets, resetPhone);
          } else {
            blocksWereUpgraded = doLoadCodeblocksSource(newCodeblocksSourceSavePath,
                codeblocksSource, clearAssets, resetPhone);
          }
          System.out.println("WorkspaceController: loaded Codeblocks Source, starting JSON");
          if (differentProject) {
            // If this is a different project, we need to load assets.
            doLoadAssets(assetFiles);
          }
          doLoadFormProperties(WorkspaceUtils.parseFormProperties(formProperties));
          // We need to call setProjectName even if this is a different form in the same project
          // because the title bar and header need to be updated.
          setProjectName(projectName);
          projectLoaded = true;
          pcm.updateStatusIndicators();
        } catch (LoadException e) {
          FeedbackReporter.showErrorMessage(e.getMessage() + NO_SAVE_WARNING);
        } finally {
          System.out.println("WorkspaceController: workspace reload done");
          autoSaver.reset();
          System.out.println("Called autoSaver.reset();");
          autoSaver.saveFormProperties(formProperties);
          System.out.println("Called autoSaver.saveFormProperties");
          if (blocksWereUpgraded || componentRemovedOrRenamedDuringLoad) {
            System.out.println("blocksWereUpgraded or ComponentRemovedOrRenamedDuringLoad");
            autoSaver.workspaceChangedBySystem();
            System.out.println("autoSaver.workspaceChangedBySystem();");
          } else {
            if (codeblocksSource.length() == 0) {
              try {
                takeSnapshot(true, false);
              } catch (SaveException e) {
                FeedbackReporter.showErrorMessageWithExit(AutoSaver.SAVE_FAILURE_MESSAGE);
              }
            } else {
              autoSaver.onSnapshot(codeblocksSource);
            }
          }
        }
      }
    }

    try {
      SwingUtilities.invokeAndWait(new LoadRunnable());
      sendCurrentProjectDefinitionsToRepl(formProperties);
    } catch (InterruptedException e) {
      e.printStackTrace();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FeedbackReporter.showErrorMessage("An error occurred during the loading of your project."
              + NO_SAVE_WARNING);}});
      throw new LoadException("Load was interrupted");
    } catch (InvocationTargetException e) {
      final InvocationTargetException ite = e;
      e.printStackTrace();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          FeedbackReporter.showErrorMessage("An error occurred during the loading of your project."
              + NO_SAVE_WARNING);}});
      Throwable cause = ite.getCause();
      throw new LoadException(cause != null ? cause : e);
    }
    // phoneCommButton will get reenabled when we successfully load a project if
    // we didn't already enable it above.
  }

  /**
   * Load the blocks workspace from a save string and remember the source save
   * path as to newCodeblocksSourceSavePath. Upgrades any old format blocks
   * to new format, or warns about bad blocks if necessary.
   * This method assumes that a Language Definition File has already been
   * specified for this project.
   * @param codeblocksSource
   * @param clearAssets  if true, tells the phoneCommManager to clear assets
   * @param resetPhone  if true, tells the phoneCommManager to restart the phone app
   * @return true if the blocks were upgraded
   */
  private boolean doLoadCodeblocksSource(final String newCodeblocksSourceSavePath,
      String codeblocksSource, boolean clearAssets, boolean resetPhone) throws LoadException {
    //reset only if workspace actually exists
    if(workspaceLoaded) {
      resetWorkspace();
    }
    BlockSaveFile blockSaveFile = new BlockSaveFile(langDefRoot, codeblocksSource);
    try {
      loadingBlocks = true;
      getWorkspaceInstance().loadWorkspaceFrom(blockSaveFile.getRoot(), langDefRoot);
    } finally {
      loadingBlocks = false;
    }
    if (!cbm.loadComponents(blockSaveFile)) {
      // The project is not recoverable, so we clear the workspace
      doLoadFreshWorkspace(newCodeblocksSourceSavePath, null, clearAssets, resetPhone);
      throw new LoadException("An error occured while loading the project");
    }

    pcm.prepareForNewProject(clearAssets, resetPhone);

    codeblocksSourceSavePath = newCodeblocksSourceSavePath;
    warnAboutBadBlocks();
    workspaceLoaded = true;
    return blockSaveFile.wasUpgraded();
  }

  /**
   * Load the blocks from a save string. Uses the current save path.
   * Will not restart the phone app if it is running
   * @param codeblocksSource The save string for the workspace
   */
  protected void doLoadBlocks(String codeblocksSource) throws LoadException {
    // doLoadCodeblocksSource will reset the workspace. We need to save the current project name
    // now and then call setProjectName and set projectLoaded to true after.
    String projectName = currentProjectName;
    doLoadCodeblocksSource(codeblocksSourceSavePath, codeblocksSource, false, false);
    setProjectName(projectName);
    projectLoaded = true;
  }

  private void doLoadFormProperties(JSONObject properties) {
    if (properties != null) {
      try {
        currentFormName = properties.getJSONObject("Properties").getString("$Name");
      } catch (JSONException e) {
        currentFormName = "";
        FeedbackReporter.showSystemErrorMessage("Loading failure: Form properties failed to load." +
            NO_SAVE_WARNING);
      }

      if (!cbm.syncFromJson(properties)) {
        FeedbackReporter.showSystemErrorMessage("Loading failure: Form properties failed to load." +
            NO_SAVE_WARNING);
      }
    } else {
      FeedbackReporter.showSystemErrorMessage("Loading failure: Form properties were empty" +
          NO_SAVE_WARNING);
    }
  }

  private void doLoadAssets(Map<String,String> assetFileNames) {
    for (Entry<String,String> asset: assetFileNames.entrySet()) {
      if (DEBUG) {
        System.out.println("Loading asset file: " + asset.getKey());
      }
      if (asset.getKey().startsWith("badassets")) {
        FeedbackReporter.showWarningMessage(
            "Could not load asset file: " + asset.getKey() +
            ". It is possible that the asset file did not upload properly. " +
            "If you continue to see this message, try deleting and re-uploading " +
            "the asset file.", "Bad asset file");

      } else {
        pcm.addAssetAsync(asset.getKey(), asset.getValue());
      }
    }
  }

  /**
   * Sync a single component property with codeblocks
   * @param componentName the name of the component
   * @param componentType the type of the component
   * @param propertyName the name of the property
   * @param propertyValue the value of the property
   */
  public synchronized boolean syncProperty(String componentName, String componentType,
                                           String propertyName, String propertyValue) {

    if (pcm.connectedToPhone()) {
      StringBuilder yailCode = new StringBuilder();
      YABlockCompiler.generatePropertySetterYail(yailCode, componentName, componentType,
                                                 propertyName, propertyValue);
      System.out.println("Yail code for property is '" + yailCode + "'");
      pcm.replControllerCreateAndSendAsync(yailCode.toString(), "Property sync", 0L, false);
    }
    autoSaver.reset();  // can't undo/redo across a property sync
    return true;
  }

  /**
   * Notification from ComponentBlockManager that a component has been renamed.
   */
  @Override
  public void componentRenamed(String oldName, String newName) {
    if (DEBUG) {
      System.out.println("Call to rename component: " + oldName + " to " + newName);
    }
    if (loadingBlocks) {
      componentRemovedOrRenamedDuringLoad = true;
    }
    if (pcm.connectedToPhone()) {
      pcm.replControllerCreateAndSendAsync(
          YABlockCompiler.generateComponentRename(oldName, newName),
          "Component rename", 0L, true);
    }
  }

  /**
   * Notification from ComponentBlockManager that a component has been removed.
   */
  @Override
  public void componentRemoved(String name) {
    if (DEBUG) {
      System.out.println("Call to remove component: " + name);
    }
    if (loadingBlocks) {
      componentRemovedOrRenamedDuringLoad = true;
    }
    if (pcm.connectedToPhone()) {
      pcm.replControllerCreateAndSendAsync(YABlockCompiler.generateComponentRemoval(name),
          "Component removal", 0L, true);
    }
  }

  ////////
  // UI //
  ////////

  /**
   * This method creates and lays out the entire workspace panel with its
   * different components.  Workspace and language data not loaded in
   * this function.
   * Should be call only once at application startup.
   */
  private void initWorkspacePanel(){
    zoomSlider = new ZoomSlider();

    // Set the UI properties and action listeners for the save button.
    initializeControllerHistoryButtons();

    newEmulatorButton.init();

    newEmulatorButton.setPreferredSize(new Dimension(2 * BUTTON_WIDTH, BUTTON_HEIGHT));
    deviceSelector.setPreferredSize(new Dimension(150, BUTTON_HEIGHT));

    // Initialize the Repl controller and set the UI properties and action
    // listeners for the repl comm button.
    pcm.initializeReplComm(deviceSelector, commIndicator);

    headerPane = new CHeader(
        new JComponent[] {saveButton, undoButton, redoButton},
        new JComponent[] {newEmulatorButton, deviceSelector, commIndicator, zoomSlider});

    // Add the TrashCan for deleting blocks
    if (!addTrashCan()) {
      FeedbackReporter.showSystemErrorMessage("Trash can not added successfully");
    }

    workspacePanel = new JPanel();
    workspacePanel.setLayout(new BorderLayout());
    workspacePanel.add(headerPane, BorderLayout.NORTH);
    workspacePanel.add(getWorkspaceInstance(), BorderLayout.CENTER);
    isWorkspacePanelInitialized = true;

    TypeBlockManager.enableTypeBlockManager(getWorkspaceInstance().getBlockCanvas());
  }

  private boolean addTrashCan() {
    InputStream trashStream = this.getClass().getResourceAsStream("trash.png");
    InputStream trashOpenStream = this.getClass().getResourceAsStream("trash_open.png");
    try {
      ImageIcon trashCan = new ImageIcon(ImageIO.read(trashStream));
      ImageIcon trashCanOpen = new ImageIcon(ImageIO.read(trashOpenStream));
      TrashCan trash = new TrashCan(trashCan.getImage(), trashCanOpen.getImage());
      getWorkspaceInstance().addWidget(trash, true, true);
    } catch (IOException e) {
      FeedbackReporter.showSystemErrorMessage("Could not load image of trash can. " + e.getMessage());
      return false;
    } catch (IllegalArgumentException e) {
      FeedbackReporter.showSystemErrorMessage("Could not load image of trash cankjlkjlkjl. " + e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Returns the JComponent of the entire workspace.
   * @return the JComponent of the entire workspace.
   */
  private JComponent getWorkspacePanel(){
    if(!isWorkspacePanelInitialized)
      initWorkspacePanel();
    return workspacePanel;
  }

  private void initializeControllerHistoryButtons() {
    saveButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    undoButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
    redoButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e){
        autoSaver.saveNow();
      }
    });

    undoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e){
        try {
          autoSaver.undo();
        } catch (UndoRedoException exception) {
          exception.printStackTrace();
          FeedbackReporter.showSystemErrorMessage(exception.getMessage());
        } catch (SaveException exception) {
          exception.printStackTrace();
          FeedbackReporter.showSystemErrorMessage(exception.getMessage());
        }
      }
    });

    redoButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e){
        try {
          autoSaver.redo();
        } catch (UndoRedoException exception) {
          exception.printStackTrace();
          FeedbackReporter.showSystemErrorMessage(exception.getMessage());
        } catch (SaveException exception) {
          exception.printStackTrace();
          FeedbackReporter.showSystemErrorMessage(exception.getMessage());
        }
      }
    });
  }

  /////////////
  // STARTUP //
  /////////////

  /**
   * Start codeblocks from an external controller (e.g., a server invoked
   * by JavaWebStart).
   * @param ec  Provides a way for us to give the external controller a
   *            handle on the WorkspaceController once it is initialized.
   * @param ac  Give us a pointer to the Android-specific calls.
   */
  public synchronized void startCodeblocks(ExternalController ec, AndroidController ac) {
    externalController = ec;  // remember the handle for the external controller
    final WorkspaceController thisWorkspaceController = this;
    androidController = ac;
    pcm.setAndroidController(ac);


    // We do invokeLater here just so that whatever starts up codeblocks can continue with its
    // activities, but it is important that it remain on the UI thread so that other calls
    // which schedule on the UI don't interfere with it.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        try {
          Element langDefRoot = WorkspaceUtils.loadLangDef();
          doLoadFreshWorkspace("", langDefRoot, true, true);
          createAndShowGUI();
        } catch (Exception e) {
          FeedbackReporter.showErrorMessage("Can't find language definition. Unable to load.");
        }
      }
    });


  }


  /**
   * Provides a way for us to give the workspace controller a
   * handle on an externalController once it is initialized.
   * Used by YaBlockCompilerTest.
   */
  public synchronized void setExternalController(ExternalController ec) {
    externalController = ec;  // remember the handle for the external controller
  }

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event-dispatching thread.
   */
  private void createAndShowGUI() {

    System.out.println("Creating GUI...");
    //Create and set up the window.
    topFrame = new JFrame("App Inventor for Android Blocks Editor");
    topFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    topFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        saveAndExit();
      }
    });
    topFrame.setBounds(100, 100, 1400, 1000);
    topFrame.add(getWorkspacePanel(), BorderLayout.CENTER);
    FeedbackReporter.setFrame(topFrame);
    topFrame.setVisible(true);
  }

  private void setProjectName(String projectName) {
    currentProjectName = projectName;

    // Show both project and form names in the title bar and header pane.
    String projectAndFormNames = currentProjectName;
    if (currentProjectName.length() > 0 && currentFormName.length() > 0) {
      projectAndFormNames += " - " + currentFormName;
    }
    if (topFrame != null) {
      topFrame.setTitle("App Inventor for Android Blocks Editor: " + projectAndFormNames);
    }
    if (headerPane != null) {
      headerPane.setHeaderText(projectAndFormNames);
    }
  }

  /**
   * Saves the codeblocks source file and then exits.
   */
  private void saveAndExit() {
    try {
      persistCodeblocksSourceFile(true /* wait for save to complete */);
    } catch (SaveException e) {
      int response = JOptionPane.showConfirmDialog(topFrame,
          "Auto-save failed. Retry before exiting?", "Blocks Editor Save Failure",
          JOptionPane.YES_NO_OPTION);

      if (response == JOptionPane.YES_OPTION) {
        saveAndExit();
      }
    } finally {
      System.exit(0);
    }
  }

  //////////////////////
  // WORKSPACE EVENTS //
  //////////////////////

  /**
   *  TODO(sharon): the code to handle workspace events that cause stuff to be
   *  sent to the phone was moved here from ComponentBlockManager (where it
   *  clearly didn't belong). Perhaps there is an even better home for it.
   */

  /**
   * Handles workspace events that require sending stuff to the phone.
   * Note(sharon): this code used to be in ComponentBlockManager and was in
   * a synchronized method. I've removed the synchronization here because
   * it causes deadlocks for WorkspaceController and seems unnecessary.
   */
  public void workspaceEventOccurred(WorkspaceEvent event)  {
    if (isLoadingBlocks() || !pcm.connectedToPhone()) {
      return;
    }
    int eventType = event.getEventType();
    switch (eventType) {
      case WorkspaceEvent.BLOCK_RENAMED:
      case WorkspaceEvent.BLOCK_ACTIVATED:
      case WorkspaceEvent.BLOCK_DEACTIVATED:
      case WorkspaceEvent.BLOCK_GENUS_CHANGED:
        executeContainingDecl(event.getSourceBlockID());
        break;
      case WorkspaceEvent.BLOCKS_CONNECTED:
      case WorkspaceEvent.BLOCKS_DISCONNECTED:
        executeContainingDecl(event.getSourceLink().getSocketBlockID());
        break;
      case WorkspaceEvent.BLOCK_ADDED:
      case WorkspaceEvent.BLOCK_REMOVED:
        long blockID = event.getSourceBlockID();
        Block block = Block.getBlock(blockID);
        if (block != null && (event.getSourceWidget() instanceof Page)) {
          if (block.isDeclaration()) {
            executeNullDecl(block);
          } else {
            executeContainingDecl(blockID);
          }
        }
        break;
      case WorkspaceEvent.BLOCK_DO_IT:
        if (pcm.connectedToPhone() && event.getSourceWidget() instanceof Page) {
          // Block not in a drawer
          performDoIt(event.getSourceBlockID());
        }
        break;
      case WorkspaceEvent.BLOCK_REPORT_CHANGE:
        performReportChange(event.getSourceBlockID());
        break;
      default:
        break;
    }
  }

  private void performDoIt(long blockID) {
    // Clean up from previous Do It.
    for (RenderableBlock rb : RenderableBlock.getAllRenderableBlocks()) {
      if (rb.hasReport()) {
        if (!rb.getBlock().shouldReceiveReport()) {
          rb.removeReport();
        } else {
          rb.getReport().getBlockNoteLabel().setActive(false);
        }
      }
    }
    executeOnPhone(REPL_DO_IT, blockID);
    doItBlock = Block.getBlock(blockID);
    RenderableBlock doItRb = RenderableBlock.getRenderableBlock(blockID);
    Point blockLoc = doItRb.getLocation();
    Rectangle blockMenuAndBalloon = new Rectangle(blockLoc.x, blockLoc.y,
        doItRb.getWidth(), doItRb.getHeight() + 100).union(doItRb.getReport().getBounds());
    doItBlockRegion = SwingUtilities.convertRectangle(doItRb.getParent(),
        blockMenuAndBalloon, Workspace.getInstance().getBlockCanvas().getCanvas());
  }

  private void performReportChange(long blockID) {
    Block block = Block.getBlock(blockID);
    if (block.isVariableDeclBlock()) {
      for (long bs : BlockStub.getStubsOfParent(blockID)) {
        // Recompile all the decls containing setter stubs. The stubs
        // will figure out if they have to generate report calls.
        // TODO(user): Avoid sending a decl multiple times.
        if (Block.getBlock(bs).getProperty("ya-kind").equals("setter")) {
          executeContainingDecl(bs);
        }
      }
      if (block.shouldReceiveReport()) {
        // Need to get current value of var if change was to add report.
        String blockCode = blockParser.genVarGetYailFromDecl(block);
        if (ComplaintDepartment.showCompiletimeComplaints() == 0 &&
            blockCode.length() > 0) {
          pcm.replControllerCreateAndSendAsync(blockCode,
              REPL_DO_IT, block.getBlockID(), false);
        }
      }
    } else {
      executeContainingDecl(blockID);
    }
  }

  /*
   * block should be a declaration (procedure, event or variable)
   * Redefine the block to have a null value
   */
  private void executeNullDecl(Block block) {
    String blockCode = blockParser.genNullDecl(block);
    if (blockCode.length() != 0) {
      pcm.replControllerCreateAndSendAsync(blockCode, REPL_DEFINE_IT, block.getBlockID(),
          false);
    }
  }

  private void executeContainingDecl(long blockID) {
    Block block = RenderableBlock.getRenderableBlock(blockID).getTopmost().getBlock();
    if (block.isDeclaration()) {
      executeOnPhone(REPL_DEFINE_IT, block.getBlockID());
    }
  }

  private void executeOnPhone(String purpose, long id) {
    Block block = Block.getBlock(id);
    ComplaintDepartment.clearComplaints();
    String blockCode = blockParser.genYail(block);
    if (ComplaintDepartment.showCompiletimeComplaints() > 0 ||
        blockCode.length() == 0) {
      // Don't execute if there are serious errors.
      return;
    }
    pcm.replControllerCreateAndSendAsync(blockCode, purpose, id, false);
  }

  /*
   * A notification from BlockCanvas
   * @param e the MouseEvent containing the location
   * TODO(user) The origin of the doItBlock is usually many pixels above the
   *               click on the Do It in the menu. If we could get that location
   *               of the menu click, this test might be a little better.
   */
  @Override
  public void mouseMovedOnCanvas(MouseEvent e) {
    if (doItBlock != null && !doItBlock.shouldReceiveReport() &&
        !doItBlockRegion.contains(e.getPoint())) {
      doItBlock.getRenderableBlock().removeReport();
      doItBlock = null;
    }
  }

  /*
   * Shows a warning message if there are any bad blocks.
   */
  private static void warnAboutBadBlocks() {
    // See if there are any bad blocks.
    for (Block block : Block.getAllBlocks()) {
      if (block.isBad()) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            FeedbackReporter.showWarningMessage(
                "This program has blocks from an old version of the system. " +
                "Please replace blocks outlined in red before building your app. " +
                "We apologize for the inconvenience.");
          }});
          break;
      }
    }
  }
}
