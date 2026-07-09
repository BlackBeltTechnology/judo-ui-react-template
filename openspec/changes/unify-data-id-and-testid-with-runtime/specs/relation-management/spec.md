## MODIFIED Requirements

### Requirement: Selector Dialog Generation

The generator SHALL produce selector dialog pages from `OpenAddSelectorActionDefinition` and `OpenSetSelectorActionDefinition` elements.

The selector SHALL present a table with filtering and sorting for entity selection. **Add Selector** SHALL support multi-select and return selected entities. **Set Selector** SHALL support single-select and return one entity. Pagination SHALL be controlled by `selectorRowsPerPage`.

Row identity inside the selector table SHALL be resolved through `resolveTransferId` and row `data-testid` attributes SHALL be constructed through `buildRowTestId` — both per the `transfer-identity` capability. This ensures that Playwright tests written against the selector's row DOM address the same row regardless of which engine (template or runtime) rendered it.

#### Scenario: Add selector (multi-select)

- **WHEN** a selector page is opened for an Add action
- **THEN** the table allows multi-row selection
- **AND** the selected entities are returned on confirmation
- **AND** each row's `data-testid` is `table::<selectorTableId>::row::${resolveTransferId(row)}`

#### Scenario: Set selector (single-select)

- **WHEN** a selector page is opened for a Set action
- **THEN** the table allows single-row selection
- **AND** the selected entity is returned on confirmation

**Key Helpers**: `UiActionsHelper.getRangeActionDefinitionForTable()`
**Runtime Helpers**: `resolveTransferId`, `buildRowTestId` (see `transfer-identity`)

---

### Requirement: Autocomplete for Relations

The generator SHALL produce type-ahead autocomplete components when link or table elements have autocomplete action definitions.

A text input SHALL provide suggestions from the relation range as the user types. `autoCompleteRows` SHALL limit the number of suggestions displayed. For links, `autocompleteSetActionDefinition` SHALL enable set-by-typing. For tables, `autocompleteRangeActionDefinition` and `autocompleteAddActionDefinition` SHALL enable add-by-typing.

The relation-flavoured autocomplete widgets (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`) SHALL emit three structurally distinct `data-testid` attributes at the outer wrapper, the autocomplete element, and the underlying input element — see `transfer-identity` for the exact scheme. The action buttons rendered inline (set, clear, create, selector, view) SHALL each carry a role-suffixed `data-testid`. This resolves the nested-wrapper collision documented in the source design (`dataid-template-runtime.pdf` §2.5).

#### Scenario: Autocomplete set on link

- **WHEN** a link has `autocompleteSetActionDefinition` set
- **THEN** typing in the field shows suggestions from the relation range
- **AND** selecting a suggestion sets the relation

#### Scenario: Distinct testids across widget levels

- **GIVEN** a `SingleRelationInput` widget bound to a relation
- **WHEN** the widget is rendered
- **THEN** `page.getByTestId('field::<xmiId>')` returns exactly one element (the outer wrapper)
- **AND** `page.getByTestId('field::<xmiId>::autocomplete')` returns exactly one element
- **AND** `page.getByTestId('field::<xmiId>::input')` returns exactly one element
- **AND** no two elements in the widget share a `data-testid` value

**Key Helpers**: `UiWidgetHelper.isAutocompleteAvailable()`, `UiWidgetHelper.calculateLinkAutocompleteRows()`, `UiWidgetHelper.calculateTableAutocompleteRows()`
**Runtime Helper**: `buildFieldTestId` (see `transfer-identity`)

---

### Requirement: Inline Creatable Relations

When a `RelationType` has `isInlineCreatable = true`, the generator SHALL produce inline row creation in the relation table.

An `InlineCreateRowActionDefinition` SHALL add an empty row. The user SHALL fill values inline and save on blur or explicit action.

The new row SHALL be seeded per the `transfer-identity` capability: `__tempId: newTempId()`, `__isNew: true`, no `__identifier`. The inline relation-column DataGrid (`src/fragments/relation/column.fragment.hbs`) SHALL use `getRowId={(r) => resolveTransferId(r)}` — never `identifierAttribute={'__identifier'}`.

#### Scenario: Inline creatable relation

- **WHEN** a relation has `isInlineCreatable = true`
- **THEN** new rows can be added directly in the table without opening a dialog
- **AND** the newly-appended row object has `__tempId` and `__isNew: true`
- **AND** the row's `data-testid` includes the resolved `__tempId` as its key

**Runtime Helpers**: `resolveTransferId`, `newTempId` (see `transfer-identity`)
