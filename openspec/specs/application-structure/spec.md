# Application Structure

## Purpose

Generates the top-level React application shell from a UI `Application` model element. Each `Application` corresponds to one actor (user role) and produces a complete, self-contained React SPA with routing, navigation, theming, authentication, and branding.

## Requirements

### Requirement: Application Scaffolding

The generator SHALL produce a complete React SPA project structure for each `Application` model element.

The generated project SHALL include `package.json`, `vite.config.ts`, `tsconfig.json`, `biome.json`, `index.html`, `.nvmrc`, `.editorconfig`, `.gitignore`, and `.npmrc` configuration files.

The generated project SHALL include `src/main.tsx` as the React entry point with Pandino bootstrap, `src/App.tsx` as the root component with providers, and `src/routes.tsx` with route definitions for all pages.

The `package.json` name SHALL be derived from `appScope` and `appModelName` template parameters. The version SHALL come from `appVersion`.

#### Scenario: Minimal application

- **WHEN** an `Application` model element exists with required fields (`actor`, `theme`, `modelName`)
- **THEN** the generator produces a buildable React project with all configuration files
- **AND** `src/routes.tsx` contains route definitions for all routable pages

#### Scenario: Custom branding

- **WHEN** `Application.title`, `Application.logo`, `Application.icon`, and `Application.backgroundImage` are set
- **THEN** the generated app displays the configured title, logo, icon, and background image

**Key Helpers**: `UiGeneralHelper.getAppTitle()`, `UiGeneralHelper.getFullAppName()`, `UiPageHelper.getPagesForRouting()`

**Templates**: `biome.json.hbs`, `package.json.hbs`, `tsconfig.json.hbs`, `vite.config.ts.hbs`, `src/App.tsx.hbs`, `src/main.tsx.hbs`, `src/routes.tsx.hbs`

---

### Requirement: Navigation Menu

The generator SHALL produce a navigation menu from `Application.navigationController`.

The menu layout SHALL be determined by `Application.defaultMenuLayout`: `VERTICAL` produces a sidebar drawer with collapsible sections; `HORIZONTAL` produces a top bar with dropdown menus.

Navigation items SHALL form a recursive tree. Group items (with child `items`) SHALL render as expandable sections. Leaf items (with `target`) SHALL navigate to the referenced `PageDefinition`.

Items with an `actionDefinition` SHALL trigger the associated action (e.g., static operations) from the menu.

Items with `hiddenBy` SHALL be conditionally visible based on runtime state.

Each navigation item SHALL display its `label` and `icon`.

The vertical sidebar drawer SHALL render as a **permanent** drawer at `sm` and wider, and as a **temporary** (overlay) drawer only at `xs` (below the `sm` breakpoint) — so `sm` is on the permanent-drawer side. Entering `xs` from any wider breakpoint, `sm` included, SHALL NOT leave a click-blocking overlay over the page content: on that transition the temporary drawer SHALL start closed and SHALL NOT capture pointer events over page content.

The drawer's collapsed/expanded state SHALL be a function of the **breakpoint tier**, not of the individual breakpoint transition. Breakpoints SHALL form three tiers: `xs`/`sm` (**compact**), `md` (**medium**), and `lg`/`xl` (**wide**). In the compact tier the drawer SHALL always be collapsed — no collapse toggle is rendered there, so an expanded drawer would be unrecoverable; at `xs` "collapsed" means the temporary overlay drawer is closed. The medium tier SHALL default to collapsed and the wide tier to expanded.

Within a tier the user's toggle choice SHALL be preserved. Crossing a tier boundary SHALL re-apply the entered tier's default. Before the first resolved breakpoint the application's configured `miniDrawer` default SHALL be honoured for the medium and wide tiers.

#### Scenario: Vertical sidebar navigation

- **WHEN** `Application.defaultMenuLayout = VERTICAL`
- **AND** `navigationController.items` contains leaf items and group items
- **THEN** the generator produces `src/layout/Drawer/` components with a collapsible sidebar menu
- **AND** leaf items link to their target pages
- **AND** group items expand to reveal sub-items

#### Scenario: Horizontal top bar navigation

- **WHEN** `Application.defaultMenuLayout = HORIZONTAL`
- **THEN** the generator produces `src/layout/Header/` components with a top bar menu

#### Scenario: Menu with operations

- **WHEN** navigation items have `actionDefinition` set (e.g., static operations)
- **THEN** the generator produces `menuTypes.ts` and `menuCustomization.ts`
- **AND** clicking the menu item triggers the operation

#### Scenario: Conditional menu item visibility

- **WHEN** a navigation item has `hiddenBy` set
- **THEN** the menu item is rendered conditionally based on the runtime value

#### Scenario: Responsive drawer breakpoint switch

- **WHEN** the viewport shrinks from any wider breakpoint (`sm`/`md`/`lg`/`xl`) into `xs`, where the temporary drawer branch is rendered
- **THEN** the generated temporary drawer starts closed for that transition
- **AND** its underlying MUI Modal root is not left visible with `pointer-events: auto` over the page
- **AND** page buttons, links, and navigation items remain clickable without a reload

