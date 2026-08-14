# Code Generation Specification

## Purpose

The `judo-ui-react` module transforms JUDO UI Models (EMF/XMI) into complete React/TypeScript frontend applications by combining Handlebars templates with Java helper classes, orchestrated through a YAML template registry.

## Architecture

The generation pipeline centers on three components:

- **Template Registry** (`ui-react.yaml`): Maps each Handlebars template to an output file path, using SpEL expressions for dynamic paths and factory expressions for generating multiple files from collections (e.g., one page component per model page).
- **Handlebars Templates** (`src/main/resources/actor/`): `.hbs` files whose directory structure mirrors the generated React app. Fragment templates (`*.fragment.hbs`) enable composition via `{{> fragment.hbs}}`.
- **Java Helper Classes** (`hu.blackbelt.judo.ui.generator.react`): Annotated with `@TemplateHelper`, these provide utility functions callable from templates. Key helpers include `UiPageHelper` (routing/navigation), `UiActionsHelper` (action/button logic), `UiWidgetHelper` (component generation), `UiTableHelper` (grid generation), `UiPageContainerHelper` (container layout), `UiImportHelper` (TypeScript imports), `UiI18NHelper` (internationalization), `UiPandinoHelper` (DI service registration), `UIMenuHelper` (navigation menus), `UiSecurityHelper` (authentication), `UiNPMHelper` (package management), `UiGeneralHelper` (utilities), and `ReactStoredVariableHelper` (state management).

The generator plugin (`judo-ui-generator-maven-plugin`) executes two phases:
1. **Phase 1** (`execute-ui-services-generation`): Generates TypeScript REST layer using `judo-ui-typescript-rest-template`
2. **Phase 2** (`execute-ui-generation`): Generates React UI using templates from this module

## Requirements

### Requirement: Template registry SHALL map templates to output files

Each entry in `ui-react.yaml` SHALL define a template name, a `pathExpression` (SpEL) for the output file path, and optionally a `factoryExpression` for generating multiple files from a collection.

#### Scenario: Static file generation
- **GIVEN** a template entry with a literal `pathExpression` (e.g., `"'package.json'"`)
- **WHEN** the generator processes the entry
- **THEN** exactly one output file is produced at the specified path

#### Scenario: Factory-based file generation
- **GIVEN** a template entry with a `factoryExpression` (e.g., `#getPagesForRouting(#application)`)
- **WHEN** the generator processes the entry
- **THEN** one output file is produced for each element in the collection returned by the factory expression

#### Scenario: Copy mode
- **GIVEN** a template entry with `copy: true`
- **WHEN** the generator processes the entry
- **THEN** the file is copied verbatim without Handlebars processing

### Requirement: Generated app SHALL include all required React application structure

The generated output SHALL contain a complete, buildable React application including routing, pages, containers, components, authentication, theming, i18n, and configuration.

#### Scenario: Full app generation
- **GIVEN** a valid JUDO UI Model with at least one actor and one page
- **WHEN** both generation phases complete successfully
- **THEN** the `target/frontend-react/` directory contains `package.json`, `index.html`, `vite.config.ts`, `tsconfig.json`, and `src/` with pages, containers, components, auth, theme, layout, and i18n directories

### Requirement: Helper classes SHALL be accessible from templates via SpEL

Java helper classes annotated with `@TemplateHelper` SHALL be available in the template context and callable from both `ui-react.yaml` expressions and Handlebars templates.

#### Scenario: SpEL invocation in template registry
- **GIVEN** a helper method `UiPageHelper#getPagesForRouting(Application)`
- **WHEN** referenced in `ui-react.yaml` as `#getPagesForRouting(#application)`
- **THEN** the method is invoked with the current application model element and returns the expected collection

### Requirement: Template parameters SHALL be passed through from Maven configuration

Parameters defined in `<templateParameters>` in the consumer POM SHALL be available in the Handlebars template context.

#### Scenario: MUI license plan parameter
- **GIVEN** `<muiLicensePlan>pro</muiLicensePlan>` in the consumer POM's `<templateParameters>`
- **WHEN** templates reference the `muiLicensePlan` variable
- **THEN** the value `pro` is used, enabling MUI Pro features in the generated code

### Requirement: Generated code SHALL support Pandino extensibility

The generated application SHALL register Pandino interface keys for pages, routes, and components to allow runtime customization without modifying generated code.

#### Scenario: Route interface key registration
- **GIVEN** a generated page for actor "God" and entity "Galaxies" table
- **WHEN** the page component is generated
- **THEN** a Pandino interface key (e.g., `ROUTE_GOD_GALAXIES_TABLE_INTERFACE_KEY`) is exported and used for service registration

### Requirement: Generated code SHALL NOT carry design rationale as comments

Templates SHALL keep design rationale — why an approach was chosen, what defect it avoids, what alternatives were rejected — out of the generated output. Such rationale SHALL live in the `openspec/changes/**` documents for the change that introduced it.

Templates MAY emit a short comment where it explains something a reader of the generated file cannot otherwise recover, such as a non-obvious external constraint at the point of use. Rationale that explains a *decision* SHALL NOT be emitted.

Where a rationale comment would otherwise be needed to name a set of states, the generated code SHALL make those states self-describing instead — for example a named union type whose members are the states.

This applies because generated output is duplicated into every consuming application, for every actor, and cannot be revised there: a comment that drifts out of step with the requirement it implements keeps contradicting it in every generated app.

#### Scenario: Rationale accompanies a non-obvious template construct

- **GIVEN** a template construct whose reasoning needs recording (for example a breakpoint-tier rule that exists to prevent a state latch)
- **WHEN** the template is written
- **THEN** the generated output contains the construct without the explanatory prose
- **AND** the reasoning is recorded in the `proposal.md` or `design.md` of the change that introduced it

#### Scenario: State names carried by the type system

- **GIVEN** a construct that would need a comment to enumerate its states
- **WHEN** the states can be expressed as a named union type
- **THEN** the generated code declares that type so the states are visible at the point of use
- **AND** no comment enumerating them is emitted

#### Scenario: Rationale comment removed from a shipped template

- **GIVEN** a template that already emits a rationale comment block into generated apps
- **WHEN** the comment is removed and its content moved into the change documents
- **THEN** regenerating a consuming application yields a file whose only differences are the removed comment lines
- **AND** no executable line changes

