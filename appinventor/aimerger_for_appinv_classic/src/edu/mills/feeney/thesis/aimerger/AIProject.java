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
 * Represents an App Inventor project, including its path, name,
 * list of assets, screens, and properties file.
 * 
 * @autor feeney.kate@gmail.com (Kate Feeney)
 */
public class AIProject {

    private final String projectPath; // Project's directory path
    private final List<AIScreen> screensList; // List of screens in the project
    private final List<AIAsset> assetsList; // List of assets in the project
    private String propertiesFilePath; // Path to the project properties file
    private boolean valid; // Validity of the project

    /**
     * Creates a new AIProject from a project file path.
     *
     * @param projectPath the path to the project zip file
     */
    public AIProject(String projectPath) {
        this.projectPath = projectPath;
        this.screensList = new LinkedList<>();
        this.assetsList = new LinkedList<>();
        loadProject();
    }

    /**
     * Loads the project by reading from the provided zip file path.
     */
    private void loadProject() {
        try (ZipFile zipFile = new ZipFile(new File(projectPath))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                processZipEntry(entries.nextElement());
            }

            validateProject();
        } catch (ZipException e) {
            showErrorDialog("Error opening zip file: " + e.getMessage(), "File error");
            valid = false;
        } catch (IOException e) {
            showErrorDialog("Error reading file: " + e.getMessage(), "File error");
            valid = false;
        }
    }

    /**
     * Processes individual entries in the zip file to categorize them as screens,
     * assets, or properties.
     *
     * @param entry the zip entry representing a file in the project
     */
    private void processZipEntry(ZipEntry entry) {
        String fileName = entry.getName();

        if (fileName.startsWith("src") && fileName.endsWith(".scm")) {
            screensList.add(new AIScreen(fileName));
        } else if (fileName.startsWith("assets")) {
            assetsList.add(new AIAsset(fileName));
        } else if (fileName.endsWith("project.properties")) {
            setPropertiesFilePath(fileName);
        }
    }

    /**
     * Validates the project to ensure it has essential components.
     */
    private void validateProject() {
        valid = !screensList.isEmpty() && propertiesFilePath != null;
        if (!valid) {
            showErrorDialog("The selected project is not a valid source file! Project source files are zip files.", "File error");
        }
    }

    /**
     * Shows an error dialog with the given message and title.
     *
     * @param message the error message
     * @param title   the dialog title
     */
    private void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(AIMerger.getInstance().myCP, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Returns the AIProject's name derived from the zip file name.
     *
     * @return AIProject's name
     */
    public String getProjectName() {
        String fileName = new File(projectPath).getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public String getProjectPath() {
        return projectPath;
    }

    public List<AIScreen> getScreensList() {
        return screensList;
    }

    public List<AIAsset> getAssetsList() {
        return assetsList;
    }

    public String getPropertiesFilePath() {
        return propertiesFilePath;
    }

    public void setPropertiesFilePath(String propertiesFilePath) {
        this.propertiesFilePath = propertiesFilePath;
    }

    public boolean isValid() {
        return valid;
    }
}
