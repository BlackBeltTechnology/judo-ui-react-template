# Design

## Problem reframed

`Table.checkboxSelection` is set in the designer (`esm.odesign` rows 21761 & 23626 expose all three values: `ENABLED`, `DISABLED`, `AUTO`). It survives the ESM → UI transformation untouched (`tabular.etl` lines 25–27 forwards `s.checkboxSelection` to `t.checkboxSelection` when defined). The React generator then reads it back via:

```java
// judo-ui-react/src/main/java/.../UiTableHelper.java:373
public static boolean checkboxSelectionEnabled(Table table) {
    return table.getCheckboxSelection() == null
        || table.getCheckboxSelection() != CheckboxSelection.DISABLED;
}
```

This treats `null`, `ENABLED`, and `AUTO` as the same thing, and is the only gate. `DISABLED` correctly suppresses the checkbox column via the existing template:

```handlebars
{{# unless (checkboxSelectionEnabled table) }}
  checkboxSelection={ false }
{{/ unless }}
```

But `allowSelectMultiple` and the bulk toolbar buttons are not gated by it, so `DISABLED` leaves an inconsistent UI: no checkboxes, but Shift-click still selects multiple rows and bulk buttons are still visible.

## Two contexts that converge on the same Table

A given `Table` model element is rendered in two distinct contexts at runtime:

| Context | Where | How it's identified at render time |
|---|---|---|
| 1. Own page | Access-table page, view page, regular form embedding | `isSelector === false` (computed from props: `selectionDiff` is not an array) |
| 2. Selector dialog | Add/Set selector page, operation-input selector | `isSelector === true` |

Both contexts use the same generated `containers/components/<Path>/<TableComponent>/index.tsx` file. The difference is purely runtime: the same component receives different props.

### Current source of truth for `allowSelectMultiple`

- **Context 1** (own page): the table component default `allowSelectMultiple = true`, propagated through `containers/components/table/index.tsx.hbs` line 137. No per-table override.
- **Context 2** (selector): `dialogs/index.tsx.hbs` line 476 passes `allowSelectMultiple={{ allowSelectMultipleForPage(page) }}` — derived from whether the parent page has an Add action wired into a container button group (i.e. the form-add-multiple-from-Galaxy scenario).

`checkboxSelection` is set the same way in both contexts (a single template position in `containers/components/table/index.tsx.hbs`), and today only Context 1 honors `DISABLED`. Context 2 also calls the gate, which means today **a `DISABLED` source table loses its selector checkbox column too** — that's a bug we will fix.

## Decisions

### D1. Three distinct modes; AUTO is the inference path

The three enum values have non-overlapping meanings:

| Value | Own-page rule |
|---|---|
| `ENABLED` (and ecore-default `null`) | Always show checkbox column, always allow multi-select |
| `DISABLED` | Never show, never allow |
| `AUTO` | Show iff `tableHasAnyBulkAction(table)` is true |

A "bulk action" is any button on `tableActionButtonGroup` whose action definition is one of `BulkDeleteActionDefinition`, `BulkRemoveActionDefinition`, or `BulkCallOperationActionDefinition`. The list is closed (no custom-action escape hatch) so the helper is deterministic.

Reasoning: ENABLED is the literal "yes" path — no inference. AUTO is opt-in smart behavior the modeler invokes deliberately by picking it in the designer. DISABLED is the literal "no" path. Each value answers exactly one question, with no overlap.

### D2. Selector context overrides everything

When the table renders in selector mode (`isSelector === true`), the source table's `checkboxSelection` is ignored entirely, regardless of value. The checkbox column always renders and `allowSelectMultiple` is derived from `allowSelectMultipleForPage(page)`. This applies to all three modes (ENABLED, DISABLED, AUTO).

The override happens at template time, not in the Java helper, because the helper has no notion of "which page is hosting this Table right now".

Implementation:

