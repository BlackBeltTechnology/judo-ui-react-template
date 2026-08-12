## 1. Establish the failing case first

- [x] 1.1 Build the exhaustive transition harness from `design.md` (all 20 ordered size pairs + initial mount + the `size === ''` transient, against both `miniDrawer` values) and run it against the chain as of `e5877709` — expect it to report the open cell `xs(mini=false) → sm` (expanded at `sm` with no toggle rendered)
- [x] 1.2 Confirm the same harness reports **0** bad cells for the proposed tier rule, and that its only deviations from the original pre-PR chain are the 4 never-enumerated fall-through cells

## 2. Template Fix

- [x] 2.1 In `actor/src/layout/Drawer/index.tsx.hbs`, add a module-level `drawerTier(size?: string)` returning `'compact'` (`xs`/`sm`), `'medium'` (`md`), or `'wide'` (`lg`/`xl`)
- [x] 2.2 Replace the 4-branch resize `useEffect` chain with the tier rule: unconditionally collapse in `compact`; otherwise re-apply the entered tier's default only when `prevSize` is set and the tier changed. Keep `prevSizeRef.current = size` as the effect's own commit and keep deps at `[size]`
- [x] 2.3 Leave the render-time guard (`enteringMobile`, `temporaryDrawerOpen`) and the temporary `<MuiDrawer open={temporaryDrawerOpen}>` prop untouched — it addresses the crossing render, not the settled state
- [x] 2.4 Leave the permanent `MiniDrawerStyled`/`DrawerHeader` `open={!miniDrawer}` props, the `ToggleButton` visibility condition, and `Header/index.tsx.hbs` untouched

## 3. Spec Delta

- [x] 3.1 In `application-structure` → **Navigation Menu**, state drawer collapsed/expanded state as a function of the breakpoint tier (three tiers, within-tier choice preserved, boundary crossing re-applies the default, configured default honoured before the first resolved breakpoint)
- [x] 3.2 Add scenarios for crossing a tier (`xs` overlay open → widen to `sm` ⇒ collapsed) and for staying within one (`lg` manually collapsed → `xl` ⇒ still collapsed)

## 4. Verify

- [x] 4.1 `mvn -o clean install -pl judo-ui-react` succeeds
- [x] 4.2 `mvn -o clean install -pl judo-ui-react-itest/RelationTest/relation_test__actor -DskipPrepareNodeJS` succeeds — Biome formatting, snapshot diff-checker, and Vite build all clean
- [x] 4.3 Inspect the generated `target/frontend-react/src/layout/Drawer/index.tsx` and confirm it carries `drawerTier` + the tier effect, and still `open={temporaryDrawerOpen}`

## 5. Downstream (out of repo — tracked here, not completable here)

This generator repo has no runtime test harness: no browser, no React test renderer, and
`src/layout/Drawer/index.tsx` is in no itest snapshot set. Runtime coverage therefore lives in the
consuming app, exactly as task 4.3 of `2026-08-02-fix-mobile-drawer-overlay-freeze` does. The
in-repo guarantees for this change are the exhaustive transition harness in `design.md` (0 bad
cells, 0 unresolved mutations, 0 unresolved-hop divergences) plus the generate + Biome + snapshot
+ Vite build; neither exercises a real viewport.

- [ ] 5.1 Extend the consuming app's Playwright resize regression (park-here `tier-mobile-resize.spec.ts`) with the newly closed path: load at `xs` → open the overlay drawer → widen to `sm` → assert the drawer is collapsed to the mini rail and page content is clickable. Deliberately left unchecked: it is a commit in another repository and cannot be verified from here.
