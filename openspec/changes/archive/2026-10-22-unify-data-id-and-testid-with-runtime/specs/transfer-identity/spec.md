## ADDED Requirements

### Requirement: Row-key resolution follows a runtime-aligned fallback chain

Every generator-emitted site that needs to identify a data row — MUI DataGrid `getRowId`, per-row validation maps, per-row state models, action target lookups, `Array.prototype.find(...__identifier === …)` comparisons — SHALL derive that identity through a single shared helper `resolveTransferId(row, index?)`. The helper SHALL evaluate the following fallback chain in this order and SHALL return the first non-`undefined`, non-empty result:

1. `row.__signedIdentifier`
2. `row.__identifier`
3. `row.__tempId`
4. `row.id`
5. `` `idx-${index}` `` when an index is supplied
6. `` `idx-undefined` `` as a last resort (never expected in practice)

The helper SHALL be exported from the actor-local file `src/utilities/transfer-id.ts`. It SHALL be a pure function of its arguments (no closures over module state, no side effects). Its return type SHALL be `string`.

Templates SHALL NOT emit inline row-key expressions such as `row.__identifier!` or `getRowIdentifier`-style closures.

#### Scenario: Saved row resolves to its signed identifier

- **GIVEN** a row `{ __signedIdentifier: "sig-abc", __identifier: "id-xyz", __tempId: undefined, id: 42 }`
- **WHEN** `resolveTransferId(row)` is called
- **THEN** the result is `"sig-abc"`

#### Scenario: Client-created row resolves to its temp id

- **GIVEN** a row `{ __signedIdentifier: undefined, __identifier: undefined, __tempId: "temp::1719660000000::a3f9c2b", __isNew: true }`
- **WHEN** `resolveTransferId(row)` is called
- **THEN** the result is `"temp::1719660000000::a3f9c2b"`

#### Scenario: Legacy draft-seeded row resolves to its identifier verbatim

