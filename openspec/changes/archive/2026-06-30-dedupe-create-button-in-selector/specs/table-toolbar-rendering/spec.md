## ADDED Requirements

### Requirement: Selector-mode tables suppress form-opening Create buttons

The generator SHALL emit a static-time boolean property `hiddenInSelectorMode: true` on every toolbar-action entry in a TableComponent's `toolBarActions` array whose underlying action is an instance of `OpenCreateFormActionDefinition` (Handlebars-side: `button.actionDefinition.isOpenCreateFormAction`). All other toolbar-action entries SHALL omit the `hiddenInSelectorMode` property entirely (so it becomes `undefined` at runtime, equivalent to `false` for the filter check below).

The runtime table components (`LazyTable.tsx` and `EagerTable.tsx`) SHALL filter their toolbar render loop with the predicate `(toolBarAction) => !(<selectorFlag> && toolBarAction.hiddenInSelectorMode)`, where `<selectorFlag>` is `containerIsSelector` in `LazyTable` and `isSelectorTable` in `EagerTable`. This filter SHALL precede the existing `.map(...)` that emits the toolbar `<Button>` elements.

The effect: when a table is mounted inside a selector dialog (AddSelector / SetSelector overlay), its toolbar SHALL NOT render the form-opening Create button. The inline-create button (`inlineCreateRowAction`, id suffix `…/TransferObjectTableInlineCreateButton`) continues to render based on its own `enabled()` callback.

#### Scenario: Form-opening Create button hidden when table is rendered inside a selector dialog

- **GIVEN** a table model element whose `tableActionButtonGroup.buttons` contains a Button bound to an `OpenCreateFormActionDefinition`
- **AND** the table is mounted in a context where `selectionDiff` is an array (i.e. `Array.isArray(selectionDiff) === true`, propagated as `containerIsSelector === true` to LazyTable or `isSelectorTable === true` to EagerTable)
- **WHEN** the toolbar renders
- **THEN** the DOM SHALL NOT contain a `<Button data-testid="…/TransferObjectTableCreateButton">` for that table
- **AND** the DOM SHALL still contain `<Button data-testid="…/TransferObjectTableInlineCreateButton">` if the model defines an inline-create action and that action's `enabled()` callback returns true
- **AND** all other toolbar buttons (filter, refresh, export, add, set, clear, bulk-remove, bulk-delete) SHALL render exactly as they did before this change

#### Scenario: Form-opening Create button visible when table is rendered as a regular page

- **GIVEN** the same table model element as in the previous scenario
- **AND** the table is mounted in a context where `selectionDiff` is undefined or non-array (i.e. `containerIsSelector === false` / `isSelectorTable === false`)
- **WHEN** the toolbar renders
- **THEN** the DOM SHALL contain `<Button data-testid="…/TransferObjectTableCreateButton">` exactly once (assuming the page is not also being overlayed by a stacked selector)
- **AND** the toolbar SHALL behave identically to its pre-change rendering

#### Scenario: Other toolbar actions are unaffected

- **GIVEN** a table whose `tableActionButtonGroup.buttons` includes buttons bound to `RefreshAction`, `OpenAddSelectorAction`, `OpenSetSelectorAction`, `InlineCreateRowAction`, `ClearAction`, `BulkDeleteAction`, or any non-`OpenCreateFormActionDefinition` action
- **WHEN** the TableComponent is generated
- **THEN** those buttons' emitted action objects SHALL NOT contain `hiddenInSelectorMode: true`
- **AND** those buttons SHALL render in BOTH selector and non-selector contexts according to their existing `enabled()` callbacks

**NOTE** — The `hiddenInSelectorMode` flag is a single-purpose suppression mechanism scoped to the duplicate-testid problem documented in audit finding F10 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md`). It is not a general-purpose "hide-this-button-in-this-context" facility. If future findings require additional contextual suppressions (e.g. "hide in form mode", "hide when read-only"), new flags should be introduced alongside, not by overloading this one.
