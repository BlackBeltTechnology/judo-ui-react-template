# Proposal

## Why

The shared Tatami `TableHelper.applyFilters` picks an enum filter value by clicking `table::<id>::filter-panel::row::<i>::value` and then `::value::option::<member>`. In the React filter dialog the value id sits on the hidden native input of the enum Select (`aria-hidden`), and the menu options have no id. The spec cannot open the menu or pick an option on React. The runtime emits both ids.

## What Changes

- New helper `buildFilterPanelValueOptionTestId(tableId, index, member)` in `utilities/transfer-id.ts`. It returns `table::<id>::filter-panel::row::<i>::value::option::<member>`, where `<member>` is the enum member sent to the server.
- `components/dialog/FilterDialog.tsx`: the enumeration value editor puts `::value` on the Select display element (`role="combobox"`) through `slotProps.select.SelectDisplayProps`, the same approach as the pager page-size select. Each `MenuItem` carries the option id.
- String and numeric editors keep `::value` on their input. Date editors keep it on the field root.
- The operator list does not change. Enums still offer `equals` / `notEquals`.
- No visual or behavioural change.

## Capabilities

### Modified Capabilities

- `data-tables`: the enum filter value editor carries canonical test ids.

## Impact

- Templates: `components/dialog/FilterDialog.tsx.hbs`, `utilities/transfer-id.ts.hbs`. Test: `FilterTemplateTest`.
- Only the filter dialog is affected. The inline MUI X filter panel (Pro license with `useInlineColumnFilters`) is not used by the shared suite and is unchanged.
