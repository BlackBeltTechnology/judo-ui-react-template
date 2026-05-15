## Context

The React code generator uses Handlebars templates to produce React/TypeScript components from JUDO UI model elements. Each UI model element has an XMI ID, and the E2E template generator (`judo-ui-e2e-template`) produces a `VisualElementIds.ts` file that declares these IDs as expected `data-testid` values. Playwright tests assert that each declared ID is visible in the DOM.

Four template files produce output where `data-testid` is either missing, misplaced, or the element is skipped entirely:

1. `datetimeinput.hbs` — sets `data-testid` on the React component prop, but MUI `<DateTimePicker>` does not forward arbitrary props to the DOM
2. `buttongroup.hbs` — uses `id=` instead of `data-testid=` and appends a `-button-group` suffix
3. `table/index.tsx.hbs` — column `GridColDef` objects have no mechanism to put `data-testid` on column headers
4. `table/index.tsx.hbs` — `{{# unless button.actionDefinition.isOpenPageAction }}` excludes the View button from row actions

Recent commits established the pattern: `68b609bb` introduced `data-testid` attributes broadly, `a55bd388` changed `id` to `data-testid` on MUI components, and `6d8f9cfa` (JNG-6393) removed duplicate `data-testid` from flex containers. These fixes follow the same pattern.

## Goals / Non-Goals

**Goals:**
- Every UI model element declared in VisualElementIds renders exactly one DOM element with a matching `data-testid`
- Fixes are minimal, template-only changes — no Java helper or model changes
- All itest snapshots are updated to reflect the new output

**Non-Goals:**
- Fixing the `SingleRelationInput` duplicate `data-testid` (Autocomplete + TextField) — that's a widget-level issue requiring a different approach
- Adding `data-testid` to elements not yet declared in VisualElementIds
- Changing the VisualElementIds generator

## Decisions

### Decision 1: DateTimePicker — use `slotProps.textField.inputProps`

MUI `<DateTimePicker>` does not forward unknown props like `data-testid` to any DOM element. The `slotProps.textField` already receives `id` (line 35 of `datetimeinput.hbs`).

**Approach:** Remove `data-testid` from the top-level `<DateTimePicker>` prop. Add `'data-testid': '{{ getXMIID child }}'` inside `slotProps.textField.inputProps` so it lands on the actual `<input>` element.

**Alternative considered:** Using `slotProps.textField['data-testid']` — this would put it on the `<div class="MuiTextField-root">` wrapper, but `inputProps` is more precise and guaranteed to reach a visible DOM element.

### Decision 2: ButtonGroup — replace `id` with `data-testid`, drop suffix

The `DropdownButton` component currently renders `id="{{ getXMIID child }}-button-group"`. The VisualElementIds declares the ID without the `-button-group` suffix.

**Approach:** Change `id=` to `data-testid=` and remove the `-button-group` suffix. Keep the `id` prop as well with the suffix (it may be used for accessibility/anchoring), but add a separate `data-testid` with the clean XMI ID.

### Decision 3: Table columns — add `renderHeader` with `data-testid`

MUI DataGrid `GridColDef` has no built-in `data-testid` support on column headers. The `headerClassName` prop only sets CSS classes.

**Approach:** Add a `renderHeader` function to each `GridColDef` that wraps the header label in a `<span data-testid="...">` element. This is non-invasive — it only adds a wrapper inside the existing header cell.

The `data-testid` value follows the existing VisualElementIds pattern: `{{ getXMIID column }}/TableColumn/(discriminator/{{ getXMIID table }}/{{ tableTypeSuffix table }})`. This requires the column's XMI ID and the table's discriminator ID, both available in the template context.

### Decision 4: Row View button — remove the `isOpenPageAction` exclusion

Line 244 of `table/index.tsx.hbs` has `{{# unless button.actionDefinition.isOpenPageAction }}` which skips the View button. The View action is instead wired as `onRowClick` (whole-row click). However, the UI model explicitly defines `TransferObjectTableRowViewButton` and `TabularReferenceTableRowViewButton` as row action buttons.

**Approach:** Remove the `unless` guard so the View button renders in `rowActions[]` like Delete and Remove. The `onRowClick` handler can remain as a convenience — both can coexist.

## Risks / Trade-offs

- **renderHeader adds DOM nesting** — Each column header gains a `<span>` wrapper. This is cosmetic-only and shouldn't affect DataGrid layout or functionality. → Mitigated by using an inline `<span>` which doesn't change block flow.
- **Snapshot churn** — All itests with tables, DateTimePickers, DropdownButtons, or row actions will need snapshot updates. → Expected and unavoidable; the diff-checker plugin will flag them.
- **View button alongside row click** — Users now see both a View button and can click the row. This is intentional (consistent with the model) but could feel redundant. → The model defines the button; removing it is a model-level decision, not a generator one.
