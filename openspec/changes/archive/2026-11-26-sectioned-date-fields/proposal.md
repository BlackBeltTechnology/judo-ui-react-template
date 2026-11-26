# Proposal

## Why

Commit `f4c258c0` (#604) sets `enableAccessibleFieldDOMStructure={false}` on every date-like picker so that Tatami can `fill()` `field::<id>::input`. The runtime cannot do the same: MUI X v9 removed the option, so its date fields render only the sectioned structure (runtime change `sectioned-date-fields`). A Tatami spec must behave the same on both engines, so the template has to render the same structure. The template meets the same wall anyway when it upgrades to MUI X v9.

A probe on MUI X 8.23 and 9.11 (2026-10-19) showed identical keyboard behaviour on the sectioned field: click `field::<id>`, select all, type the digits. It also showed that `fill()` on the hidden `::input` does not set a value on either version.

## What Changes

- **BREAKING** (for specs that `fill()` a date): drop `enableAccessibleFieldDOMStructure={false}` from `dateinput.hbs`, `datetimeinput.hbs`, `timeinput.hbs`, `FilterDialog.tsx.hbs` and `SingleValueFilterComponent.tsx.hbs`. Date-like widgets keep MUI X's default sectioned field.
- Move the legacy picker text-field props (`inputProps`, `InputProps`, `InputLabelProps`) to `slotProps.textField.slotProps.{htmlInput,input,inputLabel}`. `field::<id>::input` then lands on the value input on v8 and on a future v9 alike, matching the runtime's DOM.
- Put `field::<id>` on the text field root, where the runtime has it. Today it is a picker prop.
- Date filter pickers get the filter-value role on their field root and `::input`. This closes the `ponytail` left in `FilterDialog.tsx.hbs`.
- Fixed (always-shown) table filters get the runtime's ids, which they lack today: `table::<id>::fixed-filters` on the bar and `table::<id>::fixed-filter::<attributeName>` on each filter's field root (`::input` on date-like value inputs). `tableTestId` is threaded into `FixedTableFilters`.
- Autofocus of a date-like field targets the field, not the hidden value input.
- `FieldTemplateTest` asserts the new structure in place of the single-input structure.
- The locale already follows the model's `defaultLanguage` (`l10n-context.tsx.hbs`), and the runtime adopts the same source. Nothing changes here beyond a guard test.

## Capabilities

### New Capabilities

<!-- None. -->

### Modified Capabilities

- `input-widgets`: the "Date/Time/DateTime Input" requirement gains the sectioned structure, the root/value test ids, keyboard entry and the locale-driven format.

## Impact

- Templates: `actor/src/containers/widget-fragments/{date,datetime,time}input.hbs`, `actor/src/components/dialog/FilterDialog.tsx.hbs`, `actor/src/components/table/SingleValueFilterComponent.tsx.hbs`, `FixedTableFilters.tsx.hbs`, `LazyTable.tsx.hbs`, `EagerTable.tsx.hbs`, `widgets/TrinaryLogicCombobox.tsx.hbs` (+ `TrinaryLogicProps.testId`), `utilities/transfer-id.ts.hbs` (+ its test).
- Tests: `judo-ui-react/src/test/java/hu/blackbelt/judo/ui/generator/react/FieldTemplateTest.java` (`dateWidgetsEmitTheCanonicalNativeInputId`, `dateWidgetsUseTheSingleInputStructure`).
- Plan: `scope-pr604-to-testid-contract` moves the `f4c258c0` behaviour half to `feature/react-followups`. This change replaces that half instead of moving it. Its `input-widgets` delta says the date-like structure stays as specified, which is now this change's requirement.
- Counterparts: runtime `sectioned-date-fields` and Tatami `keyboard-temporal-entry`. They land together.
- Test coverage: the Java template tests change as listed above. Behaviour is covered by the Tatami dual-engine run (Tatami `keyboard-temporal-entry` group 3) on a generated model.
