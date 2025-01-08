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
 * list of assets, list of screens, and project properties file.
 *
 * <p>Provides functionality to validate and extract components of the project.
 *
 * @author feeney.kate@gmail.com (Kate Feeney)
 */
public class AIProject {

  /** Path to the project file. */
  private final String projectPath;

  /** List of screens in the project. */
  private final List<AIScreen> screensList;

  /** List of assets in the project. */
  private final List<AIAsset> assetsList;

  /** Path to the project's properties file. */
  private String propertiesFilePath;

  /** Indicates whether the project is valid. */
  private boolean valid;

  /**
   * Creates a new AIProject.
   *
   * @param projectPath the path to the project file (ZIP archive)
   */
  public AIProject(String projectPath) {
    this.projectPath = projectPath;
    this.screensList = new LinkedList<>();
    this.assetsList = new LinkedList<>();
    this.valid = false;
    initializeProject();
  }

  /**
   * Initializes the project by reading the ZIP file and extracting components.
   */
  private void initializeProject() {
    try (ZipFile zipFile = new ZipFile(new File(projectPath))) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String fileName = entry.getName();

        if (fileName.startsWith("src") && fileName.endsWith(".scm")) {
          screensList.add(new AIScreen(fileName));
        } else if (fileName.startsWith("assets")) {
          assetsList.add(new AIAsset(fileName));
        } else if (fileName.endsWith("project.properties")) {
          propertiesFilePath = fileName;
        }
      }

      validateProject();
    } catch (ZipException e) {
      showErrorDialog("The selected file is not a valid project source file. Source files must be ZIP archives.");
    } catch (IOException e) {
      showErrorDialog("An error occurred while reading the project file.");
    }
  }

  /**
   * Validates the project by ensuring essential components are present.
   */
  private void validateProject() {
    valid = !screensList.isEmpty() && propertiesFilePath != null;
    if (!valid) {
      showErrorDialog("The selected project is missing essential components (e.g., screens or properties file).");
    }
  }

  /**
   * Displays an error dialog with the given message.
   *
   * @param message the error message to display
   */
  private void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(
        AIMerger.getInstance().myCP,
        message,
        "File Error",
        JOptionPane.ERROR_MESSAGE
    );
  }

  /**
   * Returns the name of the project, derived from the ZIP file name.
   *
   * @return the project name
   */
  public String getProjectName() {
    File file = new File(projectPath);
    String fileName = file.getName();
    int dotIndex = fileName.lastIndexOf('.');
    return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
  }

  /**
   * Returns the path to the project file.
   *
   * @return the project file path
   */
  public String getProjectPath() {
    return projectPath;
  }

  /**
   * Returns the list of screens in the project.
   *
   * @return a list of {@link AIScreen} objects
   */
  public List<AIScreen> getScreensList() {
    return screensList;
  }

  /**
   * Returns the list of assets in the project.
   *
   * @return a list of {@link AIAsset} objects
   */
  public List<AIAsset> getAssetsList() {
    return assetsList;
  }

  /**
   * Returns the path to the project's properties file within the ZIP archive.
   *
   * @return the properties file path
   */
  public String getPropertiesFilePath() {
    return propertiesFilePath;
  }

  /**
   * Checks whether the project is valid for merging.
   *
   * @return {@code true} if the project is valid, {@code false} otherwise
   */
  public boolean isValid() {
    return valid;
  }
}
