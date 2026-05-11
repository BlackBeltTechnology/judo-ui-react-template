## MODIFIED Requirements

### Requirement: Date/Time/DateTime Input

The generator SHALL produce MUI date/time picker components from date/time input elements: `DateInput` SHALL map to `<DatePicker>`, `TimeInput` SHALL map to `<TimePicker>`, `DateTimeInput` SHALL map to `<DateTimePicker>`.

`baseUnit` SHALL affect time precision display. `minValueBy` and `maxValueBy` SHALL bind to runtime attribute values for min/max date constraints.

Each date/time picker SHALL render a `data-testid` attribute on a visible DOM element matching the element's XMI ID. For `<DateTimePicker>` and `<DatePicker>`, `data-testid` SHALL be set via `slotProps.textField.inputProps['data-testid']` so it reaches the actual `<input>` DOM element. The `data-testid` SHALL NOT be set as a top-level component prop, because MUI does not forward unknown props to the DOM.

#### Scenario: Date input with constraints

- **WHEN** a `DateInput` element has `minValueBy` and `maxValueBy` set
- **THEN** the date picker enforces the bound min/max date range

#### Scenario: Time input with base unit

- **WHEN** a `TimeInput` element has `baseUnit = SECOND`
- **THEN** the time picker displays seconds precision

#### Scenario: DateTimePicker data-testid reaches the DOM

- **WHEN** a `DateTimeInput` element is rendered
- **THEN** the generated `<DateTimePicker>` places `data-testid` inside `slotProps.textField.inputProps`
- **AND** exactly one DOM element has `data-testid` matching the element's XMI ID

**Key Helpers**: `UiPageContainerHelper.containerHasDateInput()`, `UiPageContainerHelper.containerHasTimeInput()`, `UiPageContainerHelper.containerHasDateTimeInput()`, `UiGeneralHelper.getWritableDateAttributesForClass()`

**Templates**: `actor/src/containers/widget-fragments/dateinput.hbs`, `timeinput.hbs`, `datetimeinput.hbs`
