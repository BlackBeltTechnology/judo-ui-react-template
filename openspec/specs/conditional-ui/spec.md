# Conditional UI

Generates runtime conditional rendering logic for visibility, enabled state, and required state of UI elements. Conditions are driven by data-bound attribute values, allowing the UI to dynamically adapt based on the current entity state without custom code.

## Requirements

### Requirement: Conditional Visibility (hiddenBy)

The generator SHALL produce conditional rendering checks from `VisualElement.hiddenBy` attribute bindings.

When the bound attribute's value is truthy, the element SHALL be hidden (not rendered). When the value is falsy/null, the element SHALL be visible. This SHALL work on any `VisualElement`: inputs, links, tables, buttons, containers. The bound attribute SHALL be part of the same data context (`ClassType`).

#### Scenario: Input hidden by attribute

- **WHEN** a `TextInput` has `hiddenBy` set to a boolean attribute
- **AND** the attribute value is `true`
- **THEN** the text input is not rendered

#### Scenario: Input visible when attribute is falsy

- **WHEN** a `TextInput` has `hiddenBy` set to a boolean attribute
- **AND** the attribute value is `false` or `null`
- **THEN** the text input is rendered normally

**Key Helpers**: `UiPandinoHelper.getElementsWithHiddenBy()`

---

### Requirement: Conditional Enabled State (enabledBy)

The generator SHALL produce conditional enabled/disabled logic from `VisualElement.enabledBy` attribute bindings.

When the bound attribute is truthy, the element SHALL be enabled. When falsy/null, the element SHALL be disabled (grayed out, not interactive). This SHALL be applied as a `disabled` prop on the MUI component. The enabled state SHALL combine with the static `disabled` flag — both must allow the enabled state.

#### Scenario: Input enabled by attribute

- **WHEN** an input has `enabledBy` set to a boolean attribute
- **AND** the attribute value is `true`
- **THEN** the input is enabled

#### Scenario: Input disabled when attribute is falsy

- **WHEN** an input has `enabledBy` set to a boolean attribute
- **AND** the attribute value is `false`
- **THEN** the input is disabled

---

### Requirement: Conditional Required State (requiredBy)

The generator SHALL produce conditional required logic from `VisualElement.requiredBy` attribute bindings.

When the bound attribute is truthy, the input field SHALL be required. When falsy/null, the field SHALL be optional. Required fields SHALL display an asterisk (*) indicator on the label. Required state SHALL affect form validation: required fields SHALL have a value before submit. The required state SHALL combine with the static `required` flag.

#### Scenario: Field dynamically required

- **WHEN** an input has `requiredBy` set to a boolean attribute
- **AND** the attribute value is `true`
- **THEN** the input displays a required asterisk and validation enforces a value

#### Scenario: Field dynamically optional

- **WHEN** an input has `requiredBy` set to a boolean attribute
- **AND** the attribute value is `false`
- **THEN** the input is optional and validation does not require a value

**Key Helpers**: `UiPageHelper.hasPageRequiredBy()`, `UiPageHelper.getRequiredByWidgetsForPage()`

---

### Requirement: Read-Only Mode

When an attribute or relation has `isReadOnly = true`, the generator SHALL render input elements in read-only mode.

Read-only SHALL apply regardless of page edit mode. Read-only SHALL differ from disabled: read-only fields SHALL be visually readable and selectable. Read-only fields SHALL be excluded from update/save actions.

#### Scenario: Read-only field in edit mode

- **WHEN** an attribute has `isReadOnly = true`
- **AND** the page is in edit mode
- **THEN** the input remains read-only and is not included in save

---

### Requirement: Table Row Button Conditions

The generator SHALL produce conditional visibility and enabled state for table row action buttons.

Row buttons SHALL be conditionally hidden based on row data attributes. Row buttons SHALL be conditionally disabled based on row data attributes. Conditions SHALL be evaluated per-row against the row's entity data.

#### Scenario: Row button hidden by row data

- **WHEN** a table row button has a hidden condition referencing a row attribute
- **AND** the attribute value makes the condition true
- **THEN** the button is not rendered for that row

#### Scenario: Row button disabled by row data

- **WHEN** a table row button has a disabled condition referencing a row attribute
- **AND** the attribute value makes the condition true
- **THEN** the button is disabled for that row

**Key Helpers**: `UiWidgetHelper.tableRowButtonHiddenConditions()`, `UiWidgetHelper.tableRowButtonDisabledConditions()`, `UiWidgetHelper.tableButtonVisibilityConditions()`

---

### Requirement: Container Button Conditions

The generator SHALL produce conditional disabled state for container-level button groups.

Button groups on containers SHALL have disabled conditions referencing data attributes from the container's data context.

#### Scenario: Container button disabled by data

- **WHEN** a container button has a disabled condition
- **AND** the condition evaluates to true
- **THEN** the button is disabled

**Key Helpers**: `UiPageContainerHelper.containerButtonGroupButtonDisabledConditions()`, `UiPageContainerHelper.containerButtonHasDisabledConditions()`

---

### Requirement: Navigation Item Visibility

When a `NavigationItem` has `hiddenBy` set, the generator SHALL produce conditional visibility for the menu item.

The `hiddenBy` expression on `NavigationItem` SHALL be a string expression (not a direct attribute reference). It SHALL be evaluated against the actor's access context.

#### Scenario: Menu item conditionally hidden

- **WHEN** a navigation item has `hiddenBy` set
- **AND** the expression evaluates to true at runtime
- **THEN** the menu item is not rendered

---

### Requirement: Edit Mode Toggle

The generator SHALL produce edit mode toggle logic for view pages.

In read-only mode: all inputs SHALL be disabled, save/cancel buttons SHALL be hidden. In edit mode: writable inputs SHALL become editable, save/cancel buttons SHALL appear. A navigation confirmation dialog SHALL be shown when navigating away with unsaved changes.

#### Scenario: Toggle to edit mode

- **WHEN** a view page is in read-only mode
- **AND** the user clicks the edit button
- **THEN** writable inputs become editable and save/cancel buttons appear

#### Scenario: Navigate away with unsaved changes

- **WHEN** a view page is in edit mode with unsaved changes
- **AND** the user attempts to navigate away
- **THEN** a navigation confirmation dialog is shown

**Generated Component**: `src/hooks/useNavigationConfirmation.tsx`

---

## Integration Test Coverage

- **ActionGroupTest**: Conditional button visibility in table rows, edit mode toggle
- **CRUDActionsTest**: Read-only vs editable modes across view/form containers
- **RelationTest**: Conditional relation actions based on relation state
