## ADDED Requirements

### Requirement: Canonical input test ID addresses the editable element

A generated widget SHALL put `field::<id>::input` on the input element that carries the field's value, not on a wrapper. A checkbox SHALL put the id on its inner `input[type=checkbox]`. The id SHALL be the same one the runtime emits for the same model element. How date-like widgets are structured is not changed by this requirement.

#### Scenario: Date id on the value input
- **GIVEN** a generated date field
- **WHEN** the page renders
- **THEN** `field::<id>::input` is an `input` element carrying the field's value

#### Scenario: Checking a checkbox
- **GIVEN** a generated checkbox
- **WHEN** a spec checks `field::<id>::input`
- **THEN** the value becomes true

#### Scenario: Same id on both engines
- **WHEN** the same model element is rendered by the generated app and by the runtime
- **THEN** both editable elements carry the same `field::<id>::input`
