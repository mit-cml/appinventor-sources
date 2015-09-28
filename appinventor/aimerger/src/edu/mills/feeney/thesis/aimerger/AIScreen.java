package edu.mills.feeney.thesis.aimerger;

/**
 * A representation of an App Inventor screen.
 *
 * @author feeney.kate@gmail.com (Kate Feeney)
 *         <p/>
 *         Modified by Arezu Esmaili (arezuesmaili1@gmail.com) - July 2015
 */
public class AIScreen {

  // Backing for the screen's directory path from project file
  private String screenPath;

  /**
   * Creates a new AIScreen.
   *
   * @param screenPath the path to the screen within the project file
   */
  public AIScreen(String screenPath) {
    this.screenPath = screenPath;
  }

  /**
   * Returns an AIScreen's directory path within the project file.
   *
   * @return AIScreen's directory path within the project file
   */
  public String getPath() {
    return screenPath;
  }

  /**
   * Returns the AIScreen's name.
   *
   * @return AIScreen's name
   */
  public String getName() {
    // The screenName is the name of the screen's file.
    return screenPath.substring(screenPath.lastIndexOf('/') + 1, screenPath.lastIndexOf('.'));
  }

  /**
   * Returns the AIScreen's file extension.
   *
   * @return AIScreen's file extension
   */
  public String getExtension() {
    return screenPath.substring(screenPath.lastIndexOf('.'));
  }
}
