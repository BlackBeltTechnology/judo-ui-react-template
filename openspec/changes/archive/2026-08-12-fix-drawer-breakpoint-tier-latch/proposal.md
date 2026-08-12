# Fix the drawer breakpoint-state latch

## Why

The resize `useEffect` in `actor/src/layout/Drawer/index.tsx.hbs` encodes a **partial 5×5
transition table** as an if/else chain over `(previous, current)` breakpoint string pairs. Any
pair no branch matches falls through to an implicit *"keep the previous `miniDrawer` value"* —
so a forgotten cell is silent, not a compile or lint error.

That would be tolerable if `miniDrawer` had one meaning. It does not. It is a single boolean
carrying two:

- at `md`/`lg`/`xl` — "the **permanent** drawer is collapsed to the mini rail"
- at `xs` — "the **temporary** overlay drawer is closed"

and the Header hamburger writes that same flag (`onChangeMiniDrawer(!miniDrawer)` in
`actor/src/layout/Header/index.tsx.hbs`). So a value written with one meaning latches into a
breakpoint that reads it with the other.

An exhaustive walk of all 40 transitions (5 sizes × 4 destinations × both `miniDrawer` values)
plus initial mount finds **4 fall-through cells**, 2 of them user-visible defects:

| cell | consequence |
|---|---|
| `sm → xs` | `miniDrawer=false` carried into `xs` → the overlay drawer **and its backdrop open with no user action** once the self-clearing render guard releases. Reported by CodeRabbit on PR #593, patched in `e5877709`. |
| `xs → sm` | `miniDrawer=false` carried into `sm` → the permanent drawer renders **expanded (260 px) on a ≤899 px viewport**, and the collapse `ToggleButton` is not rendered at `sm` (`!isXs && !isSm`) — so the user **cannot collapse it**. Still broken. |
| `xs → md`, `sm → md` | an expanded drawer latches into `md`, where `lg → md` deliberately collapses — inconsistent. |

`xs → sm` is reachable in three steps from a clean load: **load at `xs` → tap the hamburger →
widen to `sm`**. And it is the *very state that feeds* the `sm → xs` defect: the previous fix
closed the exit door and left the entrance open. Patching cells one at a time is what produced
this class of bug; the remaining cells are latent repeats of it.

## What Changes

Replace the transition chain with a **breakpoint tier** rule — the invariant the chain was
approximating all along:

- `compact` (`xs`, `sm`) — no collapse toggle is rendered here, so the drawer is **always**
  collapsed (at `xs`, "collapsed" means the overlay drawer is closed)
- `medium` (`md`) — collapsed by default, toggleable
- `wide` (`lg`, `xl`) — expanded by default, toggleable

Within a tier the user's toggle choice stands. Crossing a tier boundary re-applies the entered
tier's default. That is the whole rule — no pair table, and the fall-through is now a
deliberate *within-tier* decision rather than an omission.

The render-time guard from PR #593 (`enteringMobile` / `temporaryDrawerOpen`) **stays
untouched**: effects run *after* render, so on the crossing render `miniDrawer` still holds the
stale desktop value and the MUI `Modal` would mount open into the `exited`-stranded freeze
race. The tier rule fixes *which state settles*; the guard fixes *the crossing render*. Both
are needed.

## Capabilities

### Modified Capabilities

- `application-structure` — **Navigation Menu**: specify drawer collapsed/expanded state as a
  function of the breakpoint **tier** rather than of the individual transition.

## Impact

- **Handlebars template**: `actor/src/layout/Drawer/index.tsx.hbs` only — a `drawerTier()`
  helper plus a rewritten resize effect. Net ~10 lines replacing ~20.
- **Behaviour**: the only deviations from the pre-`e5877709` chain are the 4 fall-through
  cells above. Every cell the chain actually handled is preserved — including `lg ↔ xl`
  keeping a manual collapse, and initial mount at `md`/`lg`/`xl` honouring the consuming
  app's configured `miniDrawer` default (so a consumer shipping `miniDrawer: true` sees no
  regression).
- **Snapshots**: none — `src/layout/Drawer/index.tsx` is in no itest snapshot set
  (confirmed: no `*/snapshots/**/layout/Drawer/*` path exists).
- **Consuming projects**: pick up on the next generator release + regenerate.
- **Risk**: low, and lower than the status quo — this removes reachable broken states rather
  than adding behaviour. `size === ''` (before the media queries resolve) is now handled by
  construction instead of by the accident that `''` is falsy.
