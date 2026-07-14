# align-testids-with-runtime-package

Close the remaining byte-parity gaps between this template's `data-testid` output and the runtime's `@judo/test-ids` package, so a single Playwright suite runs unchanged against both engines. Focuses on three concrete drifts observed after the runtime landed `feature/unify-data-testid-contract` (commit `70fd3317`, 2026-07-14): the `button::selector` role label, the relation-dropdown menu-wrapper slot names, and the generator's element-id preference (`sourceId` vs `xmi:id`).

**JIRA**: JNG-6391-follow-up (name pending — extends the JNG-6391 branch)
**Depends on**: `unify-data-id-and-testid-with-runtime` (already merged / code-complete).
**Authoritative source**: `judo-frontend-runtime@feature/unify-data-testid-contract`, specifically `packages/test-ids/src/element.ts` and `packages/model-api/src/data/transfer-identity.ts`, cross-referenced against `dataid-template-runtime.pdf`.
