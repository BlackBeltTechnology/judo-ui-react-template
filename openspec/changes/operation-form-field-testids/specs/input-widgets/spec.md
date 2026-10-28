## ADDED Requirements

### Requirement: Enumeration combo carries the canonical input role

An `EnumerationCombo` SHALL mark its root `field::<id>` and its native `<input>` `field::<id>::input`, both built with `buildFieldTestId`. The input role SHALL be merged into the `TextField`'s `inputProps` after Autocomplete's own `params.inputProps`. This mirrors the runtime's `EnumerationComboComponent`.

#### Scenario: Enumeration combo on a view

- **WHEN** a view renders an `EnumerationCombo` `type` with source id `_S0SVYHtTEeusor6jyLynzw`
- **THEN** its `<input>` carries `data-testid="field::_S0SVYHtTEeusor6jyLynzw::input"` and holds the selected option's label
