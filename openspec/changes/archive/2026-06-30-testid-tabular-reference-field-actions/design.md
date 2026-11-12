## Context

Audit finding F6 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F6): the `Tags` widget — the DOM realisation of a JUDO `TabularReferenceField` — renders three inline action `IconButton`s (Clear-all, Inline-create, Open-selector) without any `data-testid`. Spec authors fall back to title-based or icon-glyph-based locators, both fragile (titles are i18n'd; glyphs are private-area unicode that change with MDI bumps). The `SingleRelationInput` widget exhibits the same gap on its `set` / `view` / `create` `buttonProps`-driven IconButtons and its chevron dropdown toggle.

Audit (Explore agent, 2026-06-10) estimates ~38/~75 sites convertible (~50%). The remaining ~37 are blocked by four orthogonal reasons that this DOM fix cannot resolve in isolation (see Risks).

## Render chain (file:line)

1. **Field container** — `actor/src/containers/components/tag/index.tsx.hbs:73` emits `<Box data-testid="{{ getXMIID table }}">` and instantiates `<Tags id={...} ... />`. The `id` prop carries the parent TabularReferenceField's XMI ID.
2. **Action wiring** — `actor/src/containers/components/tag/index.tsx.hbs:73-121` maps EMF action definitions to handler props:
   - line 92: `onSearchDialogsClick` ← `OpenAddSelectorAction`
   - line 118: `onItemClick` ← `OpenPageAction`
   - line 119: `onCreateDialogsClick` ← `OpenCreateFormAction`
   - line 121: `onClearDialogsClick` ← `ClearAction`
3. **Tags component prop interface** — `actor/src/components/widgets/Tags.tsx.hbs:20-44` declares `TagsProps<P, T>` with `id: string` at line 21 and the four handler props above. `id` is destructured at line 58.
4. **Existing testid usage in Tags** — `actor/src/components/widgets/Tags.tsx.hbs` already uses the `${id}-<suffix>` pattern at five sites: 200 (root), 223 (delete chip), 239 (open-dialog), 253 (view), 263 (download). The pattern is established and idiomatic.
5. **The three gaps** — `actor/src/components/widgets/Tags.tsx.hbs:309-323`:
   - 309-313: Clear-all `IconButton`, gated on `!readOnly && onClearDialogsClick`
   - 314-318: Inline-create `IconButton`, gated on `!readOnly && onCreateDialogsClick`
   - 319-323: Open-selector `IconButton`, gated on `!readOnly && onSearchDialogsClick`
6. **SingleRelationInput gap** — `actor/src/components/widgets/SingleRelationInput.tsx.hbs:30` declares `id: string`, destructured at line 53. Lines 225-236 render IconButtons from a `buttonProps` array filtered by `a.name === 'set' || a.name === 'view' || a.name === 'create'`. The IconButton has no `data-testid`. Line 240 has a chevron dropdown toggle, also no `data-testid`.

## Goals

1. Spec authors can address each Tags inline IconButton via `getByTestId(`${fieldXmiId}-clear-all`)` / `-inline-create` / `-open-selector` without falling back to title or glyph locators.
2. Spec authors can address each SingleRelationInput inline IconButton via `getByTestId(`${fieldXmiId}-set`)` / `-view` / `-create` / `-dropdown-toggle`.
3. The catalogue side (companion change in `judo-ui-e2e-template`) can expose `fieldActions.clearAll` / `.inlineCreate` / `.openSelector` leaves under each TabularReferenceField and analogous leaves under each SingleRelationInput consumer.
4. Implementation surface stays under 6 lines across 2 files. No Java helper change.
5. No conflict with the existing `${id}-view` testid in Tags (line 253, on the per-row preview/view button inside the chip render). The new testids use distinct suffixes (`clear-all`, `inline-create`, `open-selector`).

## Non-Goals

1. NOT touching the `customImplementation` override path. When a TabularReferenceField has `customImplementation` set, the entire `Tags` widget is replaced by a user-supplied `ComponentProxy`. The user-supplied component is outside the generator's control; the catalogue must omit leaves for such fields (handled in the companion catalogue change's `VisualElementHelper` filter).
2. NOT extending the catalogue. The catalogue side is a separate Class II companion change.
3. NOT renaming or restructuring the existing `${id}-view` testid in Tags (line 253). Cohabitation is documented in D3.
4. NOT touching the per-row chip render (lines ~220-265). Those already have testids.
5. NOT introducing a new EMF model attribute. The four action suffixes (`clear-all`, `inline-create`, `open-selector`, plus SingleRelationInput's `set` / `view` / `create` / `dropdown-toggle`) are intrinsic to the widget shape, not a per-instance user-configurable flag.

## Decisions

### D1 — Use literal kebab-case suffixes (`clear-all`, `inline-create`, `open-selector`) rather than action-definition names

**Decision**: Hardcode the four suffixes in the `.hbs` JSX. Each IconButton in `Tags.tsx.hbs` already KNOWS what action it represents (the handler-prop check `onClearDialogsClick` vs `onCreateDialogsClick` vs `onSearchDialogsClick` is the discriminator), so the suffix is a fixed string per render branch.

**Alternatives considered**:
- Pass the suffix as a new prop from the container. Overkill — the discriminator is already the handler prop, which is one-to-one with the action class.
- Use the action-definition class name (`OpenAddSelectorAction`, etc.) as the suffix. Couples the DOM contract to a Java class name, which can change. Kebab-case suffixes are stable user-facing tokens.

### D2 — Compose the testid as `${id}-${suffix}` to match the existing pattern in the same file

**Decision**: Use template literals `data-testid={`${id}-clear-all`}` etc., mirroring lines 223, 239, 253, 263 in the same file. Established idiom.

### D3 — Cohabitation with the existing `${id}-view` testid in Tags

**Decision**: The new Tags testids (`clear-all`, `inline-create`, `open-selector`) do NOT introduce a `${id}-view`. The existing line 253 already has `data-testid={`${id}-view`}` on the per-row chip preview button. Adding a `-view` to the inline button group would create a duplicate testid violation under Playwright strict mode.

The view action on a TabularReferenceField is conceptually rendered per-row (you view a specific row), not as a single inline button — the inline button group only has Clear-all / Inline-create / Open-selector. The catalogue leaves on the companion change reflect this: `fieldActions.view` does NOT exist; the view leaf is per-row under the row-level catalogue (out of this change's scope).

For SingleRelationInput, the situation is different: the inline `view` button (one of three `buttonProps`) refers to viewing THE single referenced row, not per-row, so `${id}-view` IS appropriate there and does NOT collide (different widget, different DOM tree).

### D4 — SingleRelationInput uses `${id}-${buttonProp.name}` (computed) rather than three branched literals

**Decision**: The render is already a `.map(...)` over the `buttonProps` array. The cleanest one-line addition is `data-testid={`${id}-${buttonProp.name}`}` on the `<IconButton>` inside the map. `buttonProp.name` is already one of the string literals `'set'`, `'view'`, `'create'` per the filter on line 222.

**Alternatives considered**:
- Hardcoded literals via `switch` or per-name `?:` in the JSX. Heavier diff, no benefit — `buttonProp.name` is already typed to a finite string union.

### D5 — Defer SingleRelationInput catalogue coverage

**Decision**: The DOM testid is emitted here, but the companion catalogue change does NOT (initially) expose SingleRelationInput action leaves. The audit's 50% estimate is partly bounded by the catalogue's TabularReferenceField focus; expanding the catalogue to single-relation widgets is a follow-up. The DOM attribute being present is harmless — spec authors who need it can use it directly while waiting for the catalogue.

## Risks

| Risk | Mitigation |
|---|---|
| `customImplementation`: when a TabularReferenceField has `customImplementation` set, the `Tags` widget is replaced by a `ComponentProxy` — testids are NOT emitted. | Documented as Non-Goal #1. The companion catalogue change filters out `customImplementation` fields from the `fieldActions` leaves. Spec authors of customised fields use their own DOM contract. |
| Dynamically-gated buttons: a button gated on `actions.hasCapability(...)` or on model-time presence of an action definition may NOT render even when its handler prop is wired. The new `data-testid` only appears when the button renders. | Spec authors handle missing-element gracefully (same precedent as `editActions` on non-editable tables, F11 change). The catalogue's leaf identifies the intended testid; presence is best-effort. |
| Coverage gap: SingleRelationInput is NOT a TabularReferenceField but exhibits the same DOM testid gap. This change fixes both, but the audit's 75 site count was Tags-focused. | Requirement 3 explicitly covers SingleRelationInput. Catalogue side is deferred (D5). |
| Non-standard glyph/label mappings: some audit sites locate buttons by a glyph the JUDO model has aliased to a different action (e.g. a custom icon for an OpenSelectorAction). Those sites match by glyph, not by action semantics; switching to testid by action semantics may change the locator's binding. | Reviewer of each downstream spec conversion verifies the new testid maps to the intended button. Out of scope for this generator change. |
| The `id` prop on `Tags` is normally the parent TabularReferenceField's XMI ID, but if any non-standard call site passes a different value (e.g. a sub-identifier), the new testids inherit that and may be unstable. | Tasks step 1.1 verifies all `<Tags id={...}>` call sites pass the field's XMI ID. Pre-implementation grep + manual inspection. |
| Future audit findings may want to use suffixes that collide (e.g. `-view` on the Tags inline group). | D3 explicitly reserves `-clear-all` / `-inline-create` / `-open-selector` and documents the rationale for NOT introducing `-view` on the Tags inline group. |
| Testid duplication if a page contains the same TabularReferenceField twice (rare but possible in nested layouts). | Same invariant as for the existing `${id}-delete` / `-view` etc. testids in the same file — pre-existing, not regressed. |

## Implementation sketch

**`Tags.tsx.hbs` (lines 309-323):**

```jsx
  {!readOnly && onClearDialogsClick ? (
-   <IconButton disabled={disabled} onClick={onClearDialogsClick} title={clearTitle}>
+   <IconButton data-testid={`${id}-clear-all`} disabled={disabled} onClick={onClearDialogsClick} title={clearTitle}>
      <MdiIcon path={clearIcon} />
    </IconButton>
  ) : null}
  {!readOnly && onCreateDialogsClick ? (
-   <IconButton disabled={disabled} onClick={onCreateDialogsClick} title={createDialogTitle}>
+   <IconButton data-testid={`${id}-inline-create`} disabled={disabled} onClick={onCreateDialogsClick} title={createDialogTitle}>
      <MdiIcon path={createDialogIcon} />
    </IconButton>
  ) : null}
  {!readOnly && onSearchDialogsClick ? (
-   <IconButton disabled={disabled} onClick={onSearchDialogsClick} title={searchDialogTitle}>
+   <IconButton data-testid={`${id}-open-selector`} disabled={disabled} onClick={onSearchDialogsClick} title={searchDialogTitle}>
      <MdiIcon path={searchDialogIcon} />
    </IconButton>
  ) : null}
```

**`SingleRelationInput.tsx.hbs` (lines 225-236, 240):**

```jsx
  .map((buttonProp) => (
    <IconButton
+     data-testid={`${id}-${buttonProp.name}`}
      className={buttonProp.className}
      ...
    >
      <MdiIcon path={buttonProp.iconName} />
    </IconButton>
  ))
  ...
- <IconButton className={`${name}-dropdown`} disabled={disabled} onClick={handleDropdownToggle}>
+ <IconButton data-testid={`${id}-dropdown-toggle`} className={`${name}-dropdown`} disabled={disabled} onClick={handleDropdownToggle}>
```
