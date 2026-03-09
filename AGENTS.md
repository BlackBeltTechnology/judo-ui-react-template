# JUDO UI React Template - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/judo-ui-react-template
**License:** Eclipse Public License 2.0 (EPL-2.0)
**Java Version:** 21 (21.0.7-zulu via sdkman)
**Build System:** Maven 3.9.4 with mvnd parallel support

1. A Java-based code generator that transforms JUDO UI Models (EMF/XMI format) into complete, production-ready React/TypeScript frontend applications
2. Uses Handlebars templates and Java helper classes to produce pages, containers, dialogs, routing, i18n, theming, and authentication code
3. Includes a snapshot-based regression testing plugin (`judo-diff-checker-maven-plugin`) that detects unexpected changes in generated output
4. Integration tests generate full React apps from test models, then compile and snapshot-check them to validate correctness
5. Generated apps use React 19, MUI 7, Vite, TypeScript, Pandino (OSGi-style DI), and Biome for formatting

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
judo-ui-react-template/
├── judo-ui-react/                  # Core generator module (Java helpers + Handlebars templates)
├── judo-ui-react-itest/            # Integration tests (each generates a full React app)
│   ├── ActionGroupTest/            # Action groups (community MUI)
│   ├── ActionGroupTestPro/         # Action groups (pro MUI license)
│   ├── CRUDActionsTest/            # CRUD operations
│   ├── OperationParametersTest/    # Operation parameters
│   ├── RelationTest/               # Relations
│   └── SimpleOrderManagement/      # End-to-end order management scenario
├── judo-diff-checker-maven-plugin/ # Maven plugin for snapshot-based diff checking
├── docs/                           # AsciiDoc documentation source
├── .github/workflows/              # CI/CD GitHub Actions
├── full-build-parallel.sh          # Parallel build script using mvnd
└── .sdkmanrc                       # Java/Maven/mvnd version pinning
```

## Core Modules

### Generator Module

| Module | Type | Purpose |
|--------|------|---------|
| `judo-ui-react/` | OSGi bundle | Contains all Handlebars templates (`src/main/resources/actor/`), the template registry (`ui-react.yaml`), and Java `@TemplateHelper` classes that provide utility functions for templates |

### Helper Classes (`hu.blackbelt.judo.ui.generator.react`)

| Helper | Purpose |
|--------|---------|
| `UiPageHelper` | Page routing, navigation, data access |
| `UiActionsHelper` | Action generation, button/operation logic |
| `UiWidgetHelper` | Widget/component generation |
| `UiPageContainerHelper` | Container layout logic |
| `UiTableHelper` | Table/grid generation |
| `UIMenuHelper` | Menu/navigation component generation |
| `UiI18NHelper` | Internationalization keys and translations |
| `UiPandinoHelper` | Pandino (OSGi-style DI) service registration |
| `UiImportHelper` | TypeScript import statement management |
| `UiGeneralHelper` | General utility functions |
| `UiNPMHelper` | npm/pnpm package management utilities |
| `UiSecurityHelper` | Security and authentication utilities |
| `ReactStoredVariableHelper` | Stored variable state management |

### Template System

| Component | Location | Purpose |
|-----------|----------|---------|
| Template registry | `judo-ui-react/src/main/resources/ui-react.yaml` | Maps templates to output files with path expressions, factory expressions, and template context |
| Templates | `judo-ui-react/src/main/resources/actor/` | Handlebars `.hbs` files — directory structure mirrors the generated React app |
| Fragment templates | Various `*.fragment.hbs` files | Partial templates included via `{{> fragment.hbs}}` for composition |

### Testing & Tooling

| Module | Type | Purpose |
|--------|------|---------|
| `judo-diff-checker-maven-plugin/` | Maven plugin | Compares generated files against committed snapshots using java-diff-utils; fails the build on unexpected diffs |
| `judo-ui-react-itest/` | Integration tests | Each sub-module generates a full React app from a `.model` file, runs Biome formatting, snapshot checking, and Vite build |

## Technology Stack

### Core Technologies
- **Eclipse EMF** (ecore-xmi 2.2.3) — model framework for loading/traversing UI models
- **Handlebars** — template engine for generating TypeScript/React code
- **Spring Expression Language (SpEL)** 5.0.0 — used in `ui-react.yaml` for path expressions and factory expressions
- **Lombok** 1.18.34 — reduces Java boilerplate in helper classes
- **Apache Felix** maven-bundle-plugin — OSGi bundle packaging

### Generated App Technologies
- React 19 + TypeScript + Vite 7
- MUI (Material UI) 7.x + DataGrid Pro 8.x
- Pandino — runtime extensibility via OSGi-style dependency injection
- Biome — formatting and linting (not ESLint/Prettier)
- i18n via JSON files (`public/i18n/application_*.json`, `system_*.json`)

### Build & Quality
- Maven 3.9.4 with `flatten-maven-plugin` for CI-friendly versions (`${revision}`)
- JaCoCo 0.8.12 for code coverage
- SonarQube integration via `sonar-maven-plugin`
- JUnit 5 for unit tests
- `frontend-maven-plugin` 1.12.1 for Node.js/pnpm auto-installation

## Build Commands

```bash
# Full build (compile generator + run all integration tests)
mvn clean install

