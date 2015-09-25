package edu.mills.feeney.thesis.aimerger;

import java.nio.file.*;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * A user interface enabling users to merge two App Inventor projects.
 *
 * @author feeney.kate@gmail.com (Kate Feeney)
 *         <p/>
 *         modified by Arezu Esmaili (arezuesmaili1@gmail.com) - July 2015
 */
public class AIMerger extends JFrame {

  private static AIMerger instance;
  public Container myCP;
  private JPanel mainProjectDisplayP;
  private JPanel mergeButtonP;
  private JPanel secondProjectDisplayP;

  private JLabel instructMainProjectL;
  private JLabel instructMainProjectNotesL;
  private JLabel instructSecondProjectL;
  private JLabel instructSecondProjectNotesL;
  private JLabel mainProjectTitleL;
  private JLabel secondProjectTitleL;
  private JLabel mainProjectAssetsL;
  private JLabel mainProjectScreensInstrucL;
  private JLabel mainProjectScreensL;
  private JLabel mainProjectAssetsInstrucL;
  private JLabel secondProjectAssetsL;
  private JLabel secondProjectScreensInstrucL;
  private JLabel secondProjectScreensL;
  private JLabel secondProjectAssetsInstrucL;
  private JLabel picLabel;
  private JLabel revisionNote;
  private JButton mainProjectBrowseB;
  private JButton secondProjectBrowseB;
  private JButton mergeB;
  private JTextField mainProjectTF;
  private JTextField secondProjectTF;
  private static final Font HEADER_TWO_FONT = new Font("Dialog", Font.PLAIN, 18);
  private static final Font HEADER_THREE_FONT = new Font("Dialog", Font.ITALIC, 12);
  private static final double HEIGHT_PERCENT_OF_SCREEN = 0.8;
  private static final double WIDTH_PERCENT_OF_SCREEN = 0.8;
  private static final int BORDER_THICKNESS = 50;
  private Dimension projectDisplayPanelSize;
  private Point projectDisplayPanelLocation;
  private JScrollPane mainProjectScreensP;
  private JScrollPane mainProjectAssetsP;
  private JScrollPane secondProjectScreensP;
  private JScrollPane secondProjectAssetsP;
  private CheckBoxList mainProjectScreensCBL;
  private CheckBoxList mainProjectAssetsCBL;
  private AIProject mainProject;
  private CheckBoxList secondProjectScreensCBL;
  private CheckBoxList secondProjectAssetsCBL;
  private AIProject secondProject;
  private String mergeProjectPath;
  private static final Color GREY = new Color(200, 200, 200);
  private String oldName = null;
  private String newName = null;
  private String browserPath = null;

  /**
   * Returns a file path with no file extension.
   *
   * @param a file path.
   * @return a file path with no file extension.
   */
  public String pathNoExt(String path) {
    return path.substring(0, path.lastIndexOf('.'));
  }

  /**
   * Returns a file's extension given its path.
   *
   * @param a file path.
   * @return a file's extension.
   */
  public String getExt(String path) {
    return path.substring(path.lastIndexOf('.'));
  }

  /**
   * Returns the file's name given its path.
   *
   * @param a file path.
   * @return a file's name.
   */
  public String fileName(String path) {
    return path.substring(path.lastIndexOf('/') + 1);
  }


  // Action listener for the main project's browse button.
  private class MainProjectBrowseBActionListener implements ActionListener {
    // When the main project's Browse button is pressed, a file-browsing window
    // opens and the file selected by the user appears in the main project text box.
    @Override
    public void actionPerformed(ActionEvent event) {
      String path = getFileToOpen();
      if (path != null) {
        mainProjectTF.setText(path);
        browserPath = path;
        // Create AIProject for the main project.
        mainProject = new AIProject(mainProjectTF.getText());
      }
      // Display main project.
      if (mainProject.isValid()) {
        mainProjectDisplayP.setVisible(true);
        updateMainProjectView();
        // Set the lower center panel to visible if the lower right panel is already visible.
        mergeButtonP.setVisible(secondProjectDisplayP.isVisible());
      } else {
        mainProjectDisplayP.setVisible(false);
      }
    }
  }

