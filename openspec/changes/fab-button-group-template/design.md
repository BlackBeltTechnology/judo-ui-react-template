## Context

The `ButtonGroup` Ecore model class now has two new attributes:
- `isFab: Boolean` (default `false`) — Java API: `isIsFab()` / `setIsFab(boolean)`
- `alignment: Alignment` (default `BOTTOM_RIGHT`) — Java API: `getAlignment()` / `setAlignment(Alignment)`

In Handlebars templates, these are accessed as `child.isFab` (boolean) and `child.alignment` (enum literal string like `TOP_LEFT`, `BOTTOM_RIGHT`).

The model XML looks like:
```xml
<actionButtonGroups ... isFab="true" alignment="TOP_LEFT">
```

EVL validations already enforce:
- `isFab=true` only valid on `Container.actionButtonGroups` (not Table/Link)
- Non-corner alignment produces a warning (falls back to `BOTTOM_RIGHT`)

Currently, all `Container.actionButtonGroups` render in the `PageHeader` (pages) or `DialogActions` (dialogs) as inline `<Button>` components and `<DropdownButton>` overflow menus. The code paths are:
- `page.tsx.hbs`: `getDefaultButtonsForContainer()` → individual buttons; `getNonDefaultButtonGroupsForContainer()` → dropdown menus
- `dialog.tsx.hbs`: `getDefaultButtonsForContainer()` → dialog action buttons
- `buttongroup.hbs`: inline container button groups with `featuredButtonsForButtonGroup()` and `displayDropdownForButtonGroup()`

The `getDefaultButtonGroupForContainer()` helper simply takes the first `actionButtonGroups` entry. If that first group has `isFab=true`, it would currently render as header buttons — which is wrong.

## Goals / Non-Goals

**Goals:**
- FAB button groups render as MUI `<Fab>` / `<SpeedDial>` floating over page content
- Non-FAB button groups continue rendering exactly as before (backward compatible)
- The `alignment` attribute maps to CSS positioning (4 corners)
- `featuredActions` interacts correctly with FAB rendering (individual Fabs vs. SpeedDial)
- Works in both page and dialog contexts (though dialogs may need special consideration)

**Non-Goals:**
- Custom FAB styling/theming beyond MUI defaults
- Animation customization for SpeedDial open/close
- FAB on Table or Link button groups (blocked by EVL)
- Mobile-specific responsive behavior (FAB is inherently mobile-friendly)

## Decisions

### 1. New Java helper methods that filter by isFab, templates call the filtered versions

**Decision:** Add new helper methods in `UiPageContainerHelper`:
- `getFabButtonGroupsForContainer(PageContainer)` → `List<ButtonGroup>` where `isIsFab() == true`
- `getNonFabDefaultButtonGroupForContainer(PageContainer)` → first non-FAB group (or null)
- `getNonFabDefaultButtonsForContainer(PageContainer)` → buttons from first non-FAB group
- `getNonFabNonDefaultButtonGroupsForContainer(PageContainer)` → remaining non-FAB groups

And in `UiWidgetHelper`:
- `fabAlignmentCss(ButtonGroup)` → returns CSS object string for the alignment position

**Alternative considered:** Adding `{{# unless child.isFab }}` guards in existing templates — simpler but scatters FAB logic across templates and doesn't fix the "default group" selection problem where the first group might be a FAB group.

**Rationale:** The "default button group" concept (first group renders inline, rest as dropdowns) must skip FAB groups entirely. This is cleanest at the Java helper level. Existing helper methods remain unchanged for backward compatibility.

### 2. FAB rendering as a new fragment template

**Decision:** Create `actor/src/containers/widget-fragments/fab-button-group.fragment.hbs` containing the Fab/SpeedDial rendering logic. This fragment is included from `page.tsx.hbs` (and potentially `dialog.tsx.hbs`) for each FAB button group.

**Rationale:** Keeps the FAB rendering logic isolated and reusable. The existing `buttongroup.hbs` template handles inline button groups inside flex containers — FAB groups are structurally different (floating overlay, not part of the layout flow).

### 3. Rendering logic based on button count and featuredActions

The MUI component mapping:

