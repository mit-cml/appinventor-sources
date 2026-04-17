# Move AIAgentMode from Form Property to ProjectSettings-Only

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove AIAgentMode from the Form component so it no longer appears in .scm files or AIA exports, storing it exclusively in the Datastore-backed ProjectSettings.

**Architecture:** AIAgentMode is currently a `@DesignerProperty` on `Form.java`, which serializes it into Screen1.scm (exported in AIA files). The client already reads it from ProjectSettings via `AIContextCollector.getCurrentAIAgentMode()`. The only consumer of the .scm copy is the server-side `AIAgentEngine.getProjectAIMode()`. We remove the Form property entirely and make both client and server use ProjectSettings exclusively. Since `FORM_COMPONENT_VERSION` 32 exists only on the `ai-agent` branch, we revert it to 31.

**Tech Stack:** Java (GWT client + App Engine server), JavaScript (Blockly versioning)

---

## File Map

| Action | File | Responsibility |
|--------|------|---------------|
| Modify | `appengine/src/.../server/aiagent/AIAgentEngine.java` | Switch `getProjectAIMode()` to read from Datastore ProjectSettings |
| Modify | `appengine/src/.../client/editor/youngandroid/aiagent/AIModeSelectionDialog.java` | Write mode to ProjectSettings directly, remove Form/MockForm dependency |
| Modify | `appengine/src/.../client/editor/simple/components/MockForm.java` | Remove `PROPERTY_NAME_AI_AGENT_MODE` constant and its two usages |
| Modify | `components/src/.../components/runtime/Form.java` | Remove `AIAgentMode` property method and annotations |
| Modify | `components/src/.../components/common/YaVersion.java` | Revert `FORM_COMPONENT_VERSION` from 32 to 31 |
| Modify | `appengine/src/.../client/youngandroid/YoungAndroidFormUpgrader.java` | Remove version 32 upgrade block |
| Modify | `blocklyeditor/src/versioning.js` | Remove version 32 entry for Screen |
| Modify | `appengine/src/.../client/editor/simple/components/utils/PropertiesUtil.java` | Remove AI_AGENT_MODE editor case and import |
| Modify | `docs/markdown/reference/components/userinterface.md` | Remove AIAgentMode property docs |
| Delete | `components/src/.../components/common/AIAgentMode.java` | No longer needed (was the enum for the Form property) |
| Delete | `appengine/src/.../client/editor/youngandroid/properties/YoungAndroidAIAgentModeChoicePropertyEditor.java` | No longer needed (Form property editor) |

Note: paths in the File Map above are abbreviated. Full paths relative to repo root use the `appinventor/` prefix (e.g. `appinventor/docs/markdown/...`).

Files that stay unchanged:
- `shared/settings/SettingsConstants.java` — still defines `YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE` and mode value constants (used by ProjectSettings, AIContextCollector, AIChatDialog, AIAgentEngine)
- `client/settings/project/YoungAndroidSettings.java` — already registers `AIAgentMode` with default "Off" in ProjectSettings
- `client/editor/youngandroid/aiagent/AIContextCollector.java` — already reads from ProjectSettings
- `client/editor/youngandroid/AIChatDialog.java` — already reads from ProjectSettings via AIContextCollector
- `components/src/.../components/common/PropertyTypeConstants.java` — `PROPERTY_TYPE_AI_AGENT_MODE` becomes unused but is harmless; removing it is optional cleanup
- `components/src/.../components/annotations/PropertyCategory.java` — `AI` enum value becomes unused but is harmless

**Backward compatibility with existing .scm files:** Projects that already have `"AIAgentMode"` in their Screen1.scm will self-clean. On load, `MockComponent.changeProperty` ignores unrecognized properties (logs a warning, returns false). On the next save, the encoder only serializes registered properties, so the stale key is stripped automatically.

---

### Task 1: Switch server-side mode reading to ProjectSettings

**Files:**
- Modify: `appengine/src/com/google/appinventor/server/aiagent/AIAgentEngine.java:563-590`

- [ ] **Step 1: Rewrite `getProjectAIMode()` to read from Datastore settings**

Replace the current method that parses Screen1.scm with one that reads from `storageIo.loadProjectSettings()`:

```java
public String getProjectAIMode(String userId, long projectId) {
  try {
    String settingsJson = storageIo.loadProjectSettings(userId, projectId);
    if (settingsJson != null && !settingsJson.isEmpty()) {
      JSONObject settings = new JSONObject(settingsJson);
      JSONObject simple = settings.optJSONObject(
          SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS);
      if (simple != null) {
        String mode = simple.optString(
            SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE, AI_AGENT_MODE_OFF);
        if (!mode.isEmpty()) {
          return mode;
        }
      }
    }
  } catch (Exception e) {
    LOG.log(Level.WARNING, "Failed to read AI agent mode for project " + projectId, e);
  }
  return AI_AGENT_MODE_OFF;
}
```

Remove the `ContextUtils` import (line 8) — `getProjectAIMode()` was its only usage in this file.

- [ ] **Step 2: Add SettingsConstants import if not already present**

