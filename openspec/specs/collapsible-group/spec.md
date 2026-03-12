# Collapsible Group

Renders collapsible Flex containers as MUI Accordion components. When a `Flex` element has `collapsible = true`, the entire group can be expanded/collapsed by the user. Supports icons, labels, action buttons in the header, and all existing wrapper layers.

## Requirements

### Requirement: Collapsible Flex renders as MUI Accordion

When a `Flex` element has `collapsible = true`, the generator SHALL render it as an MUI `<Accordion>` with `<AccordionSummary>` and `<AccordionDetails>` instead of plain Grid/Stack or Card.

The Accordion SHALL have `defaultExpanded` set (starts open).

The AccordionSummary SHALL use `<ExpandMoreIcon />` as the expand/collapse icon.

#### Scenario: Basic collapsible group

- **WHEN** a `Flex` element has `collapsible = true`
- **THEN** the generator produces `<Accordion defaultExpanded>` wrapping the children
- **AND** `<AccordionSummary expandIcon={<ExpandMoreIcon />}>` is rendered
- **AND** children are inside `<AccordionDetails>`

---

### Requirement: Collapsible header displays icon and label

When a collapsible `Flex` has a label, the generator SHALL render it in the AccordionSummary using `<Typography variant="h6">`.

When a collapsible `Flex` has an icon, the generator SHALL render it before the label using `<MdiIcon>`.

When a collapsible `Flex` has neither label nor icon, the AccordionSummary SHALL display a fallback text using the i18n translation key for the element.

#### Scenario: Collapsible with label and icon

- **WHEN** a collapsible `Flex` has both `icon` and `label`
- **THEN** the AccordionSummary contains `<MdiIcon>` followed by `<Typography variant="h6">`

#### Scenario: Collapsible with label only

- **WHEN** a collapsible `Flex` has `label` but no `icon`
- **THEN** the AccordionSummary contains only `<Typography variant="h6">`

#### Scenario: Collapsible with icon only

- **WHEN** a collapsible `Flex` has `icon` but no `label`
- **THEN** the AccordionSummary contains only `<MdiIcon>`

---

### Requirement: Collapsible with action button group

When a collapsible `Flex` has an `actionButtonGroup`, the generator SHALL render the buttons inside the AccordionSummary, after the icon/label area.

Each button's `onClick` handler SHALL call `e.stopPropagation()` before the action to prevent the Accordion from toggling.

#### Scenario: Action buttons in collapsible header

- **WHEN** a collapsible `Flex` has `actionButtonGroup` with buttons
- **THEN** buttons render inside AccordionSummary in a `<ButtonGroup>`
- **AND** each button's onClick calls `e.stopPropagation()`

---

### Requirement: Collapsible children layout direction

Inside `<AccordionDetails>`, children SHALL follow the same horizontal/vertical layout rules as other Flex modes.

`isDirectionHorizontal = true` SHALL use `<Grid container>`. `isDirectionHorizontal = false` SHALL use `<Stack>`.

#### Scenario: Collapsible with horizontal children

- **WHEN** a collapsible `Flex` has `isDirectionHorizontal = true`
- **THEN** children inside AccordionDetails are laid out in a `<Grid container>` row

#### Scenario: Collapsible with vertical children

- **WHEN** a collapsible `Flex` has `isDirectionHorizontal = false`
- **THEN** children inside AccordionDetails are laid out in a `<Stack>`

---

### Requirement: Collapsible priority over Card

When a `Flex` has both `collapsible = true` and `card = true`, the generator SHALL render as Accordion only. The Card rendering SHALL be skipped.

#### Scenario: Both collapsible and card are true

- **WHEN** a `Flex` has `collapsible = true` AND `card = true`
- **THEN** the generator renders `<Accordion>` (not `<Card>`)

---

### Requirement: Collapsible with wrapper layers

Collapsible Flex SHALL work with all existing wrapper layers: `hiddenBy`, `subTheme`, and `customImplementation`. The nesting order SHALL be: `Grid > hiddenBy > subTheme > customImplementation > Accordion`.

#### Scenario: Collapsible with hiddenBy

- **WHEN** a collapsible `Flex` has `hiddenBy` set
- **THEN** the conditional render wraps the entire Accordion

#### Scenario: Collapsible with subTheme

- **WHEN** a collapsible `Flex` has `subTheme` set
- **THEN** `<SubThemeWrapper>` wraps the Accordion

#### Scenario: Collapsible with customImplementation

- **WHEN** a collapsible `Flex` has `customImplementation = true`
- **THEN** `<ComponentProxy>` wraps the Accordion

---

### Requirement: Conditional Accordion imports

The generator SHALL import `Accordion`, `AccordionSummary`, `AccordionDetails` from `@mui/material` and `ExpandMoreIcon` from `@mui/icons-material/ExpandMore` only when the container has at least one collapsible Flex element.

#### Scenario: Container with collapsible element

- **WHEN** a `PageContainer` contains at least one `Flex` with `collapsible = true`
- **THEN** the generated file includes Accordion and ExpandMoreIcon imports

#### Scenario: Container without collapsible element

- **WHEN** a `PageContainer` has no collapsible `Flex` elements
- **THEN** no Accordion or ExpandMoreIcon imports are generated
