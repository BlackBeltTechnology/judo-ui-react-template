## ADDED Requirements

### Requirement: Enum filter value carries canonical test ids

The enumeration value editor of a filter dialog row SHALL carry `table::<id>::filter-panel::row::<i>::value` on its clickable Select display (`role="combobox"`). Each option SHALL carry `::value::option::<member>`, where `<member>` is the enum member sent to the server. String and numeric editors SHALL keep `::value` on their input. The ids SHALL NOT change behaviour or the operator list.

#### Scenario: Opening the enum value menu

- **WHEN** row 0 of the filter dialog of table `_T1` filters an enumeration attribute
- **THEN** the element with `role="combobox"` carries `data-testid="table::_T1::filter-panel::row::0::value"`
- **AND** clicking it opens the option menu

#### Scenario: Picking an enum member

- **WHEN** the value menu of row 1 is open and the enum has the member `dark` labelled "Dark matter"
- **THEN** the option carries `data-testid="table::_T1::filter-panel::row::1::value::option::dark"`

#### Scenario: Text value input

- **WHEN** row 0 filters a string attribute
- **THEN** the text `input` element carries `data-testid="table::_T1::filter-panel::row::0::value"`
