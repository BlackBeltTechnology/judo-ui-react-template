## Context

Audit findings F1 (sidebar navigation, ~155 conv sites) and F5 (actor-menu operations, ~95 conv sites) both originate from the same DOM emission gap: the `<ListItemButton>` rendered by `NavItem.tsx.hbs` carries no `data-testid`. The audit (`/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F1, §F5) classified them separately because the catalogue-side leaf names differ (`Sidebar.navItems` vs `ActorMenu.operations`), but the underlying generator gap is single — and so is the fix.

Render-graph audit (Explore agent, 2026-06-10) confirmed:

- The drawer is rendered by `judo-ui-react/src/main/resources/actor/src/layout/Drawer/DrawerContent/Navigation/`.
- `menu-items-recurse.fragment.hbs:2` emits a flat-ish JS object per navigation item, recursing into `item.items` for group entries. Each emitted object already carries `id: '{{ createId item }}'`.
- `createId` is defined in `judo-ui-react/src/main/java/.../UiGeneralHelper.java:65` and returns `getXMIID(element).replaceAll("@", "")` — a deterministic, stable, model-derived identifier. The exact same routine is used as the source of testid leaves throughout the rest of the catalogue (containers, buttons, table cells, etc.).
- `NavItem.tsx.hbs` consumes the per-item object and renders TWO branches: one for the vertical menu (line 100) and one for the horizontal menu (line 131). Both render `<ListItemButton component={LinkComponent} ...>`. Neither currently sets `data-testid`.
- The `item.target` vs `item.actionDefinition` distinguisher decides which click handler is wired up (router push vs actor action) but does NOT change which element is the click target. Both flavours land on the same `<ListItemButton>`.

## Source mapping table

| File | Line | Role | Change |
|---|---|---|---|
| `actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs` | ~100 | Vertical drawer `<ListItemButton>` | + `data-testid={item.id}` |
| `actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs` | ~131 | Horizontal drawer `<ListItemButton>` | + `data-testid={item.id}` |
| `actor/src/layout/Drawer/DrawerContent/Navigation/menu-items-recurse.fragment.hbs` | 2 | Source of `item.id` (already emitted) | unchanged |
| `judo-ui-react/src/main/java/.../UiGeneralHelper.java` | 65 | `createId` Java helper | unchanged |
| `judo-ui-e2e-template/.../expose-sidebar-and-actor-menu-catalogue/` | — | Companion catalogue leaves | tracked separately |

## Goals

1. Every drawer button — page-nav (F1), operation (F5), and any nested group child — exposes a stable, XMI-derived `data-testid` for Playwright `getByTestId(...)`.
2. The testid value matches the catalogue's leaf `.id` exactly (both use `createId(item)`), so `Sidebar.navItems.<...>.id` and `ActorMenu.operations.<...>.id` resolve to the same DOM `data-testid` on the same element.
3. Implementation surface stays under 5 lines in a single file. No Java helper change, no model change, no extra fragment.
4. Existing label-based / `aria-label`-based locators continue to work unchanged — the change is purely additive on the DOM.

## Non-Goals

1. NOT adding testids to header chrome elements (logo, locale picker, theme toggle, profile avatar, profile menu, submenu). Those render through different `.hbs` files and are already covered by the shipped F2/F4/F8 catalogue under `PlatformTestIds.applicationChrome.profile` / `.submenu`. Adding testids there now would risk duplicate emission or collision with the platform catalogue.
2. NOT introducing per-sub-element testids (separate id on the icon, separate id on the label `<Typography>`). The button is the click target; one testid on the button is sufficient.
3. NOT renaming `createId` or the `id` property emitted by `menu-items-recurse.fragment.hbs`. The existing name is canonical across the catalogue.
4. NOT splitting the F1 and F5 fixes into two separate changes. They share the same DOM element and the same single-line fix; splitting would double the review surface for zero gain.
5. NOT touching the catalogue side. The e2e-template change `expose-sidebar-and-actor-menu-catalogue` ships the corresponding `Sidebar.navItems` (F1) and `ActorMenu.operations` (F5) top-level exports under separate review.

## Decisions

### D1 — One DOM attribute on the `<ListItemButton>`, both call sites

**Decision**: Add `data-testid={item.id}` to the `<ListItemButton>` at line ~100 (vertical) AND at line ~131 (horizontal) of `NavItem.tsx.hbs`. Both must be touched because they are independent JSX call sites — the file has two render branches, not one.

**Alternatives considered**:
- Wrap the button in a `<span data-testid={...}>` (catalogue-style "anchor wrapper"). Heavier DOM, no Playwright benefit (`getByTestId(...).click()` works equally well on the button directly), and inconsistent with how the rest of this codebase emits testids (always on the click target itself — cf. `TableCellRenderer.hbs`, `dialog.tsx.hbs`).
- Add `data-testid` only to the vertical branch and assume the horizontal branch is unused. False — the horizontal branch is active in landscape-oriented actor layouts; both must be covered.

### D2 — Use `item.id` directly, not a derived expression

**Decision**: Emit `data-testid={item.id}` verbatim. `item.id` is already set by `menu-items-recurse.fragment.hbs:2` via `createId item`, which strips `@` from the XMI id. No transformation needed at the consumer site.

**Alternatives considered**:
- Recompute the id at the consumer site (e.g. `data-testid={createId item}` in Handlebars). Wrong scope — `createId` is a Handlebars helper that runs at generator time on the model element, not on the runtime JS `item` object. The runtime `item.id` IS the post-`createId` string already.
- Compose a prefixed testid like `data-testid="navItem/{item.id}"`. Rejected — the catalogue resolves leaf `.id` to the raw `createId` output; adding a prefix here would force the catalogue to mirror the prefix and double the surface area of the change.

### D3 — Class III: react-template-only, no model, no Java

**Decision**: The fix lives entirely in one `.hbs` file. The id source (`createId` Java helper + `menu-items-recurse.fragment.hbs` emission of `id:`) is already in place from earlier work. No new generator-side plumbing is required.

**Alternatives considered**:
- Introduce a new Java helper (`navItemTestId(NavigationItem)`). Pure overhead — `item.id` is the value that helper would return.
- Add a per-item `testId` attribute in the EMF UI model. Massive overkill for a DOM-attribute emission gap, and would require co-evolving metamodel + tatami transformations.

### D4 — Recursion is inherited from the existing emission

**Decision**: No explicit recursion in the fix. The `<NavItem>` component is rendered for every entry produced by `menu-items-recurse.fragment.hbs`, which already walks `application.navigationController.items` recursively. Every emitted item — top-level and nested — passes through the same `<ListItemButton>` and therefore inherits the new `data-testid`.

### D5 — Defer collapsed-drawer / tooltip wrapper concerns

**Decision**: When the drawer is collapsed, MUI may wrap the `<ListItemButton>` in a `<Tooltip>`. The new `data-testid` stays on the button, NOT on the tooltip wrapper. Playwright `getByTestId(...)` matches the button regardless of whether a tooltip wraps it — verified empirically by the F2/F4/F8 work on profile-menu items.

## Risks

| Risk | Mitigation |
|---|---|
| The audit's F1 catalogue proposal (`Sidebar.navItems.<entity>`) implies the leaf might be keyed by entity rather than by navigation-item XMI id. | The audit is descriptive, not prescriptive on key shape. The catalogue companion change keys leaves by `createId(item)` to match what this change emits. Both sides use the same source of truth. |
| Header chrome (locale picker, theme toggle, profile avatar) might get accidentally testid-decorated because of templating overlap. | Those elements live in completely different `.hbs` files (`actor/src/layout/Header/...`). `NavItem.tsx.hbs` is the drawer-list component only; touching it cannot affect header chrome. |
| Existing specs that locate the drawer button by `getByRole('button', { name: '<label>' })` may regress if MUI surfaces the `data-testid` in a way that changes the accessible name. | `data-testid` is a non-ARIA attribute and does NOT contribute to the accessible name. Verified by the F10 dedupe work on table toolbar buttons (same pattern, no a11y regression). |
| Nested group items (parents whose `.items` is non-empty) might collide with a child's testid if the model accidentally reused an XMI id. | XMI ids are globally unique per the EMF spec; `createId` only strips the `@` delimiter and does NOT collapse separate ids. A collision would require a malformed model — the model validator would catch that earlier. |
| `data-testid` strings derived from XMI ids contain `/` and `:` characters. Playwright `getByTestId` handles arbitrary strings, but CSS-selector workarounds (`[data-testid="..."]`) need quoting. | Specs SHOULD use `page.getByTestId(...)` and resolve the value via the catalogue (`Sidebar.navItems.<id>.id` / `ActorMenu.operations.<id>.id`), never as raw CSS. Documented in the catalogue companion change. |

## Implementation sketch

**`actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs`** — both call sites:

```jsx
- <ListItemButton component={LinkComponent} {...listItemButtonProps}>
+ <ListItemButton component={LinkComponent} {...listItemButtonProps} data-testid={item.id}>
```

Apply at line ~100 (vertical menu branch) and line ~131 (horizontal menu branch). The exact surrounding attributes differ slightly between the two branches, but the addition is identical.
