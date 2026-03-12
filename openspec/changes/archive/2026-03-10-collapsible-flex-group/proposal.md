## Why

The ESM metamodel has a `collapsible` boolean attribute on `ui::Group` (JNG-6371), which maps to `collapsible` on `ui::Flex` in the UI metamodel. The metamodel change is already done — the React code generator now needs to render collapsible Flex groups using MUI Accordion components so users can expand/collapse form sections.

## What Changes

- When a `Flex` element has `collapsible = true`, render it as an MUI `<Accordion>` instead of plain Grid/Stack or Card
- Collapsible takes priority over Card mode — if both `collapsible` and `card` are true, render as Accordion (collapsible wins)
- The AccordionSummary displays the Flex's icon, label, and action button group (same content as card header)
- Typography in collapsible headers uses `h6` variant (smaller than card's `h5`)
- Accordion starts expanded by default (`defaultExpanded`)
- Action buttons in AccordionSummary use `e.stopPropagation()` to prevent toggling the accordion
- Collapsible works with all existing Flex features: horizontal/vertical direction, nesting, subTheme, customImplementation, hiddenBy

## Capabilities

### New Capabilities
- `collapsible-group`: Adds collapsible Accordion rendering for Flex elements with `collapsible = true`, including all combination behaviors with existing Flex modes (card, label, icon, action buttons, direction, nesting)

### Modified Capabilities
- `layout-system`: The Flex Container requirement gains a new collapsible mode that uses MUI Accordion, and the Frame/Card Container requirement is modified to yield priority to collapsible when both flags are set

## Impact

- `actor/src/containers/widget-fragments/flex.hbs` — new Accordion rendering branch before the card branch
- `actor/src/fragments/container/common-imports.fragment.hbs` — conditional Accordion/AccordionSummary/AccordionDetails imports
- `UiPageContainerHelper.java` — new `containerHasCollapsible()` helper method
- Integration test snapshots need updating after template changes
- No breaking changes — `collapsible` defaults to `false`, so all existing Flex elements render unchanged
