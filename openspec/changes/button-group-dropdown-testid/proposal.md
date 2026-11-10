# Proposal

## Why

Tatami design row E7: a modeled button group with non-featured buttons renders a dropdown on both engines, but the ids differ. The runtime (`InlineButtonGroupRenderer.tsx`) marks the trigger `button-group::<id>::dropdown` and each menu item with the canonical button id `button::<id>::<actionType>`. The template emits `field::<xmi>-button-group::container` and `field::<xmi>-button-group::menu::item::<xmi>`. A shared spec (ActionGroupTest `MatterOperationsAndCollection.spec.ts`, `testIds.buttonGroupDropdown`) therefore cannot open the group on React.

## What Changes

- `transfer-id.ts` gains `buildButtonGroupTestId(groupId)`, the mirror of `@judo/test-ids` `getButtonGroupTestId` (`button-group::<id>`).
- The inline (`buttongroup.hbs`) and page-level (`page.tsx.hbs`) button-group dropdowns pass `testId` = `button-group::<getElementId group>::dropdown` and give every menu item `buildButtonTestId(getElementId button, getButtonActionType button)`, through the existing `DropdownButton` `testId` props.
- No visual change: the component, props and layout stay as they are.

## Capabilities

### Modified Capabilities

- `transfer-identity`: adds the button-group dropdown grammar.

## Impact

- Templates: `containers/widget-fragments/buttongroup.hbs`, `containers/page.tsx.hbs`, `fragments/container/common-imports.fragment.hbs`, `utilities/transfer-id.ts.hbs` (+ `transfer-id.test.ts`).
- Tests: new `ButtonGroupTemplateTest`.
- Itest snapshots of models with button-group dropdowns change (ids only).
