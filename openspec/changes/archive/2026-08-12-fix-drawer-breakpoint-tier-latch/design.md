# Design

## The shape is the bug

Before (the chain, with the `sm` cell added by `e5877709`):

```ts
if (prev && (prev == 'sm' || prev == 'md' || prev == 'lg' || prev == 'xl') && (size == 'sm' || size == 'xs')) {
  onChangeMiniDrawer(true);
} else if (prev && (prev == 'lg' || prev == 'xl') && size == 'md') {
  onChangeMiniDrawer(true);
} else if (prev && (prev == 'md' || prev == 'sm' || prev == 'xs') && (size == 'lg' || size == 'xl')) {
  onChangeMiniDrawer(false);
} else if (!prev && (size == 'sm' || size == 'xs')) {
  onChangeMiniDrawer(true);
}
```

Four branches enumerating `(prev, size)` pairs. There are 20 ordered pairs plus 5 initial
mounts. Whatever is not enumerated silently keeps the old `miniDrawer` — and because
`miniDrawer` means *"rail collapsed"* at `md`/`lg`/`xl` but *"overlay closed"* at `xs`, keeping
it across a boundary is exactly how a value written under one meaning is read under the other.

Relevant facts, all verified in source:

- `downSM` and `isXs` are the **same media query** in `utilities/layout-helper.tsx.hbs`
  (`breakpoints.down('sm')`, < 600 px). So the temporary overlay drawer branch (`{!downSM ? … : …}`)
  renders **only** at `size === 'xs'`; `sm` (600–899 px) is on the *permanent*-drawer side.
- The collapse `ToggleButton` is rendered only when `!isXs && !isSm` — so at `sm` there is
  **no way to collapse an expanded drawer**. `sm` with `miniDrawer === false` is a trap state.
- `Header/index.tsx.hbs` toggles the same flag: `onChangeMiniDrawer(!miniDrawer)`.
- `size` is `''` until the media queries resolve; the old code only survived that because
  `''` is falsy and happened to route into the `!prev` branch.

## Deriving the tiers

Read the 4 branches as *intent* rather than as pairs and a 3-tier structure falls out:

| tier | sizes | rule | why |
|---|---|---|---|
| `compact` | `xs`, `sm` | collapsed baseline, re-asserted on every change into or within the tier | no toggle is rendered, so any expanded state is unrecoverable; at `xs` "collapsed" = overlay closed |
| `medium` | `md` | collapsed on entry, toggleable | matches the old `lg`/`xl` → `md` branch |
| `wide` | `lg`, `xl` | expanded on entry, toggleable | matches the old `→ lg`/`xl` branch |
| `unresolved` | `''` | change nothing, keep the last resolved size | the media queries have not reported yet; see "Unresolved breakpoints" below |

Within the `medium` and `wide` tiers the user's choice is preserved — which is what the old
`lg ↔ xl` fall-through was deliberately doing. Note that `xs → sm` is *within* `compact`, not a
boundary crossing: it is the unconditional compact arm that handles it.

The compact rule is a **responsive baseline, not an absolute runtime state**. The effect deps are
`[size]`, so it only runs on a breakpoint *change*; a hamburger tap at `xs` between changes is
never clobbered, which is exactly what keeps the mobile drawer usable.

## The fix

```ts
type DrawerTier = 'unresolved' | 'compact' | 'medium' | 'wide';

const drawerTier = (size?: string): DrawerTier => {
  if (size === 'xs' || size === 'sm') return 'compact';
  if (size === 'md') return 'medium';
  if (size === 'lg' || size === 'xl') return 'wide';
  return 'unresolved';
};

useEffect(() => {
  const tier = drawerTier(size);
  if (tier === 'unresolved') {
    return;
  }
  const prevTier = drawerTier(prevSizeRef.current);
  prevSizeRef.current = size;
  if (tier === 'compact') {
    onChangeMiniDrawer(true);
  } else if (prevTier !== 'unresolved' && prevTier !== tier) {
    onChangeMiniDrawer(tier === 'medium');
  }
}, [size]);
```

`prevTier` is `unresolved` on the first resolved commit, which is what defers to the consuming
app's configured `miniDrawer` default at `md`/`lg`/`xl`. The `compact` arm is deliberately
unconditional — it must win even on the first commit, and re-asserting `true` at a size where no
toggle exists cannot fight a user action, because the effect only runs on a size change.

### Unresolved breakpoints

`useMediaQuery` returns `false` for every breakpoint until it resolves, so `layout-helper` leaves
`size` as `''`. A first cut folded that into `wide` via the ternary fallthrough and overwrote
`prevSizeRef` unconditionally. Both were wrong, and the harness below now proves it:

- an unresolved value **mutated** `miniDrawer` in 6 cases — e.g. `xs`(collapsed) `→ ''` classified
  `''` as `wide`, saw a tier change from `compact`, and silently expanded the drawer
- an unresolved **hop** changed the end state in 12 cases versus the direct transition — e.g.
  `xs → '' → md` ended expanded where `xs → md` ends collapsed, because `prevSizeRef` had been
  clobbered to `''` and the next resolved size was misread as a first mount

Giving `unresolved` its own tier and returning early fixes both: the state is untouched and the
last resolved breakpoint survives, so the next resolved size still sees its own tier transition.
The realistic mount path (`''` first, then resolve) is unchanged for all five sizes.

## The render guard still has to stay

`enteringMobile` / `temporaryDrawerOpen` from PR #593 are **not** made redundant by this. Effects
run after render, so on the `md → xs` crossing render `miniDrawer` is still the stale desktop
value; without the guard the `keepMounted` MUI `Modal` mounts `open` and is closed one tick
later, stranding its internal `exited` flag at `false` so the full-viewport root never gets
`visibility: hidden` — the click-trap freeze. Division of labour:

- **guard** → the drawer is closed *during the crossing render* (prevents the Modal race)
- **tier rule** → the drawer is closed *in the state that settles* (prevents the latch)

## Verification

No runtime test harness exists in this generator repo, and `src/layout/Drawer/index.tsx` is in no
itest snapshot set — so the transition space is checked by exhaustive simulation of the effect
plus the settled render outcome. Paste-runnable:

Each variant returns `[miniDrawer, prevSizeRef]`, because `prevSizeRef` is part of the state the
next transition reads — an earlier harness returned only `miniDrawer` and therefore could not see
the unresolved-hop corruption at all.

```js
const SIZES = ['xs', 'sm', 'md', 'lg', 'xl'];
const UNSET = ['', undefined];                 // size before useMediaQuery resolves
const ALL = [...SIZES, ...UNSET];              // unresolved is a *current* size too, not just a prev

// the pre-PR chain, i.e. the baseline this PR changes behaviour against.
// branch 1 lists only md/lg/xl as `prev`, so sm -> xs falls through and keeps mini as-is.
function chainPrePR(prev, size, mini) {
  if (prev && (prev=='md'||prev=='lg'||prev=='xl') && (size=='sm'||size=='xs')) return [true, size];
  if (prev && (prev=='lg'||prev=='xl') && size=='md') return [true, size];
  if (prev && (prev=='md'||prev=='sm'||prev=='xs') && (size=='lg'||size=='xl')) return [false, size];
  if (!prev && (size=='sm'||size=='xs')) return [true, size];
  return [mini, size];
}

// chain as of e5877709 — the pre-PR chain plus the sm -> xs reset that commit added
function chain(prev, size, mini) {
  if (prev && (prev=='sm'||prev=='md'||prev=='lg'||prev=='xl') && (size=='sm'||size=='xs')) return [true, size];
  if (prev && (prev=='lg'||prev=='xl') && size=='md') return [true, size];
  if (prev && (prev=='md'||prev=='sm'||prev=='xs') && (size=='lg'||size=='xl')) return [false, size];
  if (!prev && (size=='sm'||size=='xs')) return [true, size];
  return [mini, size];                         // fall-through: state carries over
}

// first cut of the tier rule (shipped in cc262dc1): unresolved fell through to 'wide'
// and prevSizeRef was overwritten unconditionally
const tierV1 = (s) => (s === 'xs' || s === 'sm') ? 'compact' : s === 'md' ? 'medium' : 'wide';
function tieredV1(prev, size, mini) {
  const t = tierV1(size);
  if (t === 'compact') return [true, size];
  if (prev && tierV1(prev) !== t) return [t === 'medium', size];
  return [mini, size];
}

// explicit unresolved tier - NOT a fallthrough into 'wide'
const tierOf = (s) =>
  (s === 'xs' || s === 'sm') ? 'compact'
  : s === 'md' ? 'medium'
  : (s === 'lg' || s === 'xl') ? 'wide'
  : 'unresolved';

function tiered(prev, size, mini) {
  const t = tierOf(size);
  if (t === 'unresolved') return [mini, prev];  // no state change, prevSizeRef preserved
  const pt = tierOf(prev);
  if (t === 'compact') return [true, size];
  if (pt !== 'unresolved' && pt !== t) return [t === 'medium', size];
  return [mini, size];
}

// settled outcome, i.e. after the effect committed prevSizeRef = size so the render guard released
const bad = (size, mini) =>
  size === 'xs' && !mini ? 'overlay + backdrop open, no user action'
  : size === 'sm' && !mini ? 'expanded at sm, no toggle rendered -> unrecoverable'
  : null;

for (const [name, fn] of [['chain    ', chain], ['tieredV1 ', tieredV1], ['tiered   ', tiered]]) {
  const cells = new Set(), mutated = [], diverged = [];

  // A. single-step bad end states, unresolved included on BOTH sides
  for (const prev of ALL) for (const size of ALL) {
    if (prev === size) continue;               // deps are [size]: only fires on change
    for (const mini of [true, false]) {
      const v = bad(size, fn(prev, size, mini)[0]);
      if (v) cells.add(`${String(prev) || 'unset'}(mini=${mini}) -> ${String(size) || "''"}: ${v}`);
    }
  }
  // B. an unresolved size must never mutate miniDrawer
  for (const prev of ALL) for (const u of UNSET) for (const mini of [true, false]) {
    const [m] = fn(prev, u, mini);
    if (m !== mini) mutated.push(`${String(prev) || 'unset'} -> ${JSON.stringify(u)}: ${mini} => ${m}`);
  }
  // C. an unresolved hop must not change the outcome of the surrounding transition
  for (const prev of SIZES) for (const next of SIZES) {
    if (prev === next) continue;
    for (const mini of [true, false]) for (const u of UNSET) {
      const [direct] = fn(prev, next, mini);
      const [viaM, viaP] = fn(prev, u, mini);
      const [hop] = fn(viaP, next, viaM);
      if (direct !== hop) diverged.push(`${prev}(mini=${mini}) -> ${JSON.stringify(u)} -> ${next}: direct=${direct} hop=${hop}`);
    }
  }
  console.log(`${name}: ${cells.size} bad cells, ${mutated.length} unresolved mutations, ${diverged.length} unresolved-hop divergences`);
  [...cells].forEach(c => console.log('   cell      ' + c));
  [...new Set(mutated)].forEach(m => console.log('   mutation  ' + m));
  [...new Set(diverged)].forEach(d => console.log('   divergence ' + d));
}
```