- For `checkboxSelection` (a JSX prop deep inside the component body), the template emits a literal runtime ternary `checkboxSelection={ isSelector ? true : <own-page literal> }` because `isSelector` is in scope at that point.
- For `allowSelectMultiple` (a destructured prop default at the top of the component, *above* the `useMemo` that binds `isSelector`), the template emits only the own-page literal: `allowSelectMultiple = <own-page literal>,`. The destructure default cannot reference `isSelector` without a TDZ violation. This is safe because the only caller that puts the table in selector mode (`dialogs/index.tsx.hbs:476`) **always** passes the prop explicitly via `allowSelectMultiple={ allowSelectMultipleForPage(page) }`, so the destructure default only fires in own-page context, which is exactly the value we wrote. Own-page callers (`containers/widget-fragments/table.hbs:47`) only forward the prop under `{{# if container.table }}` (selector-only branch); the own-page branch omits the prop and the default fires.

The Java helpers compute only the own-page side; selector-mode rendering is fully handled at the call site.

### D3. Bulk toolbar buttons under DISABLED and AUTO

Bulk toolbar buttons (`isBulk: true` in `toolBarActions`) are only emitted in own-page context (their `actionDefinition` lives in the source `TransferObjectTableTableButtonGroup`; selectors don't render that group's bulk subset because selector pages don't pull in `tableActionButtonGroup` the same way). `UiWidgetHelper.tableButtonVisibilityConditions` for `isBulk` actions today returns `"selectionModel.ids.size > 0"`.

We extend it to additionally evaluate `multiSelectAllowedForOwnPage(table)` at generate time:

- `DISABLED` → helper returns `false` → `enabled:` callback collapses to `false` → button hidden.
- `AUTO` with no bulk actions → by definition there are no bulk buttons to hide. (The AUTO inference predicate is exactly `tableHasAnyBulkAction`, so this branch is unreachable.)
- `AUTO` with bulk actions → helper returns `true` → buttons render normally.
- `ENABLED` → buttons render normally regardless of whether they exist.

The selector context is unaffected because selector dialogs don't display bulk buttons in their toolbar — they show the page-level Add/Back/Set buttons defined in `TransferObjectTableButtonGroup`, which is a different list.

### D4. Card and Tag representations honor DISABLED too

`representationComponent = CARD` and `representationComponent = TAG` render via separate component templates (`containers/components/cards/index.tsx.hbs`, `containers/components/tag/index.tsx.hbs`). Both accept `allowSelectMultiple`. The same gate applies: own-page context honors DISABLED, selector context overrides.

For CARD/TAG there is no "checkbox column" concept per se; the toggle is purely about whether row click contributes to a selection set. The Java helper treats them uniformly with `Table`.

## New / changed Java helpers

```java
// UiTableHelper.java

/**
 * True iff the table has at least one table-level bulk action button.
 * The closed list of bulk action definitions:
 *   - BulkDeleteActionDefinition
 *   - BulkRemoveActionDefinition
 *   - BulkCallOperationActionDefinition
 */
public static boolean tableHasAnyBulkAction(Table table) {
    if (table.getTableActionButtonGroup() == null) {
        return false;
    }
    return table.getTableActionButtonGroup().getButtons().stream()
        .map(Button::getActionDefinition)
        .anyMatch(ad -> ad.getIsBulkDeleteAction()
                     || ad.getIsBulkRemoveAction()
                     || ad.getIsBulkCallOperationAction());
}

/**
 * True iff the checkbox column should render when this table is on its own page.
 *   ENABLED / null  → true
 *   DISABLED        → false
 *   AUTO            → tableHasAnyBulkAction(table)
 */
public static boolean checkboxSelectionForOwnPage(Table table) {
    CheckboxSelection cb = table.getCheckboxSelection();
    if (cb == CheckboxSelection.DISABLED) return false;
    if (cb == CheckboxSelection.AUTO)     return tableHasAnyBulkAction(table);
    return true; // ENABLED or null
}

/**
 * True iff multi-row select should be allowed when this table is on its own page.
 * Mirrors checkboxSelectionForOwnPage exactly.
 */
public static boolean multiSelectAllowedForOwnPage(Table table) {
    return checkboxSelectionForOwnPage(table);
}

// The old checkboxSelectionEnabled(table) helper is replaced by the above.
// Its sole template caller is migrated to a runtime expression
// (see template change below) so the rename is safe.
```

The two output helpers are kept as separate methods (rather than one with two callers) so future divergence — e.g. "AUTO shows the column but disallows multi-select" — can land without renaming.

## Template changes

### `containers/components/table/index.tsx.hbs`

**Before** (two occurrences, one per Eager / Lazy branch):

```handlebars
{{# unless (checkboxSelectionEnabled table) }}
  checkboxSelection={ false }
{{/ unless }}
```

**After**:

```handlebars
checkboxSelection={ isSelector ? true : {{ boolValue (checkboxSelectionForOwnPage table) }} }
```

(MUI's `<DataGrid checkboxSelection>` defaults to `false`, so passing the boolean explicitly is correct.)

`allowSelectMultiple` default at line 137 changes from:

```ts
allowSelectMultiple = true,
```

to:

```ts
allowSelectMultiple = {{ boolValue (multiSelectAllowedForOwnPage table) }},
```

Note on the missing `isSelector` ternary: `allowSelectMultiple` is explicitly passed by `dialogs/index.tsx.hbs:476` (from `allowSelectMultipleForPage(page)`) when the page is a selector, and the destructured default fires only when the prop is undefined. Selector callers always pass it; own-page callers (`containers/widget-fragments/table.hbs:47`, gated by `{{# if container.table }}`) only pass it in selector branches. So the destructure default fires exclusively in own-page context, which is the literal we wrote. The author's first instinct to write `isSelector ? true : <literal>` here would have been a temporal-dead-zone error — `isSelector` is declared by a `useMemo` *below* the destructure block.

### `containers/components/cards/index.tsx.hbs` and `containers/components/tag/index.tsx.hbs`

Same `allowSelectMultiple` default shift.

### `UiWidgetHelper.tableButtonVisibilityConditions`

For any button whose `actionDefinition.isIsBulk()` is true, prepend `multiSelectAllowedForOwnPage(table)` (compile-time literal `true` / `false`) ANDed into the result. When `false`, the entire JS condition collapses to `false` and the toolbar entry is rendered with `enabled: () => false`. Combined with no checkbox column → no selection → bulk button greys out and is functionally unreachable, matching user expectation.

## Test fixture

`judo-ui-react-itest/RelationTest/model/RelationTest-ui.model` will gain `checkboxSelection="DISABLED"` on exactly one Table chosen to be:

1. Reachable from at least one actor's generated app
2. Not also used as a selector elsewhere (to keep the test isolated)
3. Not a Card / Tag representation (separate test for those if we want one)

After regeneration, the affected component's `index.tsx` will pass `checkboxSelection={false}` and `allowSelectMultiple={false}` in own-page context. The corresponding actor's snapshot file is updated in the same commit.

If we additionally want to lock in the selector override, a second fixture is added: a *different* table marked DISABLED that is ALSO opened as a selector from another page. The generated selector dialog must still pass `checkboxSelection={true}` and `allowSelectMultiple={allowSelectMultipleForPage}`. This is the regression guard against future "helpful" simplifications that forget the selector override.

## Risk inventory

- **Snapshot churn**: zero on existing models (no current itest UI model sets `checkboxSelection=`). The added fixtures are the only intentional diff.
- **Backward compatibility for downstream consumers**: customers whose models do not touch `checkboxSelection` see no change. Customers who explicitly set `DISABLED` and previously worked around the inconsistency by post-processing the generated app: their workaround becomes unnecessary; output now matches their intent.
- **AUTO inference correctness**: any model that previously set `AUTO` (none in current itests) will now render its checkbox column conditionally on bulk-action presence instead of unconditionally. This is intentional. Customers who wanted unconditional checkboxes must pick `ENABLED` explicitly.
- **"No bulk → no checkbox" only fires under AUTO**: a customer who picks `ENABLED` on a bulk-less table gets a usable but pointless checkbox column. This is by design — the modeler's pick is law.
