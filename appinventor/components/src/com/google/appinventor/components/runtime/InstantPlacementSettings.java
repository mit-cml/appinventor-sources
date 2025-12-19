package com.google.appinventor.components.runtime;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the Instant Placement option setting and shared preferences.
 */
public class InstantPlacementSettings {
    public static final String SHARED_PREFERENCES_ID = "SHARED_PREFERENCES_INSTANT_PLACEMENT_OPTIONS";
    public static final String SHARED_PREFERENCES_INSTANT_PLACEMENT_ENABLED =
            "instant_placement_enabled";
    private boolean instantPlacementEnabled = true;
    private SharedPreferences sharedPreferences;

    /**
     * Initializes the current settings based on the saved value.
     */
    public void onCreate(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
        instantPlacementEnabled =
                sharedPreferences.getBoolean(SHARED_PREFERENCES_INSTANT_PLACEMENT_ENABLED, false);
    }

    /**
     * Retrieves whether Instant Placement is enabled,
     */
    public boolean isInstantPlacementEnabled() {
        return instantPlacementEnabled;
    }

    public void setInstantPlacementEnabled(boolean enable) {
        if (enable == instantPlacementEnabled) {
            return; // No change.
        }

        // Updates the stored default settings.
        instantPlacementEnabled = enable;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHARED_PREFERENCES_INSTANT_PLACEMENT_ENABLED, instantPlacementEnabled);
        editor.apply();
    }
}
