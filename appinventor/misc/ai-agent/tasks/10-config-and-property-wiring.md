# Task 10: Config & Property Wiring

## Status: Not Started

## Plan Reference
Step 9 from plan.md

## Files to Create
- `components/src/com/google/appinventor/components/common/AIAgentMode.java` — Enum: Off, Advisor, ScreenEditor, ProjectEditor (implements OptionList<String>)
- `client/editor/youngandroid/properties/YoungAndroidAIAgentModeChoicePropertyEditor.java` — Dropdown property editor for AIAgentMode

Path prefixes: `appinventor/` for all.

## Files to Modify
- `components/src/com/google/appinventor/components/runtime/Form.java` — Add AIAgentMode dropdown property (default "Off"), getter/setter with @DesignerProperty and @SimpleProperty annotations
- `client/editor/simple/components/MockForm.java` — Add PROPERTY_NAME_AI_AGENT_MODE constant, onPropertyChange() sync to project settings, isPropertyVisible() shows on Screen1 only when ai.agent.available is true
- `client/editor/simple/components/utils/PropertiesUtil.java` — Register YoungAndroidAIAgentModeChoicePropertyEditor for PROPERTY_TYPE_AI_AGENT_MODE
- `components/src/com/google/appinventor/components/common/PropertyTypeConstants.java` — Add PROPERTY_TYPE_AI_AGENT_MODE = "ai_agent_mode"
- `shared/rpc/user/Config.java` — Add aiAgentAvailable field + getter/setter
- `server/UserInfoServiceImpl.java` — Read ai.agent.available flag into Config via Flag.createFlag
- `shared/settings/SettingsConstants.java` — Add YOUNG_ANDROID_SETTINGS_AI_AGENT_MODE constant
- `components/src/com/google/appinventor/components/common/YaVersion.java` — Bump FORM_COMPONENT_VERSION 31 → 32

## Dependencies
None — foundational config task

## Acceptance Criteria

### AIAgentMode enum
- [ ] Off, Advisor, ScreenEditor, ProjectEditor values
- [ ] Implements OptionList<String> with toUnderlyingValue() and fromUnderlyingValue()

### Form property
- [ ] AIAgentMode property with @DesignerProperty(editorType = PROPERTY_TYPE_AI_AGENT_MODE, defaultValue = "Off")
- [ ] @SimpleProperty with userVisible = false
- [ ] Property category: BEHAVIOR

### MockForm integration
- [ ] PROPERTY_NAME_AI_AGENT_MODE constant
- [ ] onPropertyChange() syncs to project settings via changeProjectSettingsProperty()
- [ ] isPropertyVisible(): only on Screen1 AND Ode.getSystemConfig().getAiAgentAvailable()

### Server config
- [ ] Config.java: aiAgentAvailable boolean with getter/setter
- [ ] UserInfoServiceImpl: reads Flag("ai.agent.available", false)

### Property editor
- [ ] Extends ChoicePropertyEditor with 4 choices (Off, Advisor, Screen Editor, Project Editor)
- [ ] Registered in PropertiesUtil for PROPERTY_TYPE_AI_AGENT_MODE

### Version bump
- [ ] FORM_COMPONENT_VERSION incremented for new property

## Progress Log
