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

The generator SHALL produce a custom `<TrinaryLogicCombobox>` component from `TrinaryLogicCombo` elements bound to a nullable `BooleanType` attribute.

The component SHALL render as a dropdown with three states: true, false, and null (unknown).

#### Scenario: Trinary logic input

- **WHEN** a `TrinaryLogicCombo` element exists bound to a nullable boolean
- **THEN** the generated combobox displays Yes/No/Unknown options

**Key Helpers**: `UiPageContainerHelper.containerHasTrinaryLogicCombo()`

**Template**: `actor/src/containers/widget-fragments/trinarylogiccombo.hbs`

---

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
