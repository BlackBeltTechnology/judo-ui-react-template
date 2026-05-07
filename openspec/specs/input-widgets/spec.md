# Input Widgets

## Purpose

Generates React form input components from UI input model elements. Each input type in the metamodel maps to a specific MUI-based React widget. Input widgets are data-type-aware: the generator selects the correct widget based on the bound attribute's data type.
## Requirements
### Requirement: Text Input

The generator SHALL produce a MUI `<TextField>` or `<DebouncedTextField>` component from `TextInput` elements bound to a `StringType` attribute.

`maxLength` SHALL set the HTML `maxlength` attribute. `mask` SHALL generate input masking logic. `regexp` SHALL add client-side validation pattern. The debounced variant SHALL prevent excessive state updates during typing.

#### Scenario: Text input with mask

- **WHEN** a `TextInput` element has `mask` set
- **THEN** the generated text field applies input masking logic

#### Scenario: Text input with max length

- **WHEN** a `TextInput` element has `maxLength` set
- **THEN** the generated text field enforces the character limit

**Key Helpers**: `UiWidgetHelper.getWidgetTemplate()`, `UiPageContainerHelper.containerHasDebouncedTextField()`

**Template**: `actor/src/containers/widget-fragments/textinput.hbs`

---

### Requirement: Text Area

The generator SHALL produce a multi-line MUI `<TextField>` with `multiline` prop from `TextArea` elements bound to a `StringType` attribute.

`lines` SHALL set `minRows`. When `countCharacters` is `true`, a `<CharacterCounter>` component SHALL be rendered below the field.

#### Scenario: Text area with character counter

- **WHEN** a `TextArea` element has `countCharacters = true`
- **THEN** a character counter component is displayed below the text area

**Key Helpers**: `UiPageContainerHelper.containerHasTextAreaWithCountCharacters()`

**Template**: `actor/src/containers/widget-fragments/textarea.hbs`

---

### Requirement: Password Input

The generator SHALL produce a MUI `<TextField>` with `type="password"` from `PasswordInput` elements bound to a `PasswordType` attribute.

The input SHALL be masked by default.

#### Scenario: Password field

- **WHEN** a `PasswordInput` element exists
- **THEN** the generated text field masks user input

**Template**: `actor/src/containers/widget-fragments/passwordinput.hbs`

---

### Requirement: Numeric Input

The generator SHALL produce a `<DebouncedNumericInput>` component from `NumericInput` elements bound to a `NumericType` attribute.

`precision` and `scale` SHALL control number formatting. When `formatValue` is `true`, thousand separators SHALL be applied. `minValueBy` and `maxValueBy` SHALL bind min/max constraints to other attribute values at runtime.

#### Scenario: Numeric input with formatting

- **WHEN** a `NumericInput` element has `formatValue = true`
- **THEN** the generated numeric field displays values with thousand separators

#### Scenario: Numeric input with value constraints

- **WHEN** a `NumericInput` element has `minValueBy` or `maxValueBy` set
- **THEN** the min/max constraints are dynamically bound to the referenced attribute values

**Key Helpers**: `UiPageContainerHelper.containerHasNumericInput()`, `UiWidgetHelper.hasMinOrMaxConstraintValue()`

**Template**: `actor/src/containers/widget-fragments/numericinput.hbs`

---

### Requirement: Checkbox

The generator SHALL produce a MUI `<Checkbox>` wrapped in `<FormControlLabel>` from `Checkbox` elements bound to a non-nullable `BooleanType` attribute.

`valueLabelPlacement` SHALL map to MUI's `labelPlacement` prop (TOP, BOTTOM, END, START, DEFAULT).

#### Scenario: Checkbox with label placement

- **WHEN** a `Checkbox` element has `valueLabelPlacement = END`
- **THEN** the label is rendered to the end of the checkbox

**Key Helpers**: `UiWidgetHelper.checkboxLabelPlacement()`

**Template**: `actor/src/containers/widget-fragments/checkbox.hbs`

---

### Requirement: Switch

The generator SHALL produce a MUI `<Switch>` wrapped in `<FormControlLabel>` from `Switch` elements bound to a `BooleanType` attribute.

`valueLabelPlacement` SHALL map to MUI's `labelPlacement` prop.

#### Scenario: Switch toggle

- **WHEN** a `Switch` element exists
- **THEN** the generated switch toggles a boolean value

**Template**: `actor/src/containers/widget-fragments/switch.hbs`

---

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

**Key Helpers**: `UiPageContainerHelper.containerHasTrinaryLogicCombo()`, `UiPageHelper.getRequiredByWidgetsForPage()`, `UiPageHelper.hasPageRequiredBy()`

**Templates**:
- `actor/src/components/widgets/TrinaryLogicCombobox.tsx.hbs` — the component itself
- `actor/src/containers/widget-fragments/trinarylogiccombo.hbs` — the form-input call site (passes `required` from the model)
- `actor/src/fragments/page/local-validate.fragment.hbs` — emits `requiredByRecord` entries for statically-required combos

### Requirement: Date/Time/DateTime Input

The generator SHALL produce MUI date/time picker components from date/time input elements: `DateInput` SHALL map to `<DatePicker>`, `TimeInput` SHALL map to `<TimePicker>`, `DateTimeInput` SHALL map to `<DateTimePicker>`.

