## MODIFIED Requirements

### Requirement: Autocomplete/Type-Ahead

The generator SHALL produce a `<TextWithTypeAhead>` component from `TextInput` elements with `isTypeAheadField = true`.

The component SHALL provide autocomplete suggestions as the user types. The suggestion source SHALL come from a range action on the bound relation.

`TextWithTypeAhead` SHALL emit three structurally distinct `data-testid` attributes at the outer wrapper, the MUI `<Autocomplete>` element, and the underlying `<input>` element per the `transfer-identity` capability. It SHALL NOT repeat the raw XMI id on all three DOM levels.

#### Scenario: Text input with type-ahead

- **WHEN** a `TextInput` element has `isTypeAheadField = true`
- **THEN** the generated field shows autocomplete suggestions as the user types
- **AND** the widget renders `data-testid` attributes `field::<xmiId>`, `field::<xmiId>::autocomplete`, and `field::<xmiId>::input` on three distinct DOM nodes

**Key Helpers**: `UiPageContainerHelper.getTextInputsWithTypeAhead()`, `UiPageContainerHelper.containerHasTypeAhead()`
**Runtime Helper**: `buildFieldTestId` (see `transfer-identity`)

---

### Requirement: Common Input Properties

All input widgets SHALL support common properties like disabled, readonly, required, and label.

The `disabled` property SHALL disable the input. The `readonly` property SHALL make the input non-editable but display the value. The `required` property SHALL mark the field as required with validation. The `label` property SHALL display a label. The `helperText` property SHALL show help text below the input.

Every input widget SHALL emit a `data-testid` on its outer wrapper constructed via `buildFieldTestId(xmiId)`. Widgets with multiple interactive DOM elements (autocomplete, buttons, dropdowns) SHALL emit additional role-suffixed `data-testid` attributes per the vocabulary defined in the `transfer-identity` capability. No two DOM elements within a single widget SHALL share a `data-testid` value.

#### Scenario: Common properties applied

- **WHEN** an input element has `disabled = true`
- **THEN** the generated field is disabled
- **AND** `readonly`, `required`, `label`, and `helperText` are handled similarly

#### Scenario: Widget outer wrapper carries a field-scoped testid

- **WHEN** any input widget is rendered
- **THEN** its outer wrapper DOM node carries `data-testid="field::<xmiId>"`
- **AND** interactive descendants (input, button, dropdown, autocomplete) carry role-suffixed testids from the vocabulary in `transfer-identity`

**Runtime Helper**: `buildFieldTestId` (see `transfer-identity`)