The file already imports `SettingsConstants` values via static imports for `AI_AGENT_MODE_OFF` etc. Add:

```java
import com.google.appinventor.shared.settings.SettingsConstants;
```

if not already present (check existing imports).

- [ ] **Step 3: Build and verify**

Run: `cd appinventor && ant -f appengine/build.xml AiServerLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```
feat(ai-agent): read AIAgentMode from ProjectSettings instead of Screen1.scm
```

---

### Task 2: Update AIModeSelectionDialog to write directly to ProjectSettings

**Files:**
- Modify: `appengine/src/com/google/appinventor/client/editor/youngandroid/aiagent/AIModeSelectionDialog.java`

- [ ] **Step 1: Replace the form.changeProperty() call with direct ProjectSettings write**

In the `onClick` handler (around line 104-116), replace:

```java
        // Set the AIAgentMode on the Screen1 form component property.
        // MockForm.onPropertyChange will propagate this to project settings
        // and the property will be persisted in Screen1.scm for the backend.
        ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
            .getOpenProjectEditor(contextCollector.getCurrentProjectId());
        if (projectEditor instanceof YaProjectEditor) {
          YaProjectEditor yaProjectEditor = (YaProjectEditor) projectEditor;
          MockForm form = (MockForm) yaProjectEditor.getFormFileEditor("Screen1").getRoot();
          if (form != null) {
            form.changeProperty(
                SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE,
                selectedMode);
          }
        }
```

with:

```java
        // Write mode directly to ProjectSettings (Datastore-backed, not in .scm/AIA files).
        ProjectEditor projectEditor = Ode.getInstance().getEditorManager()
            .getOpenProjectEditor(contextCollector.getCurrentProjectId());
        if (projectEditor != null) {
          projectEditor.changeProjectSettingsProperty(
              SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
              SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE,
              selectedMode);
        }
```

- [ ] **Step 2: Update the Javadoc**

Change the class-level Javadoc (line 34-36) from:

```java
 * <p>When the user selects a mode and confirms, the AIAgentMode project
 * setting is updated on Screen1 and the provided callback is invoked.</p>
```

to:

```java
 * <p>When the user selects a mode and confirms, the AIAgentMode project
 * setting is updated and the provided callback is invoked.</p>
```

And the `show()` method Javadoc (line 55-56) from:

```java
   * "Select and Open", the AIAgentMode property is set on Screen1, dirty
   * editors are saved, and then {@code onModeSelected} is invoked.
```

to:

```java
   * "Select and Open", the AIAgentMode project setting is persisted and
   * then {@code onModeSelected} is invoked.
```

- [ ] **Step 3: Update the saveDirtyEditors comment**

Around line 119-120, update the stale comment:

```java
        // Force an immediate save so the backend sees the updated mode
        // in Screen1.scm before the first AI request is sent.
```

to:

```java
        // Force an immediate save so the backend sees the updated mode
        // in project settings before the first AI request is sent.
```

- [ ] **Step 4: Remove unused imports**

Remove these imports that are no longer needed:

```java
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
```

Keep `ProjectEditor` (still used) and `SettingsConstants` (still used).

- [ ] **Step 5: Build and verify**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
refactor(ai-agent): write AIAgentMode directly to ProjectSettings
```

---

### Task 3: Remove AIAgentMode from Form component and MockForm

**Files:**
- Modify: `components/src/com/google/appinventor/components/runtime/Form.java:1421-1431`
- Modify: `appengine/src/com/google/appinventor/client/editor/simple/components/MockForm.java:391,831,1394`

- [ ] **Step 1: Remove the AIAgentMode property from Form.java**

Delete the entire method and its annotations (lines 1421-1431):

```java
  /**
   * Sets the AI agent mode for this project.
   * Controls what the AI assistant is allowed to do: Off, Advisor (read-only),
   * ScreenEditor (current screen), or ProjectEditor (full project).
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_AI_AGENT_MODE,
      defaultValue = "Off")
  @SimpleProperty(userVisible = false, category = PropertyCategory.AI)
  public void AIAgentMode(String mode) {
    // Designer-only property; no runtime effect.
  }
```

- [ ] **Step 2: Remove the PROPERTY_NAME_AI_AGENT_MODE constant from MockForm.java**

Delete line 391:

```java
  private static final String PROPERTY_NAME_AI_AGENT_MODE = SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE;
```

- [ ] **Step 3: Remove the isPropertyVisible case from MockForm.java**

In the `isPropertyVisible` switch (around line 831), remove:

```java
      case PROPERTY_NAME_AI_AGENT_MODE: {
        return false;
      }
```

Note: this was a `case` in a switch statement. Verify the surrounding cases still compile correctly (the `default` case should follow the previous case like `PROPERTY_NAME_DEFAULTFILESCOPE`).

- [ ] **Step 4: Remove the onPropertyChange handler from MockForm.java**

Around line 1394, remove:

```java
    } else if (propertyName.equals(PROPERTY_NAME_AI_AGENT_MODE)) {
      if (editor.isScreen1()) {
        editor.getProjectEditor().changeProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE, newValue);
      }
    }
```

