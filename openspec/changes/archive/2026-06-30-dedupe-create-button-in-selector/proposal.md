## Why

The generated TableComponent (`containers/components/table/index.tsx.hbs`) unconditionally renders every button in `table.tableActionButtonGroup.buttons` as a toolbar button. Among those buttons, the model emits TWO Create-flavoured actions for every table that supports `Create`:

| Action name in JS | DOM testid suffix | Purpose |
|---|---|---|
| `openCreateFormAction` | `…/TransferObjectTableCreateButton` | Opens a Create form dialog |
| `inlineCreateRowAction` | `…/TransferObjectTableInlineCreateButton` | Adds a draft row inline in the grid |

When the user opens an `AddSelector` overlay on top of a page that owns this table, the selector page mounts the same `<TableComponent>` (same XMI ID, same testids) on top of the underlying page. Both copies live in the DOM simultaneously because MUI dialogs are portals — they do NOT unmount the underlying page. The result: `data-testid="…/TransferObjectTableCreateButton"` appears TWICE, which violates Playwright `getByTestId(...)` strict-mode and forces specs to fall back to label-based locators (audit finding F10, `/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F10; ~20 conversion sites blocked).

The "Create a new instance via form dialog" action is a redundant UX inside a selector dialog — the user is in a selector to PICK existing rows, not to create new ones. Hiding the `openCreateFormAction` button in selector context is therefore both:
- a render-correctness fix (eliminates the duplicate testid)
- a UX consistency fix (removes a misleading button from the selector toolbar)

The `inlineCreateRowAction` button can continue to render in selector context if needed; its id (`…/TransferObjectTableInlineCreateButton`) is different so no duplicate testid arises.

## What Changes

Single Class III (react-template-only) change per the upstream-testid-fix workflow playbook. Touches three files. No model change, no Java helper change, no catalogue change.

### (a) Mark `openCreateFormAction` toolbar buttons as hidden in selector mode

- In `actor/src/containers/components/table/index.tsx.hbs` (line ~215, the `toolBarActions` array emission), conditionally emit a new boolean property `hiddenInSelectorMode: true,` on each button object when `button.actionDefinition.isOpenCreateFormAction` is true.
- Other buttons are unaffected — they emit no such property (which becomes `undefined` at runtime, equivalent to `false` for the filter check below).

### (b) Extend the `ToolBarActionProps<T>` interface

- In `actor/src/utilities/table.ts.hbs`, add `hiddenInSelectorMode?: boolean;` to `ToolBarActionProps<T>` so the new property is type-checked.

### (c) Filter the toolbar render in `LazyTable` / `EagerTable`

- In `actor/src/components/table/LazyTable.tsx.hbs` (line ~737, the `toolBarActions.map(...)` inside `<GridToolbarContainer>`), wrap the map with `.filter((a) => !(containerIsSelector && a.hiddenInSelectorMode))`. The `containerIsSelector` prop is already destructured at line 143.
- In `actor/src/components/table/EagerTable.tsx.hbs` (line ~575), apply the same filter. EagerTable already declares `isSelectorTable?: boolean;` (line 89) — but unlike LazyTable it never destructures or reads that prop. To keep this change minimal and consistent, this change also destructures `isSelectorTable` at the props-destructuring site and uses it in the filter.

### What this change does NOT do

- Does **not** modify the EMF UI model or any generator Java helper. The decision lives entirely in the template's runtime layer.
- Does **not** alter the catalogue exposed by `judo-ui-e2e-template`. Both `openCreateFormAction` and `inlineCreateRowAction` testids continue to be exposed under `<table>.toolbarActions.create` and `<table>.toolbarActions.inlineCreate` respectively — they still EXIST in the catalogue; they're just not all rendered in every context.
- Does **not** hide `openCreateFormAction` in non-selector context. Form-opening Create remains visible on regular table pages exactly as before.
- Does **not** hide `inlineCreateRowAction` in selector context — if a selector model intentionally exposes inline-create, that button still renders. (The audit's hypothesis that the second mount was meant to render InlineCreate is half-right: the FIX is to suppress the form-opening Create, not to swap to InlineCreate.)
- Does **not** address F11 (already shipped as Class I in `judo-ui-e2e-template`), F14 (active under JNG-6391), F1 / F5 / F3 / F9 / F15 (separate clusters).

## Capabilities

### Modified Capabilities

- **`table-toolbar-rendering`** — One new MODIFIED requirement ("Selector-mode tables suppress form-opening Create buttons") locking the runtime filter semantics. (If `table-toolbar-rendering` does not exist yet as a capability, this change adds it as a new capability with one requirement.)

## Impact

- **`actor/src/containers/components/table/index.tsx.hbs`** (line ~215): one new conditional emission inside the `toolBarActions` `{{# each }}` loop. ~3 lines.
- **`actor/src/utilities/table.ts.hbs`** (interface `ToolBarActionProps<T>`): one new optional property `hiddenInSelectorMode?: boolean;`. 1 line.
- **`actor/src/components/table/LazyTable.tsx.hbs`** (line ~737): wrap `.map` with `.filter`. ~1 line.
- **`actor/src/components/table/EagerTable.tsx.hbs`** (line ~135 destructuring + line ~575 filter): destructure `isSelectorTable` + wrap `.map` with `.filter`. ~2 lines.
- **Generated TypeScript**: every TableComponent now emits `hiddenInSelectorMode: true,` for `openCreateFormAction` entries. LazyTable / EagerTable now render the toolbar via a filtered list. Existing tests should pass.
- **Integration test (`judo-ui-react-itest`)**: `./mvnw clean install` regenerates and runs Vitest/Playwright on the fixture frontends. The change is purely additive at the type level (`hiddenInSelectorMode?:`) so no existing test should break.
- **Downstream consumer (`BlackBeltTechnology/judo-tatami-tests`)**: after release + bump, the `CrudActionsOnSingleAndManyRelationsTest.spec.ts` sites that were reverted to label can be converted back to `getByTestId(<table>.toolbarActions.create.id)` — uniqueness is restored. ~20 conversion sites.
