package android.util;

public class StateSet {
  public static final int[] WILD_CARD = new int[0];

  public static boolean isWildCard(int[] stateSetOrSpec) {
    return stateSetOrSpec.length == 0 || stateSetOrSpec[0] == 0;
  }

  public static boolean stateSetMatches(int[] stateSpec, int[] stateSet) {
    if (stateSet == null) {
      return (stateSpec == null || isWildCard(stateSpec));
    }
    int stateSpecSize = stateSpec.length;
    int stateSetSize = stateSet.length;
    for (int i = 0; i < stateSpecSize; i++) {
      int stateSpecState = stateSpec[i];
      if (stateSpecState == 0) {
        // We've reached the end of the cases to match against.
        return true;
      }
      final boolean mustMatch;
      if (stateSpecState > 0) {
        mustMatch = true;
      } else {
        // We use negative values to indicate must-NOT-match states.
        mustMatch = false;
        stateSpecState = -stateSpecState;
      }
      boolean found = false;
      for (int j = 0; j < stateSetSize; j++) {
        final int state = stateSet[j];
        if (state == 0) {
          // We've reached the end of states to match.
          if (mustMatch) {
            // We didn't find this must-match state.
            return false;
          } else {
            // Continue checking other must-not-match states.
            break;
          }
        }
        if (state == stateSpecState) {
          if (mustMatch) {
            found = true;
            // Continue checking other other must-match states.
            break;
          } else {
            // Any match of a must-not-match state returns false.
            return false;
          }
        }
      }
      if (mustMatch && !found) {
        // We've reached the end of states to match and we didn't
        // find a must-match state.
        return false;
      }
    }
    return true;
  }
}
