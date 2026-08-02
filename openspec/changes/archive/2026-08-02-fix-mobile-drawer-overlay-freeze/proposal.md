## Why

The generated navigation shell (`src/layout/Drawer/index.tsx`) renders a **permanent** `MiniDrawerStyled` on desktop breakpoints and a **temporary** `MuiDrawer` (with `ModalProps={{ keepMounted: true }}`) on mobile breakpoints (`downSM`). The temporary drawer's `open` prop is derived from the desktop mini/expanded flag: `open={!miniDrawer}`.

When the viewport shrinks from a desktop breakpoint (`md`/`lg`/`xl`) into a mobile one (`sm`/`xs`), the temporary `MuiDrawer` mounts with `open={!miniDrawer}` still holding the **stale desktop value** (`miniDrawer = false` → `open = true`). A resize `useEffect` then sets `miniDrawer = true` (→ `open = false`) on the very next tick. That open→close race on the freshly mounted, `keepMounted` MUI `Modal` strands the Modal's internal `exited` flag at `false`, so its full-viewport root never receives `visibility: hidden`.

The result is an **invisible, full-viewport overlay** (`position: fixed; inset: 0; z-index: 1200; visibility: visible; pointer-events: auto`) sitting on top of the whole page. Every click is captured by the overlay, so the app appears **frozen** — no button, link, or nav item responds — until the page is reloaded. A fresh load at the same mobile width is fine (the drawer mounts already-closed), which is why the defect only manifests on the **desktop→mobile resize transition**.

## What Changes

- In `actor/src/layout/Drawer/index.tsx.hbs`, the temporary (mobile) `MuiDrawer` no longer inherits the stale desktop `!miniDrawer` value on the render where the app first enters a mobile breakpoint.
- A small render-time guard forces the temporary drawer **closed** on the first render after entering `sm`/`xs` (whether crossing from a desktop breakpoint or on an initial mobile mount), so the `keepMounted` Modal mounts already-closed and never enters the open→close race that strands the overlay.
- No change to desktop behavior, to the permanent `MiniDrawerStyled`, to the hamburger/`onChangeMiniDrawer` wiring, or to the existing resize `useEffect`.
- The guard ships **without an explanatory comment in the generated output** — generated app code stays terse, and the rationale below (plus `design.md`) is the single place where the mechanics are documented.

### Guard rationale (kept out of the generated code)

The temporary (mobile) `MuiDrawer` must start **closed** on the first render after entering the mobile breakpoint. If it inherits the desktop `!miniDrawer` (open) value, MUI mounts its `keepMounted` Modal already-open and the resize `useEffect` immediately closes it; that open→close race on a freshly mounted node strands the Modal's internal `exited` flag at `false`, leaving an invisible full-viewport overlay (`visibility: visible; pointer-events: auto`) that swallows every click until reload.

`downSM` and `isXs` resolve to the *same* media query (`breakpoints.down('sm')`), so the temporary Drawer branch only ever renders at size `'xs'`. On the crossing render `prevSizeRef` still holds the previous (larger, or `undefined`) size, so `enteringMobile` is `true` and the drawer is forced closed for exactly that render. Once the effect commits `prevSizeRef.current = 'xs'`, `enteringMobile` becomes `false`, `open` falls back to `!miniDrawer`, and the hamburger toggle behaves normally — the guard is self-clearing.

```tsx
const enteringMobile = downSM && prevSizeRef.current !== 'xs';
const temporaryDrawerOpen = !miniDrawer && !enteringMobile;
```

## Capabilities

### Modified Capabilities
- `application-structure`: The **Navigation Menu** requirement's vertical sidebar drawer gains an explicit responsive-behavior scenario — switching between desktop (permanent) and mobile (temporary) breakpoints SHALL NOT leave a click-blocking overlay over page content.

## Impact

- **Handlebars template**: `actor/src/layout/Drawer/index.tsx.hbs` (add a `temporaryDrawerOpen` guard; feed it to the temporary `<MuiDrawer open=…>`).
- **Snapshots**: none — the generated app shell (`src/layout/Drawer/index.tsx`) is not part of any itest snapshot set.
- **Consuming projects**: pick up the fix on the next generator release + regenerate. Runtime regression coverage (a Playwright resize test) lives in the consuming app, not in this generator repo.
- **Risk**: low. Single-file, 2 added lines + 1 changed prop, desktop and fresh-mobile-load behavior unchanged; only the transitional render is affected.
