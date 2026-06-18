# Extensibility Documentation

Ensures the developer-facing guide `docs/pages/01_ui_react.adoc` documents every runtime customization surface the React generator produces, and that all paths, imports, directory names, and identifiers it shows match the current generator source.

## Requirements

### Requirement: Documented Customization Surface Completeness

The developer documentation `docs/pages/01_ui_react.adoc` SHALL document every runtime customization surface that the generator produces and a developer is expected to register or implement. Each documented surface SHALL include its interface key (or hook/util identifier), its registration mechanism, and at least one usage example consistent with the current source.

#### Scenario: Error handler interceptor documented

- **WHEN** the documentation is reviewed for error handling customization
- **THEN** it describes `ErrorHandlerInterceptorHook` and `ERROR_HANDLER_INTERCEPTOR_INTERFACE_KEY`
- **AND** it shows the `shouldInterceptError` / `interceptError` contract and the `registerErrorHandlerInterceptorHook` registration

#### Scenario: Menu actions hook documented

- **WHEN** the documentation is reviewed for menu customization
- **THEN** it describes `MenuActionsHook` and `MENU_ACTIONS_HOOK_INTERFACE_KEY` for implementing menu-operation actions
- **AND** it distinguishes this from the existing `MenuItemsCustomizerHook` (item-tree mutation)

#### Scenario: Guest page implementation documented

- **WHEN** the documentation is reviewed for guest-page customization
- **THEN** it references `GUEST_PAGE_INTERFACE_KEY` and the actual registration file `src/custom/default-guest-page-hook-registration.tsx`

#### Scenario: File handling utility documented

- **WHEN** the documentation is reviewed for file download/upload customization
- **THEN** it describes the `fileHandling()` utility (`FileHandlingHook`) and its `downloadFile`, `uploadFile`, `exportFile`, and `extractFileNameFromToken` members

#### Scenario: Data store hook documented

- **WHEN** the documentation is reviewed for client-side storage access
- **THEN** it describes `useDataStore` (`UseDataStoreHook`), its `sessionStorage` / `localStorage` driver argument, and its typed get/set helpers

#### Scenario: On-blur action mechanism documented

- **WHEN** the documentation is reviewed for input blur handling
- **THEN** it explains the generated `on<Attr>BlurAction` action mechanism as a distinct concept rather than only an incidental example

### Requirement: Documentation Source Consistency

All file paths, import statements, directory names, and identifiers shown in `docs/pages/01_ui_react.adoc` SHALL match the current generator source. The documentation SHALL NOT reference paths, files, or import schemes that no longer exist in the templates.

#### Scenario: Guest page path is correct

- **WHEN** the guest-page section is read
- **THEN** it instructs copying `src/custom/default-guest-page-hook-registration.tsx`
- **AND** it does not reference a non-existent `src/custom/hooks/` subdirectory or a shipped `.default` file

#### Scenario: i18n directory spelled correctly

- **WHEN** the extra-translations section is read
- **THEN** the override path is `public/i18n/application_default_extra.fragment.hbs`
- **AND** the typo `public/18n/` does not appear

#### Scenario: Data-api import scheme is current

- **WHEN** any code example imports generated data types
- **THEN** it uses the current `~/services/data-api/model/...` scheme
- **AND** the obsolete `~/generated/data-api` import does not appear

#### Scenario: Deprecated hook guidance is clear

- **WHEN** the hooks section is read
- **THEN** `useViewData` is clearly marked deprecated with `useViewContext` named as the replacement, or `useViewData` guidance is removed

### Requirement: Architecture Section Content

The `Architecture` section of `docs/pages/01_ui_react.adoc` SHALL contain a substantive overview of the React generator architecture and SHALL NOT remain a placeholder.

#### Scenario: Architecture section is populated

- **WHEN** the `Architecture` section is read
- **THEN** it contains a real description of the generation pipeline and runtime structure
- **AND** it does not consist solely of the placeholder text "WIP"
