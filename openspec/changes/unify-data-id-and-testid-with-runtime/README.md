# unify-data-id-and-testid-with-runtime

Unify the transfer-object identity contract (row keys, new-row seeding, `data-testid` structure) between the React template and the JUDO frontend runtime so that a single Playwright test suite can address the same DOM node and the same data row on outputs from either engine. Introduces a shared `resolveTransferId` fallback chain, replaces the `"draft:<uuid>"` new-row scheme with `__tempId` + `__isNew`, and reformats every existing `data-testid` emission to a role-suffixed hierarchical scheme derived from that resolver.

**JIRA**: JNG-6391
**Authoritative design source**: internal PDF `dataid-template-runtime.pdf` (2026-07-08).
