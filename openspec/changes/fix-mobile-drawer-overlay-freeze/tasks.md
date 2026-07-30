## 1. Template Fix

- [x] 1.1 In `actor/src/layout/Drawer/index.tsx.hbs`, add a render-time guard `enteringMobile = downSM && prevSizeRef.current !== 'xs'` and `temporaryDrawerOpen = !miniDrawer && !enteringMobile` (with an explanatory comment). `downSM` and `isXs` are the same media query, so the temporary drawer only renders at size `'xs'` — the guard is self-clearing once the effect commits `prevSizeRef = 'xs'`, leaving the hamburger toggle unaffected
- [x] 1.2 Change the temporary `<MuiDrawer …>` `open` prop from `open={!miniDrawer}` to `open={temporaryDrawerOpen}` (leave the permanent `MiniDrawerStyled` and `DrawerHeader` `open={!miniDrawer}` untouched)

## 2. Spec Delta

- [x] 2.1 Add a "Responsive drawer breakpoint switch" scenario to the **Navigation Menu** requirement in `application-structure` asserting no click-blocking overlay after a desktop↔mobile transition

## 3. Verify (consuming app)

- [x] 3.1 Patched the consuming app's generated `src/layout/Drawer/index.tsx` with the same guard and confirmed the temporary drawer uses `open={temporaryDrawerOpen}`
- [x] 3.2 Ran the patched source (Vite dev against the live karaf) and reproduced the original steps (login → resize desktop→mobile): buttons/nav remain responsive; `document.elementFromPoint` over a nav button returns the button (not `.MuiDrawer-modal`); the `.MuiDrawer-modal` root is `visibility: hidden`; clicking a nav button navigates
- [x] 3.3 Desktop (permanent drawer) render verified unchanged; steady-state mobile open (`open = !miniDrawer`) is preserved by construction — the guard only affects the single crossing render, so the hamburger open/close path is untouched

## 4. Downstream

- [x] 4.1 Added a Playwright resize regression test in the consuming app (park-here) as the runtime guard (this repo has no runtime test harness): `application/docker/compose-e2e/e2e/tests/tier-mobile-resize.spec.ts`
- [x] 4.2 Cross-checked the full chain: generator built locally as `1.0.0-SNAPSHOT` → park-here repointed at it → `./judo.sh build -f -i` regenerated `src/layout/Drawer/index.tsx` **with** the fix → e2e spec PASSES; reverting the generated `open` prop to `!miniDrawer` makes the same spec FAIL on the overlay assertion (negative control)
- [ ] 4.3 After this change is released, bump `judo-ui-react-template-version` in park-here's `application/frontend-react/pom.xml` to the released generator (it is intentionally left at the previous released version — the SNAPSHOT is local-only)
