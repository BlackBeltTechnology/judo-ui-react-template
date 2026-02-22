# Action System

Generates TypeScript action handler functions from UI `ActionDefinition` and `Action` model elements. The action system uses a two-layer pattern: `ActionDefinition` specifies WHAT an action does (35+ subtypes), while `Action` wraps it with WHERE/WHEN context (target page, data element). Each page and dialog gets a typed actions interface with implementations for every action it supports.

## Requirements

### Requirement: Page Action Interface Generation

The generator SHALL produce a TypeScript interface listing all available actions for each page and dialog.

Each action SHALL be a typed async function with appropriate parameters and return type. An "extended" interface SHALL add lifecycle callbacks (e.g., `postRefresh`, `postCreate`). An actions hook interface SHALL allow runtime overrides via Pandino.

#### Scenario: Page with CRUD actions

- **WHEN** a page definition has refresh, update, and delete actions
- **THEN** the generated interface includes typed async functions for each action
- **AND** the extended interface includes `postRefreshAction`, `postUpdateAction`, and `postDeleteAction` callbacks

**Key Helpers**: `UiActionsHelper.getContainerOwnActionDefinitions()`, `UiActionsHelper.simpleActionDefinitionName()`, `UiActionsHelper.getActionTemplate()`

---

### Requirement: Action Implementation Generation

The generator SHALL produce async function implementations for each action on a page.

Each action type SHALL have a dedicated Handlebars template fragment. Each implementation SHALL include a service method call (e.g., `service.refresh()`, `service.update()`), error handling via `useErrorHandler`, success feedback via `useSnacks`, navigation via `useJudoNavigation`, dialog opening via dialog hooks, and post-action callbacks (e.g., `postRefreshAction`, `postCreateAction`).

#### Scenario: Refresh action implementation

- **WHEN** a page has a `RefreshActionDefinition`
- **THEN** the generated refresh action calls the service refresh method
- **AND** invokes `postRefreshAction` callback on success

#### Scenario: Delete action with confirmation

- **WHEN** a page has a `DeleteActionDefinition` with a confirmation
- **THEN** the generated delete action shows a confirmation dialog before calling the service

**Key Helpers**: `UiActionsHelper.getServiceMethodSuffix()`, `UiActionsHelper.postRefreshActionParams()`, `UiActionsHelper.postCreateActionParams()`, `UiActionsHelper.postCallOperationActionParams()`, `UiActionsHelper.allowRefreshAfterOperationCall()`

---

### Requirement: CRUD Actions

The generator SHALL produce CRUD action implementations from the following action definition subtypes:

`CreateActionDefinition` SHALL create a new entity. When `autoOpenAfterCreate` is `true`, the page SHALL navigate to the created entity after creation. `UpdateActionDefinition` SHALL save changes. When `autoCloseOnSave` is `true`, the page SHALL close/navigate back after saving. `DeleteActionDefinition` SHALL delete a single entity. `RefreshActionDefinition` SHALL reload data. `GetTemplateActionDefinition` SHALL load default values for create forms.

#### Scenario: Create with auto-open

- **WHEN** a `CreateActionDefinition` has `autoOpenAfterCreate = true`
- **THEN** after successful creation, the page navigates to the new entity

#### Scenario: Update with auto-close

- **WHEN** an `UpdateActionDefinition` has `autoCloseOnSave = true`
- **THEN** after successful save, the page closes or navigates back

---

### Requirement: Navigation Actions

The generator SHALL produce navigation action implementations from the following subtypes:

`OpenPageActionDefinition` SHALL navigate to a target page. `RowOpenPageActionDefinition` SHALL navigate from a table row to the entity detail view; `linkRelation` SHALL specify the relation to follow. `BackActionDefinition` SHALL navigate back. `CancelActionDefinition` SHALL cancel the current operation.

#### Scenario: Row navigation to detail

- **WHEN** a table row has a `RowOpenPageActionDefinition` with `linkRelation` set
- **THEN** clicking the row navigates to the detail view of the related entity

---

### Requirement: Relation Actions

The generator SHALL produce relation action implementations from the following subtypes:

`AddActionDefinition` SHALL add entities to a collection. `RemoveActionDefinition` SHALL remove entities from a collection. `SetActionDefinition` SHALL bind a single relation. `UnsetActionDefinition` SHALL unbind a single relation. `ClearActionDefinition` SHALL clear filters or data.

#### Scenario: Add to collection

- **WHEN** a table has an `AddActionDefinition`
- **THEN** the action opens a selector dialog and adds selected entities to the collection

---

### Requirement: Operation Call Actions

The generator SHALL produce operation call implementations from the following subtypes:

`CallOperationActionDefinition` SHALL call a custom operation. `ParameterlessCallOperationActionDefinition` SHALL call an operation without input parameters. `InputFormCallOperationActionDefinition` SHALL open a form dialog to collect parameters, then call the operation. `InputSelectorCallOperationActionDefinition` SHALL open a selector dialog to pick input, then call the operation. `BulkCallOperationActionDefinition` SHALL call an operation on multiple selected rows.

Faults SHALL generate error dialogs via `OperationFaultDialog`. When `postCallAccessNavigation` is set, post-call navigation SHALL be performed.

#### Scenario: Operation with input form

- **WHEN** an operation has a `CallOperationActionDefinition` with an input parameter
- **THEN** a form dialog opens to collect parameters
- **AND** the operation is called on form submission

#### Scenario: Operation with fault handling

- **WHEN** an operation call fails with a fault
- **THEN** an error dialog displays the fault information

