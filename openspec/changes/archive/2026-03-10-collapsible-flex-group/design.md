## Context

The `flex.hbs` template currently has two rendering branches: `card` mode (wraps content in MUI `<Card>`) and default mode (plain `<Grid>` container). Both branches support icon, label, action button groups, horizontal/vertical direction, subTheme, customImplementation, and hiddenBy wrappers.

The UI metamodel's `Flex` EClass now has a `collapsible` boolean attribute (default `false`). The React generator needs a third rendering branch using MUI `<Accordion>`.

## Goals / Non-Goals

**Goals:**
- Render collapsible Flex groups as MUI Accordion with expand/collapse behavior
- Support all existing Flex features (icon, label, action buttons, direction, nesting, subTheme, customImplementation, hiddenBy)
- Maintain the existing wrapper nesting order: `Grid > hiddenBy > subTheme > customImplementation > [collapsible|card|plain]`

**Non-Goals:**
- Controlled expand/collapse state (no external state management — Accordion manages its own state)
- A separate `defaultExpanded` metamodel attribute — always starts expanded
- Animated transitions beyond MUI Accordion defaults

## Decisions

### Decision 1: Collapsible takes priority over Card

When both `collapsible=true` and `card=true`, render as Accordion only. No Card wrapping.

**Rationale**: Tested B1-B4 combinations in ReactSample. B1 (Accordion replaces Card) is the cleanest — Card-inside-Accordion or Accordion-inside-Card creates visual noise and confusing nesting. The Accordion already provides a visual container boundary.

**Alternative**: Card wrapping Accordion (B2/B4) — rejected due to double-border visual artifacts.

### Decision 2: Accordion branch placement in flex.hbs

Insert the `{{# if this.collapsible }}` branch BEFORE the existing `{{# if this.card }}` branch. This naturally implements the priority rule.

```
{{# if this.collapsible }}
    <Accordion> ... </Accordion>
{{ else if this.card }}
    <Card> ... </Card>
{{ else }}
    <Grid> ... </Grid>
{{/ if }}
```

### Decision 3: Header content in AccordionSummary

The AccordionSummary contains the same elements as the Card header: icon + label + action button group. Layout mirrors the card branch's header structure.

Typography uses `h6` variant (vs card's `h5`) — tested in ReactSample C3, gives a proportionally better look inside the Accordion's compact summary.

### Decision 4: Action button stopPropagation

Action buttons inside AccordionSummary need `onClick={(e) => { e.stopPropagation(); ... }}` to prevent the click from toggling the Accordion. Tested in ReactSample E1 — works correctly.

### Decision 5: Conditional MUI imports

Add `containerHasCollapsible(PageContainer)` helper in `UiPageContainerHelper.java` (mirrors `containerHasCards()`). Use it in `common-imports.fragment.hbs` to conditionally import Accordion components + ExpandMoreIcon.

### Decision 6: ExpandMore icon

Use MUI's `ExpandMoreIcon` from `@mui/icons-material/ExpandMore` as the Accordion expand icon. This is the standard MUI pattern and doesn't conflict with the app's MdiIcon system.

## Risks / Trade-offs

- **Collapsible + Card ambiguity** → Mitigated by clear priority rule (collapsible wins). Document in spec.
- **AccordionSummary click area** → Action buttons need stopPropagation. Template must include this. Tested and verified in ReactSample E1.
- **New MUI import (`@mui/icons-material/ExpandMore`)** → This package should already be available in generated apps. Verify during implementation.
