## 1. Capture the rationale before deleting it

- [x] 1.1 Move the full tier rationale into `proposal.md` under "Tier rationale (kept out of the generated code)", mirroring the "Guard rationale (kept out of the generated code)" section of `2026-08-02-fix-mobile-drawer-overlay-freeze` — including the four-tier table, the `xs → sm` is-within-compact note, and the unresolved-breakpoint analysis with its 6-mutation / 12-divergence figures
- [x] 1.2 Correct the "must always be collapsed" wording while moving it: the compact rule is a responsive baseline re-asserted on breakpoint change, not an absolute runtime state, since a user action at `xs` may open the overlay

## 2. Template

- [x] 2.1 Remove the 12-line module-level tier comment block above `type DrawerTier` in `actor/src/layout/Drawer/index.tsx.hbs`
- [x] 2.2 Remove the two in-effect comments (the `unresolved` early-return note and the `prevTier`/configured-default note)
- [x] 2.3 Change no executable line: `type DrawerTier`, `drawerTier`, the effect body, the `enteringMobile`/`temporaryDrawerOpen` guard and every JSX prop stay exactly as they are

## 3. Verify

- [x] 3.1 Capture the generated `target/frontend-react/src/layout/Drawer/index.tsx` before the edit, regenerate after, and diff — the only differences must be removed comment lines
- [x] 3.2 `mvn -o clean install -pl judo-ui-react` succeeds
- [x] 3.3 `mvn -o clean install -pl judo-ui-react-itest/RelationTest/relation_test__actor -DskipPrepareNodeJS` succeeds: Biome formatting, snapshot diff-checker and Vite build all clean, and Biome reformats nothing (generated output matches the template verbatim)
- [x] 3.4 Confirm no stale "always collapsed" wording survives anywhere in the template or the specs

## 4. Spec Delta and Archive

- [x] 4.1 Add a `code-generation` delta codifying the convention: generated code SHALL NOT carry design rationale as comments, rationale belongs in the change documents, and a named union type is preferred over a comment enumerating states
- [x] 4.2 `openspec validate drop-drawer-tier-generated-comments --strict` passes
- [x] 4.3 Archive the change and confirm the `code-generation` delta merges into `openspec/specs/code-generation/spec.md` without disturbing existing requirements
