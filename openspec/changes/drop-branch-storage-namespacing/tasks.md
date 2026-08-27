## 1. Confirm the premise before editing

- [ ] 1.1 Confirm `git branch --contains 19d9281b` names only `feature/JNG-6411_Unify_data_testId_contract`, and that the PR is still an unmerged draft. If it has merged, this change's framing as unmerged-branch cleanup no longer applies and the upgrade path must be reconsidered instead
- [ ] 1.2 Confirm `develop` has no `storage-namespace` module, so `develop` is the authoritative target state for every restoration below
- [ ] 1.3 List the exact set of files that reference the namespacing, and check the count is still 8 — the module, its test, and six consumers. Treat any additional file as a scope discovery, not a silent extra edit

## 2. Remove the namespacing module

- [ ] 2.1 Delete `judo-ui-react/src/main/resources/actor/src/utilities/storage-namespace.ts.hbs`
- [ ] 2.2 Delete `judo-ui-react/src/main/resources/actor/src/utilities/storage-namespace.test.ts`
- [ ] 2.3 Remove the module's registration from the generator file list (`ui-react.yaml`), so it is no longer emitted
- [ ] 2.4 Remove the re-export from `utilities/index.tsx.hbs`; a dangling re-export breaks the barrel even when no consumer remains

## 3. Restore the six consumers to their `develop` form

- [ ] 3.1 `auth/axiosInterceptor.ts.hbs` — restore the unprefixed OIDC key `oidc.user:{realm}:{clientId}`. This is the file whose unconditional rewrite would have signed out every existing user
- [ ] 3.2 `hooks/useDataStore.ts.hbs` — remove the key wrapping from `getItem`, `setItem` and `removeItem`. Verify **both** the `sessionStorage` and the `localStorage` driver paths, since this file namespaced the whole data-store key space rather than a single key
- [ ] 3.3 `hooks/useLocalStorage.ts.hbs` — restore the unwrapped key
- [ ] 3.4 `components/RootErrorBoundary.tsx.hbs` — restore the unwrapped key
- [ ] 3.5 `layout/Header/HeaderContent/Customization/index.tsx.hbs` — restore the unwrapped customization key
- [ ] 3.6 `utilities/index.tsx.hbs` — confirm no import of the deleted module remains

## 4. Prove nothing partial was left

- [ ] 4.1 Grep the generator resources for `storage-namespace` and `namespacedStorageKey` and confirm zero matches
- [ ] 4.2 Generate an application and compile it; a surviving import must surface as a compilation failure, not a warning
- [ ] 4.3 Grep the generated output for any storage key containing the application base path, and confirm zero matches
- [ ] 4.4 Confirm no compatibility shim, dual-read, or migration helper was introduced — nothing shipped, so nothing needs migrating

## 5. Tests

- [ ] 5.1 Remove or replace any test that asserted a namespaced key shape; do not leave a test asserting behaviour this change removes
- [ ] 5.2 Add a generator-level assertion that generated sources contain no namespacing helper and no import of one
- [ ] 5.3 Verify the existing auth and data-store tests pass against the restored keys without modification; if one needs changing, establish why it encoded the branch's behaviour
- [ ] 5.4 Confirm the data-store tests cover both storage drivers, so a restoration missed in one driver cannot pass

## 6. Guard the co-located work on the same branch

- [ ] 6.1 Confirm the data-testid work on this branch is untouched — it is the branch's legitimate scope
- [ ] 6.2 Confirm no unrelated behavioural change is swept in alongside this cleanup; this change exists precisely because scope leaked once already
- [ ] 6.3 Review the diff for accidental reformatting of the six restored files, so the diff shows only key changes

## 7. Acceptance verification

- [ ] 7.1 Build a generated application from this change, deploy it over a deployment predating the branch with a signed-in user, and confirm the user remains signed in
- [ ] 7.2 With the same deployment, confirm previously stored data-store entries are still readable in both drivers
- [ ] 7.3 Deploy the same generated application at two different base paths and confirm both use identical storage keys
- [ ] 7.4 Record in the PR description, under an explicit "Behaviour changes" heading, that browser-storage key namespacing is withdrawn from this branch and the keys return to their `develop` values
- [ ] 7.5 Run `openspec validate drop-branch-storage-namespacing --strict --no-interactive` and repair any failure
