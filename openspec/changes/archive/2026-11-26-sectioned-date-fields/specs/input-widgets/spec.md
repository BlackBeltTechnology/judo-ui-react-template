## MODIFIED Requirements

### Requirement: Date/Time/DateTime Input

The generator SHALL produce MUI date/time picker components from date/time input elements: `DateInput` SHALL map to `<DatePicker>`, `TimeInput` SHALL map to `<TimePicker>`, `DateTimeInput` SHALL map to `<DateTimePicker>`.

`baseUnit` SHALL affect time precision display. `minValueBy` and `maxValueBy` SHALL bind to runtime attribute values for min/max date constraints.

Generated pickers, including table filter pickers, SHALL use the picker library's default sectioned field, with `field::<id>` on the field root and `field::<id>::input` on the input holding the formatted value. Entry SHALL work by clicking the root, selecting all and typing the digits. The format SHALL follow the model's default language, on a 24-hour clock.

#### Scenario: Date input with constraints

- **WHEN** a `DateInput` element has `minValueBy` and `maxValueBy` set
- **THEN** the date picker enforces the bound min/max date range

#### Scenario: Time input with base unit

- **WHEN** a `TimeInput` element has `baseUnit = SECOND`
- **THEN** the time picker displays seconds precision

#### Scenario: Sectioned structure with root and value ids

- **WHEN** a generated date field renders
- **THEN** `field::<id>` is the field root containing separate date sections, and `field::<id>::input` is the input whose value is the formatted date

#### Scenario: Keyboard entry

- **WHEN** a spec clicks `field::<id>` of an empty datetime field in an `en-US` model, selects all, types `11052024121212` and moves focus away
- **THEN** the field holds 5 November 2024 12:12:12 and `field::<id>::input` shows `11/05/2024 12:12:12`

#### Scenario: Filter picker follows the field grammar

- **WHEN** a table date filter renders
- **THEN** its filter-value test id is on the picker's field root and `::input` is on its value input

#### Scenario: Fixed filter follows the runtime grammar

- **WHEN** a table renders its always-shown filters
- **THEN** the bar carries `table::<id>::fixed-filters`, each filter's field root carries `table::<id>::fixed-filter::<attributeName>`, and a date-like fixed filter carries `::input` on its value input

#### Scenario: Same structure as the runtime

- **WHEN** the same model element renders on the React template and on the runtime
- **THEN** both expose the same root and value ids, and the same keystrokes produce the same value

**Key Helpers**: `UiPageContainerHelper.containerHasDateInput()`, `UiPageContainerHelper.containerHasTimeInput()`, `UiPageContainerHelper.containerHasDateTimeInput()`, `UiGeneralHelper.getWritableDateAttributesForClass()`

**Templates**: `actor/src/containers/widget-fragments/dateinput.hbs`, `timeinput.hbs`, `datetimeinput.hbs`
