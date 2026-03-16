## 1. Java Helper

- [x] 1.1 Add `containerHasCollapsible(PageContainer)` method to `UiPageContainerHelper.java` — collect all Flex elements and check if any has `isCollapsible() == true`

## 2. Conditional Imports

- [x] 2.1 Add conditional Accordion imports to `common-imports.fragment.hbs` — `Accordion`, `AccordionSummary`, `AccordionDetails` from `@mui/material` when `containerHasCollapsible` is true
- [x] 2.2 Add conditional `ExpandMoreIcon` import from `@mui/icons-material/ExpandMore` when `containerHasCollapsible` is true

## 3. Flex Template — Collapsible Branch

- [x] 3.1 Add `{{# if this.collapsible }}` branch in `flex.hbs` BEFORE the existing `{{# if this.card }}` branch, making it an `{{# if this.collapsible }} ... {{ else if this.card }} ... {{ else }} ... {{/ if }}` structure
- [x] 3.2 Implement AccordionSummary content: icon (via `elementHasIcon`) + label (via `elementHasLabel`) using `<Typography variant="h6">`
- [x] 3.3 Implement actionButtonGroup rendering inside AccordionSummary with `e.stopPropagation()` on each button's onClick
- [x] 3.4 Implement AccordionDetails content: horizontal (`<Grid container>`) vs vertical (`<Stack>`) children layout based on `isDirectionHorizontal`
- [x] 3.5 Set `defaultExpanded` on the `<Accordion>` element

## 4. Build Verification

- [x] 4.1 Run `mvn clean install` to verify compilation and all existing integration tests pass
- [x] 4.2 Update snapshot files in affected itest modules if diff-checker reports changes
