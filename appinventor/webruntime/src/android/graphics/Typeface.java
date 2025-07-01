package android.graphics;

import android.annotation.NonNull;

public class Typeface {

  /** The default NORMAL typeface object */
  public static final Typeface DEFAULT = null;
  /**
   * The default BOLD typeface object. Note: this may be not actually be
   * bold, depending on what fonts are installed. Call getStyle() to know
   * for sure.
   */
  public static final Typeface DEFAULT_BOLD = null;
  /** The NORMAL style of the default sans serif typeface. */
  public static final Typeface SANS_SERIF = new Typeface();
  /** The NORMAL style of the default serif typeface. */
  public static final Typeface SERIF = null;
  /** The NORMAL style of the default monospace typeface. */
  public static final Typeface MONOSPACE = null;

  public static final int NORMAL = 0;
  public static final int BOLD = 1;
  public static final int ITALIC = 2;
  public static final int BOLD_ITALIC = 3;

  private String familyName;
  private boolean bold;
  private boolean italic;

  private Typeface() {
    this.familyName = "sans-serif"; // Default family name
    this.bold = false;
    this.italic = false;
  }

  /**
   * Create a typeface object given a family name, and option style information.
   * If null is passed for the name, then the "default" font will be chosen.
   * The resulting typeface object can be queried (getStyle()) to discover what
   * its "real" style characteristics are.
   *
   * @param familyName May be null. The name of the font family.
   * @param style  The style (normal, bold, italic) of the typeface.
   *               e.g. NORMAL, BOLD, ITALIC, BOLD_ITALIC
   * @return The best matching typeface.
   */
  public static Typeface create(Typeface familyName, int style) {
    Typeface tf = new Typeface();
    tf.familyName = familyName.familyName;
    tf.bold = (style & Typeface.BOLD) != 0;
    tf.italic = (style & Typeface.ITALIC) != 0;
    return tf;
  }

  public String getSystemFontFamilyName() {
    return familyName;
  }

  public boolean isBold() {
    return bold;
  }

  public boolean isItalic() {
    return italic;
  }
}
