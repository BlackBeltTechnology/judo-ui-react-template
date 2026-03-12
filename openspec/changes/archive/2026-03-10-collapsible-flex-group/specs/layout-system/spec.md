## MODIFIED Requirements

### Requirement: Flex Container

The generator SHALL produce flexbox-based layout containers from `Flex` model elements.

`direction = HORIZONTAL` SHALL map to `flex-direction: row`. `direction = VERTICAL` SHALL map to `flex-direction: column`.

`mainAxisAlignment` SHALL map to `justify-content`: START → `flex-start`, CENTER → `center`, END → `flex-end`, SPACEBETWEEN → `space-between`, SPACEAROUND → `space-around`, SPACEEVENLY → `space-evenly`.

`crossAxisAlignment` SHALL map to `align-items`: START → `flex-start`, CENTER → `center`, END → `flex-end`, BASELINE → `baseline`, STRETCH → `stretch`.

Children SHALL be rendered in order, each sized by `col / 12 * 100%` width proportion.

Flex containers SHALL nest arbitrarily deep, each level applying its own direction, alignment, and sizing.

When `collapsible = true`, the Flex SHALL render as an MUI `<Accordion>` (see `collapsible-group` spec for full details). The collapsible mode takes priority over card mode.

#### Scenario: Horizontal flex with children

- **WHEN** a `Flex` element has `direction = HORIZONTAL` and children
- **THEN** children are laid out in a row
- **AND** each child's width is `col / 12 * 100%`

#### Scenario: Nested flex containers

- **WHEN** a `Flex` contains child `Flex` elements
- **THEN** each nested flex applies its own direction and alignment independently

#### Scenario: Flex with data context

- **WHEN** a `Flex` element has `dataElement` set
- **THEN** children are scoped to the referenced data sub-object

#### Scenario: Collapsible flex

- **WHEN** a `Flex` element has `collapsible = true`
- **THEN** the Flex renders as an MUI Accordion with expand/collapse behavior
- **AND** the collapsible mode takes priority over card mode if both are set

**Key Helpers**: `UiWidgetHelper.calculateSize()`, `UiWidgetHelper.alignItems()`, `UiWidgetHelper.justifyContent()`, `UiWidgetHelper.isParentStretchVertical()`, `UiPageContainerHelper.containerHasCollapsible()`

**Template**: `actor/src/containers/widget-fragments/flex.hbs`

### Requirement: Frame/Card Container

When a `Flex` element has a non-null `frame`, the generator SHALL wrap the flex content in a MUI `<Card>` or `<Paper>` component.

`elevation` SHALL set the shadow depth. `radius` SHALL set the border radius. The Flex's label (from `LabeledElement`) SHALL become the card header.

The `isInCard` flag SHALL propagate to all children inside a frame.

When `collapsible = true` is also set, the collapsible Accordion rendering SHALL take priority and Card rendering SHALL be skipped.

#### Scenario: Card with label and elevation

- **WHEN** a `Flex` has `frame` with `elevation = 4` and `radius = 10`
- **AND** the `Flex` has a label
- **THEN** a card wrapper is generated with the configured shadow and radius
- **AND** the label renders as the card header

#### Scenario: Collapsible overrides card

- **WHEN** a `Flex` has both `frame` (card) and `collapsible = true`
- **THEN** the Accordion is rendered instead of the Card