Make sure the preceding `else if` block's closing `}` connects to the end of the `onPropertyChange` method correctly (the closing `}` of the method body at the old line 1401 should remain).

- [ ] **Step 5: Build and verify**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
refactor(ai-agent): remove AIAgentMode from Form component and MockForm
```

---

### Task 4: Revert FORM_COMPONENT_VERSION and remove upgrade/versioning entries

**Files:**
- Modify: `components/src/com/google/appinventor/components/common/YaVersion.java:1077-1082`
- Modify: `appengine/src/com/google/appinventor/client/youngandroid/YoungAndroidFormUpgrader.java:1261-1265`
- Modify: `blocklyeditor/src/versioning.js:3103-3105`

- [ ] **Step 1: Revert FORM_COMPONENT_VERSION to 31 in YaVersion.java**

Change lines 1080-1082 from:

```java
  // For FORM_COMPONENT_VERSION 32:
  // - Added AIAgentMode property.
  public static final int FORM_COMPONENT_VERSION = 32;
```

to:

```java
  public static final int FORM_COMPONENT_VERSION = 31;
```

(Remove the version 32 comment entirely.)

- [ ] **Step 2: Remove version 32 upgrade block from YoungAndroidFormUpgrader.java**

Delete lines 1261-1265:

```java
    if (srcCompVersion < 32) {
      // The AIAgentMode property was added.
      // No properties need to be modified to upgrade to version 32.
      srcCompVersion = 32;
    }
```

- [ ] **Step 3: Remove version 32 entry from versioning.js**

Delete lines 3103-3105:

```javascript
    // For FORM_COMPONENT_VERSION 32:
    // - The AIAgentMode designer property was added. No block changes required.
    32: "noUpgrade"
```

Also: the preceding line (version 31 entry) currently ends with a comma. After removing version 32, version 31 becomes the last entry. **Remove the trailing comma** from the version 31 line so it reads:

```javascript
    31: "noUpgrade"
```

(No trailing comma before `}, // End Screen`.)

- [ ] **Step 4: Build and verify**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```
refactor(ai-agent): revert FORM_COMPONENT_VERSION to 31, remove version 32 entries
```

---

### Task 5: Delete dead code and clean up remaining references

**Files:**
- Delete: `components/src/com/google/appinventor/components/common/AIAgentMode.java`
- Delete: `appengine/src/com/google/appinventor/client/editor/youngandroid/properties/YoungAndroidAIAgentModeChoicePropertyEditor.java`
- Modify: `appengine/src/com/google/appinventor/client/editor/simple/components/utils/PropertiesUtil.java:356-357`
- Modify: `docs/markdown/reference/components/userinterface.md:1229-1235`

- [ ] **Step 1: Delete AIAgentMode.java**

```bash
rm components/src/com/google/appinventor/components/common/AIAgentMode.java
```

- [ ] **Step 2: Delete YoungAndroidAIAgentModeChoicePropertyEditor.java**

```bash
rm appengine/src/com/google/appinventor/client/editor/youngandroid/properties/YoungAndroidAIAgentModeChoicePropertyEditor.java
```

- [ ] **Step 3: Remove the AI_AGENT_MODE case from PropertiesUtil.java**

Remove lines 356-357:

```java
    } else if (editorType.equals(PropertyTypeConstants.PROPERTY_TYPE_AI_AGENT_MODE)) {
      return new YoungAndroidAIAgentModeChoicePropertyEditor();
```

Also remove the import for `YoungAndroidAIAgentModeChoicePropertyEditor` (line 16):

```java
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidAIAgentModeChoicePropertyEditor;
```

- [ ] **Step 4: Remove AIAgentMode docs from userinterface.md**

Delete lines 1229-1232:

```markdown
{:id="Screen.AIAgentMode" .text .wo .do} *AIAgentMode*
: Sets the AI agent mode for this project.
 Controls what the AI assistant is allowed to do: Off, Advisor (read-only),
 ScreenEditor (current screen), or ProjectEditor (full project).
```

Leave a blank line between the surrounding properties so formatting stays clean.

- [ ] **Step 5: Build full client to verify nothing is broken**

Run: `cd appinventor && ant -f appengine/build.xml AiClientLib`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```
chore(ai-agent): delete dead AIAgentMode Form property code and docs
```

---

### Task 6: Final verification

- [ ] **Step 1: Full build**

Run: `cd appinventor && ant -f appengine/build.xml YaClientApp`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify no stale references**

Run a grep to confirm no remaining references to `AIAgentMode` in Form, MockForm, or .scm contexts:

```bash
grep -r "AIAgentMode" --include="*.java" --include="*.js" | grep -v "SettingsConstants\|YoungAndroidSettings\|AIContextCollector\|AIChatDialog\|AIAgentServiceImpl\|AIAgentEngine\|AIModeSelectionDialog\|ModeModule\|ModeEnforcer\|CONTRIBUTING_AI"
```

Expected: no hits (only the files that legitimately still reference the setting constant should appear).

- [ ] **Step 3: Commit if any fixups were needed, otherwise done**
