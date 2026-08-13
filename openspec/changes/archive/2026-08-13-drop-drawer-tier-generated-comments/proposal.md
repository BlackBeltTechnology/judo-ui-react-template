# Keep the drawer tier rationale out of the generated output

## Why

`fix-drawer-breakpoint-tier-latch` shipped the breakpoint-tier rule with a 12-line comment block
above `drawerTier` plus two inline comments inside the resize effect. Those comments are emitted
verbatim into **every generated application**, for every actor, in every consuming project.

This repo already settled that question once: `2026-08-02-fix-mobile-drawer-overlay-freeze`
removed the 9-line comment that shipped with the `enteringMobile` guard and moved its rationale
into the change docs under "Guard rationale (kept out of the generated code)". Generated app code
stays terse; the reasoning lives where it can be reviewed and revised.

There is a second reason to drop this particular block now. It opens with:

```text
// - compact (xs, sm): no ToggleButton is rendered, so the drawer must always be collapsed.
```

"must always be collapsed" is exactly the overstatement CodeRabbit flagged in review
`4917894160`, which was corrected in the spec to a *responsive baseline* that a user action at
`xs` may override. Leaving the sentence in the template would ship a comment that contradicts the
requirement it implements — and would keep contradicting it in every generated app until someone
noticed.

## What Changes

- Remove the module-level tier comment block and the two in-effect comments from
  `actor/src/layout/Drawer/index.tsx.hbs`. The code itself is unchanged.
- The `type DrawerTier` union stays, and it is what makes the tiers self-documenting in the
  generated output: `'unresolved' | 'compact' | 'medium' | 'wide'` names all four states at the
  point of use, so a reader of generated code sees the model without prose.
- Record the rationale here, in the section below, and in the `design.md` of
  `fix-drawer-breakpoint-tier-latch` (which already carries the tier derivation table, the
  unresolved-breakpoint analysis, and the verification harness).

## Tier rationale (kept out of the generated code)

The drawer's collapsed/expanded state is a function of the **breakpoint tier**, not of the
individual breakpoint transition. `miniDrawer` is one boolean with two meanings — "permanent rail
collapsed" at `md`/`lg`/`xl`, "temporary overlay closed" at `xs` — and the Header hamburger writes
the same flag, so a pair-by-pair transition table silently leaks a value written under one meaning
into a breakpoint that reads it under the other.

| tier | sizes | rule |
|---|---|---|
| `compact` | `xs`, `sm` | Collapsed baseline, re-asserted on every breakpoint change into or within the tier. No `ToggleButton` is rendered at `xs`/`sm`, so an expanded permanent drawer there would be unrecoverable. At `xs`, "collapsed" means the temporary overlay drawer is closed. This is the responsive baseline, **not** an absolute runtime state: the effect deps are `[size]`, so it only runs on a breakpoint change, and a hamburger tap at `xs` between changes stands. |
| `medium` | `md` | Collapsed on entry, toggleable. |
| `wide` | `lg`, `xl` | Expanded on entry, toggleable. |
| `unresolved` | `''` | Change nothing, and keep the last resolved breakpoint. |

Within `medium` and `wide` the user's toggle choice is preserved; crossing a tier boundary
re-applies the entered tier's default. `xs → sm` is *within* `compact`, not a boundary crossing —
the unconditional compact arm handles it.

`unresolved` is deliberately its own tier rather than a ternary fallthrough into `wide`.
`useMediaQuery` reports `false` for every breakpoint until it resolves, so `utilities/layout-helper`
leaves `size` as `''`. Classifying that as `wide` made an unresolved value apply the wide default
(6 cases where `miniDrawer` was mutated, e.g. `xs`(collapsed) `→ ''` silently expanded the drawer)
and overwrite `prevSizeRef` (12 cases where an unresolved hop changed the end state, e.g.
`xs → '' → md` ended expanded where `xs → md` ends collapsed). Returning early leaves the state
untouched and preserves the last resolved breakpoint, so the next resolved size still sees its own
tier transition.

`prevTier` is `unresolved` on the first resolved commit, which is what defers to the consuming
app's configured `miniDrawer` default at `md`/`lg`/`xl`.

The render-time guard is a separate mechanism and stays: effects run after render, so on the
crossing render `miniDrawer` still holds the stale value and the `keepMounted` MUI `Modal` would
mount open into the `exited`-stranded freeze race. The guard fixes the crossing render; the tier
rule fixes the state that settles.

## Capabilities

### Added Capabilities

- `code-generation` — **Generated code SHALL NOT carry design rationale as comments**: codify the
  convention this repo has now applied twice (`2026-08-02-fix-mobile-drawer-overlay-freeze` and
  this change) so it is an enforceable requirement rather than tribal knowledge, including the
  "prefer a named union type over a comment that enumerates states" guidance that `DrawerTier`
  demonstrates.

No behavioural requirement changes: the generated drawer behaves identically.

## Impact

- **Handlebars template**: `actor/src/layout/Drawer/index.tsx.hbs` — 14 comment lines removed, no
  executable line touched.
- **Generated apps**: every generated `src/layout/Drawer/index.tsx` loses 14 comment lines. No
  behavioural difference.
- **Snapshots**: none — `src/layout/Drawer/index.tsx` is in no itest snapshot set.
- **Risk**: minimal. Verified by regenerating a consuming app and diffing the generated file
  against the previous output to confirm comments are the only difference.
