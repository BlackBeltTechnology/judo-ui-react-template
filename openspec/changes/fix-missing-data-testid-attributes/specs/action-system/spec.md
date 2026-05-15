## MODIFIED Requirements

### Requirement: Button Generation

The generator SHALL produce MUI `<Button>` or `<IconButton>` components from `Button` elements.

Button variant SHALL be determined by `buttonStyle` (contained, outlined, text). Button icon SHALL come from `icon.iconName` using `<MdiIcon>`. `tooltipText` SHALL render a tooltip. `label` SHALL render button text.

When a `ButtonGroup` has `featuredActions < total buttons`, excess buttons SHALL go to a `<DropdownButton>` overflow menu. When `isFab = true`, the button group SHALL render as a floating action button with `alignment` positioning (BOTTOM_RIGHT, etc.).

The `<DropdownButton>` component SHALL render a `data-testid` attribute matching the `ButtonGroup` element's XMI ID (without any suffix). The `data-testid` SHALL be used instead of `id` for test targeting. The `id` attribute MAY be retained separately with the `-button-group` suffix for accessibility/anchoring purposes.

#### Scenario: Button group with overflow

- **WHEN** a `ButtonGroup` has 5 buttons and `featuredActions = 2`
- **THEN** 2 buttons are rendered visibly and 3 are in a dropdown overflow menu

#### Scenario: Floating action button

- **WHEN** a `ButtonGroup` has `isFab = true` and `alignment = BOTTOM_RIGHT`
- **THEN** the button group renders as a floating action button in the bottom-right corner

#### Scenario: DropdownButton has data-testid matching XMI ID

- **WHEN** a `ButtonGroup` renders a `<DropdownButton>` overflow menu
- **THEN** the `<DropdownButton>` SHALL have `data-testid` set to the ButtonGroup's XMI ID
- **AND** the `data-testid` SHALL NOT include a `-button-group` suffix

**Key Helpers**: `UiWidgetHelper.variantForButton()`, `UiWidgetHelper.displayDropdownForButtonGroup()`, `UiWidgetHelper.elementHasIcon()`, `UiWidgetHelper.elementHasLabel()`

**Templates**: `actor/src/containers/widget-fragments/button.hbs`, `buttongroup.hbs`
