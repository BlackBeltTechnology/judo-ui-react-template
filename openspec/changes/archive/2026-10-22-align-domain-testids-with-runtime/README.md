# align-domain-testids-with-runtime

Extend `data-testid` runtime parity beyond widgets and tables into the remaining shared DOM domains: dialogs, navigation, tabs, and breadcrumbs. Adds four new build helpers (`buildDialogTestId`, `buildTabTestId`, `buildNavItemTestId`, `buildBreadcrumbTestId`) mirroring the runtime's `@judo/test-ids` `dialog::`, `tabs::`, `nav::`, and `breadcrumb::` domain prefixes, then sweeps the seven affected template files.

**JIRA**: JNG-6391 follow-up (name pending)
**Depends on**: `align-testids-with-runtime-package` (merged / branch `feature/JNG-6391_align_testids_with_runtime_package`).
**Authoritative source**: `judo-frontend-runtime@feature/unify-data-testid-contract`, specifically `packages/test-ids/src/{dialog,navigation,tabs}.ts`.
