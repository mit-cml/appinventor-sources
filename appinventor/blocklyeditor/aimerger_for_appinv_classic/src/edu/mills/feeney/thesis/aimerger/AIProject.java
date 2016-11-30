package edu.mills.feeney.thesis.aimerger;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;

/**
 * A representation of an App Inventor project. This includes its path, name,
 * list of assets, list of screens and project properties file.
 * 
 * @author feeney.kate@gmail.com (Kate Feeney)
 */
public class AIProject {

  // Backing for the project's directory path from home
  private String projectPath;

  // Backing for the list of AIScreens
  private List<AIScreen> screensList;

  // Backing for the list of AIAssets
  private List<AIAsset> assetsList;

  // Backing for the path to the projects properties file
  private String propertiesFilePath;

  // Backing for if the project is a valid source file
  private boolean valid;

  /**
   * Creates a new AIProject.
   * 
   * @param projectPath the path to the project from the home directory
   */
  public AIProject(String projectPath) {
    try {
      this.projectPath = projectPath;
      // Create screens list.
      this.screensList = new LinkedList<AIScreen>();
      // Create assets list.
      this.assetsList = new LinkedList<AIAsset>();
      // Go through each file in the project and create the appropriate classes.
      Enumeration<? extends ZipEntry> e = new ZipFile(new File(projectPath)).entries();
      while (e.hasMoreElements()) {
        // fileName is the path of the file in the project file.
        String fileName = (new ZipEntry(e.nextElement())).getName();
        // Create an AIScreen from any screen file in the project's src folder.
        if (fileName.startsWith("src") && fileName.endsWith(".scm")) {
          AIScreen screen = new AIScreen(fileName);
          screensList.add(screen);
          // Create an AIAsset from any file in the project's assets folder.
        } else if (fileName.startsWith("assets")) {
          AIAsset asset = new AIAsset(fileName);
          assetsList.add(asset);
        } else if (fileName.endsWith("project.properties")) {
          this.setPropertiesFilePath(fileName);
        }
      }
      // Check if valid project, if not show error.
      valid = screensList != null && propertiesFilePath != null;
      if (!valid) {
        JOptionPane.showMessageDialog(AIMerger.getInstance().myCP, "The selected project is not a"
        		+ " project source file! Project source files are zip files.", "File error",
            JOptionPane.ERROR_MESSAGE);
      }
    } catch (ZipException e) {
      JOptionPane.showMessageDialog(AIMerger.getInstance().myCP, 
          "The selected project is not a project source file! Project source files are zip files.", 
          "File error", JOptionPane.ERROR_MESSAGE);
      valid = false;
    } catch (IOException e) {
      JOptionPane.showMessageDialog(AIMerger.getInstance().myCP, 
          "The selected project is not a project source file! Project source files are zip files.", 
          "File error", JOptionPane.ERROR_MESSAGE);
      valid = false;
    }
  }

  /**
   * Returns the AIProject's name.
   * 
   * @return AIProject's name
   */
  public String getProjectName() {
    // The projectName is the name of the zip file.
    if (projectPath.contains(File.separator)) {
      return projectPath.substring(projectPath.lastIndexOf(File.separator) + 1, 
          projectPath.lastIndexOf("."));
    } else {
      return projectPath;
    }
  }

  /**
   * Returns the AIProject's path from home directory.
   * 
   * @return AIProject's path from home directory
   */
  public String getProjectPath() {
    return projectPath;
  }

  /**
   * Returns the AIProject's list of AIScreens.
   * 
   * @return list of project's AIScreens
   */
  public List<AIScreen> getScreensList() {
    return screensList;
  }

  /**
   * Returns the AIProject's list of AIAssets.
   * 
   * @return list of project's AIAssets
   */
  public List<AIAsset> getAssetsList() {
    return assetsList;
  }

  /**
   * Returns the path to the projects properties file from project file.
   * 
   * @return path to project's properties file within the project file
   */
  public String getPropertiesFilePath() {
    return propertiesFilePath;
  }

  /**
   * Sets the projects properties file
   * 
   * @param propertiesFilePath the path to the project's properties file within the project
   */
  public void setPropertiesFilePath(String propertiesFilePath) {
    this.propertiesFilePath = propertiesFilePath;
  }

  /**
   * Returns if the project is valid and can be used for a merge.
   * 
   * @return if project is valid
   */
  public boolean isValid() {
    return this.valid;
  }
}