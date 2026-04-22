# Neo UI Consistency Fixes

## Issues Fixed

This PR addresses multiple UI consistency issues in the Neo interface:

### 1. Chromebook Connect Menu Reset (#3660)

**Problem:** After disconnecting on Chromebooks, the Connect menu would incorrectly show Emulator and USB options which aren't available on Chromebooks.

**Solution:** Updated `TopToolbar.java` to explicitly manage Chromebook vs non-Chromebook connection states in the `updateConnectToDropDownButton()` method.

**Changes:**
- When disconnected on Chromebook: Enable Wireless and Chromebook options, disable Emulator and USB
- When disconnected on non-Chromebook: Enable Wireless, Emulator, and USB, disable Chromebook
- When connected: Disable all connection options, enable only Refresh Companion

**Technical:** Switched from `setItemEnabled(MESSAGES.xxx())` to `setItemEnabledById(WIDGET_NAME_xxx)` for more reliable state management using IDs instead of text labels.

### 2. View State Constants

**Problem:** Code used magic numbers (0, 1) to check view states, making code unclear.

**Solution:** 
- Changed `if (view == 0)` to `if (view == Ode.PROJECTS)`
- Changed `else` to `else if (view == Ode.DESIGNER)`

This makes the code more readable and maintainable.

### 3. Adaptive Icon White Edges (#2328)

**Problem:** Adaptive icons had white edges because background color was set to `#ffffff` (opaque white).

**Solution:** Changed launcher background color from `#ffffff` to `#00ffffff` (transparent white) in `XmlConfig.java`, eliminating the white edges while preserving the parallax motion effect.

## Files Modified

1. **appinventor/appengine/src/com/google/appinventor/client/TopToolbar.java**
   - Chromebook connection state management
   - View constant improvements

2. **appinventor/buildserver/src/com/google/appinventor/buildserver/tasks/android/XmlConfig.java**
   - Adaptive icon background transparency fix

## Testing

- Chromebook menu behavior tested on Chromebook devices
- Adaptive icon fix manually tested by building apps and verifying no white edges on Android 8.0+ devices
- View state changes verified through code review

## Impact

- Improves user experience on Chromebooks
- Makes code more maintainable with named constants
- Fixes visual artifact on Android adaptive icons
- No breaking changes to existing functionality
