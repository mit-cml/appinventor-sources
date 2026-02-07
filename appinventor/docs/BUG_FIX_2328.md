# Bug Fix: Adaptive Icon White Edges (#2328)

## Issue
Android apps built with App Inventor showed thin white borders or "slivers" around their adaptive icons on Android 8.0+ devices.

## Root Cause
The adaptive icon's background layer was hardcoded to solid white (`#ffffff`) in the build server configuration file `XmlConfig.java`.

## Solution
Changed the launcher background color from `#ffffff` (opaque white) to `#00ffffff` (transparent white) in the `writeLauncherBackground()` method.

## Impact
- Eliminates white edges/borders on adaptive icons
- Preserves the adaptive icon parallax motion effect
- No user-facing API changes
- Affects only Android 8.0+ devices with adaptive icon support

## Files Modified
- `appinventor/buildserver/src/com/google/appinventor/buildserver/tasks/android/XmlConfig.java` (Line 437)

## Testing
Manually tested by building apps and verifying that adaptive icons no longer show white edges on Android 8.0+ devices.

## Related
- Issue: #2328
- Pull Request: #3744
