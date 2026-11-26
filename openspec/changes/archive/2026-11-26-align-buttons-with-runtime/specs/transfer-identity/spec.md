## ADDED Requirements

### Requirement: Standalone-button testid grammar

Every standalone button emitted by the generator SHALL carry a `data-testid` constructed through `buildButtonTestId(buttonId, actionType?)` exported from `src/utilities/transfer-id.ts`. The grammar SHALL be:

- Button with an associated action definition: `button::<buttonId>::<actionType>`.
- Button without an associated action definition: `button::<buttonId>` (bare).

The `<buttonId>` SHALL be the button element's `sourceId` when present, otherwise its `xmi:id`, resolved via the existing `UiGeneralHelper.getElementId(EObject)` helper.

The `<actionType>` SHALL be the string returned by `UiActionsHelper.getButtonActionType(EObject)`, defined as:

- Read the button's `actionDefinition` reference.
- Take the referenced element's `@type` (the EMF class name).
- Apply the transformation `.replace("ui:", "").replace("ActionDefinition", "").toLowerCase()`.

This transformation is a byte-exact port of the runtime's `packages/test-ids/src/element.ts::getButtonActionType`. The runtime's `packages/test-ids/src/element.ts::getButtonTestId` is the authoritative source for the grammar itself. Any future change to the transformation or grammar SHALL be made in the runtime first and mirrored here.

**Distinction from field-scoped buttons.** Buttons rendered inside a relation widget (`SingleRelationInput`, `Tags`, `TextWithTypeAhead`) SHALL continue to use the `field::<fieldId>::button::<role>` grammar defined under the "Hierarchical `data-testid` scheme" Requirement. The two grammars are non-overlapping:

- `button::<id>::<actionType>` — standalone buttons (page-level, container-level, button groups, FAB, table toolbar).
- `field::<fieldId>::button::<role>` — buttons that are semantic descendants of a specific field.

The choice between them SHALL be determined by whether the button's DOM ancestry includes a `field::<id>` wrapper.

**Row-action buttons: OPEN.** Per-row action buttons rendered inside a table's `__actions__` cell SHALL follow whichever grammar is settled by the runtime's clarification of `RowActionCell.tsx` (which currently uses `getButtonTestId`) vs `getRowActionTestId` (which is exported by `@judo/test-ids` but unused by any current renderer). This spec does NOT normatively fix a grammar for row-action buttons; the current template output at `components/table/table-row-actions.tsx.hbs` remains in the row-scoped form (`table::<tableId>::row::<transferId>::button::<actionId>`) until the runtime clarification lands.

#### Scenario: Table toolbar Create button emits `button::<id>::opencreateform`

- **GIVEN** a table with a toolbar action whose `actionDefinition` is `OpenCreateFormActionDefinition`
- **WHEN** the toolbar button is rendered
- **THEN** the DOM `data-testid` is `button::<toolbar-button-xmiId>::opencreateform`
- **AND** the value is byte-identical to what the runtime emits for the same model element

#### Scenario: Standalone page button emits `button::<id>::<actionType>`

- **GIVEN** a page-level button whose `actionDefinition` is `OpenPageActionDefinition`
- **WHEN** the button is rendered
- **THEN** the DOM `data-testid` is `button::<button-xmiId>::openpage`

#### Scenario: Button without action-definition emits bare form

- **GIVEN** a button model shape without an `actionDefinition` reference (decorative button, custom-extension button)
- **WHEN** the button is rendered
- **THEN** the DOM `data-testid` is `button::<button-xmiId>` (no `::<actionType>` suffix)
- **AND** the value is byte-identical to the runtime's `joinTestId('button', id, undefined)` output for the same shape

#### Scenario: Field-scoped button retains its existing grammar

- **GIVEN** a `SingleRelationInput` widget with a primary set button whose `actionDefinition` is `OpenSetSelectorActionDefinition`
- **WHEN** the widget is rendered
- **THEN** the set button's DOM `data-testid` is `field::<field-xmiId>::button::set`
- **AND** it is NOT `button::<button-xmiId>::opensetselector`

#### Scenario: Standalone button testid is Playwright-addressable cross-engine

- **GIVEN** the same UI model rendered by (i) this template and (ii) the JUDO frontend runtime
- **WHEN** `page.getByTestId('button::<toolbar-button-xmiId>::opencreateform')` is evaluated on both outputs
- **THEN** the selector resolves exactly one element on each output
- **AND** each resolved element is the same DOM level (the actual `<button>` receiving click events)

**Templates**:
- `actor/src/utilities/transfer-id.ts.hbs` (new export `buildButtonTestId`)
- `actor/src/utilities/table.ts.hbs` (new `actionType` field on `ToolBarActionProps<T>`)
- `actor/src/containers/components/table/index.tsx.hbs` (every `ToolBarActionProps` literal gains `actionType`)
- `actor/src/fragments/container/common-imports.fragment.hbs` (adds `buildButtonTestId` to the shared transfer-id import)
- `actor/src/containers/page.tsx.hbs`
- `actor/src/containers/dialog.tsx.hbs` (dialog action-bar buttons — same emission class as `page.tsx.hbs`, missed in the initial site enumeration)
- `actor/src/containers/widget-fragments/button.hbs`
- `actor/src/containers/widget-fragments/buttongroup.hbs`
- `actor/src/containers/widget-fragments/fab-button-group.fragment.hbs`
- `actor/src/containers/widget-fragments/flex.hbs`
- `actor/src/components/table/EagerTable.tsx.hbs`
- `actor/src/components/table/LazyTable.tsx.hbs`
- `actor/src/components/widgets/AssociationButton.tsx.hbs` (shared TSX component for single-relation `OpenPageActionDefinition` navigation buttons; gains an optional `actionType` prop, threaded from `containers/widget-fragments/button.hbs`)

**Java helpers**:
- `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiActionsHelper.java` (new public static method `getButtonActionType(EObject)`)
