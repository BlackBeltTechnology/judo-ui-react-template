## Why

The generated app's left-hand navigation drawer renders TWO semantically distinct kinds of items through the SAME `<ListItemButton>` element in `NavItem.tsx.hbs`:

| Audit finding | Convs | Item flavour | Distinguisher | Catalogue leaf (companion change) |
|---|---|---|---|---|
| F1 | 155 | Sidebar page navigation | `item.target` is a `PageDefinition` (route link) | `Sidebar.navItems.<id>` |
| F5 | 95 | Actor-menu operation button | `item.actionDefinition` is set (calls into the actor) | `ActorMenu.operations.<id>` |

Both are emitted by `judo-ui-react/src/main/resources/actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs` — once for the vertical menu (line 100) and once for the horizontal menu (line 131). Both call sites render `<ListItemButton component={LinkComponent} ...>` with NO `data-testid` attribute. As a result, every Playwright spec that needs to click "navigate to the X page" OR "trigger actor operation Y" is forced onto label-based locators (visible text / `aria-label`), which break under i18n, label edits, and ambiguity between same-labelled items in nested groups (audit `/home/balazs/Asztal/prompts/reports/canonize-e2e-locators-to-testid.md` §F1, §F5).

A stable, XMI-derived `id` is ALREADY computed for each navigation item at generation time. `actor/src/layout/Drawer/DrawerContent/Navigation/menu-items-recurse.fragment.hbs:2` emits `id: '{{ createId item }}'` for every item (including nested children of group items), where `createId` returns `getXMIID(element).replaceAll("@", "")` (cf. `UiGeneralHelper.java:65`). The id is therefore already in scope on `item.id` at the render site — no model change, no Java helper change, and no new template plumbing is needed to surface it as a `data-testid`.

A SINGLE two-line `data-testid={item.id}` addition (one per call site in `NavItem.tsx.hbs`) lights up BOTH audit findings simultaneously, because the same component is reused for the two flavours.

## What Changes

Single Class III (react-template-only) change per the upstream-testid-fix workflow playbook. Touches one file, two lines. No model change, no Java helper change, no extra .hbs file. The catalogue side ships separately in the e2e-template repo (`openspec/changes/expose-sidebar-and-actor-menu-catalogue/`) where new `Sidebar.navItems` (F1) and `ActorMenu.operations` (F5) top-level exports are added.

### Add `data-testid={item.id}` to both `<ListItemButton>` call sites in `NavItem.tsx.hbs`

- Vertical menu (line ~100): `<ListItemButton component={LinkComponent} ... data-testid={item.id}>`
- Horizontal menu (line ~131): `<ListItemButton component={LinkComponent} ... data-testid={item.id}>`

Because `menu-items-recurse.fragment.hbs` walks `application.navigationController.items` recursively (top-level entries AND children of group items), this single change yields a unique stable `data-testid` for every nav-page item, every operation item, and every nested group entry — at every level of the tree.

### What this change does NOT do

- Does **not** modify the EMF UI model or any generator Java helper. The id is already on the item via `createId`; only the DOM attribute is added.
- Does **not** add testids to the header chrome (logo, locale picker, theme toggle, user-profile avatar, profile dropdown, profile submenu). Those render through DIFFERENT `.hbs` files and are already covered by the shipped F2/F4/F8 catalogue under `PlatformTestIds.applicationChrome.profile` / `.submenu`. Touching them here would risk double-emission and naming collisions.
- Does **not** touch the catalogue. The e2e-template companion change `expose-sidebar-and-actor-menu-catalogue` is responsible for declaring `Sidebar.navItems.<id>` (F1) and `ActorMenu.operations.<id>` (F5) leaves whose `.id` values match the DOM testids emitted here.
- Does **not** change behaviour for items that fail visibility (`hiddenBy`) — those continue to be filtered earlier in the render tree; their testids only appear in the DOM when the item is actually rendered.
- Does **not** introduce a separate testid for the icon vs label sub-elements inside the button. The button is the click target; one testid on the button is sufficient for Playwright `click()` / `getByTestId(...)`.
- Does **not** address F2/F3/F4/F8/F9/F10/F11/F14/F15 (separate clusters or already shipped).

## Capabilities

### New Capabilities

- **`sidebar-and-actor-menu-testids`** — Declares that every `<ListItemButton>` rendered by the navigation drawer SHALL carry `data-testid={item.id}` where `item.id` is the navigation item's XMI-derived stable identifier. Covers the recursive case (groups → children → grandchildren), the nav-page flavour (`item.target`), and the operation flavour (`item.actionDefinition`).

## Impact

- **`judo-ui-react/src/main/resources/actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs`** — two new `data-testid={item.id}` attributes on the two `<ListItemButton>` call sites (line ~100 vertical, line ~131 horizontal). ~2 lines.
- **Generated TypeScript**: every rendered drawer button now emits `data-testid="<XMI id with @ stripped>"`. Existing label-based and `aria-label`-based locators continue to work unchanged.
- **Integration test (`judo-ui-react-itest`)**: `./mvnw clean install` regenerates and runs Vitest/Playwright on the fixture frontends. The change is purely additive on the DOM (one new attribute on an element that already exists) so no existing test should break.
- **Downstream consumer (`BlackBeltTechnology/judo-tatami-tests`)**: after the React-template release AND the e2e-template catalogue companion release land, the ~155 (F1) + ~95 (F5) audit conversion sites become eligible for label → `getByTestId(Sidebar.navItems.<id>.id)` / `getByTestId(ActorMenu.operations.<id>.id)` conversions.
- **Cross-link**: companion catalogue change lives at `judo-ui-e2e-template/openspec/changes/expose-sidebar-and-actor-menu-catalogue/`.
