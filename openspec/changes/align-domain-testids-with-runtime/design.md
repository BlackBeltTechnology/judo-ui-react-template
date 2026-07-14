## Decisions

### D1. Adopt runtime's domain prefixes rather than funneling through `field::`.

The runtime ships four domain-prefixed modules (`dialog.ts`, `navigation.ts`, `tabs.ts`) with 20+ helpers already released as `@judo/test-ids@0.33`. Every helper emits under `dialog::`, `nav::`, `tabs::`, or `breadcrumb::` — none through `field::`. Byte-parity requires the template to adopt the same prefixes.

Rejected: keep `field::<id>::dialog-title` etc. and ask the runtime to funnel through `field::` for uniformity. Reason: (a) the runtime is already published and consumed by downstream apps; (b) domain prefixes are more grep-friendly for locating testids by DOM level; (c) `field::` is genuinely wrong for dialogs — a dialog is not a form field.

### D2. Semantic mapping — FilterDialog `apply` maps to `confirm`.

The runtime's dialog vocabulary is generic: `title / content / actions / close / confirm / cancel`. FilterDialog has an "Apply" button that is semantically the confirm-equivalent (applies the filter, closes the dialog). Mapping `button::apply` → `dialog::<id>::confirm` gives cross-engine tests a single stable selector for "the confirm-equivalent action of any dialog".

Rejected: keep `button::apply` as a distinct sub-role of the dialog domain. Reason: no runtime counterpart to bind against. The whole point of this change is aligning the vocabulary.

Rejected: rename to something more filter-specific like `button::apply-filter`. Reason: same problem — no runtime counterpart. The runtime's dialog vocab is deliberately generic because the same shell renders confirm/apply/save dialogs.

### D3. OperationFaultDialog gets a synthetic dialog id.

OperationFaultDialog is a template-internal dialog with no model xmi:id — it renders a generic error surface not tied to any model element. It currently emits bare inline literals like `"fault-dialog-title"`. Routing them through `buildDialogRoleTestId(dialogId, role)` needs a stable string dialog id.

Chosen: use the literal `'operation-fault'` as the synthetic id. Emitted testid is `dialog::operation-fault::title`. Stable across engines because the runtime's operation-fault dialog (when it has one) SHALL use the same synthetic id — recorded in the spec.

Rejected: leave the bare literals alone. Reason: they're the only bare-literal dialog testids in the template. Aligning them here removes one class of exception.

Rejected: derive the id from some runtime property (error code, timestamp). Reason: makes testids non-deterministic; Playwright suites can't bind against them.

### D4. Per-fault list items in OperationFaultDialog stay as template-only.

The per-fault list emissions `'faults.' + faultObjectKey + '.' + key` produce runtime-varying testids based on backend error payload keys. The runtime has no matching DOM (or if it does, its own error-fault DOM is not yet in `@judo/test-ids`). Deferring these to a future change once the runtime's error-surface DOM is defined avoids a speculative rename.

### D5. Nav item nesting — parent path threaded through props, not model traversal.

The runtime's `getNavItemTestId(item, parentPath)` composes the path from the caller — the runtime component knows its render context. The template's Handlebars codegen does not have easy access to the caller's testid at generation time; the natural place to thread the parent path is through React props at render time.

Chosen: add a `parentPath?: string` prop to `NavItemProps`, `NavCollapseProps`, `NavGroupProps`. Each nested component computes its own testid path and passes the composed path down to children. Root menu invocation passes `parentPath=undefined`; each recursion appends. Utility `buildNavItemPath(parentPath, currentId)` is added to `transfer-id.ts.hbs` for the composition.

Rejected: encode the path at codegen time (Java helper walks the menu tree). Reason: menu structure is dynamic at runtime (user permissions may hide items); paths would be wrong for the effective render.

Rejected: read the path from a React context. Reason: adds context-provider boilerplate for a value that flows naturally through props.

### D6. Retire the double `data-testid` emission on NavItem.

`NavItem.tsx.hbs` emits `field::<item.id>` twice — once at line 99 on the outer `<ListItemButton>`, once at line 142 on a child element. Both DOM levels get the same testid — a real collision (`getByTestId(...)` returns two elements).

The runtime's DOM equivalent has a single testid on the outer `<ListItemButton>`. Chosen: remove the second emission at line 142, or rename it to a distinguishing sub-role (`buildNavItemRoleTestId(navItemTestId, 'label')` if it wraps the label content). Implementation-time decision after DOM inspection.

