# AGENTS.md

This file provides guidance to LLM agents when working with code in this repository.

## Project Overview

This is **judo-ui-react-template**, a Java-based code generator that transforms JUDO UI Models (EMF/XMI) into production-ready React/TypeScript frontend applications. It is part of the JUDO platform ecosystem by BlackBelt Technology.

## Build Commands

```bash
# Full build (compile generator + run integration tests which generate & build React apps)
mvn clean install

# Run tests only
mvn clean test

# Parallel build using mvnd (requires sdkman with mvnd installed)
./full-build-parallel.sh

# Build a single integration test
mvn clean install -pl judo-ui-react-itest/ActionGroupTest/action_group_test__god -am

# Skip Node.js setup if already installed
mvn clean install -DskipPrepareNodeJS
```

## Requirements

- Java 21 (via sdkman: `sdk env`)
- Maven 3.9.4
- Node 22.11.0 / pnpm 9.12.3 (auto-installed by `frontend-maven-plugin` during build)

## Module Structure

```
judo-ui-react-template/
├── judo-ui-react/                  # Core generator: Java helpers + Handlebars templates
├── judo-ui-react-itest/            # Integration tests (each generates a full React app)
│   ├── ActionGroupTest/            # Tests action groups (community MUI)
│   ├── ActionGroupTestPro/         # Tests action groups (pro MUI)
│   ├── CRUDActionsTest/            # Tests CRUD operations
│   ├── OperationParametersTest/    # Tests operation parameters
│   ├── RelationTest/               # Tests relations
│   └── SimpleOrderManagement/      # End-to-end order management scenario
└── judo-diff-checker-maven-plugin/ # Maven plugin for snapshot-based regression testing
```

## Architecture

### Code Generation Pipeline

1. **Input**: JUDO UI Model (`.model` files in EMF/XMI format) defines pages, containers, actions, relations
2. **Generator**: `judo-ui-generator-maven-plugin` runs two phases:
   - **Phase 1** (`execute-ui-services-generation`): Generates TypeScript REST layer (API types, service interfaces, Axios implementations) using `judo-ui-typescript-rest-template`
   - **Phase 2** (`execute-ui-generation`): Generates React UI using templates from this repo
3. **Output**: Complete React app in `target/frontend-react/` with pages, containers, dialogs, i18n, theming, auth

### Template System

- **Templates**: Handlebars (`.hbs`) files in `judo-ui-react/src/main/resources/actor/` — directory structure mirrors the generated React app
- **Template Registry**: `judo-ui-react/src/main/resources/ui-react.yaml` maps templates to output files with path expressions, factory expressions (for generating multiple files from collections), and template context
- **Fragment Templates**: Partial templates included via `{{> fragment.hbs}}` for composition
- **Helper Classes**: Java classes annotated with `@TemplateHelper` in `judo-ui-react/src/main/java/.../react/` provide utility functions callable from templates:
  - `UiPageHelper` — page routing, navigation, data access
  - `UiActionsHelper` — action generation, button/operation logic
  - `UiWidgetHelper` — widget/component generation
  - `UiPageContainerHelper` — container layout
  - `UiTableHelper` — table/grid generation
  - `UiI18NHelper` — internationalization
  - `UiPandinoHelper` — Pandino (OSGi-style DI) service registration
  - `UiImportHelper` — TypeScript import management

### Generated React App Stack

- React 19 + TypeScript + Vite
- MUI (Material UI) 6.x with DataGridPro for tables
- Pandino for runtime extensibility (OSGi-style dependency injection)
- Biome for formatting/linting (not ESLint/Prettier)
- i18n via JSON files (`public/i18n/application_*.json`, `system_*.json`)

### Integration Test Pipeline

Each itest module follows this Maven lifecycle:
1. `generate-sources`: Run code generator on `.model` → produces React app in `target/frontend-react/`
2. `generate-sources`: `pnpm install` + `pnpm run format` (Biome)
3. `generate-sources`: `judo-diff-checker-maven-plugin:checkDiffs` — compares generated files against snapshots in `src/test/resources/snapshots/`
4. `test`: `pnpm run build` (Vite) + `pnpm run test` (if present)

### Snapshot Testing

The `judo-diff-checker-maven-plugin` compares specific generated files against committed snapshots. When templates change, update snapshots by copying from `target/frontend-react/` to `src/test/resources/snapshots/frontend-react/`. Each test module's POM lists the exact files being snapshot-tested.

## Key Conventions

- Helper methods use Spring Expression Language (SpEL) syntax in `ui-react.yaml` (e.g., `#getPagesForRouting(#application)`)
- Template parameters (like `muiLicensePlan`, `tablePageLimit`, `defaultLanguage`) are configured in itest POMs and passed to templates
- Each itest has sub-modules per "actor" (user role), e.g., `action_group_test__god` — each actor gets its own generated React app
- Generated code uses Pandino interface keys (e.g., `ROUTE_GOD_GALAXIES_TABLE_INTERFACE_KEY`) for runtime customization hooks
