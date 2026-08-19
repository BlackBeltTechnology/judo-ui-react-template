# Runtime Extensibility

## Purpose

Generates Pandino-based extensibility hooks that allow developers to customize generated applications at runtime without modifying generated code. Pandino (an OSGi-style dependency injection framework for JavaScript) provides interface-based service registration. The generator produces interface keys, hook registration points, and default implementations that can be overridden.

## Requirements

### Requirement: Pandino Interface Key Generation

The generator SHALL produce a unique Pandino interface key constant for each page, dialog, and container.

Key format SHALL follow `ACTOR_ENTITY_PAGE_TYPE_ACTIONS_HOOK_INTERFACE_KEY`. Keys SHALL be exported from page `customization.ts` files. Runtime code SHALL look up services by these keys via `useTrackService`.

#### Scenario: Page interface key

- **WHEN** a page definition exists
- **THEN** the generator produces a unique Pandino interface key in `customization.ts`
- **AND** the key is usable for runtime service registration

**Key Helpers**: `UiPandinoHelper.camelCaseNameToInterfaceKey()`, `UiPandinoHelper.pageActionInterfaceKey()`, `UiPandinoHelper.pageActionHookInterfaceKey()`

**Template**: `actor/src/pages/customization.ts.hbs`

---

### Requirement: Page Action Hooks

The generator SHALL produce an actions hook interface for each page and dialog.

The hook SHALL receive the default action implementations as a parameter. The hook SHALL return potentially modified actions (override, wrap, or extend). Developers SHALL register a Pandino service implementing the hook interface. Runtime SHALL resolve the hook and apply overrides.

#### Scenario: Custom action hook override

- **WHEN** a developer registers a Pandino service for a page's action hook key
- **THEN** the custom hook receives default actions and returns modified actions
- **AND** the page uses the modified actions at runtime

#### Scenario: No custom hook registered

- **WHEN** no custom action hook is registered for a page
- **THEN** the page uses the default action implementations

**Key Helpers**: `UiPandinoHelper.pagesWithCustomActions()`, `UiPandinoHelper.dialogsWithCustomActions()`, `UiPageHelper.pageHookCallParams()`

---

### Requirement: Custom Widget Implementations

When a `VisualElement` has `customImplementation = true`, the generator SHALL replace the default widget with a Pandino-registered custom component.

The generator SHALL produce a Pandino interface key for the custom component, a registration hook file, and a `useTrackService` lookup in the widget rendering code. If no custom implementation is registered, the generator SHALL fall back to the default. Custom components SHALL receive the same props as the default widget.

The annotation name prefix (configurable via `customComponentAnnotationPrefix`) SHALL determine matching.

#### Scenario: Custom widget registered

- **WHEN** a visual element has `customImplementation = true`
- **AND** a developer registers a Pandino service for the widget's interface key
- **THEN** the custom component replaces the default widget

#### Scenario: Custom widget not registered (fallback)

- **WHEN** a visual element has `customImplementation = true`
- **AND** no custom Pandino service is registered
- **THEN** the default widget implementation is rendered

**Key Helpers**: `UiPandinoHelper.getVisualElementsWithCustomImplementation()`, `UiPandinoHelper.getCustomizationComponentInterface()`, `UiPandinoHelper.getCustomizationComponentInterfaceKey()`, `UiPageContainerHelper.getPageContainersWithCustomImplementations()`, `UiPageContainerHelper.getProxyPropsForCustomImplementation()`, `UiPageContainerHelper.getCustomImplementationProps()`

---

### Requirement: Custom Container Implementations

When a `PageContainer` or `Flex` has `customImplementation = true`, the generator SHALL replace the entire container with a Pandino-registered custom React component.

The custom component SHALL be registered via Pandino with the container's interface key. Props SHALL include data, actions, and editing state.

#### Scenario: Custom container

- **WHEN** a container has `customImplementation = true`
- **AND** a developer registers a custom component
- **THEN** the entire container is replaced with the custom implementation

**Key Helpers**: `UiPandinoHelper.containersWithDefaultImplementation()`

---

### Requirement: Sub-Theme Registration

When a `VisualElement` has a non-empty `subTheme` string, the generator SHALL produce a sub-theme registration hook.

Developers SHALL be able to register a MUI theme override for the named sub-theme. A `SubThemeWrapper` component SHALL apply the theme around the element.

#### Scenario: Sub-theme applied

- **WHEN** a visual element has `subTheme = "dark-panel"`
- **AND** a developer registers a MUI theme override for "dark-panel"
- **THEN** the `SubThemeWrapper` applies the custom theme around the element

**Key Helpers**: `UiPageContainerHelper.getSubThemes()`, `UiPageContainerHelper.containerHasSubTheme()`

---

### Requirement: Card Configuration Hooks

When a `Table` has `representationComponent = CARD`, the generator SHALL produce a card configuration hook.

Developers SHALL be able to customize card layout, content, and behavior via the registration hook.

#### Scenario: Custom card configuration

- **WHEN** a table has card representation
- **AND** a developer registers a card configuration hook
- **THEN** the card rendering uses the custom configuration

---

### Requirement: Application Customizer

The generator SHALL produce a global customizer framework for application-level overrides.

The customizer SHALL allow customizing error handling, navigation behavior, and global state. `application-customizer.tsx` SHALL provide the entry point.

#### Scenario: Custom error handling

- **WHEN** a developer registers an application customizer
- **THEN** error handling behavior can be customized globally

**Generated Output**: `src/custom/application-customizer.tsx`, `src/custom/custom-element-types.ts`, `src/custom/interfaces.ts`

---

### Requirement: OnBlur Event Handlers

When input elements have `onBlur = true`, the generator SHALL produce onBlur callback hooks.

The callback SHALL receive the current field value and data context. Side-effects (e.g., auto-calculate, validate) SHALL be triggerable when the user leaves an input field.

#### Scenario: OnBlur auto-calculation

- **WHEN** an input has `onBlur = true`
- **THEN** the generated component fires an onBlur callback when the user leaves the field
- **AND** the callback receives the current value and data context

**Key Helpers**: `UiPandinoHelper.getOnBlurAttributesForContainer()`

---

### Requirement: HiddenBy Dynamic Hooks

When elements have `hiddenBy` references, the generator SHALL produce dynamic visibility logic.

Runtime SHALL check the bound attribute value to show/hide elements.

#### Scenario: Dynamic visibility via hiddenBy

- **WHEN** an element has `hiddenBy` set to an attribute
- **THEN** the element is hidden when the attribute value is truthy

**Key Helpers**: `UiPandinoHelper.getElementsWithHiddenBy()`

---

## Integration Test Coverage

- **ActionGroupTest**: Action hooks, custom implementations in Galaxy/Matter views, Pandino service keys
- **All tests**: Every generated page produces customization.ts with interface keys
