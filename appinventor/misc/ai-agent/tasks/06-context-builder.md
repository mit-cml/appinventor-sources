# Task 06: Context Builder

## Status: Not Started

## Plan Reference
Step 3 from plan.md (context strategy section)

## Files to Create
- `server/aiagent/AIContextBuilder.java` — Builds the tiered LLM prompt (Layers 1-5)

Path relative to `appinventor/appengine/src/com/google/appinventor/`.

## Files to Modify
None

## Dependencies
- Task 04: BlocksPseudocodeGenerator (for converting BKY to pseudocode in Layer 4b)
- Task 13: Static resource files (appinventor_reference.md, component_catalog.json, few_shot_examples.json)

## Acceptance Criteria

### Layer 1: Static system prompt (~5K tokens)
- [ ] Assembles from appinventor_reference.md resource
- [ ] Includes: platform description, role/instructions, operation output schemas, format conventions
- [ ] Mode-specific tool filtering: Advisor gets only lookup tools, ScreenEditor gets component/block tools, ProjectEditor gets all tools

### Layer 2: Compact component catalog (~3K tokens)
- [ ] Pre-generated listing of ~100 user-facing components (excludes 7 INTERNAL-category components)
- [ ] Each entry: name, category, visibility, description, top 4-6 properties/events/methods
- [ ] Extension components included with same format, tagged with ext:true

### Layer 3: On-demand lookup_component tool
- [ ] Tool definition for querying full component metadata
- [ ] Returns complete entry from simple_components.json

### Layer 4: Current app state (per-request)
- [ ] buildProjectOverview(): project name, app name, version, theme, colors, sizing, screens list, assets list, extensions list
- [ ] buildComponentTree(scmJson): parses SCM JSON, walks $Components recursively, emits indented tree with type/name/non-default properties, tags non-visible components
- [ ] Blocks pseudocode via BlocksPseudocodeGenerator.generate(bkyContent)
- [ ] Multi-screen: full state for current screen, one-line summaries for others

### Layer 5: Few-shot examples (~3K tokens)
- [ ] 3-4 complete worked examples from few_shot_examples.json
- [ ] Shows input (user message) → output (operations list)

### General
- [ ] build(userId, projectId, screenName, mode) produces complete system prompt
- [ ] Static content (Layers 1, 2, 5) cached on server startup
- [ ] Layer 4 built fresh per-request from project state
- [ ] Extension metadata loaded from project's assets/external_comps/*/components.json
- [ ] Total baseline: ~10-12K tokens per request

## Progress Log