Rejected: keep both with the same testid. Reason: violates the "no two DOM elements share a testid" rule from the transfer-identity spec.

### D7. Breadcrumb outer literal `"application-breadcrumb"` retained.

The runtime exposes only per-item breadcrumb testids (`breadcrumb::<index>`), no outer-container testid. The template's `"application-breadcrumb"` on the outer `<Breadcrumbs>` is a template-only hook, useful for scoping `page.getByTestId('application-breadcrumb').getByRole('link')` in Playwright. Retaining it costs nothing.

### D8. Options / chips / actor-selector / user-menu explicitly out of scope.

Runtime has `options.ts` (7 helpers) and `navigation.ts::getUserMenuTestId`, `::getActorSelectorTestId`. Template has some option testids (TrinaryLogicCombobox) and inline literals (ApplicationSelector) that could map. Deferring because:

- The runtime's `getOptionTestId(inputId, ...)` takes an already-built input testid as `inputId`. Whether that includes the `field::` prefix or is bare is unclear from the source; both interpretations are structurally valid. Live DOM comparison is needed to resolve.
- ApplicationSelector has template-only DOM (a full modal with app-switching controls) that has no direct runtime counterpart on the current runtime branch. Speculative mapping risks making things worse.

Both are scheduled as a further follow-up once runtime devtools inspection is possible.

### D9. Tab panel nested under tab id, not controller id.

Runtime `getTabPanelTestId(ctrlId, tab, index)` produces `tabs::<ctrlId>::<tabId>::panel` — the panel is nested under the specific tab, not just the controller. Template currently emits `field::<id>::panel` — flat under the controller, no tab id.

Chosen: adopt runtime's nesting. The panel is intuitively scoped to a specific tab (the content that appears when that tab is selected). Playwright can address a specific tab's panel with a single selector without needing to combine controller-scope and tab-index. Rewriting the `ModeledTabs.tsx.hbs` panel emission is a one-line change but requires the tab id to be in scope where the panel is rendered — verify at implementation time.

Rejected: emit `tabs::<ctrlId>::panel::<tabId>` (panel first, then tab id). Reason: does not match runtime's `<ctrlId>::<tabId>::panel` order.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Nav parent-path threading requires plumbing new props through three components. Missing a call site leaves child items with a broken path. | Add a runtime assertion (`console.warn` in dev) if a nested `<NavCollapse>` or `<NavGroup>` receives `parentPath=undefined` — that surfaces missing plumbing on first render. |
| FilterDialog `apply` → `confirm` mapping breaks any Playwright suite currently binding on `field::<id>::button::apply`. | Same class of change as `selector` → `set` in the previous follow-up — expected and acceptable. Suites on the pre-JNG-6391 branch also break the same way. |
| Tab panel emits under tab id, not controller id — call sites that scoped by controller may become brittle if they relied on panel selection with a fixed suffix. | The specific selector `tabs::<ctrlId>::<tabId>::panel` is more addressable than `tabs::<ctrlId>::panel`, so tests gain precision. Existing suites (if any) are broken by design. |
| Nav double-emission on NavItem may be load-bearing for some rendering path we don't fully understand. | Task 4.1 explicitly says "verify DOM at render time before removing". Fallback plan: rename rather than remove, giving each DOM level a distinct role suffix. |
| The synthetic id `'operation-fault'` collides with a hypothetical model element named "operation-fault". | Extremely unlikely; model xmi:ids all begin with `_` or contain `/`. `'operation-fault'` is a stable, human-readable synthetic id. Documented in the spec. |
| Snapshot churn hits every template that renders a dialog, tab, or drawer — potentially 30–50 files. | Bounded surface. Snapshot refresh procedure is mechanical (`-DforceSnapshotOverwrite=true`). One snapshot commit per itest keeps the diff navigable. |

## Non-goals

1. No changes to widgets or tables — those are done in the parent changes.
2. No changes to options, chips, actor-selector, user-menu, ApplicationSelector. Follow-up.
3. No changes to layout containers in `flex.hbs`, `DropdownButton`, `flex.hbs` Accordion / Card / Grid / Stack `container` role.
4. No `build*TestId` → `get*TestId` rename.
5. No new runtime helper — the template implements the domain-prefixed helpers as pure string functions matching the runtime's grammar. Both engines produce identical strings independently.
6. No changes to the runtime side. Runtime is the authoritative source.
7. No addition of `data-testid` to elements that don't already have one, except where explicitly listed (per-breadcrumb-item testids, tab-list testid). Extending coverage is out of scope.
