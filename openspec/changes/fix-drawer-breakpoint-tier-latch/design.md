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
| `compact` | `xs`, `sm` | always collapsed | no toggle is rendered, so any expanded state is unrecoverable; at `xs` "collapsed" = overlay closed |
| `medium` | `md` | collapsed on entry, toggleable | matches the old `lg`/`xl` → `md` branch |
| `wide` | `lg`, `xl` | expanded on entry, toggleable | matches the old `→ lg`/`xl` branch |

Within a tier the user's choice is preserved — which is what the old `lg ↔ xl` fall-through was
deliberately doing.

## The fix

```ts
const drawerTier = (size?: string) =>
  size === 'xs' || size === 'sm' ? 'compact' : size === 'md' ? 'medium' : 'wide';

useEffect(() => {
  const prevSize = prevSizeRef.current;
  prevSizeRef.current = size;
  if (drawerTier(size) === 'compact') {
    onChangeMiniDrawer(true);
  } else if (prevSize && drawerTier(prevSize) !== drawerTier(size)) {
    onChangeMiniDrawer(drawerTier(size) === 'medium');
  }
}, [size]);
```

`prevSize` is falsy on the first commit and while `size === ''`, which is what defers to the
consuming app's configured `miniDrawer` default at `md`/`lg`/`xl`. The `compact` arm is
deliberately unconditional — it must win even on the first commit, and re-asserting `true` at a
size where no toggle exists can never fight a user action.

The effect deps are `[size]`, so it only runs on a breakpoint *change*; a hamburger tap inside a
tier is never clobbered.

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

```js
const SIZES = ['xs', 'sm', 'md', 'lg', 'xl'];
const UNSET = ['', undefined];                 // size before useMediaQuery resolves

// chain as of e5877709
function chain(prev, size, mini) {
  if (prev && (prev=='sm'||prev=='md'||prev=='lg'||prev=='xl') && (size=='sm'||size=='xs')) return true;
  if (prev && (prev=='lg'||prev=='xl') && size=='md') return true;
  if (prev && (prev=='md'||prev=='sm'||prev=='xs') && (size=='lg'||size=='xl')) return false;
  if (!prev && (size=='sm'||size=='xs')) return true;
  return mini;                                 // fall-through: state carries over
}

const tierOf = (s) => (s === 'xs' || s === 'sm') ? 'compact' : s === 'md' ? 'medium' : 'wide';
function tiered(prev, size, mini) {
  if (tierOf(size) === 'compact') return true;
  if (prev && tierOf(prev) !== tierOf(size)) return tierOf(size) === 'medium';
  return mini;
}

// settled outcome, i.e. after the effect committed prevSizeRef = size so the render guard released
const bad = (size, mini) =>
  size === 'xs' && !mini ? 'overlay + backdrop open, no user action'
  : size === 'sm' && !mini ? 'expanded at sm, no toggle rendered -> unrecoverable'
  : null;

for (const [name, fn] of [['chain', chain], ['tiered', tiered]]) {
  const found = new Set();
  for (const prev of [...SIZES, ...UNSET]) for (const size of SIZES) {
    if (prev === size) continue;               // deps are [size]: only fires on change
    for (const mini of [true, false]) {
      const v = bad(size, fn(prev, size, mini));
      if (v) found.add(`${String(prev) || 'unset'}(mini=${mini}) -> ${size}: ${v}`);
    }
  }
  console.log(`${name}: ${found.size} bad cells`, [...found]);
}
```

Result:

- `chain` — **1 bad cell**: `xs(mini=false) → sm` (`sm → xs` was already closed by `e5877709`;
  before that commit the same harness reported 2)
- `tiered` — **0 bad cells**

Diffing `tiered` against the original pre-PR chain over the same space yields exactly 4
deviations, all at `mini=false`: `xs→sm`, `xs→md`, `sm→xs`, `sm→md` — precisely the cells the
chain never enumerated. No cell the chain deliberately handled changes behaviour, so `lg ↔ xl`
still preserves a manual collapse and initial mount at `md`/`lg`/`xl` still honours the
configured default.

Also verified end to end: `mvn install -pl judo-ui-react`, then regenerating
`judo-ui-react-itest/RelationTest/relation_test__actor` — Biome formatting, the snapshot
diff-checker, and the Vite build all pass, and the generated
`target/frontend-react/src/layout/Drawer/index.tsx` carries the tier rule.

## Alternatives considered

- **Keep patching cells.** Add `xs` to the first branch's `prev` list. Closes the one open
  defect; leaves the pair table, the two `→ md` inconsistencies, and the next forgotten cell.
  Rejected — this is the third patch to the same construct.
- **Split the boolean** (a dedicated overlay-open state independent of the desktop mini flag)
  and rewire the Header hamburger. Genuinely correct separation of concerns and it removes the
  dual meaning at the root, but it touches `Drawer` + `Header` + config and changes toggle
  semantics. Deferred: the tier rule removes every currently reachable broken state within one
  file, so the larger refactor no longer has a defect forcing it.
