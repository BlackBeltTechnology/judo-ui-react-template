## ADDED Requirements

### Requirement: Button-group dropdown testid grammar

A non-FAB button group whose non-featured buttons render in a dropdown SHALL mark the trigger `button-group::<groupId>::dropdown`, built with `buildButtonGroupTestId(groupId)` from `src/utilities/transfer-id.ts`, where `groupId` is `getElementId` of the button group. Each menu item SHALL carry the canonical button id `buildButtonTestId(getElementId(button), getButtonActionType(button))`. This mirrors the runtime's `InlineButtonGroupRenderer` and `@judo/test-ids` `getButtonGroupTestId`.

#### Scenario: Dropdown trigger

- **WHEN** a button group `ops` with no featured actions renders inside a container or on a page
- **THEN** its trigger carries `data-testid="button-group::ops::dropdown"`

#### Scenario: Dropdown menu item

- **WHEN** the dropdown of that group is opened and contains a call-operation button `createMatter`
- **THEN** the menu item carries the same id as a standalone button would, `button::createMatter::<actionType>`, and no `field::…::menu::item::…` id
