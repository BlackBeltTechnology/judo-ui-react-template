## MODIFIED Requirements

### Requirement: Trinary Logic Combo

The generator SHALL produce a custom `<TrinaryLogicCombobox>` component from `TrinaryLogicCombo` elements bound to a `BooleanType` attribute.

The component SHALL render as a dropdown whose option set is keyed on the `required` prop:

- When `required = false` (the default — typical for nullable boolean attributes and for filter contexts), the combobox SHALL render three options: `Yes` (true), `No` (false), and `Unknown` (null/undefined).
- When `required = true` (typical for `BooleanType` attributes with `isRequired = true`, or for fields whose dynamic `isXxxRequired` action returns `true`, or whose `requiredBy` reference resolves true), the combobox SHALL render only two selectable options: `Yes` (true) and `No` (false). The `Unknown` option SHALL NOT be selectable.

When `required = true` and the underlying value is `null` or `undefined`, the underlying MUI `<Select>` `value` SHALL be the empty string so MUI does not emit an out-of-range warning. The visible field starts empty and relies on form-level submit-guard validation to surface a missing pick at submit time.

The `data-testid={`${id}-undefined`}` testid SHALL be emitted for every rendered `TrinaryLogicCombobox`, regardless of the `required` prop value. When `required = true` the testid SHALL be attached to a non-selectable, visually hidden anchor `<MenuItem>` (e.g. `sx={ { display: 'none' } }`, `aria-hidden="true"`, `disabled`) so existing test consumers (per JNG-6381 standardized test data IDs) keep working but end users cannot pick it.

The `required` prop SHALL be reactive: if a runtime action causes the field to flip between required and optional, the option set SHALL update accordingly.

The form input fragment SHALL pass `required` derived from the model: `actions.isXxxRequired(...)` if defined, else `(data.requiredByName || attributeType.isRequired)`. Filter contexts (`FilterDialog.tsx`, `SingleValueFilterComponent.tsx`) SHALL NOT pass `required`, so the prop defaults to `false` and the 3-option behavior is preserved for "no filter on this attribute" semantics.

The form action generator SHALL produce a client-side submit guard for statically-required `TrinaryLogicCombo` widgets. Specifically:

- `UiPageHelper.getRequiredByWidgetsForPage()` SHALL collect any `TrinaryLogicCombo` whose bound `BooleanType` attribute has `isRequired = true`, in addition to all visual elements with a non-null `requiredBy` reference. The combined list SHALL remain ordered by `FQName`.
- `hasPageRequiredBy()` SHALL therefore return `true` whenever the page contains either a `requiredBy`-driven widget or a statically-required `TrinaryLogicCombo`. This causes `passesLocalValidation` to be imported into the page/dialog action file and invoked at the top of the generated `Create*Action`/`Update*Action`/`InputForm*` actions.
- For each statically-required `TrinaryLogicCombo` collected this way, the `local-validate.fragment.hbs` SHALL emit `requiredByRecord[<attributeName>] = true` (a literal `true`, since there is no `requiredBy` flag to read at runtime). For widgets that have a `requiredBy` reference, the existing `!!data.<requiredByName>` entry SHALL be emitted unchanged.
- When `passesLocalValidation` reports a failed required check (because `data[attr]` is `null`, `undefined`, or `''`), the form action SHALL `return` without invoking the backend service, and the existing validation map SHALL be populated with the standard `judo.error.validation-failed.MISSING_REQUIRED_ATTRIBUTE` message so the trinary combo renders its `error`/`helperText` props from `validation.get(attr)`.

Filter contexts SHALL NOT participate in submit-guard generation: `getRequiredByWidgetsForPage()` walks `pageDefinition.getContainer()`, which never includes a filter dialog body or a column filter, so filter-bound trinary combos cannot enter `requiredByRecord`.

#### Scenario: Required boolean trinary combo

- **WHEN** a `TrinaryLogicCombo` element is bound to a `BooleanType` attribute with `isRequired = true`
- **THEN** the generated combobox displays exactly two selectable options: `Yes` and `No`
- **AND** a non-selectable, visually hidden `<MenuItem>` carrying `data-testid={`${id}-undefined`}` is still present in the DOM
- **AND** the underlying `<Select>` resolves to the empty string when the attribute value is `null` or `undefined`

#### Scenario: Optional boolean trinary combo

- **WHEN** a `TrinaryLogicCombo` element is bound to a `BooleanType` attribute with `isRequired = false` (or unset)
- **THEN** the generated combobox displays three selectable options: `Yes`, `No`, and `Unknown`
- **AND** the `Unknown` option carries `data-testid={`${id}-undefined`}` and is selectable

#### Scenario: Filter context trinary combo

- **WHEN** the trinary combo is rendered inside a filter dialog or column filter (no `required` prop passed)
- **THEN** the combobox displays three selectable options regardless of any backing attribute's `isRequired` flag
- **AND** the form action for the page hosting the filter dialog SHALL NOT include the filter's bound attribute in `requiredByRecord`

#### Scenario: Reactive required flip

- **WHEN** a `TrinaryLogicCombo`'s effective `required` value changes at runtime (e.g. a dynamic `isXxxRequired` action result depends on other field values)
- **THEN** the option set updates: the `Unknown` option becomes selectable when `required` becomes `false` and becomes hidden/disabled when `required` becomes `true`

#### Scenario: Submit blocked when required boolean is empty

- **WHEN** a form contains a `TrinaryLogicCombo` bound to a `BooleanType` attribute with `isRequired = true`
- **AND** the user clicks the form's `Save`/`Create` action without picking `Yes` or `No`
- **THEN** the generated form action invokes `passesLocalValidation` which returns `false` for that attribute
- **AND** the form action returns early without calling the backend service
- **AND** the validation map gains a `MISSING_REQUIRED_ATTRIBUTE` entry for that attribute, surfacing as `error` and `helperText` on the trinary combo

#### Scenario: Submit succeeds when required boolean is filled

- **WHEN** a form contains a `TrinaryLogicCombo` bound to a `BooleanType` attribute with `isRequired = true`
- **AND** the user picks either `Yes` or `No`
- **AND** the user clicks the form's `Save`/`Create` action
- **THEN** `passesLocalValidation` reports success for that attribute
- **AND** the form action proceeds to call the backend service with the populated payload

#### Scenario: Submit-guard not generated for purely optional pages

- **WHEN** a form contains only optional `TrinaryLogicCombo` widgets (no `requiredBy`, `isRequired = false`) and no other `requiredBy`-driven widgets
- **THEN** `hasPageRequiredBy()` returns `false`
- **AND** `passesLocalValidation` is NOT imported into the generated action file
- **AND** the form action submits to the backend regardless of the trinary combo value, including `null` / `undefined`
