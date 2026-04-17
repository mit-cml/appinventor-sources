# Task 11: Deployment Wiring

## Status: Not Started

## Plan Reference
Steps 10a, 10b, 10c from plan.md

## Files to Create
None

## Files to Modify
- `appinventor/appengine/war/WEB-INF/web.xml` — Add aiAgentService servlet + servlet-mapping + odeAuthFilter filter-mapping
- `appinventor/appengine/war/WEB-INF/appengine-web.xml` — Add AI configuration system properties (ai.agent.available, ai.agent.provider, ai.agent.model, ai.agent.api.key, ai.agent.base.url, ai.agent.rate.limit)
- `appinventor/appengine/src/com/google/appinventor/YaClient.gwt.xml` — Add <servlet path="/aiagent"> and <source path="shared/rpc/aiagent"/>
- `appinventor/appengine/build.xml` — Add <copy> rule for server/aiagent/resources/*.md,*.json files

## Dependencies
- Task 01: RPC paths must exist (shared/rpc/aiagent/ source path)
- Task 03: Servlet class (AIAgentServiceImpl for web.xml registration)

## Acceptance Criteria

### web.xml
- [ ] <servlet> entry for aiAgentService with class com.google.appinventor.server.aiagent.AIAgentServiceImpl
- [ ] <servlet-mapping> maps aiAgentService to /ode/aiagent
- [ ] <filter-mapping> links odeAuthFilter to aiAgentService (ensures OdeAuthFilter runs on every request)

### appengine-web.xml
- [ ] ai.agent.available property (default: false)
- [ ] ai.agent.provider property (default: anthropic)
- [ ] ai.agent.model property (default: claude-sonnet-4-20250514)
- [ ] ai.agent.api.key property (default: empty)
- [ ] ai.agent.base.url property (default: empty)
- [ ] ai.agent.rate.limit property (default: 10)

### YaClient.gwt.xml
- [ ] <servlet path="/aiagent" class="...AIAgentServiceImpl" /> after existing servlet entries
- [ ] <source path="shared/rpc/aiagent"/> after existing shared/rpc source paths

### build.xml
- [ ] <copy> rule in AiServerLib target for server/aiagent/resources/*.md,*.json
- [ ] Files end up on classpath at correct package path (loadable via getResourceAsStream)

## Progress Log