| Condition | Rendering |
|---|---|
| 1 button total | Single `<Fab>` |
| N buttons, `featuredActions = 0` | `<SpeedDial>` with all as `<SpeedDialAction>` |
| N buttons, `0 < featuredActions < N` | First K as individual `<Fab>` + `<SpeedDial>` for rest |
| N buttons, `featuredActions >= N` | All as individual `<Fab>`, no SpeedDial |

Fab variant: button has label → `<Fab variant="extended">` (text + icon); no label → circular `<Fab>` (icon only).

SpeedDial toggle: ButtonGroup has label → extended Fab toggle; no label → circular with default SpeedDialIcon.

**Rationale:** Mirrors the existing `featuredActions` / `DropdownButton` pattern but with mobile-appropriate MUI components. Reuses `featuredButtonsForButtonGroup()` and `nonFeaturedButtonsForButtonGroup()` helpers unchanged.

### 4. Alignment-to-CSS mapping

**Decision:** Map `Alignment` enum to CSS `position: fixed` with appropriate `top`/`bottom` and `left`/`right` values:

| Alignment | CSS |
|---|---|
| `TOP_LEFT` | `{ position: 'fixed', top: 16, left: 16 }` |
| `TOP_RIGHT` | `{ position: 'fixed', top: 16, right: 16 }` |
| `BOTTOM_LEFT` | `{ position: 'fixed', bottom: 16, left: 16 }` |
| `BOTTOM_RIGHT` | `{ position: 'fixed', bottom: 16, right: 16 }` |
| Any other | Falls back to `BOTTOM_RIGHT` |

The `16px` offset matches MUI's standard FAB spacing (`theme.spacing(2)`).

**Alternative considered:** Using `position: absolute` relative to the page content area — but FABs should stay in viewport corner during scroll, so `fixed` is correct.

**Rationale:** `position: fixed` is the standard Material Design FAB behavior. The spacing uses MUI's spacing unit for theme consistency.

### 5. Page vs Dialog handling

**Decision:** In `page.tsx.hbs`, FAB groups render as a floating overlay after the `<PageHeader>` and alongside the content `<Box>`. In `dialog.tsx.hbs`, FAB groups are filtered out of `<DialogActions>` and rendered inside the dialog body with `position: absolute` (relative to the dialog content, not the viewport).

**Alternative considered:** Ignoring `isFab` in dialogs entirely — but the model allows it (EVL only blocks Table/Link), so we should handle it.

**Rationale:** Dialogs are constrained viewport elements; `position: fixed` would place FABs outside the dialog visually. `position: absolute` within the dialog content area is more appropriate.

### 6. MUI imports strategy

**Decision:** Add conditional MUI imports in `page.tsx.hbs` and `dialog.tsx.hbs` guarded by `{{# if (containerHasFabButtonGroups container) }}`. New helper `containerHasFabButtonGroups(PageContainer)` returns true if any `actionButtonGroups` has `isIsFab() == true`.

Components to import: `Fab`, `SpeedDial`, `SpeedDialAction`, `SpeedDialIcon` from `@mui/material`.

**Rationale:** Only import when needed to keep bundle size minimal for pages without FABs.

## Risks / Trade-offs

- **Multiple FABs can overlap** — When multiple button groups on the same container have `isFab=true` with the same alignment, they will overlap. → Mitigation: This is a designer's responsibility. The generator handles each FAB group independently; stacking logic (if needed) would be a future enhancement.

- **SpeedDial z-index conflicts** — FABs use `position: fixed` which can conflict with MUI's dialog/snackbar z-index system. → Mitigation: Use MUI's `zIndex.fab` (1050) which is below modal (1300) and snackbar (1400) by default.

- **Dialog FAB positioning** — Using `position: absolute` in dialogs requires the dialog content container to have `position: relative`. → Mitigation: DialogContent already has relative positioning in MUI's default styles; verify in testing.

- **judo-meta-ui version dependency** — The generator will call `isIsFab()` and `getAlignment()` which don't exist in the current dependency version. → Mitigation: Must bump `judo-meta-ui-version` in pom.xml to a snapshot that includes commit `43f896fe` or later.
