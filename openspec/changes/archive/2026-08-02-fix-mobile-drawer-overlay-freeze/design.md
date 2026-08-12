# Design

## Root cause (empirically confirmed)

Reproduced live on a full build of a consuming app: log in, load at a desktop width, then
narrow the window below the `md` breakpoint. Every button/nav goes dead; a reload at the
same width restores it.

`document.elementFromPoint()` over any nav button returns, after the resize:

```text
div.MuiDrawer-root.MuiDrawer-modal.MuiModal-root
  position: fixed   inset: 0   z-index: 1200
  visibility: VISIBLE   pointer-events: AUTO      ← captures every click
  backdrop: visibility:hidden                     ← invisible, so nothing is seen
  drawer paper transform: translateX(-260px)      ← panel is "closed" / off-screen
```

After a *fresh* reload at the identical mobile width the same root reports
`visibility: hidden` and the nav button is the hit-test target — i.e. it works. The only
difference between the two states is that one `visibility` value.

### Why

`Drawer/index.tsx` branches on `downSM`:

- desktop → `<MiniDrawerStyled variant="permanent" open={!miniDrawer}>` (no Modal)
- mobile  → `<MuiDrawer variant="temporary" open={!miniDrawer} ModalProps={{ keepMounted: true }}>`

The resize `useEffect` sets `miniDrawer = true` when the size crosses into `sm`/`xs`, but
effects run *after* render. So on the crossing render the temporary `MuiDrawer` mounts with
`open = !miniDrawer = true` (stale desktop value), and the effect flips it to `false` on the
next tick.

MUI's `Modal` only sets `visibility: hidden` on its root when `!open && exited`. `exited`
becomes `true` via the child transition's `onExited`. The open→close race on the freshly
mounted node strands `exited` at `false`, so with `keepMounted: true` the root stays mounted,
`visibility: visible`, `pointer-events: auto` — a transparent full-viewport click trap.

## Options considered

- **A. Render-time guard (chosen).** Force the temporary drawer closed on the first render
  after entering mobile, so the Modal mounts already-closed (`open=false`, `exited=true`,
  `visibility:hidden`) and never enters the race. One file, 2 added lines + 1 changed prop, no
  new external state, no cross-file changes, desktop untouched. The rationale lives in
  `proposal.md` / this file rather than as a comment in the generated output.
- **B. Decouple a dedicated mobile-open state.** Give the temporary drawer its own
  `useState` open flag, independent of the desktop `miniDrawer` mini/expanded flag, and
  rewire the Header hamburger to toggle it. Cleaner separation of concerns, but touches
  `Drawer` + `Header` (+ config) and changes toggle semantics. Rejected as higher-risk for a
  defect fix.
- **C. Drop `keepMounted`.** Does not reliably fix it: when `exited` is stranded `false`,
  `Modal` still renders (its unmount guard is `!keepMounted && !open && (!hasTransition ||
  exited)`), so a transitioned drawer with stranded `exited` keeps the overlay regardless.

## The fix

```tsx
const enteringMobile = downSM && prevSizeRef.current !== 'xs';
const temporaryDrawerOpen = !miniDrawer && !enteringMobile;
// <MuiDrawer ... open={temporaryDrawerOpen} ... >
```

`prevSizeRef` is the previous *committed* size (updated in the resize effect above). On the
first render after entering mobile it still holds the larger/`undefined` size, so the drawer is
forced closed for exactly that render; afterwards it follows `miniDrawer`.

`prevSizeRef` is only *read* during render (never mutated there — the mutation stays in the
existing effect), so the render output is a pure function of the last committed size and is
stable across concurrent re-renders (the `usePrevious` pattern).

### Why the guard cannot latch on (and the hamburger keeps working)

In `utilities/layout-helper`, `downSM` and `isXs` are the **same media query**
(`breakpoints.down('sm')`, i.e. < 600px):

```ts
downSM = useMediaQuery(breakpoints.down('sm'))
isXs   = useMediaQuery(breakpoints.down('sm'))
```

So `downSM` ⇒ `isXs` ⇒ `size === 'xs'`: the temporary Drawer branch only ever renders at
size `'xs'`. Once the resize effect commits `prevSizeRef.current = 'xs'`, `enteringMobile`
becomes `false` and `open` falls back to `!miniDrawer` — so the guard is self-clearing and the
mobile hamburger toggle is unaffected. Note `'sm'` (600–899px) renders the **permanent** mini
drawer, so it is a desktop-side size here; a `sm → xs` crossing is therefore also guarded,
which additionally avoids popping a modal drawer + backdrop over the page unbidden.

### `sm` belongs on the desktop side of the resize effect too

Because the guard is self-clearing, it only suppresses the *crossing* render — it does not
change `miniDrawer`. So every size that can enter `xs` with `miniDrawer === false` must have a
resize-effect branch that resets it, otherwise the drawer pops open one render after the guard
releases. The `xs(open) → sm → xs` path did exactly that: `xs → sm` matches no branch, so
`miniDrawer` stays `false`, and `sm → xs` then had no branch either. `'sm'` is therefore listed
alongside `md`/`lg`/`xl` as a previous size in the "entering small" branch:

```ts
if (prevSizeRef.current &&
    (prevSizeRef.current == 'sm' || prevSizeRef.current == 'md' ||
     prevSizeRef.current == 'lg' || prevSizeRef.current == 'xl') &&
    (size == 'sm' || size == 'xs')) {
  onChangeMiniDrawer(true);
}
```

The effect only runs on a size *change*, so adding `'sm'` introduces exactly one new
transition: `sm → xs`.

### Transition coverage

| Situation | `prevSizeRef` at render | `enteringMobile` | `open` |
|---|---|---|---|
| desktop→mobile crossing render | `sm`/`md`/`lg`/`xl` | true | forced closed |
| initial mobile mount | `undefined` | true | forced closed |
| settled mobile (post-effect) | `xs` | false | `!miniDrawer` (hamburger works) |
| `xs(open) → sm → xs`, post-effect | `xs` | false | closed — effect reset `miniDrawer` on `sm → xs` |
| desktop (permanent branch) | — | — | unaffected |

## Testing

- This repo: no snapshot covers `src/layout/Drawer/index.tsx`, so there is nothing to
  regenerate/update here; the change is verified by regenerating a consuming app and
  confirming the generated `open={temporaryDrawerOpen}` output plus a clean Vite build.
- Consuming app (park-here): a Playwright resize regression test (login → `setViewportSize`
  desktop→mobile → assert a nav button is clickable / not intercepted) is the runtime guard.