  // Action listener for the second project's browse button.
  private class SecondProjectBrowseBActionListener implements ActionListener {
    // When the second project's Browse button is pressed, a file-browsing window
    // opens and the file selected by the user appears in the second project text box.
    @Override
    public void actionPerformed(ActionEvent event) {
      String path = getFileToOpen();
      if (path != null) {
        secondProjectTF.setText(path);
        browserPath = path;
        secondProject = new AIProject(secondProjectTF.getText());
      }
      if (secondProject.isValid()) {
        secondProjectDisplayP.setVisible(true);
        updateSecondProjectView();
        // Set the lower center panel to visible if the lower left panel is already visible.
        mergeButtonP.setVisible(mainProjectDisplayP.isVisible());
      } else {
        secondProjectDisplayP.setVisible(false);
      }
    }
  }

  // Action listener for the merge button.
  private class MergeBActionListener implements ActionListener {
    // Action performed when button is clicked.
    @Override
    public void actionPerformed(ActionEvent event) {
      if (alertToDuplicates() && containsScreen1()) {
        // List to hold files to be included in the new project from the main
        // project.
        List<String> filesFromMainProject = new ArrayList<String>();
        // The properties file from the main project is always included in the new project.
        filesFromMainProject.add(mainProject.getPropertiesFilePath());

        // List to hold files to be included in the new project from the second project.
        List<String> filesFromSecondProject = new ArrayList<String>();

        // Temporary list to hold the name of the screens from the main project that have been
        // checked to be included in the new project.
        List<String> mainProjectCheckedScreens = mainProjectScreensCBL.getChecked();
        // Add checked screens to the list of files to include from the main project.
        if (!mainProjectCheckedScreens.isEmpty()) {
          for (AIScreen aiScreen : mainProject.getScreensList()) {
            if (mainProjectCheckedScreens.contains(aiScreen.getName())) {
              String path = aiScreen.getPath();
              filesFromMainProject.add(path);
              filesFromMainProject.add(path.substring(0, path.lastIndexOf(".scm")).concat(".bky"));
            }
          }
        }

        // Temporary list to hold the name of the assets from the main project that have been
        // checked to be included in the new project.
        List<String> mainProjectCheckedAssets = mainProjectAssetsCBL.getChecked();
        // Add checked assets to the list of files to include from the main project.
        if (!mainProjectCheckedAssets.isEmpty()) {
          for (AIAsset aiAsset : mainProject.getAssetsList()) {
            if (mainProjectCheckedAssets.contains(aiAsset.getName())) {
              filesFromMainProject.add(aiAsset.getPath());
            }
          }
        }

        // Temporary list to hold the name of the screens from the second project that have been
        // checked to be included in the new project.
        List<String> secondProjectCheckedScreens = secondProjectScreensCBL.getChecked();
        // Add checked screens to the list of files to include from the second project.
        if (!secondProjectCheckedScreens.isEmpty()) {
          for (AIScreen aiScreen : secondProject.getScreensList()) {
            if (secondProjectCheckedScreens.contains(aiScreen.getName())) {
              String path = aiScreen.getPath();
              filesFromSecondProject.add(path);
              filesFromSecondProject.add(path.substring(0, path.lastIndexOf(".scm")).concat(".bky"));
            }
          }
        }

        // Temporary list to hold the name of the assets from the second project that have been
        // checked to be included in the new project.
        List<String> secondProjectCheckedAssets = secondProjectAssetsCBL.getChecked();
        // Add checked assets to the list of files to include from the second project.
        if (!secondProjectCheckedAssets.isEmpty()) {
          for (AIAsset aiAsset : secondProject.getAssetsList()) {
            if (secondProjectCheckedAssets.contains(aiAsset.getName())) {
              filesFromSecondProject.add(aiAsset.getPath());
            }
          }
        }

        try {
          mergeProjectPath = getFileToSaveTo();
          ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(mergeProjectPath));

          byte[] buf = new byte[4096];

          ZipInputStream mainZipInput = new ZipInputStream(new BufferedInputStream(
              new FileInputStream(mainProject.getProjectPath())));
          // Write files from main project to new project.
          ZipEntry curEntry;
          while ((curEntry = mainZipInput.getNextEntry()) != null) {
            if (filesFromMainProject.contains(curEntry.getName())) {
              outZip.putNextEntry(curEntry);
              int len;
              while ((len = mainZipInput.read(buf)) > 0) {
                outZip.write(buf, 0, len);
              }
              outZip.closeEntry();
              mainZipInput.closeEntry();
            }
          }
          mainZipInput.close();
          if (!filesFromSecondProject.isEmpty()) {
            ZipInputStream secondZipInput = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(secondProject.getProjectPath())));
            // Write files from second project to new project.
            while ((curEntry = secondZipInput.getNextEntry()) != null) {
              if (filesFromSecondProject.contains(curEntry.getName())) {
                if (oldName != null && curEntry.toString().contains(oldName)) {
                  curEntry = new ZipEntry(newName + getExt(curEntry.toString()));
                }
                outZip.putNextEntry(curEntry);
                int len;
                while ((len = secondZipInput.read(buf)) > 0) {
                  if (getExt(curEntry.toString()).equals(".scm") && curEntry.toString().equals(
                      newName + getExt(curEntry.toString()))) {
                    String s = new String(buf, 0, len);
                    String s2 = new String(s.replace("\"$Name\":\"Screen1\"", "\"$Name\":\"" +
                        fileName(newName) + "\""));
                    String s3 = new String(s2.replace("\"Title\":\"Screen1\"", "\"Title\":\"" +
                        fileName(newName) + "\""));
                    buf = s3.getBytes();
                    len = s3.length();
                  }
                  outZip.write(buf, 0, len);
                }
                outZip.closeEntry();
                secondZipInput.closeEntry();
              }
            }
            secondZipInput.close();
          }
          outZip.close();
          offerNewMerge();
        } catch (IOException e1) {
          JOptionPane.showMessageDialog(myCP, "Invalid file name.", "File Name Error",
              JOptionPane.ERROR_MESSAGE);
          actionPerformed(event);
        }
      }
    }
  }

  /*
   * Resets UI to what appears when the application starts. If a project's path
   * is passed then this is loaded as the main project.
   */
  private void resetAIMerger(String aiProjectPath) {
    oldName = null;
    newName = null;
    browserPath = null;
    mainProjectScreensCBL.clearChecked();
    mainProjectAssetsCBL.clearChecked();
    secondProjectScreensCBL.clearChecked();
    secondProjectAssetsCBL.clearChecked();
    mainProjectTF.setText(null);
    secondProjectTF.setText(null);
    mainProjectDisplayP.setVisible(false);
    secondProjectDisplayP.setVisible(false);
    mergeButtonP.setVisible(false);
    if (aiProjectPath != null) {
      mainProjectTF.setText(aiProjectPath);
      mainProject = new AIProject(aiProjectPath);
      mainProjectDisplayP.setVisible(true);
      updateMainProjectView();
    }
  }

  /*
   * Informs the user their merge was successful and asks if they would like to
   * merge another project.
   */
  private void offerNewMerge() {
    int response = JOptionPane.showOptionDialog(myCP, "Projects Successfully Merged. "
            + "Would you like to merge more projects?", "Projects Merged", JOptionPane.YES_NO_OPTION,
        JOptionPane.INFORMATION_MESSAGE, null, null, JOptionPane.YES_OPTION);
    switch (response) {
      default:
        // This should never happen
        throw new IllegalArgumentException("not an option");
      case JOptionPane.CLOSED_OPTION:
      case JOptionPane.NO_OPTION:
        closeApplication();
        break;
      case JOptionPane.YES_OPTION:
        offerToMergeToNewProject();
        break;
    }
  }

  /*
   * Asks user if they want to merge another project to the recently merged project.
   */
  private void offerToMergeToNewProject() {
    int response = JOptionPane.showOptionDialog(myCP, "Would you like one of the projects to merge"
            + " to be the project you just created?", "Merge More Projects", JOptionPane.YES_NO_OPTION,
        JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
    switch (response) {
      default:
        // This should never happen
        throw new IllegalArgumentException("not an option");
      case JOptionPane.CLOSED_OPTION:
        closeApplication();
        break;
      case JOptionPane.NO_OPTION:
        resetAIMerger(null);
        break;
      case JOptionPane.YES_OPTION:
        resetAIMerger(mergeProjectPath);
        break;
    }
  }

  /*
   * Confirms the user wants to exit AIMerger.
   */
  private void closeApplication() {
    int response = JOptionPane.showOptionDialog(myCP, "Exit AIMerger?", "Exit",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
        JOptionPane.YES_OPTION);
    switch (response) {
      default:
        // This should never happen
        throw new IllegalArgumentException("not an option");
      case JOptionPane.CLOSED_OPTION:
      case JOptionPane.CANCEL_OPTION:
        offerNewMerge();
        break;
      case JOptionPane.OK_OPTION:
        System.exit(0);
        break;
    }
  }

  private String getFileToSaveTo() {
    // Get new project's file path.
    JFileChooser mergeProjectFS = new JFileChooser();
    mergeProjectFS.setDialogType(JFileChooser.SAVE_DIALOG);
    String projectPath = null;
    int validPath = mergeProjectFS.showSaveDialog(myCP);
    if (validPath != JFileChooser.ERROR_OPTION || validPath != JFileChooser.CANCEL_OPTION) {
      // Make sure the file is an aia file.
      File projectFile = mergeProjectFS.getSelectedFile();
      projectPath = projectFile.getPath();
      if (!projectPath.toLowerCase().endsWith(".aia")) {
        projectPath = projectPath.concat(".aia");
        projectFile = new File(projectPath);
      }

      // Confirm that the user wants to overwrite an existing project, but do not allow
      // overwriting one of the two projects being merged.
      if (projectFile.exists()) {
        if (projectFile.getPath().equalsIgnoreCase(mainProject.getProjectPath())
            || projectFile.getPath().equalsIgnoreCase(secondProject.getProjectPath())) {
          JOptionPane.showMessageDialog(myCP, "You can not overwrite one of the two "
                  + "projects being merged. Select anther file name.", "File Name Error",
              JOptionPane.ERROR_MESSAGE);
          return getFileToSaveTo();
        }
        validPath = JOptionPane.showOptionDialog(myCP, "The file name you selected already "
                + "exists. Would you like to replace it?", "Replace Existing File",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null,
            JOptionPane.NO_OPTION);
        if (validPath == JOptionPane.CLOSED_OPTION || validPath == JOptionPane.NO_OPTION) {
          return getFileToSaveTo();
        }
      }
      // The project's name is the name of the aia file.
      String projectName = projectPath.substring(projectPath.lastIndexOf(File.separator) + 1,
          projectPath.lastIndexOf(".aia"));
      // The projects name must start with a letter and can only contain letters,
      // numbers and underscores.

      if (!Character.isLetter(projectName.charAt(0)) || !projectName.matches("^[a-zA-Z0-9_]*$")) {
        JOptionPane.showMessageDialog(myCP, "Project names must start with a letter and "
                + "can contain only letters, numbers, and underscores", "File Name Error",
            JOptionPane.ERROR_MESSAGE);
        return getFileToSaveTo();
      }
    }
    return projectPath;
  }

  /*
   * Returns true is all screens and assets to be merged have unique names, else returns false.
   */
  private boolean alertToDuplicates() {
    for (String screen : mainProjectScreensCBL.getChecked()) {
      //Asks to rename Screen1 of second project if both Screen1s are checked.
      if (secondProjectScreensCBL.getChecked().contains("Screen1") && screen.equals("Screen1") &&
          newName == null) {
        JOptionPane.showMessageDialog(myCP, "Your project will not correctly merge with two " +
                "Screen1s checked." + "\n" + "Rename your second project's Screen1 to continue.",
            "Rename Screen1", JOptionPane.WARNING_MESSAGE);
        String path = "";
        String screenName = JOptionPane.showInputDialog(secondProjectDisplayP, "Rename",
            "Enter new name");
        // The screen's name must start with a letter and can only contain letters,
        // numbers and underscores.
        while (!Character.isLetter(screenName.charAt(0)) || !screenName.matches("^[a-zA-Z0-9_]*$") ||
            secondProjectScreensCBL.getChecked().contains(screenName) ||
            mainProjectScreensCBL.getChecked().contains(screenName) ||
            screenName.toLowerCase().equals("screen1")) {
          JOptionPane.showMessageDialog(myCP, "Screen names must start with a letter and "
                  + "can contain only letters, numbers, " + "\n" + "and underscores, and cannot be "
                  + "the same as another checked screen.", "File Name Error",
              JOptionPane.ERROR_MESSAGE);
          screenName = JOptionPane.showInputDialog(myCP, "Rename", "Enter new name");
        }
        for (AIScreen aiScreen : secondProject.getScreensList()) {
          if (aiScreen.getName().equals("Screen1")) {
            path += aiScreen.getPath();
          }
        }
        String newPath = path.substring(0, path.lastIndexOf('/') + 1) + screenName;
        oldName = pathNoExt(path);
        newName = newPath;
      }
      if (secondProjectScreensCBL.getChecked().contains(screen) && screen != "Check All" &&
          screen != "Screen1") {
        JOptionPane.showMessageDialog(myCP, "You cannot select two screens with the "
                + "same name. Please uncheck one of the screens and remerge.", "Duplicate Error",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    for (String asset : mainProjectAssetsCBL.getChecked()) {
      if (secondProjectAssetsCBL.getChecked().contains(asset) && asset != "Check All") {
        JOptionPane.showMessageDialog(myCP, "You cannot select two assets with the "
                + "same name. Please uncheck one of the assets and remerge.", "Duplicate Error",
            JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    return true;
  }

  /*
   * Returns true if at least one Screen1 is checked, otherwise returns false.
   */
  private boolean containsScreen1() {
    for (String screen : mainProjectScreensCBL.getChecked()) {
      if (screen.equals("Screen1")) {
        return true;
      }
    }
    for (String screen : secondProjectScreensCBL.getChecked()) {
      if (screen.equals("Screen1")) {
        return true;
      }
    }
    JOptionPane.showMessageDialog(myCP, "Your project must have a Screen1 to merge properly. "
            + "Please check a Screen1 and remerge.", "No Screen1 Error",
        JOptionPane.ERROR_MESSAGE);
    return false;
  }

  /*
   * Launches a file chooser and returns the file chosen. The file returned will be the new merged
   * project.
   */
  private String getFileToOpen() {
    JFileChooser projectFC = new JFileChooser(browserPath);
    int validPath = projectFC.showOpenDialog(myCP);
    if (validPath == JFileChooser.ERROR_OPTION || validPath == JFileChooser.CANCEL_OPTION) {
      return null;
    } else {
      return projectFC.getSelectedFile().toString();
    }
  }

  /*an
   * Updates the lower left part of the screen to display the main project.
   */
  private static void updateMainProjectView() {
    getInstance().mainProjectTitleL.setText("Main Project: "
        + getInstance().mainProject.getProjectName());

    getInstance().mainProjectScreensCBL.setListData(getScreenCheckBoxes(getInstance().mainProject,
        true));
    getInstance().mainProjectScreensCBL.clearChecked();
    getInstance().mainProjectScreensCBL.getChecked().add("Screen1");
    getInstance().mainProjectScreensP.setViewportView(getInstance().mainProjectScreensCBL);

    getInstance().mainProjectAssetsCBL.setListData(getAssetCheckBoxes(getInstance().mainProject));
    getInstance().mainProjectAssetsCBL.clearChecked();
    getInstance().mainProjectAssetsP.setViewportView(getInstance().mainProjectAssetsCBL);

    getInstance().mainProjectDisplayP.repaint();
  }

  /*
   * Updates the lower right part of the screen to display the second project.
   */
  private static void updateSecondProjectView() {
    getInstance().secondProjectTitleL.setText("Second Project: "
        + getInstance().secondProject.getProjectName());

    getInstance().secondProjectScreensCBL.setListData(getScreenCheckBoxes(
        getInstance().secondProject, false));
    getInstance().secondProjectScreensCBL.clearChecked();
    getInstance().secondProjectScreensP.setViewportView(getInstance().secondProjectScreensCBL);

    getInstance().secondProjectAssetsCBL
        .setListData(getAssetCheckBoxes(getInstance().secondProject));
    getInstance().secondProjectAssetsCBL.clearChecked();
    getInstance().secondProjectAssetsP.setViewportView(getInstance().secondProjectAssetsCBL);

    getInstance().secondProjectDisplayP.repaint();
  }

  /*
   * Creates an array of JCheckBoxes, a JCheckBox for each asset in the project.
   */
  private static JCheckBox[] getAssetCheckBoxes(AIProject project) {
    List<AIAsset> tempAssetsList = project.getAssetsList();
    JCheckBox[] assetCheckBoxLabels = new JCheckBox[1 + tempAssetsList.size()];
    assetCheckBoxLabels[0] = new JCheckBox("Check All");
    for (int i = 1; i <= tempAssetsList.size(); i++) {
      assetCheckBoxLabels[i] = new JCheckBox(tempAssetsList.get(i - 1).getName());
    }
    return assetCheckBoxLabels;
  }

  /*
   * Creates an array of JCheckBoxes, a JCheckBox for each screen in the
   * project. If the project is the main project then "Screen1" is a checked
   * JCheckBox.
   */
  private static JCheckBox[] getScreenCheckBoxes(AIProject project, boolean isMainProject) {
    List<AIScreen> tempScreensList = project.getScreensList();
    JCheckBox[] screenCheckBoxLabels = new JCheckBox[1 + tempScreensList.size()];
    screenCheckBoxLabels[0] = new JCheckBox("Check All");
    for (int i = 1; i <= tempScreensList.size(); i++) {
      String tempScreenName = tempScreensList.get(i - 1).getName();
      if (tempScreenName.equals("Screen1") && isMainProject) {
        screenCheckBoxLabels[i] = new JCheckBox(tempScreenName, true);
      } else {
        screenCheckBoxLabels[i] = new JCheckBox(tempScreenName);
      }
    }
    return screenCheckBoxLabels;
  }

  public AIMerger() {
    super("App Inventor Merger v1.2 -- for App Inventor 2");

    // Set the size and location of the application's window based on the screen size.
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize((int) (screenSize.width * WIDTH_PERCENT_OF_SCREEN),
        (int) (screenSize.height * HEIGHT_PERCENT_OF_SCREEN));
    setLocation((int) ((screenSize.width - screenSize.width * WIDTH_PERCENT_OF_SCREEN) / 2),
        (int) ((screenSize.height - screenSize.height * HEIGHT_PERCENT_OF_SCREEN) / 2));

    // Create a reference to the content pane of the JFrame.
    myCP = this.getContentPane();
    myCP.setLayout(null);
    myCP.setBackground(GREY);

    // Main project title and instructions.
    instructMainProjectL = new JLabel("Browse for and load your Main Project.");
    instructMainProjectL.setFont(HEADER_TWO_FONT);
    instructMainProjectL.setSize(1000, 24);
    instructMainProjectL.setLocation(20, 20);
    myCP.add(instructMainProjectL);

    // Main project note.
    instructMainProjectNotesL = new JLabel("The main project's Screen1 will be the "
        + "merged project's Screen1");
    instructMainProjectNotesL.setFont(HEADER_THREE_FONT);
    instructMainProjectNotesL.setSize(1000, 20);
    instructMainProjectNotesL.setLocation(20, instructMainProjectL.getLocation().y + 20);
    myCP.add(instructMainProjectNotesL);

    // Text field to enter path to main project.
    mainProjectTF = new JTextField(300);
    mainProjectTF.setBackground(Color.WHITE);
    mainProjectTF.setEditable(true);
    mainProjectTF.setSize(300, 30);
    mainProjectTF.setLocation(20, instructMainProjectNotesL.getLocation().y + 20);
    myCP.add(mainProjectTF);

    // Browse button for main project.
    mainProjectBrowseB = new JButton("Browse");
    mainProjectBrowseB.setSize(100, 30);
    mainProjectBrowseB.setLocation(mainProjectTF.getLocation().x + mainProjectTF.getSize().width
        + 10, instructMainProjectNotesL.getLocation().y + 20);
    mainProjectBrowseB.addActionListener(new MainProjectBrowseBActionListener());
    myCP.add(mainProjectBrowseB);

    // Browse button for the second project
    secondProjectBrowseB = new JButton("Browse");
    secondProjectBrowseB.setSize(100, 30);
    secondProjectBrowseB.setLocation((int) ((screenSize.width * WIDTH_PERCENT_OF_SCREEN)
        - (mainProjectBrowseB.getSize().width + 20)), mainProjectTF.getLocation().y);
    secondProjectBrowseB.addActionListener(new SecondProjectBrowseBActionListener());
    myCP.add(secondProjectBrowseB);

    // Second project title.
    instructSecondProjectL = new JLabel("Browse for and load your Second Project.");
    instructSecondProjectL.setFont(HEADER_TWO_FONT);
    instructSecondProjectL.setSize(1000, 24);
    instructSecondProjectL.setLocation(secondProjectBrowseB.getLocation().x - (20 +
        mainProjectTF.getSize().width), 20);
    myCP.add(instructSecondProjectL);

    // Second project note.
    instructSecondProjectNotesL = new JLabel("The second project's Screen1 can be renamed " +
        "to be merged");
    instructSecondProjectNotesL.setFont(HEADER_THREE_FONT);
    instructSecondProjectNotesL.setSize(1000, 20);
    instructSecondProjectNotesL.setLocation(instructSecondProjectL.getLocation().x,
        instructSecondProjectL.getLocation().y + 20);
    myCP.add(instructSecondProjectNotesL);

    // Text field to enter the second project's path.
    secondProjectTF = new JTextField(300);
    secondProjectTF.setBackground(Color.WHITE);
    secondProjectTF.setEditable(true);
    secondProjectTF.setSize(300, 30);
    secondProjectTF.setLocation(instructSecondProjectL.getLocation().x,
        instructSecondProjectL.getLocation().y + 40);
    myCP.add(secondProjectTF);

    // AI Merger icon
    JLabel picLabel = new JLabel(new ImageIcon(getClass().getResource("img/logoclear.png")));
    picLabel.setSize(329, 145);
    picLabel.setLocation((int) (screenSize.width * WIDTH_PERCENT_OF_SCREEN) / 2 -
        (picLabel.getSize().width / 2), mainProjectBrowseB.getLocation().y + 20);
    myCP.add(picLabel);

    // Location and size of the panel that displays the projects.
    projectDisplayPanelLocation = new Point(20, secondProjectTF.getLocation().y + BORDER_THICKNESS);
    projectDisplayPanelSize = new Dimension((int) (screenSize.width * WIDTH_PERCENT_OF_SCREEN) -
        BORDER_THICKNESS, (int) (screenSize.height * HEIGHT_PERCENT_OF_SCREEN) -
        projectDisplayPanelLocation.y - BORDER_THICKNESS - 25);

    // Panel that holds the components of the main project.
    mainProjectDisplayP = new JPanel();
    mainProjectDisplayP.setVisible(false);
    mainProjectDisplayP.setSize(projectDisplayPanelSize.width / 3, projectDisplayPanelSize.height);
    mainProjectDisplayP.setLocation(20, projectDisplayPanelLocation.y);
    mainProjectDisplayP.setLayout(new BoxLayout(mainProjectDisplayP, BoxLayout.Y_AXIS));
    mainProjectDisplayP.setBorder(new EmptyBorder(5, 20, 20, 20));
    myCP.add(mainProjectDisplayP);

    // Main project's name.
    mainProjectTitleL = new JLabel();
    mainProjectTitleL.setFont(HEADER_TWO_FONT);
    mainProjectDisplayP.add(mainProjectTitleL);

    mainProjectDisplayP.add(Box.createRigidArea(new Dimension(0, 10)));

    // Label for the list of screens for the main project
    mainProjectScreensL = new JLabel("Screens");
    mainProjectScreensL.setFont(HEADER_TWO_FONT);
    mainProjectDisplayP.add(mainProjectScreensL);

    // Instructions for the list of screens for the main project.
    mainProjectScreensInstrucL = new JLabel("Check screens to merge into new project");
    mainProjectScreensInstrucL.setFont(HEADER_THREE_FONT);
    mainProjectDisplayP.add(mainProjectScreensInstrucL);

    // List of checkboxes for the screens from the main project
    mainProjectScreensCBL = new CheckBoxList();
    mainProjectScreensP = new JScrollPane();
    mainProjectDisplayP.add(mainProjectScreensP);

    mainProjectDisplayP.add(Box.createRigidArea(new Dimension(0, 10)));

    // Label for the list of assets for the main project
    mainProjectAssetsL = new JLabel("Assets");
    mainProjectAssetsL.setFont(HEADER_TWO_FONT);
    mainProjectDisplayP.add(mainProjectAssetsL);

    // Instructions for the list of assets for the main project.
    mainProjectAssetsInstrucL = new JLabel("Check assets to merge into new project");
    mainProjectAssetsInstrucL.setFont(HEADER_THREE_FONT);
    mainProjectDisplayP.add(mainProjectAssetsInstrucL);

    // List of checkboxes for the assets from the main project.
    mainProjectAssetsCBL = new CheckBoxList();
    mainProjectAssetsP = new JScrollPane();
    mainProjectDisplayP.add(mainProjectAssetsP);

    // Panel that holds the merge button and arrows.
    mergeButtonP = new JPanel();
    mergeButtonP.setVisible(false);
    mergeButtonP.setSize(projectDisplayPanelSize.width / 3, projectDisplayPanelSize.height);
    mergeButtonP.setLocation(projectDisplayPanelSize.width / 3 + 20, projectDisplayPanelLocation.y);
    mergeButtonP.setBackground(GREY);
    mergeButtonP.setLayout(null);
    myCP.add(mergeButtonP);

    // Merge button.
    mergeB = new JButton("Merge");
    mergeB.setSize(150, 100);
    mergeB.setLocation(mergeButtonP.getWidth() / 2 - mergeB.getWidth() / 2,
        mergeButtonP.getHeight() / 2 - mergeB.getHeight() / 2);
    mergeB.addActionListener(new MergeBActionListener());
    mergeButtonP.add(mergeB);

    // Merging arrows image.
    picLabel = new JLabel(new ImageIcon(getClass().getResource("img/arrows3.png")));
    picLabel.setSize(329, 250);
    picLabel.setLocation((mergeButtonP.getWidth() - picLabel.getWidth()) / 2,
        (mergeButtonP.getHeight() - picLabel.getHeight()) / 2);
    mergeButtonP.add(picLabel);

    // Panel that holds the components of the second project.
    secondProjectDisplayP = new JPanel();
    secondProjectDisplayP.setVisible(false);
    secondProjectDisplayP.setSize(projectDisplayPanelSize.width / 3,
        projectDisplayPanelSize.height);
    secondProjectDisplayP.setLocation(2 * projectDisplayPanelSize.width / 3 + 20,
        projectDisplayPanelLocation.y);
    secondProjectDisplayP.setLayout(new BoxLayout(secondProjectDisplayP, BoxLayout.Y_AXIS));
    secondProjectDisplayP.setBorder(new EmptyBorder(5, 20, 20, 20));
    myCP.add(secondProjectDisplayP);

    // Second project's name.
    secondProjectTitleL = new JLabel();
    secondProjectTitleL.setFont(HEADER_TWO_FONT);
    secondProjectDisplayP.add(secondProjectTitleL);

    secondProjectDisplayP.add(Box.createRigidArea(new Dimension(0, 10)));

    // Label for the list of screens for the second project
    secondProjectScreensL = new JLabel("Screens");
    secondProjectScreensL.setFont(HEADER_TWO_FONT);
    secondProjectDisplayP.add(secondProjectScreensL);

    // Instructions for the list of screens for the second project.
    secondProjectScreensInstrucL = new JLabel("Check screens to merge into new project");
    secondProjectScreensInstrucL.setFont(HEADER_THREE_FONT);
    secondProjectDisplayP.add(secondProjectScreensInstrucL);

    // List of checkboxes for the screens from the second project.
    secondProjectScreensCBL = new CheckBoxList();
    secondProjectScreensP = new JScrollPane();
    secondProjectDisplayP.add(secondProjectScreensP);

    secondProjectDisplayP.add(Box.createRigidArea(new Dimension(0, 10)));

    // Label for the list of assets for the second project
    secondProjectAssetsL = new JLabel("Assets");
    secondProjectAssetsL.setFont(HEADER_TWO_FONT);
    secondProjectDisplayP.add(secondProjectAssetsL);

    // Instructions for the list of assets for the second project.
    secondProjectAssetsInstrucL = new JLabel("Check assets to merge into new project");
    secondProjectAssetsInstrucL.setFont(HEADER_THREE_FONT);
    secondProjectDisplayP.add(secondProjectAssetsInstrucL);

    // List of checkboxes for the assets from the second project.
    secondProjectAssetsCBL = new CheckBoxList();
    secondProjectAssetsP = new JScrollPane();
    secondProjectDisplayP.add(secondProjectAssetsP);

    // Label for the revision note
    revisionNote = new JLabel("Last revision by Arezu "
        + "Esmaili (July 2015)");
    revisionNote.setFont(new Font("Serif", Font.PLAIN, 12));
    revisionNote.setSize(500, 30);
    revisionNote.setLocation((int) ((screenSize.width * WIDTH_PERCENT_OF_SCREEN)
        - 235), (int) (screenSize.height * HEIGHT_PERCENT_OF_SCREEN) - 75);
    myCP.add(revisionNote);

    setVisible(true);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  }

  public static AIMerger getInstance() {
    return instance;
  }

  public static void main(String[] args) {
    instance = new AIMerger();
  }

}