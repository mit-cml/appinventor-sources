package android.content.res;

import android.util.StateSet;

public class ColorStateList {
  private int[][] mStateSpecs;
  private int[] mColors;

  public ColorStateList(int[][] states, int[] colors) {
    // TODO(ewpatton): Real implementation
    mStateSpecs = states;
    mColors = colors;
  }

  public int getColorForState(int[] stateSet, int defaultColor) {
    final int setLength = mStateSpecs.length;
    for (int i = 0; i < setLength; i++) {
      final int[] stateSpec = mStateSpecs[i];
      if (StateSet.stateSetMatches(stateSpec, stateSet)) {
        return mColors[i];
      }
    }
    return defaultColor;
  }
}
