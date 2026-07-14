## Definition of Done

- `mvn clean install -DskipPrepareNodeJS` exits green from a clean workspace; `judo-diff-checker:checkDiffs` reports zero unexpected diffs after snapshot refresh.
- `grep -rn "buildFieldTestId([^)]*, 'dialog-title')\|buildFieldTestId([^)]*, 'container')" judo-ui-react/src/main/resources/actor/src/components/dialog/` returns zero lines.
- `grep -rn "buildFieldTestId([^)]*, 'menu::popper'\\|'menu::boundary')" judo-ui-react/src/main/resources/actor/src/layout/` returns zero lines.
- `grep -rn "buildFieldTestId([^)]*, 'panel'\\|'tab::" judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` returns zero lines.
- `grep -rn "fault-dialog-title\|fault-dialog-content\|close-fault-dialog" judo-ui-react/src/main/resources/actor/` returns zero lines.
- `transfer-id.ts.hbs` exports the 10 new helpers listed in proposal §(a).
- Every emitted dialog testid on `ConfirmationDialog` and `FilterDialog` starts with `dialog::`. Verified by grepping the regenerated `.tsx` output in one itest's `target/frontend-react/`.
- Every emitted tab testid on `ModeledTabs` starts with `tabs::`.
- Every emitted nav-drawer testid on `NavItem`, `NavCollapse`, `NavGroup` starts with `nav::`.
- Breadcrumb items carry `breadcrumb::<index>` testids.
- `openspec validate align-domain-testids-with-runtime --strict` reports valid.
- Cross-engine spot-check: `page.getByTestId('dialog::<id>::confirm')` succeeds on runtime *and* template output for the same rendered dialog.
- Branch: `feature/JNG-6391_align_domain_testids_with_runtime`. Every commit references `JNG-6391`.

## 0. Baseline capture

- [ ] 0.1 On the branch tip of the parent change, dump every domain-affected testid line: `grep -rn 'data-testid' judo-ui-react/src/main/resources/actor/src/components/dialog/ judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs judo-ui-react/src/main/resources/actor/src/layout/Drawer/ judo-ui-react/src/main/resources/actor/src/components/CustomBreadcrumb.tsx.hbs > /tmp/domain-testids.before.txt`. Baseline for line-count parity check.
- [ ] 0.2 Fresh branch `feature/JNG-6391_align_domain_testids_with_runtime` from wherever the parent change lands.

## 1. Extend `transfer-id.ts.hbs` with domain helpers

- [ ] 1.1 Append the 10 helpers listed in `proposal.md` §(a) to `judo-ui-react/src/main/resources/actor/src/utilities/transfer-id.ts.hbs`. Also add utility `buildNavItemPath(parentPath, currentId)` for the nav parent-path composition.
- [ ] 1.2 Add companion role-vocabulary comments in the file header pointing at the corresponding runtime files (`@judo/test-ids/src/{dialog,navigation,tabs}.ts`).
- [ ] 1.3 Rebuild `judo-ui-react` module. Verify generated `target/frontend-react/src/utilities/transfer-id.ts` for one itest exports all 10 helpers.

## 2. Dialog sweep

- [ ] 2.1 `ConfirmationDialog.tsx.hbs` — 5 re-routes per `proposal.md` §(b). Remove `buildFieldTestId` imports from this file if no longer used; add `buildDialogTestId`, `buildDialogRoleTestId` imports.
- [ ] 2.2 `FilterDialog.tsx.hbs` — 5 re-routes. Preserve `buildFieldTestId` for the filter operator/value widget-level testids inside the dialog.
- [ ] 2.3 `OperationFaultDialog.tsx.hbs` — 3 re-routes using the synthetic id `'operation-fault'`. Per-fault list items retained.
- [ ] 2.4 Verify: `grep -n "'container'\|'dialog-title'\|button::cancel\|button::confirm\|button::apply\|button::close" judo-ui-react/src/main/resources/actor/src/components/dialog/` returns only the `button::clear-all` line in `FilterDialog.tsx.hbs:390` (template-only). Fault-dialog bare literals gone.

## 3. Tabs sweep

- [ ] 3.1 `ModeledTabs.tsx.hbs` — 3 re-routes + 1 additive:
  - [ ] 3.1.1 Outer `<Box>` bare → `buildTabControllerTestId(id)`.
  - [ ] 3.1.2 `<TabPanel>` `'panel'` → `buildTabPanelTestId(id, c.id)`. Verify `c.id` is in scope where the panel is rendered; if not, plumb it through.
  - [ ] 3.1.3 `<Tab>` `\`tab::${c.id}\`` → `buildTabTestId(id, c.id)`.
  - [ ] 3.1.4 Add `data-testid={buildTabListTestId(id)}` on the `<Tabs>` element that wraps the tab strip (currently anonymous).
- [ ] 3.2 Verify: `grep -n "buildFieldTestId" judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` returns zero lines.

## 4. Nav sweep

- [ ] 4.1 `NavItem.tsx.hbs`:
  - [ ] 4.1.1 Add `parentPath?: string` to `NavItemProps` (or equivalent).
  - [ ] 4.1.2 Inspect the DOM levels at line 99 and 142 (currently double-emission). Rename the second to a distinguishing sub-role or remove it entirely per D6.
  - [ ] 4.1.3 Line 99 `<ListItemButton>` → `buildNavItemTestId(item.id, parentPath)`.