**Key Helpers**: `UiActionsHelper.isActionOnOperationInput()`, `UiActionsHelper.getOperationNameForActionOnInput()`, `UiActionsHelper.operationCallSuffix()`

---

### Requirement: Selector and Form Actions

The generator SHALL produce dialog-opening action implementations from the following subtypes:

`OpenCreateFormActionDefinition` SHALL open a create form dialog; `formFor` SHALL reference the create action. `OpenOperationInputFormActionDefinition` SHALL open an operation input form. `OpenAddSelectorActionDefinition` SHALL open an add-to-collection selector; `selectorFor` SHALL reference the add action. `OpenSetSelectorActionDefinition` SHALL open a set-single selector. `OpenOperationInputSelectorActionDefinition` SHALL open an operation input selector.

#### Scenario: Open create form

- **WHEN** a button triggers an `OpenCreateFormActionDefinition`
- **THEN** a dialog opens with a create form for the target entity

---

### Requirement: Autocomplete and Range Actions

The generator SHALL produce autocomplete/range action implementations from the following subtypes:

`AutocompleteRangeActionDefinition` SHALL provide range query for autocomplete suggestions. `AutocompleteSetActionDefinition` SHALL set a relation via autocomplete selection. `AutocompleteAddActionDefinition` SHALL add to a collection via autocomplete selection. `SelectorRangeActionDefinition` SHALL load available entities for selector dialogs. `PreFetchActionDefinition` SHALL pre-fetch prerequisite data before an action.

#### Scenario: Autocomplete range query

- **WHEN** a user types in an autocomplete field
- **THEN** the `AutocompleteRangeActionDefinition` queries the server for matching entities

---

### Requirement: Bulk Actions

The generator SHALL produce bulk action implementations from the following subtypes:

`BulkDeleteActionDefinition` SHALL delete multiple selected rows. `BulkRemoveActionDefinition` SHALL remove multiple selected rows from a collection.

#### Scenario: Bulk delete

- **WHEN** multiple rows are selected and bulk delete is triggered
- **THEN** all selected entities are deleted

---

### Requirement: Button Generation

The generator SHALL produce MUI `<Button>` or `<IconButton>` components from `Button` elements.

Button variant SHALL be determined by `buttonStyle` (contained, outlined, text). Button icon SHALL come from `icon.iconName` using `<MdiIcon>`. `tooltipText` SHALL render a tooltip. `label` SHALL render button text.

When a `ButtonGroup` has `featuredActions < total buttons`, excess buttons SHALL go to a `<DropdownButton>` overflow menu. When `isFab = true`, the button group SHALL render as a floating action button with `alignment` positioning (BOTTOM_RIGHT, etc.).

#### Scenario: Button group with overflow

- **WHEN** a `ButtonGroup` has 5 buttons and `featuredActions = 2`
- **THEN** 2 buttons are rendered visibly and 3 are in a dropdown overflow menu

#### Scenario: Floating action button

- **WHEN** a `ButtonGroup` has `isFab = true` and `alignment = BOTTOM_RIGHT`
- **THEN** the button group renders as a floating action button in the bottom-right corner

**Key Helpers**: `UiWidgetHelper.variantForButton()`, `UiWidgetHelper.displayDropdownForButtonGroup()`, `UiWidgetHelper.elementHasIcon()`, `UiWidgetHelper.elementHasLabel()`

**Templates**: `actor/src/containers/widget-fragments/button.hbs`, `buttongroup.hbs`

---

### Requirement: Confirmation Dialog

The generator SHALL produce confirmation dialogs from `Button.confirmation` when `confirmationType != NONE`.

`MANDATORY` SHALL always show a confirmation dialog before executing the action. `CONDITIONAL` SHALL show the dialog only when the `confirmationCondition` attribute evaluates to true. The dialog SHALL display `confirmationMessage` with confirm/cancel buttons. The `useConfirmDialog` hook SHALL manage dialog state.

#### Scenario: Mandatory confirmation

- **WHEN** a button has `confirmation.confirmationType = MANDATORY`
- **THEN** clicking the button always shows a confirmation dialog before execution

#### Scenario: Conditional confirmation

- **WHEN** a button has `confirmation.confirmationType = CONDITIONAL` and `confirmationCondition` set
- **THEN** the confirmation dialog is shown only when the condition attribute is truthy

**Key Helpers**: `UiWidgetHelper.shouldRenderConfirmationCondition()`

**Template**: `src/components/dialog/ConfirmationDialog.tsx.hbs`

---

### Requirement: Nested Validation

The generator SHALL produce validation logic for create and update actions when validation behaviours exist.

Before save/create, validation SHALL be sent to the server. Validation errors SHALL be mapped to form fields. Nested validation SHALL support relation hierarchies.

#### Scenario: Update with server-side validation

- **WHEN** an entity has `VALIDATE_UPDATE` behaviour
- **THEN** the update action sends validation to the server before saving
- **AND** validation errors are mapped to the corresponding form fields

**Key Helpers**: `UiActionsHelper.createNestedValidation()`, `UiPageHelper.isValidationSupported()`

---

## Integration Test Coverage

- **ActionGroupTest**: Button groups, FABs, featured actions, action confirmations, Galaxy/Matter CRUD
- **CRUDActionsTest**: Full CRUD lifecycle, create forms, update flows, delete confirmations
- **OperationParametersTest**: Custom operations with input/output parameters, fault handling
- **RelationTest**: Relation-specific actions (add, remove, set, unset), selector actions
- **SimpleOrderManagement**: End-to-end order management with operations