Result:

| variant | bad cells | unresolved mutations | unresolved-hop divergences |
|---|---|---|---|
| `chain` (as of `e5877709`) | **1** — `xs(mini=false) → sm` | 0 | 18 |
| `tieredV1` (as of `cc262dc1`) | 0 | **6** | **12** |
| `tiered` (current) | 0 | 0 | 0 |

`chain`'s single bad cell is the one the pair table never enumerated; `sm → xs` was already
closed by `e5877709`, and before that commit the same harness reported 2. Its 18 divergences come
from clobbering `prevSizeRef` with `''` — it never *mutated* `miniDrawer` on an unresolved size
because none of its branches match a `size` outside the five names.

`tieredV1` closed every bad cell but classified unresolved as `wide`, which is where the 6
mutations come from; it inherited the `prevSizeRef` clobbering, hence the 12 remaining
divergences. Only the explicit `unresolved` tier reaches 0/0/0.

Diffing `tiered` against `chainPrePR` — the pre-PR baseline, *not* the `chain` variant above —
over the resolved sizes yields exactly 4 deviations, all at `mini=false`: `xs→sm`, `xs→md`,
`sm→xs`, `sm→md` — precisely the cells the chain never enumerated. Against `chain` (as of
`e5877709`) the same diff yields 3, because `e5877709` had already closed `sm→xs` itself; that
commit is part of this PR, so the 4-cell count is the one that describes the PR as a whole. No
cell the chain deliberately handled changes behaviour, so `lg ↔ xl`
still preserves a manual collapse and initial mount at `md`/`lg`/`xl` still honours the
configured default.

Also verified end to end: `mvn install -pl judo-ui-react`, then regenerating
`judo-ui-react-itest/RelationTest/relation_test__actor` — Biome formatting, the snapshot
diff-checker, and the Vite build all pass, Biome reformats nothing (the generated
`target/frontend-react/src/layout/Drawer/index.tsx` matches the template verbatim), and the
generated file carries the tier rule including the `unresolved` arm.

## Alternatives considered

- **Keep patching cells.** Add `xs` to the first branch's `prev` list. Closes the one open
  defect; leaves the pair table, the two `→ md` inconsistencies, and the next forgotten cell.
  Rejected — this is the third patch to the same construct.
- **Split the boolean** (a dedicated overlay-open state independent of the desktop mini flag)
  and rewire the Header hamburger. Genuinely correct separation of concerns and it removes the
  dual meaning at the root, but it touches `Drawer` + `Header` + config and changes toggle
  semantics. Deferred: the tier rule removes every currently reachable broken state within one
  file, so the larger refactor no longer has a defect forcing it.
