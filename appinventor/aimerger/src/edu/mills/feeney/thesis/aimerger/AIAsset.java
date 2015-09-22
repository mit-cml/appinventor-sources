package edu.mills.feeney.thesis.aimerger;

/**
 * A representation of an App Inventor asset (media file).
 *
 * @author feeney.kate@gmail.com (Kate Feeney)
 *         <p/>
 *         Modified by Arezu Esmaili (arezuesmaili1@gmail.com) - July 2015
 */
public class AIAsset {

  // Backing for the asset's directory path from project file
  private String assetPath;

  /**
   * Creates a new AIAsset.
   *
   * @param assetPath the path to the asset within the project file
   */
  public AIAsset(String assetPath) {
    this.assetPath = assetPath;
  }

  /**
   * Returns an AIAsset's directory path within the project file.
   *
   * @return AIAsset's directory path within the project file
   */
  public String getPath() {
    return assetPath;
  }

  /**
   * Returns the AIAsset's name.
   *
   * @return AIAsset's name
   */
  public String getName() {
    // Name includes extension.
    return assetPath.substring(assetPath.lastIndexOf('/') + 1);
  }
}
