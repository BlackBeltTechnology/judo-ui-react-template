## ADDED Requirements

### Requirement: Row Highlight Legend

When a table has configured row stylers, the generator SHALL render one legend entry per styler: a dot filled with the styler's unchanged `backgroundColor` and outlined in `theme.palette.grey[600]`, followed by the styler's translated label (`table-custom-row-style-<name>`).

#### Scenario: Light styler colour stays visible

- **GIVEN** a table with a row styler whose `backgroundColor` is white
- **WHEN** the legend is rendered on the white table card
- **THEN** the legend dot is outlined in `theme.palette.grey[600]`
- **AND** the dot remains distinguishable from the card
- **AND** the outline has at least 3:1 contrast against the white card (WCAG 2.2 SC 1.4.11)

#### Scenario: Fill colour is preserved

- **GIVEN** a table with a row styler whose `backgroundColor` is a saturated colour
- **WHEN** the legend is rendered
- **THEN** the dot's fill equals the styler's `backgroundColor`
- **AND** only a 1px outline is added around it

#### Scenario: Outline follows the theme

- **GIVEN** a consuming application that overrides the MUI `grey` palette
- **WHEN** the legend is rendered
- **THEN** the outline colour is read from the active theme rather than a hard-coded value
