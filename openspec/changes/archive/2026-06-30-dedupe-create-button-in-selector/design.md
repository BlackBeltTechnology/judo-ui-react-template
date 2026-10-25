## Context

Audit finding F10 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F10): `TransferObjectTableCreateButton` renders twice in the DOM when an AddSelector dialog overlays a page that owns the toolbar Create. Render-graph audit (2026-06-10) confirmed:

- The duplicate is produced because both the underlying page AND the selector dialog page mount the same `<TableComponent>` for the same `Table` model element (same XMI ID → same testids).
- MUI dialogs are portals — they overlay but do NOT unmount the underlying page, so both copies live in the DOM during the overlay's lifetime.
- The audit's hypothesis ("`TransferObjectTableInlineCreateButton` was intended for one render site") was half-right: the redundant button on selector pages is the form-opening Create (`openCreateFormAction`), not the inline-create. The form-opening Create is semantically meaningless in a selector (you're picking existing rows, not creating new ones), so suppressing it serves both the testid-uniqueness and UX-clarity goals.

## Render chain (file:line)

1. **Page mounts TableComponent** — `actor/src/pages/index.tsx.hbs` instantiates the generated `<{{ componentName table }} />` component for each Table.
2. **AddSelector dialog page mounts TableComponent** — `actor/src/pages/actions/OpenAddSelectorAction.fragment.hbs` opens a dialog page that ALSO instantiates the same `<{{ componentName table }} />` component (same Table model element → same generated component name → same DOM testids).
3. **TableComponent emits `toolBarActions`** — `actor/src/containers/components/table/index.tsx.hbs:215` builds the `toolBarActions` array from `table.tableActionButtonGroup.buttons`. Both `openCreateFormAction` and `inlineCreateRowAction` are emitted unconditionally.
4. **Selector signal already propagated** — `actor/src/containers/components/table/index.tsx.hbs:153` defines `const isSelector = useMemo(() => Array.isArray(selectionDiff), [selectionDiff])`. This `isSelector` is propagated to the underlying table component via prop:
   - LazyTable: `containerIsSelector={isSelector}` (table/index.tsx.hbs:364, LazyTable.tsx.hbs:92 prop, :143 destructured).
   - EagerTable: `isSelectorTable={isSelector}` (table/index.tsx.hbs:315, EagerTable.tsx.hbs:89 prop — but never destructured/read).
5. **Toolbar render** — `LazyTable.tsx.hbs:737` and `EagerTable.tsx.hbs:575` call `toolBarActions.map(...)` to render the buttons inside `<GridToolbarContainer>`.

The fix inserts a runtime filter at step 5 driven by a static-time flag emitted at step 3, gated by the existing prop from step 4.

## Goals

1. Spec authors can address `getByTestId(<table>.toolbarActions.create.id)` on regular pages WITHOUT strict-mode violations from concurrent selector overlays.
2. The `inlineCreateRowAction` (catalogued under `<table>.toolbarActions.inlineCreate`) is unaffected — it continues to render based on its own `enabled()` callback.
3. The form-opening Create button never appears inside a selector dialog's toolbar — a desirable UX side-effect documented in the spec.
4. Implementation surface stays under 10 lines across 4 files. No Java helper change.

## Non-Goals

1. NOT fixing the underlying portal-related duplicate-mount of the TableComponent. That is a deeper React Router / dialog stack invariant. Suppressing the redundant Create button is the targeted minimum fix.
2. NOT renaming `containerIsSelector` to `isSelectorTable` (or vice versa) in EagerTable / LazyTable. The naming inconsistency is pre-existing; consolidating it is out of scope.
3. NOT touching the `enabled()` callback signature. Adding a new arg (e.g. `isSelector`) would ripple through `tableButtonVisibilityConditions()` in `UiWidgetHelper.java` and every action's emitted `enabled` body — too invasive.
4. NOT introducing an EMF model attribute. The hidden-in-selector property is intrinsic to the action TYPE (`isOpenCreateFormAction`), not a per-button user-configurable flag. No model change is appropriate.

## Decisions

### D1 — Add a new prop `hiddenInSelectorMode` to `ToolBarActionProps<T>` (not extending `enabled()`)

**Decision**: Emit a new static-time boolean `hiddenInSelectorMode: true` on the action object when the underlying `actionDefinition.isOpenCreateFormAction` is true. Filter in the render path.

**Alternatives considered**:
- Augment `enabled()` to receive an additional `isSelector` arg and have `tableButtonVisibilityConditions()` (Java helper) emit `&& !isSelector` into the body when emitting an OpenCreateFormAction button. Cleaner from a "single source of truth" angle but ripples through the entire callback signature: every existing `enabled()` body, every test that asserts on signatures, every wrapper component.
- Hardcode `toolBarAction.name === 'openCreateFormAction'` in the render filter. Fragile — couples the runtime to a generated string. If `simpleActionDefinitionName(...)` ever changes to use a different name, the filter silently breaks.
- Add a private helper `isFormOpeningCreateAction(toolBarAction)` to a JS utilities file. Same fragility as above, just hidden.

