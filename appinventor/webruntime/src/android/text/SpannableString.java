package android.text;

public class SpannableString implements CharSequence {
  private String mString;

  public SpannableString(Spanned spanned) {
    // This constructor is not implemented in the original code.
    // It should initialize mString with the string representation of spanned.
    mString = spanned.toString();
  }

  @Override
  public int length() {
    return 0;
  }

  @Override
  public char charAt(int index) {
    return 0;
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return null;
  }
}
