# Integrations

## JUDO Platform Dependencies
This project is tightly integrated with the JUDO platform ecosystem:

- **judo-meta-ui** (`hu.blackbelt.judo.meta:hu.blackbelt.judo.meta.ui.model`) — EMF-based UI metamodel defining `Application`, `PageDefinition`, `PageContainer`, `VisualElement`, `ActionDefinition`, `Table`, `Link`, etc.
- **judo-generator-commons** (`hu.blackbelt.judo.generator:judo-generator-commons`) — Base generator framework providing Handlebars integration, `@TemplateHelper` annotation processing, SpEL evaluation, `ThreadLocalContextHolder`
- **judo-ui-typescript-rest-template** — Generates the TypeScript REST service layer (API types, service interfaces, Axios implementations). Run as Phase 1 before UI generation.
- **judo-ui-generator-maven-plugin** (`hu.blackbelt.judo.meta:judo-ui-generator-maven-plugin`) — Maven plugin that orchestrates code generation from `.model` files

## Model Input
- **EMF/XMI Models** (`.model` files) — Input artifacts containing UI definitions
- Located in itest modules: e.g., `judo-ui-react-itest/ActionGroupTest/model/ActionGroupTest-ui.model`
- Models define: pages, containers, widgets, actions, relations, navigation, authentication

## Generated Service Layer
- Phase 1 generates into `target/frontend-react/src/services/`:
  - `data-api/` — TypeScript interfaces and model types
  - `data-axios/` — Axios-based service implementations
- Templates reference these via imports: `~/services/data-api/model/`, `~/services/data-axios/`

## Authentication
- Generated apps support configurable authentication via model's `Application.authentication` property
- Auth components generated: `Auth.tsx`, `AuthProxyComponent.tsx`, `AuthErrorBox.tsx`, `axiosInterceptor.ts`, `principal-context.tsx`
- Realm-based actor switching supported (`getAlternativeApplications` in `UiGeneralHelper`)

## Distribution Repositories
- **JUDO Nexus**: `nexus.judo.technology` (snapshot/release via `release-judong` profile)
- **Maven Central**: via Sonatype OSSRH (`release-central` profile)
- **Dummy**: File-based local distribution for testing (`release-dummy` profile)

## CI/CD
- GitHub-hosted: `BlackBeltTechnology/judo-ui-react-template`
- GitHub Actions (`.github/` directory)
- Artifact signing via `sign-maven-plugin` (`sign-artifacts` profile)
- SonarQube integration via `sonar-maven-plugin`

## External APIs (Generated App)
- Generated apps communicate with JUDO backend via REST/Axios
- No direct database access — all data access is through the JUDO service layer
- Signed identifiers used for entity references (`__signedIdentifier`)
