# AI Agent Locale Context

## Problem

The AI agent has no awareness of the user's chosen interface language. It always responds in English regardless of the user's locale setting.

## Goal

Share the user's locale with the AI agent so it defaults to responding in that language, while remaining adaptive — if the user writes in a different language, the AI matches their language.

## Changes

### 1. `AIAgentRequest.java` — Add `locale` and `languageDisplayName` fields with getters/setters

### 2. `AIContextCollector.java` — Read locale from `LocaleInfo.getCurrentLocale()` and display name from `LocaleInfo.getLocaleNativeDisplayName()`, set on request

### 3. `ContextParams.java` — Add `locale` and `languageDisplayName` fields, update constructor

### 4. `AIAgentServiceImpl.java` → `AIAgentEngine.java` → `AIContextBuilder.java` — Thread locale through to `buildContextMessages()`

### 5. `ModeModule.java` — Append language instruction to context message when locale is not English:
> The user's interface language is {displayName} ({locale}). By default, respond in this language. However, if the user writes in a different language, respond in the language they are using.

### Edge cases
- `locale` null/empty/`"default"`/`"en"` → no instruction (English is LLM default)
- `languageDisplayName` missing → use raw locale code
- User switches language mid-session → next request picks up new locale automatically
