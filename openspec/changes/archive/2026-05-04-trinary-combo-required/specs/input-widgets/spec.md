## MODIFIED Requirements

### Requirement: Trinary Logic Combo

The generator SHALL produce a custom `<TrinaryLogicCombobox>` component from `TrinaryLogicCombo` elements bound to a `BooleanType` attribute.

The component SHALL render as a dropdown whose option set is keyed on the `required` prop:

- When `required = false` (the default — typical for nullable boolean attributes and for filter contexts), the combobox SHALL render three options: `Yes` (true), `No` (false), and `Unknown` (null/undefined).
- When `required = true` (typical for `BooleanType` attributes with `isRequired = true`, or for fields whose dynamic `isXxxRequired` action returns `true`, or whose `requiredBy` reference resolves true), the combobox SHALL render only two options: `Yes` (true) and `No` (false). The `Unknown` option SHALL NOT be rendered.

When `required = true` and the underlying value is `null` or `undefined`, the underlying MUI `<Select>` `value` SHALL be the empty string so MUI does not emit an out-of-range warning. The visible field starts empty and relies on the existing required-field validation to surface a missing pick at submit time.

The `required` prop SHALL be reactive: if a runtime action causes the field to flip between required and optional, the option set SHALL update accordingly.

The form input fragment SHALL pass `required` derived from the model: `actions.isXxxRequired(...)` if defined, else `(data.requiredByName || attributeType.isRequired)`. Filter contexts (`FilterDialog.tsx`, `SingleValueFilterComponent.tsx`) SHALL NOT pass `required`, so the prop defaults to `false` and the 3-option behavior is preserved for "no filter on this attribute" semantics.

#### Scenario: Required boolean trinary combo

- **WHEN** a `TrinaryLogicCombo` element is bound to a `BooleanType` attribute with `isRequired = true`
- **THEN** the generated combobox displays exactly two options: `Yes` and `No`
- **AND** no `Unknown`/empty option is rendered
- **AND** the underlying `<Select>` resolves to the empty string when the attribute value is `null` or `undefined`

#### Scenario: Optional boolean trinary combo

- **WHEN** a `TrinaryLogicCombo` element is bound to a `BooleanType` attribute with `isRequired = false` (or unset)
- **THEN** the generated combobox displays three options: `Yes`, `No`, and `Unknown`

#### Scenario: Filter context trinary combo

- **WHEN** the trinary combo is rendered inside a filter dialog or column filter (no `required` prop passed)
- **THEN** the combobox displays three options regardless of any backing attribute's `isRequired` flag, so users can express "no filter on this attribute"

#### Scenario: Reactive required flip

- **WHEN** a `TrinaryLogicCombo`'s effective `required` value changes at runtime (e.g. a dynamic `isXxxRequired` action result depends on other field values)
- **THEN** the option set updates: `Unknown` appears when `required` becomes `false` and disappears when `required` becomes `true`

**Key Helpers**: `UiPageContainerHelper.containerHasTrinaryLogicCombo()`

**Templates**:
- `actor/src/components/widgets/TrinaryLogicCombobox.tsx.hbs` — the component itself
- `actor/src/containers/widget-fragments/trinarylogiccombo.hbs` — the form-input call site (passes `required` from the model)
