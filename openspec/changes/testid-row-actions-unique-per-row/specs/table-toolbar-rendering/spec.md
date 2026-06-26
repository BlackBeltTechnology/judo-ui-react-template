## MODIFIED Requirements

### Requirement: Selector-mode tables suppress duplicate data-testid attributes (broadened scope)

This requirement extends the prior `Selector-mode tables suppress form-opening Create buttons` requirement (introduced by `dedupe-create-button-in-selector`). The form-opening Create button suppression continues to hold as previously specified. In addition, the runtime table components SHALL omit the `data-testid` attribute on **all** of the following DOM elements when the table is mounted in a selector dialog overlay (i.e. `containerIsSelector === true` in LazyTable, `isSelectorTable === true` in EagerTable):

1. Every toolbar `<Button>` rendered from the `toolBarActions` array — `data-testid={<selectorFlag> ? undefined : toolBarAction.id}`.
2. The header selection checkbox and every per-row selection checkbox — by passing `testIdPrefix: undefined` to the `CustomCheckbox` wrapper via `slotProps.baseCheckbox`; `CustomCheckbox` SHALL detect this and return `<Checkbox>` with no `data-testid` attribute.
3. Every per-row split-action `<Button>` (edit, save, cancel, custom CRUD/transfer row actions) — `data-testid={isSelectorMount ? undefined : buildRowTestId(action.id, params.row.__identifier)}`.
4. The per-row dropdown trigger `<Button>` (rendered by `DropdownButton`) — via `suppressTestId={isSelectorMount}` on the `DropdownButton` props.
5. Every per-row dropdown `<MenuItem>` — via `suppressTestId: isSelectorMount` on each `menuItems` entry.

The page-mount (non-selector) instance of the same table SHALL retain all `data-testid` attributes unchanged; it remains the canonical testid carrier. This invariant matches existing Playwright spec patterns (e.g. `judo-tatami-tests/.../CrudActionsOnSingleAndManyRelationsTest.spec.ts:542`) which use `getByRole(...)` / label-based selectors inside selector dialog overlays.

#### Scenario: All testids omitted inside a selector dialog overlay

- **GIVEN** a table model element with both a toolbar (e.g. filter, refresh, add, set, bulk-delete buttons), selection checkboxes (`selectorType !== 'NONE'`), and per-row actions (edit/save/cancel + custom row actions)
- **AND** the table is mounted in a context where `containerIsSelector === true` (LazyTable) or `isSelectorTable === true` (EagerTable)
- **WHEN** the table renders
- **THEN** the DOM SHALL contain ZERO `<Button data-testid=...>` elements inside the table's `<GridToolbarContainer>` and inside the row-action column
- **AND** the DOM SHALL contain ZERO `<input data-testid=...>` (or wrapping `<span>`) elements for selection checkboxes inside this table mount
- **AND** the DOM SHALL contain ZERO `<li data-testid=...>` elements inside any per-row dropdown menu that may be opened
- **AND** the buttons / checkboxes / menu items themselves SHALL remain visible, enabled, and functional (only the testid attribute is omitted)

#### Scenario: Page-mount testids unchanged in same-XMI dialog overlay scenario

- **GIVEN** a page that owns a TableComponent with XMI id `X`
- **AND** the user opens an AddSelector dialog whose content is also a TableComponent with XMI id `X` (same model element)
- **WHEN** the dialog is open
- **THEN** the underlying page mount of `X` SHALL still emit all its `data-testid` attributes (toolbar Buttons, checkboxes, row-action buttons) unchanged from pre-change behaviour
- **AND** the selector mount of `X` SHALL emit ZERO `data-testid` attributes on those elements
- **AND** `getByTestId(<toolbar action id>)` against the document SHALL match exactly the page-mount instance, NOT the dialog instance

#### Scenario: Backwards compatibility — non-selector page tables behave identically

- **GIVEN** a table mounted as a regular page (not inside any dialog overlay)
- **AND** `containerIsSelector === false` / `isSelectorTable === false`
- **WHEN** the table renders
- **THEN** every toolbar Button, selection checkbox, and row-action Button SHALL emit `data-testid` exactly as it did before this change (subject to the row-suffix change covered by the `table-row-actions-testids` capability)
