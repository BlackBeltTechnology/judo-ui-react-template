# Layout System

Generates React layout components from UI container model elements. The layout system uses a flexbox-based approach where `Flex` containers arrange child elements horizontally or vertically, `Frame` provides card-style grouping, `TabController` creates tabbed interfaces, and `Spacer`/`Divider` provide spacing.

## Requirements

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

---

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

---

### Requirement: Tab Navigation

The generator SHALL produce MUI `<Tabs>` and `<Tab>` components from `TabController` model elements.

Each `Tab.element` SHALL render as a tab panel. `orientation` SHALL control horizontal vs vertical tab layout. Tab labels and icons SHALL come from the Tab's `LabeledElement` attributes. Tab switching SHALL be client-side state using React `useState`.

#### Scenario: Horizontal tabs

- **WHEN** a `TabController` has `orientation = HORIZONTAL` and multiple tabs
- **THEN** horizontal MUI Tabs are generated
- **AND** each tab panel renders its element content

#### Scenario: Vertical tabs

- **WHEN** a `TabController` has `orientation = VERTICAL`
- **THEN** vertical MUI Tabs are generated with side-by-side layout

**Key Helpers**: `UiPageContainerHelper.containerHasTabs()`, `UiWidgetHelper.flexParentIsNotTab()`

**Template**: `actor/src/containers/widget-fragments/tabcontroller.hbs`

---

### Requirement: Spacer

The generator SHALL produce empty `<div>` elements from `Spacer` model elements, sized by the `col` and `row` grid proportions.

#### Scenario: Spacer in layout

- **WHEN** a `Spacer` element exists with `col = 4`
- **THEN** an empty div occupying 4/12 width is generated

**Template**: `actor/src/containers/widget-fragments/spacer.hbs`

---

### Requirement: Divider

The generator SHALL produce MUI `<Divider>` components from `Divider` model elements.

#### Scenario: Visual separator

- **WHEN** a `Divider` element exists between two children
- **THEN** a horizontal or vertical line separator is rendered

**Key Helpers**: `UiPageContainerHelper.containerHasDivider()`

**Template**: `actor/src/containers/widget-fragments/divider.hbs`

---

### Requirement: Container Component Generation

The generator SHALL produce a reusable React component for each unique `PageContainer`.

Each container component SHALL receive actions and data as props. Container components SHALL be shared between page and dialog wrappers.

For each container, the generator SHALL produce:
- `src/containers/{ContainerPath}/{ComponentName}.tsx` — container component
- `src/containers/{ContainerPath}/types.ts` — TypeScript types for actions and props
- `src/containers/{ContainerPath}/customization.ts` — Pandino customization key

#### Scenario: Container used by multiple pages

- **WHEN** a `PageContainer` is referenced by two `PageDefinition` elements
- **THEN** one container component is generated
- **AND** both pages compose it with their own data contexts

**Key Helpers**: `UiPageContainerHelper.containerPath()`, `UiPageContainerHelper.containerComponentName()`, `UiImportHelper.getMaterialImportsForPageContainer()`

**Templates**: `actor/src/containers/types.ts.hbs`, `actor/src/containers/page.tsx.hbs`, `actor/src/containers/dialog.tsx.hbs`

---

## Integration Test Coverage

- **ActionGroupTest**: Nested layouts with frames, tabs, flex containers across Galaxy/Matter/Creature views
- **CRUDActionsTest**: Form and view layouts with varied container nesting
- **RelationTest**: Tab-based layouts for displaying multiple relation types
