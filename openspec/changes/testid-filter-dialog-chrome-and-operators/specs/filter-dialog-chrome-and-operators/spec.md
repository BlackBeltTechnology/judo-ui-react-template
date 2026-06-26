## ADDED Requirements

### Requirement: FilterDialog emits a per-Filter testid on its Add-new-filter button

Every generated `FilterDialog` component SHALL emit `data-testid={\`${id}-add-new-filter\`}` as a JSX prop on the `<DropdownButton>` that opens the attribute-pick menu (the "Add new filter" trigger). The expression `${id}` SHALL be the per-Filter scope variable already used by sibling chrome testids in the same dialog (`${id}-dialog-title`, `${id}-action-apply`, `${id}-action-clear-all`, `${id}-close`).

The `data-testid` attribute SHALL reach the rendered DOM on the user-clickable trigger button (the MUI `<Button>` produced by `DropdownButton`), not only on an internal wrapper element. If the `DropdownButton` component does not by default forward unknown JSX props to its trigger, `DropdownButton` SHALL be patched to spread unknown props onto the trigger so that this requirement holds.

#### Scenario: Add-new-filter testid present on a numeric column's filter dialog

- **GIVEN** a Filter model element bound to a numeric-type column with XMI id `_AbcDef`
- **AND** the generator emits a FilterDialog component for that Filter
- **WHEN** the dialog is opened in the running application
- **THEN** the DOM SHALL contain exactly one element matching `[data-testid="_AbcDef-add-new-filter"]`
- **AND** that element SHALL be the user-clickable "Add new filter" trigger button (clickable, of role `button`, with visible label "Add new filter" or its localized equivalent)
- **AND** the existing testids `_AbcDef-dialog-title`, `_AbcDef-action-apply`, `_AbcDef-action-clear-all`, `_AbcDef-close`, and `_AbcDef-operator` SHALL remain present and unchanged

### Requirement: Operator MenuItems emit platform-fixed kebab-case testids

Every `<MenuItem>` rendered inside the `FilterOperator` component's `getOperatorsByFilter(filter).map(...)` block SHALL emit `data-testid={\`filter-operator-<kebab>\`}` where `<kebab>` is the kebab-case form of the camelCase operator enum value `item`, computed as `item.replace(/[A-Z]/g, '-$&').toLowerCase()`.

The locked enum-to-testid table (drawn from `_FilterOperationType` in `actor/src/utilities/filter-helper.ts.hbs`):

| Enum value | Resulting `data-testid` |
|---|---|
| `equal` | `filter-operator-equal` |
| `notEqual` | `filter-operator-not-equal` |
| `lessThan` | `filter-operator-less-than` |
| `lessOrEqual` | `filter-operator-less-or-equal` |
| `greaterThan` | `filter-operator-greater-than` |
| `greaterOrEqual` | `filter-operator-greater-or-equal` |
| `isEmpty` | `filter-operator-is-empty` |
| `isNotEmpty` | `filter-operator-is-not-empty` |
| `like` | `filter-operator-like` |
| `notLike` | `filter-operator-not-like` |

The testids SHALL NOT be parameterized by the per-Filter `${id}` — operators are platform-universal enum constants, identical across every Filter dialog of every actor. The catalogue exposure (in `judo-ui-e2e-template`) SHALL therefore surface them as flat platform constants (e.g. `PlatformTestIds.filterDialog.operators.notEqual.id === 'filter-operator-not-equal'`), not as per-Filter properties.

The previous DOM attribute `data-testid={valueId}` (where `valueId === \`${id}-value\``) on these MenuItems SHALL be removed. The `valueId` local declaration in the `FilterOperator` component SHALL be deleted if it has no remaining references after the replacement.

#### Scenario: Operator MenuItems for a numeric-type filter expose the eight canonical testids

- **GIVEN** a Filter model element bound to a column of attribute type Numeric (or Date or DateTime)
- **AND** the generated FilterDialog is mounted and its operator Select is opened
- **WHEN** the operator MenuItems are rendered
- **THEN** the DOM SHALL contain exactly eight `<MenuItem>` elements with testids, one each from the set:
  ```
  filter-operator-equal
  filter-operator-not-equal
  filter-operator-less-than
  filter-operator-less-or-equal
  filter-operator-greater-than
  filter-operator-greater-or-equal
  filter-operator-is-empty
  filter-operator-is-not-empty
  ```
- **AND** no two MenuItems within the open Select SHALL share the same `data-testid`
- **AND** the visible label of each MenuItem SHALL continue to be governed by the existing `t(\`judo.modal.filter.${item}\`, ...)` translation lookup (label/testid decoupling is preserved)

#### Scenario: Operator MenuItems for a string-type filter include `filter-operator-like`

- **GIVEN** a Filter model element bound to a column of attribute type String
- **AND** the generated FilterDialog is mounted and its operator Select is opened
- **WHEN** the operator MenuItems are rendered
- **THEN** the DOM SHALL contain the eight numeric-set testids from the previous scenario
- **AND** the DOM SHALL additionally contain `<MenuItem data-testid="filter-operator-like">` (and `<MenuItem data-testid="filter-operator-not-like">` if and only if the `_FilterOperationType` enum exposes `notLike` for string filters)
- **AND** no two MenuItems within the open Select SHALL share the same `data-testid`

#### Scenario: Per-operator testid uniqueness within an open dialog

- **GIVEN** any Filter model element of any supported attribute type (Numeric, Date, DateTime, String, Boolean, Trinary, Enumeration)
- **AND** the generated FilterDialog is mounted and its operator Select is opened
- **WHEN** a Playwright author calls `page.getByTestId('filter-operator-equal')` (or any other operator testid emitted for that filter type)
- **THEN** the call SHALL resolve to exactly one DOM element under strict mode
- **AND** SHALL NOT fail with "multiple elements matched"
- **AND** the resolved element SHALL be a clickable `<MenuItem>` of role `option` whose `value` attribute equals the corresponding camelCase enum value

**NOTE** — These two requirements together close audit findings F9 (45 spec sites) and F15 (25 spec sites) on the DOM side. The catalogue exposure that makes them addressable from `PlatformTestIds.filterDialog.*` lives in the companion change `expose-filter-dialog-chrome-and-operators-catalogue` in the `judo-ui-e2e-template` repo. Together they form a Class II paired release: the DOM-side (this change) ships first or simultaneously, the catalogue-side picks up the new ids and exposes them, the downstream spec back-pass converts `getByRole(...)` fallbacks to `getByTestId(...)`.

**NOTE** — The kebab-case transformation `item.replace(/[A-Z]/g, '-$&').toLowerCase()` is mechanical and ASCII-safe. Any future addition to the `_FilterOperationType` enum that uses camelCase (the established convention) automatically produces a correct, unique testid without any further change to this template. The catalogue would need a single line added per new operator. No code in this template requires updating.
