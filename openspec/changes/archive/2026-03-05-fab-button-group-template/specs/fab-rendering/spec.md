## ADDED Requirements

### Requirement: FAB button group detection and filtering

The generator SHALL distinguish FAB button groups (`isIsFab() == true`) from standard button groups on `Container.actionButtonGroups`. Helper methods SHALL provide filtered lists so templates can render FAB and non-FAB groups independently.

#### Scenario: Container has mixed FAB and non-FAB button groups

- **WHEN** a `PageContainer` has 3 `actionButtonGroups` where the 1st has `isFab=true` and the 2nd and 3rd do not
- **THEN** `getFabButtonGroupsForContainer()` SHALL return only the 1st group
- **AND** `getNonFabDefaultButtonsForContainer()` SHALL return the buttons of the 2nd group
- **AND** `getNonFabNonDefaultButtonGroupsForContainer()` SHALL return the 3rd group

#### Scenario: Container has only FAB button groups

- **WHEN** all `actionButtonGroups` on a `PageContainer` have `isFab=true`
- **THEN** `getNonFabDefaultButtonsForContainer()` SHALL return an empty list
- **AND** the PageHeader SHALL render no inline action buttons

#### Scenario: Container has no FAB button groups

- **WHEN** no `actionButtonGroups` on a `PageContainer` have `isFab=true`
- **THEN** `getFabButtonGroupsForContainer()` SHALL return an empty list
- **AND** existing button rendering behavior SHALL be unchanged

### Requirement: Single button FAB rendering

When a FAB button group contains exactly one button, the generator SHALL produce a single MUI `<Fab>` component.

#### Scenario: Single button with label

- **WHEN** a FAB button group has 1 button and the button has a label
- **THEN** the generated code SHALL render `<Fab variant="extended">` displaying the icon and label text

#### Scenario: Single button without label

- **WHEN** a FAB button group has 1 button and the button has no label
- **THEN** the generated code SHALL render a circular `<Fab>` with icon only

### Requirement: SpeedDial rendering for non-featured buttons

When a FAB button group has multiple buttons and `featuredActions = 0`, the generator SHALL produce a MUI `<SpeedDial>` wrapping all buttons as `<SpeedDialAction>` items.

#### Scenario: SpeedDial with group label

- **WHEN** a FAB button group has multiple buttons, `featuredActions = 0`, and the ButtonGroup has a label
- **THEN** the SpeedDial toggle SHALL render as `<Fab variant="extended">` with the group label text
- **AND** all buttons SHALL appear as `<SpeedDialAction>` items

#### Scenario: SpeedDial without group label

- **WHEN** a FAB button group has multiple buttons, `featuredActions = 0`, and the ButtonGroup has no label
- **THEN** the SpeedDial toggle SHALL render as a circular `<Fab>` with a `<SpeedDialIcon>`

### Requirement: Hybrid FAB and SpeedDial rendering

When a FAB button group has N buttons and `featuredActions = K` where `0 < K < N`, the generator SHALL render the first K buttons as individual `<Fab>` components and the remaining N-K buttons inside a `<SpeedDial>`.

#### Scenario: Partial featured actions

- **WHEN** a FAB button group has 5 buttons and `featuredActions = 2`
- **THEN** the first 2 buttons SHALL render as individual `<Fab>` components (always visible)
- **AND** a `<SpeedDial>` SHALL contain the remaining 3 buttons as `<SpeedDialAction>` items

#### Scenario: Featured button with label renders extended

- **WHEN** a featured FAB button has a label
- **THEN** it SHALL render as `<Fab variant="extended">` with label and icon

#### Scenario: Featured button without label renders circular

- **WHEN** a featured FAB button has no label
- **THEN** it SHALL render as a circular `<Fab>` with icon only

### Requirement: All buttons featured as individual FABs

When a FAB button group has N buttons and `featuredActions >= N`, the generator SHALL render all buttons as individual `<Fab>` components with no SpeedDial.

#### Scenario: All buttons featured

- **WHEN** a FAB button group has 3 buttons and `featuredActions = 3`
- **THEN** all 3 buttons SHALL render as individual `<Fab>` components
- **AND** no `<SpeedDial>` SHALL be generated

#### Scenario: FeaturedActions exceeds button count

- **WHEN** a FAB button group has 2 buttons and `featuredActions = 5`
- **THEN** all 2 buttons SHALL render as individual `<Fab>` components

### Requirement: FAB alignment positioning

The generator SHALL map the `alignment` attribute to CSS positioning that fixes the FAB group to a viewport corner.

#### Scenario: BOTTOM_RIGHT alignment

- **WHEN** a FAB button group has `alignment = BOTTOM_RIGHT`
- **THEN** the generated CSS SHALL position the FAB at `bottom: 16px; right: 16px` with `position: fixed`

#### Scenario: TOP_LEFT alignment

- **WHEN** a FAB button group has `alignment = TOP_LEFT`
- **THEN** the generated CSS SHALL position the FAB at `top: 16px; left: 16px` with `position: fixed`

#### Scenario: Non-corner alignment fallback

- **WHEN** a FAB button group has a non-corner alignment (e.g. `CENTER`, `TOP_CENTER`)
- **THEN** the generator SHALL fall back to `BOTTOM_RIGHT` positioning

#### Scenario: Default alignment when not specified

- **WHEN** a FAB button group does not specify `alignment`
- **THEN** the Ecore default `BOTTOM_RIGHT` SHALL apply and the FAB SHALL be positioned at bottom-right

### Requirement: FAB action wiring

Each FAB button and SpeedDialAction SHALL be wired to its `actionDefinition` handler, consistent with how standard button groups wire actions.

#### Scenario: FAB button click triggers action

- **WHEN** a user clicks a FAB button rendered from a `Button` with an `actionDefinition`
- **THEN** the `actions.{actionDefinitionName}()` handler SHALL be invoked

#### Scenario: SpeedDialAction click triggers action

- **WHEN** a user clicks a `<SpeedDialAction>` item
- **THEN** the corresponding `actions.{actionDefinitionName}()` handler SHALL be invoked

#### Scenario: FAB button disabled conditions

- **WHEN** a FAB button has disabled conditions (from `containerButtonGroupButtonDisabledConditions`)
- **THEN** the `<Fab>` or `<SpeedDialAction>` SHALL apply the same disabled logic as standard buttons

### Requirement: Conditional MUI imports

The generator SHALL only import MUI FAB-related components (`Fab`, `SpeedDial`, `SpeedDialAction`, `SpeedDialIcon`) when the page container has at least one FAB button group.

#### Scenario: Page with FAB groups

- **WHEN** a `PageContainer` has at least one `actionButtonGroup` with `isFab=true`
- **THEN** the generated page SHALL import `Fab`, `SpeedDial`, `SpeedDialAction`, `SpeedDialIcon` from `@mui/material`

#### Scenario: Page without FAB groups

- **WHEN** no `actionButtonGroups` on a `PageContainer` have `isFab=true`
- **THEN** the generated page SHALL NOT import FAB-related MUI components

### Requirement: Backward compatibility

Setting `isFab` to `false` (or leaving it unset) SHALL have no effect on existing button group rendering behavior.

#### Scenario: Existing models unaffected

- **WHEN** a UI model has no `isFab=true` on any ButtonGroup
- **THEN** all pages and dialogs SHALL render identically to before this change
