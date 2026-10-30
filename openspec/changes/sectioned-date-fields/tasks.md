# Tasks

## 1. Align the split plan

- [x] 1.1 Update `openspec/changes/scope-pr604-to-testid-contract`: the behaviour half of `f4c258c0` (the `enableAccessibleFieldDOMStructure={false}` lines) is dropped, not moved. `feature/react-followups` carries `sectioned-date-fields` instead. Verify that `openspec validate scope-pr604-to-testid-contract` and `openspec validate sectioned-date-fields --strict` pass.

## 2. Widget fragments use the sectioned field (spec: input-widgets)

- [x] 2.1 In `dateinput.hbs`, `datetimeinput.hbs` and `timeinput.hbs`:
  - remove `enableAccessibleFieldDOMStructure={false}` and its comment;
  - move `inputProps` to `slotProps.textField.slotProps.htmlInput`, `InputProps` to `.input`, and `InputLabelProps` to `.inputLabel`;
  - put `field::<id>` on the text field root (decision 3);
  - replace `inputRef={autoFocusInputRef}` with `autoFocus` (decision 4).
  
  Verify that the updated `FieldTemplateTest` passes (`mvn -pl judo-ui-react test -Dtest=FieldTemplateTest`).
- [x] 2.2 Rename `dateWidgetsUseTheSingleInputStructure` to `dateWidgetsUseTheSectionedStructure` and make it assert zero opt-out flags and zero legacy text-field props. Make `dateWidgetsEmitTheCanonicalNativeInputId` assert the `htmlInput` form. Verify that both tests fail on the old templates and pass on the new ones.

## 3. Filter pickers follow the field grammar (spec: input-widgets)

- [x] 3.1 In `FilterDialog.tsx.hbs` and `SingleValueFilterComponent.tsx.hbs`, remove the opt-out, put the filter-value role on the field root and `::input` on `htmlInput`, and delete the `ponytail` comment. Thread `tableTestId` from `LazyTable`/`EagerTable` through `FixedTableFilters` into `SingleValueFilterComponent` and emit the runtime grammar (`buildFixedFiltersTestId`, `buildFixedFilterTestId`: `table::<id>::fixed-filters`, `table::<id>::fixed-filter::<attributeName>`) on every fixed filter type's root; date-like ones also get `::input`. Verify that `FieldTemplateTest` covers both files and passes.

## 4. Generated application check

- [ ] 4.1 Build the template (`mvn -pl judo-ui-react install`). Generate the Tatami `ActionGroupTest` model with `frontendType=react` from this branch, then run its TypeScript build. Verify that the build exits 0 with no picker prop type errors, and that an autofocus date field takes focus on page open.

## Workflow follow-up

- Open the `feature/react-followups` PR and link runtime PR B. State in the body that it merges together with runtime `sectioned-date-fields` and Tatami `keyboard-temporal-entry`.
- Archive after the dual-engine Tatami run passes (Tatami `keyboard-temporal-entry` group 3).