- [ ] 4.2 `NavCollapse.tsx.hbs`:
  - [ ] 4.2.1 Add `parentPath?: string` prop.
  - [ ] 4.2.2 Compute `const navTestId = buildNavItemTestId(menu.id, parentPath); const nextPath = buildNavItemPath(parentPath, menu.id);` in the render body.
  - [ ] 4.2.3 Line 295 `<ListItemButton>` `'menu::boundary'` → `navTestId`.
  - [ ] 4.2.4 Line 337 `<PopperStyled>` `'menu::popper'` → `buildNavItemRoleTestId(navTestId, 'expand')`.
  - [ ] 4.2.5 Pass `parentPath={nextPath}` to children rendered inside this collapse.
- [ ] 4.3 `NavGroup.tsx.hbs`:
  - [ ] 4.3.1 Add `parentPath?: string` prop.
  - [ ] 4.3.2 Line 154 `<PopperStyled>` `'menu::popper'` → `buildNavItemRoleTestId(buildNavItemTestId(item.id, parentPath), 'expand')`.
- [ ] 4.4 Root menu invocation (in `DrawerContent/index.tsx.hbs` or similar) does not pass `parentPath`, so children default to no parent.
- [ ] 4.5 Verify: `grep -rn 'menu::popper\|menu::boundary' judo-ui-react/src/main/resources/actor/src/layout/` returns zero lines.

## 5. Breadcrumb

- [ ] 5.1 `CustomBreadcrumb.tsx.hbs` — locate the `.map((item, index) => ...)` that renders each crumb. Add `data-testid={buildBreadcrumbTestId(index)}` on each rendered `<Link>` / `<Typography>` element.
- [ ] 5.2 The outer `<Breadcrumbs data-testid="application-breadcrumb">` is retained.
- [ ] 5.3 Verify: `grep -c 'buildBreadcrumbTestId' judo-ui-react/src/main/resources/actor/src/components/CustomBreadcrumb.tsx.hbs` returns a positive count.

## 6. Spec updates

- [ ] 6.1 Extend `openspec/specs/transfer-identity/spec.md`:
  - Add a new "Dialog testid grammar" requirement with role table (`title/content/actions/close/confirm/cancel`) and scenarios for `ConfirmationDialog`, `FilterDialog`, `OperationFaultDialog`.
  - Add a "Tab testid grammar" requirement (`tabs::<ctrl>[::<tab>[::panel]][::list]`) with a scenario for `ModeledTabs`.
  - Add a "Nav-item testid grammar" requirement (`nav::item::<path>[::icon|label|expand]`) with parent-path composition semantics and a scenario for nested drawer nav.
  - Add a "Breadcrumb testid grammar" requirement (`breadcrumb::<index>`) with a scenario.
  - Add normative pointers to `@judo/test-ids` `dialog.ts`, `navigation.ts`, `tabs.ts` as source of truth.
- [ ] 6.2 Update the retired-roles section to add `dialog-title`, `panel` (in tabs context), `menu::popper`, `menu::boundary` as retired.
- [ ] 6.3 Add out-of-scope note for options / chips / actor-selector / user-menu.

## 7. Verification gates

- [ ] 7.1 `grep -rn "'dialog-title'\|'button::apply'" judo-ui-react/src/main/resources/actor/src/components/dialog/` returns zero lines (accepting `button::clear-all` and widget-level `operator`/`value` inside FilterDialog).
- [ ] 7.2 `grep -rn "'menu::popper'\|'menu::boundary'" judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 7.3 `grep -rn "'panel'\|'tab::" judo-ui-react/src/main/resources/actor/src/components/ModeledTabs.tsx.hbs` returns zero lines.
- [ ] 7.4 `grep -rn 'fault-dialog-title\|fault-dialog-content\|close-fault-dialog' judo-ui-react/src/main/resources/actor/` returns zero lines.
- [ ] 7.5 `openspec validate align-domain-testids-with-runtime --strict` valid.

## 8. Integration build + snapshot refresh

- [ ] 8.1 Nuke all itest targets: `rm -rf judo-ui-react-itest/*/*/target`.
- [ ] 8.2 First pass: `mvn -DskipPrepareNodeJS -Dfrontend.skip.biome=true -DforceSnapshotOverwrite=true install` from repo root (or per itest in parallel).
- [ ] 8.3 Second pass without `forceSnapshotOverwrite`: `mvn clean install -DskipPrepareNodeJS` — `checkDiffs` SHALL report zero drifts.
- [ ] 8.4 If any parallel-JVM crash occurs on shared `judo-ui-react` surefire, retry the affected itest(s) with `-DskipTests`.

## 9. Cross-engine parity spot-check

- [ ] 9.1 If a runtime build for the same actor is available: open a page that renders a dialog, tab, and drawer. Verify `getByTestId('dialog::<id>::confirm')`, `getByTestId('tabs::<ctrl>::<tab>::panel')`, `getByTestId('nav::item::<path>::expand')`, `getByTestId('breadcrumb::0')` succeed on both engines with byte-identical strings.
- [ ] 9.2 If runtime build not available locally: annotate PR body with expected selectors and defer to follow-up integration ticket.

## 10. Commit + PR

- [ ] 10.1 Commit series: `1-domain-helpers`, `2-dialog-sweep`, `3-tabs-sweep`, `4-nav-sweep`, `5-breadcrumb-sweep`, `6-spec-update`, `7-snapshot-refresh-<itest>` (×6). Each commit prefixed `JNG-6391`.
- [ ] 10.2 Open PR against `develop`. PR body cites `@judo/test-ids` `dialog.ts`, `navigation.ts`, `tabs.ts` at the runtime's current head, lists the deferred items (options/chips, ApplicationSelector, layout containers, DropdownButton, cosmetic rename).
