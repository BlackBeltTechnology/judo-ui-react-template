# Design

## Context

See proposal.md, Why. Current state on `feature/JNG-6411_Unify_data_testId_contract` (MUI 7.3.6, `@mui/x-date-pickers` 8.23.0):

- **The opt-out.** Five templates set `enableAccessibleFieldDOMStructure={false}`: three widget fragments and two filter components.
- **Test ids.** The widget fragments put `data-testid={buildFieldTestId(id)}` on the picker element. They put `::input` through `slotProps.textField.inputProps`, together with `InputProps` (icon adornment) and `InputLabelProps`. These are legacy props: v8 still accepts them, v9 removes them.
- **Autofocus.** `inputRef={autoFocusInputRef}` is a picker prop. With the sectioned field it points at the hidden value input, and `container.tsx.hbs:82` calls `.focus()` on it.
- **Locale.** `l10n-context.tsx.hbs` renders `LocalizationProvider adapterLocale={locales[locale]}` with the model's `defaultLanguage`. Every Tatami model uses `en-US`.
- **Guards.** `FieldTemplateTest` pins the exact `inputProps` string and the count of the opt-out flag.

Probe (MUI X 8.23 and 9.11, identical results): keyboard entry through the root works, including overwrite and clear. `fill()` on `::input` does nothing.

## Goals / Non-Goals

**Goals:** produce the same DOM, the same test ids and the same keyboard behaviour as the runtime on MUI X v9, while the template stays on v8.

**Non-Goals:**
- Upgrading the template to MUI X v9.
- DataGrid inline date editing.
- Changes to the date format logic or serialization.

## Decisions

1. **Remove the opt-out and add no replacement.** This mirrors the runtime decision. The alternative, a custom single-input `textField` slot, would have to exist in both engines and would re-implement parsing and validation.
2. **Use the v9-shaped slot props now.** `slotProps.textField.slotProps.htmlInput` carries `::input`, `slotProps.textField.slotProps.input` carries the adornment, and `slotProps.textField.slotProps.inputLabel` carries `shrink`. v8.23 already supports the nested form. The ids then sit where the runtime has them, and the v9 upgrade needs no template change.
3. **`field::<id>` on the text field root** (`slotProps.textField['data-testid']`). The runtime puts it on the `PickersTextField` root (`role="group"`), and the click target for entry must be the same element on both engines. Before implementing, check that the current picker-level `data-testid` actually reaches that root on v8. If it does, keep the template untouched there and only add a test.
4. **Autofocus moves to the field.** `inputRef` is dropped from the picker. Autofocus uses the picker's `autoFocus` prop, which focuses the first section. The alternative, keeping the ref on the hidden input, focuses an `aria-hidden` element, which makes no sense for a user.
5. **Filter pickers.** They get the filter-value role on the root and `::input` on `htmlInput`, through the same slot props. This replaces the `ponytail` comment in `FilterDialog.tsx.hbs`.
6. **Guard tests.** `dateWidgetsUseTheSingleInputStructure` becomes `dateWidgetsUseTheSectionedStructure`: zero occurrences of `enableAccessibleFieldDOMStructure`, and zero legacy `inputProps`/`InputProps`/`InputLabelProps` in the five templates. `dateWidgetsEmitTheCanonicalNativeInputId` asserts the `htmlInput` form.

## Risks / Trade-offs

- [v8 and v9 section DOM differ] → Tatami specs use only the root click, select-all and digits. The probe showed these behave the same on both versions.
- [`autoFocus` focuses a section, where the old ref focused an input] → Check it on a generated page with an autofocus date field. If v8 ignores `autoFocus` on the field, keep the ref but forward it through the `slotProps.field` `unstableFieldRef` instead.
- [`scope-pr604-to-testid-contract` still plans to move `f4c258c0`'s behaviour half] → Task 1.1 updates that plan, so the two changes do not contradict each other.

## Migration Plan

Lands on `feature/react-followups`, the template counterpart of runtime PR B. Merges together with runtime `sectioned-date-fields` and Tatami `keyboard-temporal-entry`. Rollback: revert the branch. Both engines must then roll back, otherwise their date DOM diverges again.
