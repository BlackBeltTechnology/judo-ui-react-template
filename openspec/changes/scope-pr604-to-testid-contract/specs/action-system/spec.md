## ADDED Requirements

### Requirement: Button roles match the runtime contract

A generated button's test id role SHALL match the role `@judo/test-ids` assigns for the same action. `openaddselector` and `opensetselector` SHALL both map to `set`. An operation-launching action SHALL take its role from the modeled operation name, sanitized into one grammar segment, so several operations on one row or field are each addressable.

#### Scenario: Add selector on a collection field
- **WHEN** a collection relation offers an add selector
- **THEN** its button carries `field::<id>::button::set` on both engines

#### Scenario: Several row operations
- **GIVEN** a row with modeled operations `createDarkMatter` and `createIntergalacticDust`
- **THEN** the row carries `::action::createDarkMatter` and `::action::createIntergalacticDust`, each matching exactly one element

### Requirement: One confirmation identity

Every generated confirmation dialog SHALL carry the shared `confirmation` identity for the dialog and its confirm and cancel buttons, whichever action opened it. The conditions for showing the dialog SHALL stay as specified in "Confirmation Dialog".

#### Scenario: Confirming a delete
- **WHEN** a delete asks for confirmation
- **THEN** a spec confirms it through the same ids it uses for any other confirmation
