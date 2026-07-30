## 1. Template Fix

- [x] 1.1 In `actor/src/layout/Drawer/index.tsx.hbs`, add a render-time guard `enteringMobile = downSM && prevSizeRef.current !== 'xs' && prevSizeRef.current !== 'sm'` and `temporaryDrawerOpen = !miniDrawer && !enteringMobile` (with an explanatory comment)
- [x] 1.2 Change the temporary `<MuiDrawer …>` `open` prop from `open={!miniDrawer}` to `open={temporaryDrawerOpen}` (leave the permanent `MiniDrawerStyled` and `DrawerHeader` `open={!miniDrawer}` untouched)

## 2. Spec Delta

- [x] 2.1 Add a "Responsive drawer breakpoint switch" scenario to the **Navigation Menu** requirement in `application-structure` asserting no click-blocking overlay after a desktop↔mobile transition

## 3. Verify (consuming app)

- [x] 3.1 Patched the consuming app's generated `src/layout/Drawer/index.tsx` with the same guard and confirmed the temporary drawer uses `open={temporaryDrawerOpen}`
- [x] 3.2 Ran the patched source (Vite dev against the live karaf) and reproduced the original steps (login → resize desktop→mobile): buttons/nav remain responsive; `document.elementFromPoint` over a nav button returns the button (not `.MuiDrawer-modal`); the `.MuiDrawer-modal` root is `visibility: hidden`; clicking a nav button navigates
- [x] 3.3 Desktop (permanent drawer) render verified unchanged; steady-state mobile open (`open = !miniDrawer`) is preserved by construction — the guard only affects the single crossing render, so the hamburger open/close path is untouched

## 4. Downstream

- [ ] 4.1 Add a Playwright resize regression test in the consuming app (park-here) as the runtime guard (this repo has no runtime test harness)