A static-time flag in the action object is unambiguous, type-safe, and discoverable. Future analogous suppressions (e.g. "hidden in form mode") can use the same pattern.

### D2 — Filter at the render call site, not at the `toolBarActions` array construction

**Decision**: Keep the static action array intact (it remains the catalogue's source of truth for what testids exist) and filter at the JSX `.map(...)` call. This way the catalogue's claim that the testid is exposed remains true; only the runtime DOM rendering is gated.

**Alternatives considered**:
- Conditionally exclude the action from the `toolBarActions` array entirely at construction time. But the array is constructed inside a function-component body BEFORE `containerIsSelector` is consulted in the existing code shape, and tying construction to runtime state would force a `useMemo` wrapper. Heavier diff for no gain.

### D3 — Use each table's pre-existing prop name (`containerIsSelector` vs `isSelectorTable`)

**Decision**: LazyTable reads `containerIsSelector` (already destructured). EagerTable reads `isSelectorTable` (newly destructured by this change, but the prop was already declared).

**Alternatives considered**:
- Rename both to a single canonical name. Out of scope per Non-Goal #2.
- Pass a NEW prop with a new name to both. Adds a third name to the mix and the EMF-generated table/index.tsx.hbs would need to pass it to both tables.

### D4 — Use `button.actionDefinition.isOpenCreateFormAction` as the Handlebars-side condition

**Decision**: Use the EMF-derived boolean property `isOpenCreateFormAction` on the action definition. Already in use at `dialog.tsx.hbs:180,216,236,246,274` (5 sites) for sibling features — proven Handlebars idiom in this codebase.

### D5 — Defer consolidating `containerIsSelector` / `isSelectorTable` naming

**Decision**: Live with the inconsistency. A follow-up cleanup change can unify the two names once this F10 fix is downstream and validated.

## Risks

| Risk | Mitigation |
|---|---|
| The audit's spec sites (`CrudActionsOnSingleAndManyRelationsTest.spec.ts`) actually wanted to address the form-opening Create from WITHIN the selector dialog (not the underlying page's button). In that case, hiding it would break those specs. | Re-read the audit text: F10 explicitly notes the duplicate is on the SAME page that owns the toolbar Create — the underlying page, not the selector. The spec authors couldn't disambiguate because both had identical testids. Once the selector-side Create is hidden, the underlying-page testid is unique and the spec works. |
| Other tables may legitimately need a form-opening Create button visible inside selector context (e.g. a custom-implemented selector that exposes "create new option" inline). | These cases are not in the current itest. If they emerge, the model would already need a special-case escape hatch (a per-button `forceVisibleInSelector` flag). Out of scope here. |
| `hiddenInSelectorMode` becomes a magic word that future readers won't connect to F10. | Comment in `containers/components/table/index.tsx.hbs` next to the emission line + cross-link in this design.md. The change folder name `dedupe-create-button-in-selector` is itself self-documenting. |
| The `isSelectorTable` prop on EagerTable was declared but unused; destructuring it newly might surface a lint/dead-code warning. | The implementation now uses it in the filter, so the warning (if any) is resolved by the same change. |

## Implementation sketch

**`utilities/table.ts.hbs` (line ~62, inside the `ToolBarActionProps<T>` interface):**

```ts
  confirmationCondition?: boolean;
+ // F10: when true AND the table is mounted in selector mode, the toolbar render
+ // path SHALL suppress this action. Currently set on OpenCreateForm actions
+ // (see containers/components/table/index.tsx.hbs:215).
+ hiddenInSelectorMode?: boolean;
}
```

**`containers/components/table/index.tsx.hbs` (line ~227, inside the action object literal):**

```handlebars
      isBulk: {{ boolValue actionDefinition.isBulk }},
+     {{# if button.actionDefinition.isOpenCreateFormAction }}
+     // F10: hide form-opening Create inside selector dialogs (duplicate-testid suppression).
+     hiddenInSelectorMode: true,
+     {{/ if }}
      {{# if button.confirmation }}
```

**`components/table/LazyTable.tsx.hbs` (line ~737):**

```jsx
- toolBarActions.map( (toolBarAction: ToolBarActionProps<T>) => actions[toolBarAction.name] && toolBarAction.enabled(...) ? (
+ toolBarActions
+   .filter((toolBarAction: ToolBarActionProps<T>) => !(containerIsSelector && toolBarAction.hiddenInSelectorMode))
+   .map( (toolBarAction: ToolBarActionProps<T>) => actions[toolBarAction.name] && toolBarAction.enabled(...) ? (
```

**`components/table/EagerTable.tsx.hbs` (line ~135 destructure + line ~575 filter):**

```jsx
  // destructure block
    toolBarActions,
+   isSelectorTable,

  // render
- toolBarActions.map( (toolBarAction: ToolBarActionProps<T>) => actions[toolBarAction.name] && toolBarAction.enabled(...) ? (
+ toolBarActions
+   .filter((toolBarAction: ToolBarActionProps<T>) => !(isSelectorTable && toolBarAction.hiddenInSelectorMode))
+   .map( (toolBarAction: ToolBarActionProps<T>) => actions[toolBarAction.name] && toolBarAction.enabled(...) ? (
```
