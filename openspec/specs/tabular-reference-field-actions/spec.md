# tabular-reference-field-actions Specification

## Purpose
Specifies `data-testid` attributes for inline action icon buttons on TabularReferenceField widgets. `Tags.tsx` emits `${id}-clear-all`, `${id}-inline-create`, and `${id}-open-selector` on its `TagsButtonGroup` icon buttons; `SingleRelationInput.tsx` emits `${id}-${buttonProp.name}` on each mapped action IconButton and `${id}-dropdown-toggle` on the chevron. The `id` prop is the parent relation field's XMI id, threaded from `containers/components/tag/index.tsx.hbs`.
## Requirements
### Requirement: Tags widget emits inline action testids

The generated `Tags.tsx` (the DOM realisation of a JUDO `TabularReferenceField`) SHALL emit a `data-testid` attribute on each of its three inline action `IconButton`s rendered inside the `TagsButtonGroup` (Tags.tsx lines 309-323). The testid SHALL be composed as `${id}-<suffix>` where `id` is the `Tags` component's `id` prop (the parent field's XMI ID, declared in `TagsProps<P, T>` at line 21 and destructured at line 58) and `<suffix>` is one of:

| Button | Render gate (handler prop) | Suffix |
|---|---|---|
| Clear-all | `onClearDialogsClick` (bound to `ClearAction`) | `clear-all` |
| Inline-create | `onCreateDialogsClick` (bound to `OpenCreateFormAction`) | `inline-create` |
| Open-selector | `onSearchDialogsClick` (bound to `OpenAddSelectorAction`) | `open-selector` |

The testid SHALL appear in the DOM if and only if the corresponding `IconButton` renders. The render gates (`!readOnly && on<X>Click`) are unchanged by this requirement.

This Requirement does NOT introduce a `${id}-view` testid on the Tags inline button group. The view action on a TabularReferenceField is rendered per-row inside the chip render (line 253, where `data-testid={`${id}-view`}` already exists on the per-row preview button); adding a parallel inline `-view` would create a duplicate-testid collision (see Requirement 3).

This Requirement does NOT apply when the parent field declares `customImplementation`. In that case the entire `Tags` widget is replaced by a user-supplied `ComponentProxy` and the testid contract is the user's responsibility.

#### Scenario: Inline action buttons receive testids when rendered

- **GIVEN** a JUDO `TabularReferenceField` model element WITHOUT `customImplementation`, whose container declares `OpenAddSelectorAction`, `OpenCreateFormAction`, and `ClearAction` action definitions
- **AND** the field's XMI ID is `T_abc123`
- **WHEN** the generated `Tags` component renders with `readOnly === false`
- **THEN** the DOM SHALL contain `<button data-testid="T_abc123-clear-all">` (the Clear-all IconButton)
- **AND** the DOM SHALL contain `<button data-testid="T_abc123-inline-create">` (the Inline-create IconButton)
- **AND** the DOM SHALL contain `<button data-testid="T_abc123-open-selector">` (the Open-selector IconButton)

#### Scenario: Inline action buttons absent when their handler prop is unset

- **GIVEN** a JUDO `TabularReferenceField` whose container declares ONLY `OpenAddSelectorAction` (no `OpenCreateFormAction`, no `ClearAction`)
- **WHEN** the generated `Tags` component renders with `readOnly === false`
- **THEN** the DOM SHALL contain `<button data-testid="${fieldId}-open-selector">`
- **AND** the DOM SHALL NOT contain any element with `data-testid="${fieldId}-clear-all"`
- **AND** the DOM SHALL NOT contain any element with `data-testid="${fieldId}-inline-create"`
- **NOTE** — spec authors querying for a not-rendered button must handle absence gracefully (same precedent as F11 `editActions`)

### Requirement: SingleRelationInput widget emits inline action testids

The generated `SingleRelationInput.tsx` SHALL emit a `data-testid` attribute on each `IconButton` rendered inside its `AggregationInputButtonGroup`. The IconButtons are produced by two sites in the .hbs source:

