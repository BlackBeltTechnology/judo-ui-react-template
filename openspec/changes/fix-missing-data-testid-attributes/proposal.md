## Why

E2E tests in `judo-tatami-tests` validate that all UI model elements render with `data-testid` attributes matching their declared VisualElementIds. Four categories of elements fail these tests because the React code generator either omits `data-testid`, places it where MUI doesn't forward it to the DOM, or intentionally skips rendering the element. These are code generator bugs — the UI model defines the elements, VisualElementIds declares them, and the E2E specs require them.

## What Changes

- **DateTimePicker**: Move `data-testid` from the `<DateTimePicker>` component prop (which MUI ignores) into `slotProps.textField` so it reaches the actual DOM element.
- **ButtonGroup (DropdownButton)**: Change `id=` to `data-testid=` and remove the `-button-group` suffix so the rendered attribute matches the VisualElementIds declaration.
- **Table column headers**: Add `data-testid` to `GridColDef` column definitions via `renderHeader` so column headers are testable.
- **Row View button**: Remove the `{{# unless button.actionDefinition.isOpenPageAction }}` guard in the table row actions template so the View button is rendered alongside Delete/Remove buttons.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `input-widgets`: DateTimePicker's `data-testid` placement changes from a component prop to `slotProps.textField.inputProps['data-testid']`.
- `data-tables`: Table columns gain `data-testid` on headers; row actions now include the View button (OpenPageAction) instead of skipping it.
- `action-system`: ButtonGroup/DropdownButton changes from `id` to `data-testid` and drops the `-button-group` suffix.

## Impact

- **Templates affected**:
  - `actor/src/containers/widget-fragments/datetimeinput.hbs` (Bug #1)
  - `actor/src/containers/widget-fragments/buttongroup.hbs` (Bug #3)
  - `actor/src/containers/components/table/index.tsx.hbs` (Bugs #5, #6)
- **Snapshot updates**: All itest snapshots containing DateTimePicker, DropdownButton, table columns, or table row actions will change.
- **No breaking changes**: These are additive fixes — existing `data-testid` attributes on other elements are unchanged.
- **Cross-model scope**: Fixes apply to all generated React apps, not just ActionGroupTest.