#### Scenario: Drawer state after crossing a breakpoint tier

- **WHEN** the user opens the temporary overlay drawer at `xs`
- **AND** the viewport then widens into `sm`
- **THEN** the generated drawer is collapsed to the mini rail at `sm`
- **AND** it is not left expanded at a width where no collapse toggle is rendered
- **AND** narrowing back to `xs` does not reopen the overlay drawer without a user action

#### Scenario: Drawer state preserved within a breakpoint tier

- **WHEN** the user collapses the drawer at `lg`
- **AND** the viewport widens into `xl`
- **THEN** the drawer stays collapsed because no tier boundary was crossed

#### Scenario: Configured drawer default on first render

- **WHEN** the application is loaded at `md`, `lg`, or `xl`
- **THEN** the generated drawer honours the consuming application's configured `miniDrawer` default
- **AND** the resize effect does not override it until a tier boundary is crossed

**Key Helpers**: `UIMenuHelper.applicationHasMenuOperations()`, `UIMenuHelper.getMenuOperationOwnerTypes()`, `UiGeneralHelper.isNavItemAGroup()`

**Templates**: `src/layout/Drawer/**/*.hbs`, `src/layout/Header/**/*.hbs`

---

### Requirement: Theme Generation

The generator SHALL produce MUI theme configuration from `Application.theme`.

Theme colors SHALL map to MUI `createTheme()` palette values: `primaryColor` → primary, `secondaryColor` → secondary, `textPrimaryColor` → text primary, `textSecondaryColor` → text secondary, `backgroundColor` → background default, `paperBackgroundColor` → background paper.

The generated theme SHALL support both light and dark mode via palette overrides.

#### Scenario: Theme colors applied

- **WHEN** `Application.theme` has color values set
- **THEN** the generator produces `src/theme/palette.ts` with the configured colors
- **AND** `src/theme/typography.ts`, `src/theme/animations.tsx`, `src/theme/density.ts`, `src/theme/extras.ts`, `src/theme/table-row-highlighting.ts`, and `src/theme/index.css` are generated

**Key Helpers**: `UiPageContainerHelper.getSubThemes()`, `ReactStoredVariableHelper.getMUILicensePlan()`

**Templates**: `src/theme/*.hbs` (8 templates)

---

### Requirement: Authentication Flow

The generator SHALL produce authentication components when `Application.authentication` is set.

The generated auth flow SHALL use the configured `realm` for identity provider integration.

Claims SHALL map principal attributes to auth token claims. Each `Claim` maps a `ClaimType` (EMAIL, USERNAME, UNDEFINED) to an `AttributeType` on the principal.

The generator SHALL produce an Axios interceptor that attaches auth tokens to all API requests.

A principal context SHALL provide user identity information throughout the application.

#### Scenario: Authentication with claims

- **WHEN** `Application.authentication` is set with `realm` and `claims`
- **THEN** the generator produces `src/auth/Auth.tsx`, `src/auth/AuthErrorBox.tsx`, `src/auth/AuthProxyComponent.tsx`, `src/auth/axiosInterceptor.ts`, `src/auth/principal-context.tsx`, and `src/auth/constants.ts`
- **AND** the principal context exposes claim-mapped attributes

**Templates**: `src/auth/*.hbs` (6 templates)

---

### Requirement: Guest Access

When `Application.supportGuestAccess` is `true`, the generator SHALL produce an application that renders without requiring authentication.

Guest-specific page hook registrations SHALL be generated.

#### Scenario: Guest access enabled

- **WHEN** `Application.supportGuestAccess = true`
- **THEN** the auth wrapper is bypassed for guest pages
- **AND** guest page registration hooks are generated

---

### Requirement: Multi-Actor Application Switching

When the model contains multiple `Application` elements (different actors), the generator SHALL produce application switching components.

Each actor SHALL generate a separate React app. Apps SHALL be able to reference each other for role switching.

#### Scenario: Multiple actors exist

- **WHEN** multiple `Application` elements exist in the model
- **THEN** the generator produces `src/utilities/application/change-application.ts` and `src/utilities/application/generate-alternative-applications.ts`
- **AND** an `ApplicationSelector` component is generated

#### Scenario: Single actor

- **WHEN** only one `Application` element exists
- **THEN** no application switching components are generated

**Key Helpers**: `UiGeneralHelper.otherApplicationsAvailable()`, `UiGeneralHelper.getAlternativeApplications()`

---

## Template Configuration Parameters

| Parameter | Default | Purpose |
|-----------|---------|---------|
| `appModelName` | (from model) | Application name for package.json |
| `appScope` | `@blackbelt` | npm scope |
| `appVersion` | `1.0.0` | Application version |
| `defaultLanguage` | `en-US` | Fallback language |
| `muiLicensePlan` | (empty = community) | MUI license: `pro` or `premium` |
| `debugPrint` | `false` | Enable debug output in templates |

## Integration Test Coverage

- **ActionGroupTest**: Navigation with 10+ menu items, icons, groups (Galaxy domain)
- **SimpleOrderManagement**: Multi-actor (Customer + Registration) with authentication
- **All tests**: Application scaffolding compiles and builds successfully
