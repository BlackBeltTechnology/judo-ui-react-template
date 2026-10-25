# sidebar-and-actor-menu-testids Specification

## Purpose
Specifies the `data-testid={item.id}` emission on the drawer's `NavItem` `<ListItemButton>` (both vertical and horizontal menu branches). Covers F1 (page-navigation items where `item.target` is a `PageDefinition`) and F5 (operation items where `item.actionDefinition` is set), as well as nested group children. The `item.id` value is the cleaned XMI id produced by the `createId` helper in `menu-items-recurse.fragment.hbs`.
## Requirements
### Requirement: Drawer navigation items expose stable data-testid

Every `<ListItemButton>` rendered by the navigation drawer SHALL carry a `data-testid` attribute whose value equals the navigation item's XMI-derived stable identifier (`item.id`, produced by the `createId` Handlebars helper — `getXMIID(element)` with `@` characters stripped).

This requirement applies to BOTH render branches in `NavItem.tsx.hbs` — the vertical menu (line ~100) and the horizontal menu (line ~131) — and applies regardless of whether the item is a page-navigation entry (`item.target` is a `PageDefinition`, F1), an actor-menu operation (`item.actionDefinition` is set, F5), or a nested child of a group item (parent's `items` is non-empty).

The `data-testid` attribute SHALL be additive — existing `aria-label`, visible label text, and role-based locators continue to work unchanged.

#### Scenario: Page-navigation item carries data-testid (F1)

- **GIVEN** a `NavigationItem` whose `target` is a `PageDefinition`
- **WHEN** the drawer renders the item via `NavItem.tsx`
- **THEN** the emitted `<ListItemButton>` carries `data-testid={item.id}`
- **AND** `item.id` matches `createId(navigationItem)` (XMI id with `@` stripped)

#### Scenario: Actor-menu operation item carries data-testid (F5)

- **GIVEN** a `NavigationItem` whose `actionDefinition` is set
- **WHEN** the drawer renders the item via `NavItem.tsx`
- **THEN** the emitted `<ListItemButton>` carries `data-testid={item.id}`
- **AND** the testid value is identical to what `Sidebar.navItems` / `ActorMenu.operations` exposes in the e2e-template catalogue for the same model element

#### Scenario: Nested group child carries data-testid at every level

- **GIVEN** a group `NavigationItem` whose `items` collection is non-empty (including arbitrarily nested groups)
- **WHEN** the drawer recursively renders the group's children via `menu-items-recurse.fragment.hbs`
- **THEN** each rendered child `<ListItemButton>` carries its own `data-testid={item.id}`
- **AND** parent and child testids are distinct (XMI uniqueness guarantee)
- **AND** the parent group entry itself, if rendered as a clickable button, also carries `data-testid={item.id}`

