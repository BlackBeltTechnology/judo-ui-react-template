## Why

The `judo-meta-ui` model now supports `isFab` and `alignment` attributes on `ButtonGroup`, and the ESM/Designer already allows configuring them. However, the React template generator still renders all `ButtonGroup` elements as inline buttons in the page header or dropdown menus — it ignores `isFab` entirely. We need the template to generate MUI `<Fab>` and `<SpeedDial>` components for button groups marked `isFab="true"`, positioned according to `alignment`.

## What Changes

- Button groups with `isFab=true` on `Container.actionButtonGroups` are **excluded from the PageHeader** and **excluded from DialogActions**, and instead render as floating overlay elements positioned over the page content
- A new rendering path generates MUI `<Fab>` (single button or all-featured) and `<SpeedDial>` (non-featured overflow) components based on button count and `featuredActions`
- The `alignment` attribute (Alignment enum: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`) maps to CSS fixed/absolute positioning of the FAB group
- Non-corner alignment values fall back to `BOTTOM_RIGHT` (validated by EVL upstream, but generator handles it defensively)
- New Java helper methods filter FAB vs. non-FAB button groups for templates
- MUI `Fab`, `SpeedDial`, `SpeedDialAction`, and `SpeedDialIcon` imports are added to generated pages that use FAB groups
- The `ActionGroupTest` integration test model is updated with `isFab`/`alignment` attributes to exercise all rendering scenarios

## Capabilities

### New Capabilities
- `fab-rendering`: Defines the template rendering logic for floating action button groups — Fab/SpeedDial component mapping, position CSS, featuredActions interaction, and integration with existing button group infrastructure

### Modified Capabilities
- `action-system`: The Button Generation requirement already references `isFab` and `alignment` but the generator does not implement it yet. The requirement is fulfilled by this change — no spec text changes needed, only implementation.
- `page-system`: Pages and dialogs must filter FAB button groups out of their header/action areas and render them as floating overlays instead.

## Impact

- **Java helpers**: `UiPageContainerHelper.java` (new filter methods for FAB groups), `UiWidgetHelper.java` (alignment-to-CSS mapping, FAB detection helpers)
- **Handlebars templates**: `page.tsx.hbs` (filter + FAB overlay block), `dialog.tsx.hbs` (filter FAB groups), `buttongroup.hbs` (FAB conditional branch), new fragment template for FAB/SpeedDial rendering
- **MUI imports**: `common-imports.fragment.hbs` or page-level imports need `Fab`, `SpeedDial`, `SpeedDialAction`, `SpeedDialIcon`
- **Integration test**: `ActionGroupTest` model file and snapshots updated with FAB scenarios
- **Dependency**: Requires `judo-meta-ui` version that includes the `isFab`/`alignment` attributes (commit `43f896fe` or later)
