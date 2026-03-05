## 1. Dependency Update

- [x] 1.1 Bump `judo-meta-ui-version` in root `pom.xml` to a snapshot that includes the `isFab`/`alignment` attributes on `ButtonGroup` (commit `43f896fe` or later)

## 2. Java Helper Methods

- [x] 2.1 Add `containerHasFabButtonGroups(PageContainer)` method to `UiPageContainerHelper.java` — returns `true` if any `actionButtonGroups` has `isIsFab() == true`
- [x] 2.2 Add `getFabButtonGroupsForContainer(PageContainer)` method to `UiPageContainerHelper.java` — returns `List<ButtonGroup>` filtered to `isIsFab() == true`
- [x] 2.3 Add `getNonFabDefaultButtonGroupForContainer(PageContainer)` method to `UiPageContainerHelper.java` — returns first non-FAB group (or null)
- [x] 2.4 Add `getNonFabDefaultButtonsForContainer(PageContainer)` method to `UiPageContainerHelper.java` — returns sorted buttons from first non-FAB group
- [x] 2.5 Add `getNonFabNonDefaultButtonGroupsForContainer(PageContainer)` method to `UiPageContainerHelper.java` — returns remaining non-FAB groups (skip first non-FAB, sorted)
- [x] 2.6 Add `fabAlignmentCss(ButtonGroup)` method to `UiWidgetHelper.java` — maps `Alignment` enum to CSS position string (`{ position: 'fixed', top/bottom: 16, left/right: 16 }`), non-corner values fall back to `BOTTOM_RIGHT`

## 3. FAB Fragment Template

- [x] 3.1 Create `actor/src/containers/widget-fragments/fab-button-group.fragment.hbs` — the main FAB/SpeedDial rendering fragment, handling all four scenarios (single Fab, pure SpeedDial, hybrid Fab+SpeedDial, all individual Fabs) based on button count and `featuredActions`
- [x] 3.2 Wire action handlers in the FAB fragment — each `<Fab>` onClick calls `actions.{actionDefinitionName}()`, each `<SpeedDialAction>` onClick calls the same, disabled conditions applied via `containerButtonGroupButtonDisabledConditions`
- [x] 3.3 Implement Fab variant logic — button with label renders `<Fab variant="extended">` with icon + text; button without label renders circular `<Fab>` with icon only; SpeedDial toggle uses ButtonGroup label for extended variant

## 4. Page Template Changes

- [x] 4.1 Update `page.tsx.hbs` to use `getNonFabDefaultButtonsForContainer` instead of `getDefaultButtonsForContainer` for PageHeader inline buttons
- [x] 4.2 Update `page.tsx.hbs` to use `getNonFabNonDefaultButtonGroupsForContainer` instead of `getNonDefaultButtonGroupsForContainer` for PageHeader dropdown menus
- [x] 4.3 Add FAB rendering block in `page.tsx.hbs` — iterate `getFabButtonGroupsForContainer(container)` and include the FAB fragment for each, positioned via `fabAlignmentCss`
- [x] 4.4 Add conditional MUI imports in `page.tsx.hbs` — `Fab`, `SpeedDial`, `SpeedDialAction`, `SpeedDialIcon` guarded by `{{# if (containerHasFabButtonGroups container) }}`

## 5. Dialog Template Changes

- [x] 5.1 Update `dialog.tsx.hbs` to use `getNonFabDefaultButtonsForContainer` instead of `getDefaultButtonsForContainer` for DialogActions
- [x] 5.2 Add FAB rendering block in `dialog.tsx.hbs` — iterate FAB groups inside dialog content area with `position: absolute` positioning
- [x] 5.3 Add conditional MUI imports in `dialog.tsx.hbs` guarded by `containerHasFabButtonGroups`

## 6. Integration Test

- [x] 6.1 Update `ActionGroupTest-ui.model` to include `isFab="true"` and `alignment` attributes on selected `actionButtonGroups` (matching the judo-meta-ui test model changes)
- [ ] 6.2 Run the full build (`mvn clean install`) and verify generated output compiles
- [ ] 6.3 Update snapshot files in `src/test/resources/snapshots/` for affected pages to match the new FAB rendering output
