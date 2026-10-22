## Why

The developer documentation in `docs/pages/01_ui_react.adoc` has drifted from the generator source. A full bidirectional gap analysis of the customization surface (24 interface-key constants, 19 hook types, 18 `useTrackService<T>` targets) found 6 extension points that exist in the templates but are completely undocumented, plus 5 stale or incorrect references that actively mislead developers (wrong file paths, a typo'd directory, and an outdated import scheme). Developers cannot discover or correctly use these customization hooks from the docs as they stand.

## What Changes

- Document 6 currently-undocumented customization surfaces in `01_ui_react.adoc`:
  - `ErrorHandlerInterceptorHook` (`ERROR_HANDLER_INTERCEPTOR_INTERFACE_KEY`) — intercept/override the global error→toast/validation pipeline.
  - `MenuActionsHook` (`MENU_ACTIONS_HOOK_INTERFACE_KEY`) — implement menu-operation actions (distinct from the documented `MenuItemsCustomizerHook`).
  - Guest page implementation via `GUEST_PAGE_INTERFACE_KEY` and `default-guest-page-hook-registration.tsx`.
  - `FileHandlingHook` (`fileHandling()` util) — `downloadFile` / `uploadFile` / `exportFile` / `extractFileNameFromToken`.
  - `UseDataStoreHook` (`useDataStore`) — typed `sessionStorage` / `localStorage` access.
  - The `on<Attr>BlurAction` (on-blur action) mechanism as a first-class documented concept.
- Fix 5 stale/incorrect references:
  - Correct the Guest-page instructions to the real path `src/custom/default-guest-page-hook-registration.tsx` (remove the non-existent `hooks/` subdir and `.default` suffix wording).
  - Fix the `public/18n/` → `public/i18n/` typo (line ~119).
  - Update the outdated `~/generated/data-api` import to the current `~/services/data-api/model/...` scheme (line ~347).
  - Replace the empty `Architecture` "WIP" section with real content.
  - Clarify/retire the deprecated `useViewData` guidance in favor of `useViewContext`.

This is a documentation-only change. No generator templates, helper classes, or generated output change.

## Capabilities

### New Capabilities
- `extensibility-documentation`: Defines what `docs/pages/01_ui_react.adoc` must cover so that every runtime-extensibility surface the generator produces is documented for developers, and that documented file paths, import schemes, and identifiers match the current source.

### Modified Capabilities
<!-- None. The generator's runtime-extensibility behavior is unchanged; this change only documents it. -->

## Impact

- **Files**: `docs/pages/01_ui_react.adoc` (only file modified).
- **Source of truth referenced** (read-only): generator templates under `judo-ui-react/src/main/resources/actor/` and `runtime-extensibility` spec.
- **No code, build, test, or generated-output impact.** No breaking changes.
