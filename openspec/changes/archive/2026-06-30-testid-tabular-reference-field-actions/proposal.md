## Why

Audit finding F6 (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F6): inline action icon buttons rendered inside the `Tags` widget (the DOM realisation of a JUDO `TabularReferenceField`) and inside the `SingleRelationInput` widget carry NO `data-testid` attribute. Spec authors who want to address those buttons (Clear-all, Inline-create, Open-selector) fall back to title / icon-glyph locators, which are locale-sensitive (`title` is i18n'd) and fragile (icon glyphs change).

Render-graph audit (Explore agent, 2026-06-10) confirmed:

| Widget file | Lines | IconButtons missing `data-testid` |
|---|---|---|
| `actor/src/components/widgets/Tags.tsx.hbs` | 309-323 | Clear-all (`clearIcon`, glyph `󰠷`), Inline-create (`createDialogIcon`, glyph `󱪪`), Open-selector (`searchDialogIcon`, glyph `󰌷`) |
| `actor/src/components/widgets/SingleRelationInput.tsx.hbs` | 225-236 | The `buttonProps`-driven IconButtons (`set`, `view`, `create`) and the chevron dropdown toggle (line 240) |

In contrast, the same `Tags.tsx.hbs` already uses the pattern `data-testid={`${id}-<suffix>`}` at lines 200, 223, 239, 253, 263 (delete chip, open-dialog, view, download). The `id` prop is the parent TabularReferenceField's XMI ID, declared at `Tags.tsx.hbs:21` and destructured at line 58. The threading is already there — only three IconButton attributes are missing.

The view (open-page) button is NOT in scope here: it is rendered via the existing `data-testid={`${id}-view`}` (line 253) plus the catalogued `OpenPageAction` testid emitted at the per-row level. The catalogue side ("`fieldActions` leaves") is handled by a companion change in `judo-ui-e2e-template` (`expose-tabular-reference-field-actions-catalogue/`). This change is the DOM-side half of that Class II pair.

Audit estimates ~38 of ~75 sites convertible (~50%). The non-convertible half is documented as Risks (see design.md): `customImplementation` overrides, dynamically-gated buttons, non-standard glyph/label mappings, and the SingleRelationInput coverage gap addressed in part by Requirement 3 below.

## What Changes

Single Class II (template + companion catalogue) change. The DOM-side half (this proposal) touches two `.hbs` files. No Java helper change. No model change.

### (a) Emit `data-testid` on the three inline IconButtons in `Tags.tsx.hbs`

- Line 310 (clear-all): add `data-testid={`${id}-clear-all`}`
- Line 315 (inline-create): add `data-testid={`${id}-inline-create`}`
- Line 320 (open-selector): add `data-testid={`${id}-open-selector`}`

The `id` prop is already destructured at line 58 from `TagsProps<P, T>` (line 21) and is the parent TabularReferenceField's XMI ID propagated by `containers/components/tag/index.tsx.hbs:73` (`data-testid="{{ getXMIID table }}"` is set on the field container and the same XMI ID is passed as `id={...}` into `<Tags id=...>` per the tag container's prop wiring).

### (b) Emit `data-testid` on the `buttonProps`-driven IconButtons in `SingleRelationInput.tsx.hbs`

- Line 225-236 (the `.map((buttonProp) => <IconButton ...>)` rendering): add `data-testid={`${id}-${buttonProp.name}`}` so each rendered inline button receives `<id>-set`, `<id>-view`, or `<id>-create` as appropriate.
- Line 240 (chevron dropdown toggle): add `data-testid={`${id}-dropdown-toggle`}`. (`name` is already used in the className at the same line, and `id` is destructured at line 53.)

### What this change does NOT do

- Does NOT modify the catalogue. Companion catalogue change lives in `judo-ui-e2e-template/openspec/changes/expose-tabular-reference-field-actions-catalogue/`. Both halves must ship together for spec authors to consume the new leaves.
- Does NOT touch the `customImplementation` override path. When a field declares `customImplementation`, the entire `Tags` widget is replaced by a user-supplied `ComponentProxy` — those buttons are outside the generator's control. Documented as Non-Goal / Risk.
- Does NOT cover dynamically-gated buttons. A button gated on `actions.hasCapability(...)` or model-time presence of an action definition may not render even when the catalogue exposes a leaf for it. Spec authors handle missing-element gracefully — same precedent as `editActions` on non-editable tables (shipped F11 change).
- Does NOT change the existing view-button testid (`{{ table.relationName }}OpenPageAction` per-row XMI ID). The new `${id}-view` testid added by Requirement 3 covers the SingleRelationInput case only — there is no Tags-widget `${id}-view` introduced because Tags routes view via the per-row chip-level testid at line 253 (already present, `data-testid={`${id}-view`}` for the inline preview button) and via per-row action testids. The cohabitation is documented in design.md (D3).
- Does NOT change the `id` prop binding semantics. Pre-implementation tasks.md step 1.1 verifies the prop is bound to the parent field's XMI ID at the call sites; if that invariant is broken, the testid binding silently degrades to `undefined-clear-all` etc.
- Does NOT address F10 (already shipped: `dedupe-create-button-in-selector`), F11, F14, F1, F5, F3, F9, F15.

## Capabilities

### Added Capabilities

- **`tabular-reference-field-actions`** — new capability covering the DOM-side testid contract for inline action buttons rendered inside the `Tags` widget and the `SingleRelationInput` widget. Three ADDED Requirements (see `specs/tabular-reference-field-actions/spec.md`).

## Impact

- **`judo-ui-react/src/main/resources/actor/src/components/widgets/Tags.tsx.hbs`** (lines 310, 315, 320): three new `data-testid` JSX attributes. ~3 lines.
- **`judo-ui-react/src/main/resources/actor/src/components/widgets/SingleRelationInput.tsx.hbs`** (lines ~225 and ~240): two new `data-testid` JSX attributes. ~2 lines.
- **Generated TypeScript**: every Tags / SingleRelationInput widget now emits three additional DOM attributes. Pure additive change at the rendered DOM level; no behavioural change.
- **Integration test (`judo-ui-react-itest`)**: `./mvnw clean install` regenerates the `ActionGroupTest` fixture frontend. Existing Vitest / Playwright runs should pass unchanged because no existing locator depends on the absence of these attributes.
- **Downstream consumer (`BlackBeltTechnology/judo-tatami-tests`)**: after release + bump (and after the companion catalogue change ships), ~38 spec sites currently using title- or glyph-based locators on TabularReferenceField inline buttons can be converted to `getByTestId(<field>.fieldActions.clearAll.id)` etc. ~50% of the 75 audit-flagged sites; the remaining ~37 are blocked by the four convertibility caveats documented in design.md Risks.
