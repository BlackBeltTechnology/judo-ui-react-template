## Definition of Done

- `./mvnw clean install` exits green; the React-template's itest regenerates fixture frontends and runs them under Vitest/Playwright without new failures.
- Regenerated `NavItem.tsx` (under `judo-ui-react-itest/.../target/frontend-react/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx`) contains `data-testid={item.id}` on BOTH `<ListItemButton>` call sites (vertical and horizontal menu branches).
- A `grep -c 'data-testid={item.id}'` over the regenerated `NavItem.tsx` returns exactly `2`.
- Inspecting any rendered drawer page in the itest fixture app shows every `<button>` produced by the drawer list carrying a `data-testid` attribute whose value matches the corresponding navigation item's XMI id with `@` stripped.
- Commit message: `JNG-6391 emit data-testid on sidebar / actor-menu items (F1 + F5)`.

## 1. DOM emission — vertical menu branch

- [x] 1.1 In `judo-ui-react/src/main/resources/actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs`, locate the vertical-menu `<ListItemButton component={LinkComponent} ...>` opening tag at line ~100.
- [x] 1.2 Add `data-testid={item.id}` as the LAST attribute on the opening tag (immediately before the closing `>`). The `item.id` field is already emitted by `menu-items-recurse.fragment.hbs:2` via the `createId` helper — no extra wiring is needed.

## 2. DOM emission — horizontal menu branch

- [x] 2.1 In the same file `NavItem.tsx.hbs`, locate the horizontal-menu `<ListItemButton component={LinkComponent} ...>` opening tag at line ~131.
- [x] 2.2 Add the identical `data-testid={item.id}` attribute at the end of the opening tag.

## 3. Spec lockdown

- [x] 3.1 Author `openspec/changes/testid-sidebar-and-actor-menu/specs/sidebar-and-actor-menu-testids/spec.md` declaring one new requirement ("Drawer navigation items expose stable data-testid"). Scenarios:
  - Page-navigation item (F1: `item.target` is a `PageDefinition`) renders with `data-testid={item.id}`.
  - Operation item (F5: `item.actionDefinition` is set) renders with `data-testid={item.id}`.
  - Nested group child (parent has non-empty `items`) renders with `data-testid={item.id}` at every level.

## 4. Integration build

- [x] 4.1 Run `./mvnw clean install` from the repo root. Confirm:
  - All reactor modules report SUCCESS.
  - The itest module's `pnpm install` step and Vitest/Playwright run do not fail with new errors related to this change.
- [x] 4.2 Grep the regenerated `judo-ui-react-itest/.../target/frontend-react/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx` for `data-testid={item.id}`. Confirm exactly two occurrences (one per branch).
- [x] 4.3 Optional sanity: launch the itest fixture app, open the drawer, and inspect with browser devtools — confirm each list-item button has a unique non-empty `data-testid` attribute. Cross-check that group entries with children carry their own testid AND that the children carry distinct testids.

## 5. Commit

- [x] 5.1 `git add` only:
  - `judo-ui-react/src/main/resources/actor/src/layout/Drawer/DrawerContent/Navigation/NavItem.tsx.hbs`
  - `openspec/changes/testid-sidebar-and-actor-menu/**`
- [x] 5.2 Commit on the current `feature/JNG-6391_Test_Data-TestId` branch with the Definition-of-Done message:
  `JNG-6391 emit data-testid on sidebar / actor-menu items (F1 + F5)`
- [x] 5.3 Do NOT modify any catalogue or e2e-template source in this commit. The catalogue companion change ships separately via `judo-ui-e2e-template/openspec/changes/expose-sidebar-and-actor-menu-catalogue/`.
- [x] 5.4 Do NOT touch header chrome `.hbs` files (logo, locale picker, theme toggle, profile menu, submenu). Those are already covered by the shipped F2/F4/F8 `PlatformTestIds.applicationChrome` catalogue.
