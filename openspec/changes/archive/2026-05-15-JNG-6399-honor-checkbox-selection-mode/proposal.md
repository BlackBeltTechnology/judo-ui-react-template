## Why

The UI metamodel exposes `Table.checkboxSelection` with three literals (`ENABLED`, `DISABLED`, `AUTO`) and the designer surfaces all three as a user-facing pick. Today the React generator does not honor that pick faithfully:

- `UiTableHelper.checkboxSelectionEnabled(table)` returns `true` for both `ENABLED` and `AUTO`, conflating them.
- `DISABLED` hides the checkbox column but does **not** disable row multi-select, leaving the underlying MUI DataGrid in an inconsistent state (no checkboxes, yet Shift-click multi-row selection still works and bulk toolbar buttons remain visible).
- The modeler's explicit pick in the designer is therefore partially ignored.

The fix is small and entirely template-side: respect what the modeler typed.

## What Changes

### Display-page tables (the table is rendered on its own page/view)

The three enum values gain distinct, non-overlapping meanings:

- `ENABLED` (and the ecore default `null`) — always render the checkbox column, always allow multi-select. No inference, no exceptions. Current behavior preserved for any model that doesn't touch the field.
- `DISABLED` — never render the checkbox column, disable row multi-select, hide bulk toolbar buttons. Modeler's explicit opt-out.
- `AUTO` — let the generator decide based on the table's model. The checkbox column renders if and only if the table has at least one bulk action (`BulkDelete`, `BulkRemove`, or `BulkCallOperation`). When no bulk action exists, the checkbox column and multi-select are suppressed. This is the "smart" mode the original ticket wording asked for, now scoped to opt-in via `AUTO`.

The ecore default `ENABLED` is preserved — no metamodel change. AUTO is opt-in: a modeler must explicitly pick it in the designer.

### Selector-mode tables (the same Table opened as an Add/Set selector or Operation Input selector)

- Selectors SHALL continue to render the checkbox column and honor the existing page-level `allowSelectMultipleForPage` logic, independent of the source table's `checkboxSelection` value.
- Rationale: the user observation about "form-add-multiple from Galaxy" — when a form opens a selector dialog to pick rows, multi-select is part of the selector contract, not the source table's display preference. A modeler's `DISABLED` on the data display does not invalidate the picker.

### Non-goals

- The original ticket's blanket rule "Table with no bulk operation → no checkbox column" is NOT applied to every table. It applies only when the modeler picks `AUTO`. If the modeler picks `ENABLED` (or never touched the field), the checkbox column appears even on tables with zero bulk operations. The modeler's pick is law.
- The access-table / mapped-transfer-selector decoupling caveat is split off into a separate follow-up ticket. The change set here does not touch any selector-page-generation rule and therefore cannot regress selector behavior.

## Capabilities

### Modified Capabilities

- `data-tables`: The "Row Selection" requirement is rewritten so the three enum values have distinct meanings — `ENABLED` always shows, `DISABLED` always hides, `AUTO` infers from bulk-action presence — and selector-mode behavior is exempted from the source table's setting in all three modes.

## Impact

- **Java helpers modified**: `UiTableHelper.java` (existing `checkboxSelectionEnabled` replaced by mode-aware helpers, plus a new `tableHasAnyBulkAction` for AUTO inference), `UiWidgetHelper.tableButtonVisibilityConditions` (one extra AND for isBulk branches under DISABLED).
- **Templates modified**: `containers/components/table/index.tsx.hbs` (runtime-gated checkbox + allowSelectMultiple), `containers/components/cards/index.tsx.hbs` and `containers/components/tag/index.tsx.hbs` (same gate, own-page context only).
- **Test fixture added**: one Table in `RelationTest` model gains `checkboxSelection="DISABLED"` so the change is observable in snapshots.
- **Itest snapshots**: zero diff for tables that don't set the attribute (today: all 6 itest UI models have zero occurrences of `checkboxSelection=`). Diff only on the new fixture and on any actor where the new fixture is reachable.
- **No metamodel changes** in `judo-meta-ui` or `judo-meta-esm`.
- **No transformation changes** in `judo-tatami-client` or `judo-tatami-jsl`.
- **No breaking changes** for models that do not set `checkboxSelection` or set it to `ENABLED` / `AUTO`.

## Out of Scope (separate tickets)

- **Selector ↔ access decoupling**: when an access entry is removed in the modeler, mapped transfer selectors disappear from the selector list. This is a `judo-tatami-jsl` / `judo-tatami-client/esm2ui` rule wiring issue (selector page is `@lazy`-generated from the access entry, not from the selector source). To be filed as a separate JIRA ticket against the appropriate tatami repo.
- **JSL keyword for `checkbox: enabled|disabled|auto`**: the JSL DSL has no first-class syntax for the field today. Modelers using JSL inherit the ecore default. Out of scope here.