- **GIVEN** a row `{ __identifier: "draft:f47ac10b-58cc-4372-a567-0e02b2c3d479" }` (residual seed from Phase 1's deserializer)
- **WHEN** `resolveTransferId(row)` is called
- **THEN** the result is `"draft:f47ac10b-58cc-4372-a567-0e02b2c3d479"`
- **AND** the value is stable across re-renders

#### Scenario: Row with no identity resolves to indexed key

- **GIVEN** a row `{}` at index `7`
- **WHEN** `resolveTransferId(row, 7)` is called
- **THEN** the result is `"idx-7"`

#### Scenario: MUI DataGrid uses the resolver as getRowId

- **WHEN** the generator emits an `EagerTable`, `LazyTable`, or inline relation-column DataGrid
- **THEN** the `<DataGrid>` element carries the prop `getRowId={resolveTransferId}` (or an equivalent `(r) => resolveTransferId(r)` if MUI's type inference demands it)
- **AND** no emitted DataGrid carries the string prop `identifierAttribute={'__identifier'}`

### Requirement: New-row seeding uses `__tempId` and `__isNew`

Every generator-emitted site that constructs a client-side transfer object for a row the user has *not yet saved* SHALL populate two fields and SHALL leave `__identifier` unset:

- `__tempId: newTempId()` where `newTempId()` returns a string of the shape `` `temp::${Date.now()}::${randomHex(7)}` ``.
- `__isNew: true`.

Sites SHALL NOT write `__identifier: \`${draftIdentifierPrefix}${uuidv4()}\`` or any other value derived from the `draft:` prefix. The template SHALL NOT import `draftIdentifierPrefix` from `~/services/data-api/common/utils` at any seed site.

The predicate `isNewRow(row)` SHALL return `true` when either:
- `row.__isNew === true`, OR
- (compatibility rung) `typeof row.__identifier === 'string' && row.__identifier.startsWith('draft:')`

The compatibility rung exists solely to correctly classify rows seeded by Phase 1's `rest/serializer.ts.hbs:92–93` deserializer and SHALL be removed once Phase 1 stops emitting `draft:` prefixes.

Every branch previously written as `rowData.__identifier!.startsWith(draftIdentifierPrefix)` SHALL be rewritten to `isNewRow(rowData)`.

#### Scenario: Inline table row-add produces a temp row

- **GIVEN** the user clicks the inline "add row" action on an `EagerTable` or `LazyTable`
- **WHEN** the template emits the new row object
- **THEN** the object contains `__tempId` matching `/^temp::\d+::[0-9a-f]+$/` and `__isNew: true`
- **AND** the object does NOT contain a `__identifier` field
- **AND** the object does NOT contain any string beginning with `"draft:"`

#### Scenario: Save-draft form return produces a temp row

- **GIVEN** an `OpenCreateFormAction` returning with `result === 'submit-draft'`
- **WHEN** the template emits the decorated row to append to the parent collection
- **THEN** the object contains `__tempId` and `__isNew: true`
- **AND** the object does NOT contain `__identifier`

#### Scenario: Legacy draft-seeded row still classifies as new

- **GIVEN** a row `{ __identifier: "draft:xyz" }` arriving from Phase 1's deserializer
- **WHEN** `isNewRow(row)` is called
- **THEN** the result is `true`

#### Scenario: Saved row does not classify as new

- **GIVEN** a row `{ __signedIdentifier: "sig-abc", __identifier: "id-xyz" }`
- **WHEN** `isNewRow(row)` is called
- **THEN** the result is `false`

### Requirement: Hierarchical `data-testid` scheme

Every `data-testid` attribute rendered by the generator SHALL be constructed through one of four helper functions exported from `src/utilities/transfer-id.ts`:

- `buildFieldTestId(xmiId, role?)` → `` `field::${xmiId}` `` or `` `field::${xmiId}::${role}` ``
- `buildTableTestId(tableId)` → `` `table::${tableId}` ``
- `buildRowTestId(tableId, row, index?)` → `` `table::${tableId}::row::${resolveTransferId(row, index)}` ``
- `buildCellTestId(tableId, row, columnName, index?)` → `` `table::${tableId}::row::${resolveTransferId(row, index)}::cell::${columnName}` ``

The `role` argument accepted by `buildFieldTestId` SHALL be one of the following values or SHALL nest with `::` for compound roles:

| Role | Applies to |
|---|---|
| (absent) | The outer wrapper `<div>` around the widget |
| `input` | The DOM element receiving focus and keystrokes |
| `autocomplete` | The MUI `<Autocomplete>` element when nested inside a wrapper |
| `dropdown` | A popper / menu / dropdown element |
| `button::set` | Primary set/edit action button |
| `button::clear` | Clear-current-value button |
| `button::create` | Inline create button |
| `button::selector` | Open-selector-dialog button |
| `button::view` | View / open-form button |
| `button::add` | Add-item button (multi-value inputs) |
| `button::remove` | Remove-item button (multi-value inputs) |
| `menu::item::<name>` | A named menu item |
| `filter::operator::<opName>` | Filter operator selector entry |

Templates SHALL NOT emit `data-testid` values via inline string concatenation such as `${id}-clear-all`, `${id}-open-selector`, `filter-operator-${…}`, or bare `${item.id}`. Every emission site SHALL route through one of the four helpers above.

For any single widget or row, no two DOM elements SHALL carry the same `data-testid` value. This SHALL be structurally guaranteed by giving each DOM level a distinct `role` argument.

#### Scenario: RelationDefinedLink emits three distinct testids

- **GIVEN** a `SingleRelationInput` widget with XMI id `esm/_28BIQBn`
- **WHEN** the widget is rendered
- **THEN** the outer wrapper carries `data-testid="field::esm/_28BIQBn"`
- **AND** the autocomplete carries `data-testid="field::esm/_28BIQBn::autocomplete"`
- **AND** the underlying input carries `data-testid="field::esm/_28BIQBn::input"`
- **AND** the primary set button carries `data-testid="field::esm/_28BIQBn::button::set"`
- **AND** `page.getByTestId("field::esm/_28BIQBn::input")` returns exactly one element

#### Scenario: Table row testid uses the resolver

- **GIVEN** a table with id `Actor/(esm/_ZVpS8M7)/ClassType` containing a saved row `{ __signedIdentifier: "sig-42" }`
- **WHEN** the row is rendered
- **THEN** its `data-testid` is `"table::Actor/(esm/_ZVpS8M7)/ClassType::row::sig-42"`
- **AND** cells within it carry `data-testid="table::Actor/(esm/_ZVpS8M7)/ClassType::row::sig-42::cell::<column>"`

#### Scenario: Ad-hoc suffixes are eliminated

- **WHEN** the generator emits the DropdownButton, per-row action buttons, or FilterDialog operator MenuItems
- **THEN** no `data-testid` value contains a hyphen-joined ad-hoc suffix (`-dropdown-toggle`, `-clear-all`, `-open-selector`, `-inline-create`)
- **AND** every such button carries a testid produced by `buildFieldTestId(...)` with an explicit role

#### Scenario: Runtime and template output share testids

- **GIVEN** the same UI model rendered by (i) this template and (ii) the JUDO frontend runtime
- **WHEN** the generated DOM is compared for any given widget or table row
- **THEN** the `data-testid` values are byte-identical across the two outputs

**Templates**:
- `actor/src/utilities/transfer-id.ts.hbs` (new)
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
- `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`
- `actor/src/components/table/EagerTable.tsx.hbs`
- `actor/src/components/table/LazyTable.tsx.hbs`
- `actor/src/components/table/SelectCheckbox.tsx.hbs`
- `actor/src/components/table/table-row-actions.tsx.hbs`
- `actor/src/components/DropdownButton.tsx.hbs`
- `actor/src/components/dialog/FilterDialog.tsx.hbs`
- `actor/src/fragments/relation/column.fragment.hbs`
- `actor/src/containers/widget-fragments/*.hbs` (all)
- `actor/src/layout/**/*.tsx.hbs` (drawer, breadcrumb, menu)

**Registry**: `judo-ui-react/src/main/resources/ui-react.yaml` — one new entry for `transfer-id.ts`.
