# Integration Testing Specification

## Purpose

The `judo-ui-react-itest` module validates the code generator by running it against test UI models, then verifying the generated React applications compile correctly and match expected snapshots.

## Architecture

Each integration test is a Maven multi-module project under `judo-ui-react-itest/`:

- **Test suites**: `ActionGroupTest`, `ActionGroupTestPro`, `CRUDActionsTest`, `OperationParametersTest`, `RelationTest`, `SimpleOrderManagement`
- **Actor sub-modules**: Each test suite contains sub-modules per actor (user role), e.g., `action_group_test__god`. Each actor gets its own independently generated React application.
- **Test models**: `.model` files (EMF/XMI format) in each test suite define the UI model under test.

The Maven lifecycle for each actor sub-module:
1. `generate-sources`: Run `judo-ui-generator-maven-plugin` (Phase 1: REST, Phase 2: React) on the `.model` file → produces React app in `target/frontend-react/`
2. `generate-sources`: Run `frontend-maven-plugin` to install Node.js/pnpm, then `pnpm install` and `pnpm run format` (Biome)
3. `generate-sources`: Run `judo-diff-checker-maven-plugin:checkDiffs` to compare generated files against snapshots in `src/test/resources/snapshots/frontend-react/`
4. `test`: Run `pnpm run build` (Vite) to verify the generated app compiles without errors

## Requirements

### Requirement: Generated React apps SHALL compile without errors

Every integration test actor sub-module SHALL produce a generated React application that passes Vite build without TypeScript or bundling errors.

#### Scenario: Successful Vite build
- **GIVEN** a valid test UI model
- **WHEN** the code generator produces the React app and `pnpm run build` is executed
- **THEN** the Vite build completes with exit code 0

### Requirement: Generated code SHALL pass Biome formatting

All generated TypeScript/React code SHALL conform to the project's Biome configuration after running `pnpm run format`.

#### Scenario: Biome format check
- **GIVEN** a freshly generated React application
- **WHEN** `pnpm run format` is executed
- **THEN** no formatting changes are applied (the generator already produces correctly formatted code)

### Requirement: Generated code SHALL match committed snapshots

Specific generated files (configured per test module) SHALL match their committed snapshot files to detect unintended regressions.

#### Scenario: Snapshot match after no template changes
- **GIVEN** no changes to Handlebars templates or Java helpers since the last snapshot update
- **WHEN** the `judo-diff-checker-maven-plugin:checkDiffs` goal runs
- **THEN** all configured files match their snapshots and the build passes

#### Scenario: Snapshot mismatch after template change
- **GIVEN** a Handlebars template has been modified
- **WHEN** the `checkDiffs` goal runs
- **THEN** the build fails with a unified diff showing the changes
- **AND** the developer must update snapshots by copying from `target/frontend-react/` to `src/test/resources/snapshots/frontend-react/`

### Requirement: Each actor SHALL produce an independent React application

Each actor sub-module SHALL generate a complete, self-contained React application with its own `package.json`, routing, pages, and components derived from the actor's permissions in the UI model.

#### Scenario: Multi-actor test suite
- **GIVEN** a test model defining multiple actors (e.g., `God`, `Admin`)
- **WHEN** the integration test suite builds
- **THEN** each actor sub-module produces a separate React app in its own `target/frontend-react/` directory
- **AND** each app only contains pages and actions accessible to that actor

### Requirement: MUI Pro tests SHALL use pro license plan

The `ActionGroupTestPro` test suite SHALL generate code with `muiLicensePlan=pro` to verify MUI Pro/Premium feature generation.

#### Scenario: Pro license generation
- **GIVEN** the `ActionGroupTestPro` POM sets `<muiLicensePlan>pro</muiLicensePlan>`
- **WHEN** the code generator runs
- **THEN** the generated app imports MUI Pro components (e.g., `DataGridPro`) and includes license setup code
