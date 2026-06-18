## Context

`docs/pages/01_ui_react.adoc` (1707 lines, ~50 sections) is the primary developer-facing guide for customizing generated React applications. A bidirectional gap analysis against the generator source (`judo-ui-react/src/main/resources/actor/`) found:

- **6 forward gaps** — extension surfaces in source with zero documentation: `ErrorHandlerInterceptorHook`, `MenuActionsHook`, guest-page `GUEST_PAGE_INTERFACE_KEY` registration, `FileHandlingHook`, `UseDataStoreHook`, and the `on<Attr>BlurAction` mechanism.
- **5 staleness issues** — wrong guest-page path, `public/18n/` typo, obsolete `~/generated/data-api` import, empty `Architecture` (WIP) section, and unclear `useViewData` deprecation.
- **3 verified non-gaps** — `filterTypeOptions`/`getNameOptions` (model-derived example names) and `customComponentAnnotationPrefix` (Java-side config) are correct as-is and must NOT be "fixed".

The source is the single source of truth. Every documented identifier, path, and import must be copied/verified from the templates, not invented.

## Goals / Non-Goals

**Goals:**
- Bring `01_ui_react.adoc` to full coverage of the runtime customization surface.
- Eliminate all stale paths, typos, and obsolete import schemes.
- Keep documentation style and section structure consistent with the existing file (same AsciiDoc conventions, `application-customizer.tsx` example pattern).

**Non-Goals:**
- No changes to generator templates, helper classes, `ui-react.yaml`, or generated output.
- No new generator features. This documents existing behavior only.
- No restructuring/reordering of the existing (correct) sections beyond what the fixes require.
- Not documenting purely-internal `ComponentProxy` plumbing that has no developer-facing registration step.

## Decisions

**Decision: New sections placed under the existing `== Customization` block.**
The 6 undocumented hooks are all runtime customization surfaces and belong alongside the ~25 existing `=== Implementing ...` subsections. Rationale: preserves the reader's mental model and the file's existing organization. Alternative (a separate "Advanced hooks" appendix) was rejected as it fragments related content.

**Decision: Every code example is verified against source before writing.**
For each new section, the interface key, hook type signature, registration function, and member names are read directly from the corresponding `.hbs` template (e.g. `error-handling.ts.hbs`, `menuCustomization.ts.hbs`, `file-handling.tsx.hbs`, `useDataStore.ts.hbs`). Rationale: the whole reason for this change is drift; introducing new drift would be self-defeating.

**Decision: Stale references are corrected in place, not appended.**
The typo, wrong path, and obsolete import are edited at their existing locations (lines ~119, guest-page section, ~347). Rationale: minimal diff, no duplication.

**Decision: `Architecture` section gets a concise factual overview.**
Filled from known project facts (two-phase pipeline: TS REST layer then React UI; Handlebars templates + Java helpers; Pandino runtime DI). Kept short rather than exhaustive. Alternative (leave WIP) rejected since the spec requires it populated.

**Decision: `useViewData` is kept but explicitly marked deprecated, pointing to `useViewContext`.**
Rationale: it still exists in source; removing guidance entirely could strand readers of older generated code. The spec allows either mark-deprecated or remove; mark-deprecated is lower risk.

## Risks / Trade-offs

- **[Documenting an API detail incorrectly]** → Mitigation: read each `.hbs` source file immediately before writing its section; cite exact identifiers.
- **[Over-correcting verified non-gaps]** → Mitigation: design explicitly lists `filterTypeOptions`/`getNameOptions`/`customComponentAnnotationPrefix` as correct; tasks must not touch them.
- **[Example uses outdated import scheme by copy-paste habit]** → Mitigation: all new examples use `~/services/data-api/model/...`; a final grep ensures `~/generated/data-api` is absent.
- **[AsciiDoc rendering breakage]** → Mitigation: reuse existing source-block delimiters and admonition syntax already proven in the file.
