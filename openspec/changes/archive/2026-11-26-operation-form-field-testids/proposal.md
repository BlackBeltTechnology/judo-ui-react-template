# Proposal

## Why

Tatami OperationParametersTest fails on React only, in 3 tests:

- Florist "Decorate Garden" (view and row): `field::_S0SVYHtTEeusor6jyLynzw::input` is not found.
- MenuOperations "mapped output": `field::_SzFbgHtTEeusor6jyLynzw::input` is not found.

Evidence is in `/tmp/jng6411-g3w-opparams-react.log`. In the model, both elements are a `ui:DataField` with `enumWidget="COMBO"`, which becomes an `EnumerationCombo` (`FlowerGarden` and `FlowerInfo` views, attribute `type`). `enumerationcombo.hbs` puts `field::<id>` on the MUI `Autocomplete` root, but nothing marks the inner `<input>`. The runtime's `EnumerationComboComponent.tsx` sets `${testId}::input` on the `htmlInput` slot. The gap is the same kind as the earlier checkbox, date and textarea gaps (`FieldTemplateTest`).

## What Changes

- `enumerationcombo.hbs` merges `data-testid = field::<id>::input` into the `TextField`'s `inputProps`, after `params.inputProps`. This keeps Autocomplete's own ref, value and handlers.
- Nothing visual changes.

## Capabilities

### Modified Capabilities

- `input-widgets`: the enumeration combo carries the canonical input role.

## Impact

- Template: `containers/widget-fragments/enumerationcombo.hbs`. Test: `FieldTemplateTest`.
- The itest snapshots of every container that has an enumeration combo change (one added prop).
