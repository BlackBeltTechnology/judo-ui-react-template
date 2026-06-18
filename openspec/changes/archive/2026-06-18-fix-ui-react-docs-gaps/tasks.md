## 1. Fix stale and incorrect references

- [x] 1.1 Fix the `public/18n/` → `public/i18n/` typo in the "Adding extra translations" section (line ~119)
- [x] 1.2 Update the obsolete `~/generated/data-api` import to the current `~/services/data-api/model/...` scheme (line ~347); grep the whole file to confirm no other `~/generated/data-api` occurrences remain
- [x] 1.3 Correct the Guest-page section to reference `src/custom/default-guest-page-hook-registration.tsx` (remove the non-existent `hooks/` subdir and the `.default` suffix wording); verify against `src/auth/Auth.tsx.hbs` and the registration template
- [x] 1.4 Mark `useViewData` clearly deprecated and name `useViewContext` as the replacement (or remove `useViewData` guidance)

## 2. Populate the Architecture section

- [x] 2.1 Replace the `== Architecture` "WIP" placeholder with a concise overview: two-phase generation pipeline (TS REST layer then React UI), Handlebars templates + Java helpers, Pandino runtime DI, and the customization model

## 3. Document undocumented customization surfaces

- [x] 3.1 Add an "Implementing the error handler interceptor" section: read `src/utilities/error-handling.ts.hbs` and `src/custom/default-error-handler-interceptor.ts.hbs`, document `ERROR_HANDLER_INTERCEPTOR_INTERFACE_KEY`, the `shouldInterceptError`/`interceptError` contract, and `registerErrorHandlerInterceptorHook`
- [x] 3.2 Add a "Implementing menu actions" section: read `src/custom/menu-hook-registration.ts.hbs` and `.../Navigation/menuCustomization.ts.hbs`, document `MENU_ACTIONS_HOOK_INTERFACE_KEY` / `MenuActionsHook`, and clarify the distinction from `MenuItemsCustomizerHook`
- [x] 3.3 Extend the Guest-page section (or add detail) to name `GUEST_PAGE_INTERFACE_KEY` and show the registration via `default-guest-page-hook-registration.tsx`
- [x] 3.4 Add a "File handling" section: read `src/utilities/file-handling.tsx.hbs`, document the `fileHandling()` util (`FileHandlingHook`) and its `downloadFile`, `uploadFile`, `exportFile`, `extractFileNameFromToken` members
- [x] 3.5 Add a "Client-side data store" section: read `src/hooks/useDataStore.ts.hbs`, document `useDataStore` (`UseDataStoreHook`), the `sessionStorage`/`localStorage` driver argument, and the typed get/set helpers
- [x] 3.6 Add an "On-blur actions" explanation: read `src/containers/components/link/index.tsx.hbs` and `src/containers/types.ts.hbs`, document the generated `on<Attr>BlurAction` mechanism as a first-class concept

## 4. Verification

- [x] 4.1 Confirm the 3 verified non-gaps were NOT altered: `filterTypeOptions`/`getNameOptions` (model-derived example names) and `customComponentAnnotationPrefix` (Java-side config) remain as-is
- [x] 4.2 Re-run the bidirectional check: grep the adoc for each of the 6 newly-documented identifiers and confirm each now appears; grep for `public/18n` and `~/generated/data-api` and confirm zero hits
- [x] 4.3 Build the docs (or render the AsciiDoc) to confirm no syntax/rendering errors were introduced