# Run unit tests only
mvn clean test

# Build a single integration test module
mvn clean install -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am

# Parallel build using mvnd (requires sdkman)
./full-build-parallel.sh

# Skip Node.js setup if already installed
mvn clean install -DskipPrepareNodeJS

# Set up Java/Maven/mvnd versions
sdk env
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `sign-artifacts` | Sign artifacts using `sign-maven-plugin` for release |
| `release-dummy` | Deploy to local filesystem (`/tmp/`) for testing |
| `release-judong` | Deploy to JUDO NG Nexus repository |
| `release-central` | Deploy to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generate PNG diagrams from AsciiDoc PlantUML blocks |
| `update-source-code-license` | Update EPL-2.0 license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Root POM — defines modules, dependency versions, build plugins, and profiles |
| `.sdkmanrc` | Pins Java 21.0.7-zulu, Maven 3.9.4, mvnd 1.0-m6-m40 |
| `judo-ui-react/src/main/resources/ui-react.yaml` | Template registry — maps Handlebars templates to generated output files |
| `logback-test.xml` | Logging configuration for test execution |
| `full-build-parallel.sh` | Shell script for parallel builds using mvnd |

## Development Environment

**Required:**
- Java 21 JDK (via sdkman: `sdk env` reads `.sdkmanrc`)
- Maven 3.9.4+
- Node.js 22.14.0 / pnpm 9.15.9 (auto-installed during build, or skip with `-DskipPrepareNodeJS`)
- Optional: mvnd for parallel builds

## Git Workflow

- **Main Branch:** `develop`
- **Versioning:** CI-friendly `${revision}` property (1.0.0-SNAPSHOT in development)
- **Branch naming:** `feature/JNG-xxx_description`, `bugfix/JNG-xxx_description`, `release/x.y.z`
- **Commit rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)
- **CI:** GitHub Actions workflows for build, release, merge-pr handling, and changelog generation

## Important Notes

1. The code generation pipeline has two phases: Phase 1 generates the TypeScript REST layer (types, services, Axios) using `judo-ui-typescript-rest-template`, Phase 2 generates the React UI using templates from this repo
2. Helper methods are invoked from `ui-react.yaml` using SpEL syntax (e.g., `#getPagesForRouting(#application)`)
3. Template parameters like `muiLicensePlan`, `tablePageLimit`, `defaultLanguage` are configured in consumer POMs and passed to templates
4. Each itest has sub-modules per "actor" (user role), e.g., `action_group_test__god` — each actor gets its own generated React app
5. When templates change, update snapshots by copying files from `target/frontend-react/` to `src/test/resources/snapshots/frontend-react/`
6. Generated code uses Pandino interface keys (e.g., `ROUTE_GOD_GALAXIES_TABLE_INTERFACE_KEY`) for runtime customization hooks
7. The `.vscode` directory is in `.gitignore` — IDE settings are local to each developer

## Related Documentation

- [README.md](README.md) — Project overview and usage example
- [CONTRIBUTING.md](CONTRIBUTING.md) — Development setup and submission guidelines
- [.github/CIFLOW.md](.github/CIFLOW.md) — Branch strategy and CI/CD workflow documentation
- [judo-diff-checker-maven-plugin/README.md](judo-diff-checker-maven-plugin/README.md) — Snapshot diff checker usage
- [docs/pages/](docs/pages/) — Detailed generated app documentation