`baseUnit` SHALL affect time precision display. `minValueBy` and `maxValueBy` SHALL bind to runtime attribute values for min/max date constraints.

#### Scenario: Date input with constraints

- **WHEN** a `DateInput` element has `minValueBy` and `maxValueBy` set
- **THEN** the date picker enforces the bound min/max date range

#### Scenario: Time input with base unit

- **WHEN** a `TimeInput` element has `baseUnit = SECOND`
- **THEN** the time picker displays seconds precision

**Key Helpers**: `UiPageContainerHelper.containerHasDateInput()`, `UiPageContainerHelper.containerHasTimeInput()`, `UiPageContainerHelper.containerHasDateTimeInput()`, `UiGeneralHelper.getWritableDateAttributesForClass()`

**Templates**: `actor/src/containers/widget-fragments/dateinput.hbs`, `timeinput.hbs`, `datetimeinput.hbs`

---

### Requirement: Enumeration Input

The generator SHALL produce enumeration selection components from enumeration input elements bound to an `EnumerationType` attribute.

`EnumerationCombo` SHALL map to a MUI `<Select>` with `<MenuItem>` options. `EnumerationRadio` SHALL map to a MUI `<RadioGroup>` with `<Radio>` options. `EnumerationToggleButtonbar` SHALL map to a MUI `<ToggleButtonGroup>` with `<ToggleButton>` options.

Each `Option.enumerationMember` SHALL map to an enum value. Labels SHALL be internationalized via i18n keys.

#### Scenario: Enumeration combo

- **WHEN** an `EnumerationCombo` element exists with options
- **THEN** a dropdown select is generated with one menu item per enumeration member

#### Scenario: Enumeration radio group

- **WHEN** an `EnumerationRadio` element exists with options
- **THEN** a radio button group is generated with one radio per enumeration member

#### Scenario: Enumeration toggle button bar

- **WHEN** an `EnumerationToggleButtonbar` element exists with options
- **THEN** a toggle button group is generated with one button per enumeration member

**Key Helpers**: `UiPageContainerHelper.containerHasEnums()`, `UiPageContainerHelper.getEnumsForContainer()`, `UiPageContainerHelper.getEnumDataTypesForContainer()`

**Templates**: `actor/src/containers/widget-fragments/enumerationcombo.hbs`, `enumerationradio.hbs`, `enumerationtogglebuttonbar.hbs`

---

### Requirement: Binary/File Input

The generator SHALL produce a `<BinaryInput>` component from `BinaryTypeInput` elements bound to a `BinaryType` attribute.

MIME type restrictions from `BinaryType.mimeTypes` SHALL filter accepted files. `maxFileSize` SHALL be enforced on the client side. Uploaded files SHALL be base64-encoded for API transmission.

#### Scenario: Binary upload with MIME restriction

- **WHEN** a `BinaryTypeInput` element is bound to a `BinaryType` with `mimeTypes` set
- **THEN** the file input restricts accepted file types to the specified MIME types

**Key Helpers**: `UiPageContainerHelper.containerHasBinaryInput()`

**Template**: `actor/src/containers/widget-fragments/binarytypeinput.hbs`

---

### Requirement: Autocomplete/Type-Ahead

The generator SHALL produce a `<TextWithTypeAhead>` component from `TextInput` elements with `isTypeAheadField = true`.

The component SHALL provide autocomplete suggestions as the user types. The suggestion source SHALL come from a range action on the bound relation.

#### Scenario: Text input with type-ahead

- **WHEN** a `TextInput` element has `isTypeAheadField = true`
- **THEN** the generated field shows autocomplete suggestions as the user types

**Key Helpers**: `UiPageContainerHelper.getTextInputsWithTypeAhead()`, `UiPageContainerHelper.containerHasTypeAhead()`

---

### Requirement: Auto-Focus

The first editable, non-hidden input in a form container SHALL receive auto-focus.

#### Scenario: Form auto-focus

- **WHEN** a form container has multiple input elements
- **THEN** the first editable, non-hidden input receives focus on mount

**Key Helpers**: `UiWidgetHelper.shouldElementHaveAutoFocus()`

---

### Requirement: Common Input Properties

All input widgets SHALL support the following common properties from the `Input` base class:

`required` SHALL render a required indicator (asterisk). `tooltipText` SHALL render a help tooltip. `label` SHALL render the field label (via i18n). `icon` SHALL render a field icon. `disabled` SHALL set the disabled state. `isReadOnly` SHALL set the read-only state. `hiddenBy` SHALL control conditional visibility. `enabledBy` SHALL control conditional enabled state. `requiredBy` SHALL control conditional required state.

#### Scenario: Required field with tooltip

- **WHEN** an input has `required = true` and `tooltipText` set
- **THEN** the field displays a required asterisk and a help tooltip

---

## Integration Test Coverage

- **ActionGroupTest**: TextFields, Checkboxes, DateTimePickers, Dropdowns in Galaxy/Matter forms
- **CRUDActionsTest**: Full input variety across create/update forms
- **RelationTest**: Inline edit inputs within relation containers