1. **`buttonProps`-driven IconButtons** (lines 222-237 in `SingleRelationInput.tsx.hbs`): a `.filter(...).filter(...).map((buttonProp) => <IconButton .../>)` chain over a `buttonProps` array filtered by `a.name === 'set' || a.name === 'view' || a.name === 'create'`. The testid SHALL be composed as `${id}-${buttonProp.name}`, producing `<id>-set`, `<id>-view`, or `<id>-create` depending on which entry is rendered.
2. **Chevron dropdown toggle** (line 240): the IconButton with `className={`${name}-dropdown`}` rendered when `exists(value) && (onDelete || onSet || onUnSet)`. The testid SHALL be `${id}-dropdown-toggle`.

`id` is the `SingleRelationInput` component's `id` prop (declared in `SingleRelationInputProps` at line 30, destructured at line 53), bound by call sites to the parent single-relation field's XMI ID.

The testid SHALL appear in the DOM if and only if the corresponding `IconButton` renders. The render gates (the two `.filter(...)` calls for `buttonProps`, and the `exists(value) && ...` condition for the dropdown toggle) are unchanged.

#### Scenario: buttonProps and dropdown toggle receive testids

- **GIVEN** a JUDO single-relation field whose XMI ID is `S_xyz789`
- **AND** the model exposes `set`, `view`, and `create` actions and the relation currently has a non-null value
- **WHEN** the generated `SingleRelationInput` component renders
- **THEN** the DOM SHALL contain `<button data-testid="S_xyz789-set">`, `<button data-testid="S_xyz789-view">`, and `<button data-testid="S_xyz789-create">` (each appearing only if the corresponding `buttonProp.visible(value)` returns truthy)
- **AND** the DOM SHALL contain `<button data-testid="S_xyz789-dropdown-toggle">` (the chevron toggle)

### Requirement: New testids do not collide with existing testids

The new testids introduced by Requirements 1 and 2 SHALL NOT collide with any pre-existing testid emitted by the same widgets. Specifically:

| Pre-existing testid | Location | Untouched by this change |
|---|---|---|
| `${id}` (root Autocomplete) | `Tags.tsx.hbs:200` | yes |
| `${id}-delete` (per-row chip delete) | `Tags.tsx.hbs:223` | yes |
| `${id}-open-dialog` (per-row chip open-dialog) | `Tags.tsx.hbs:239` | yes |
| `${id}-view` (per-row chip preview/view) | `Tags.tsx.hbs:253` | yes |
| `${id}-download` (per-row chip download) | `Tags.tsx.hbs:263` | yes |
| `${id}` (root Autocomplete in SingleRelationInput) | `SingleRelationInput.tsx.hbs:194` | yes |
| `${name}-menu` (MenuList) | `SingleRelationInput.tsx.hbs:276` | yes |

No new testid SHALL re-use any of the suffixes above. In particular:
- The Tags inline button group SHALL NOT emit a `${id}-view` testid (the per-row chip at line 253 already owns that suffix); the view action on a TabularReferenceField is per-row, not inline.
- The SingleRelationInput inline group's `${id}-view` testid (Requirement 2) is in a DIFFERENT widget DOM tree and does NOT collide with the Tags per-row `${id}-view` (they belong to different `id` values for different field XMI IDs).

#### Scenario: Pre-existing testids remain after the change

- **GIVEN** a generated `Tags.tsx` produced by the post-change generator for a field with XMI ID `T_abc123`
- **WHEN** the component renders with at least one row chip displayed and the inline button group visible
- **THEN** the DOM SHALL contain `<div data-testid="T_abc123">` (root)
- **AND** the DOM SHALL contain `<button data-testid="T_abc123-delete">` on each chip (existing)
- **AND** the DOM SHALL contain `<button data-testid="T_abc123-view">` on each chip (existing, per-row)
- **AND** the DOM SHALL contain the three new inline testids from Requirement 1
- **AND** no testid SHALL appear more than once unless it is a per-row chip testid (`-delete`, `-view`, `-open-dialog`, `-download`) which legitimately repeats once per row

**NOTE** — The new `clear-all` / `inline-create` / `open-selector` / `dropdown-toggle` / `set` / `create` suffixes are reserved by this capability. Future audit findings that need additional inline-button testids on the same widgets SHALL introduce new suffixes, not overload these.

