## Why

The branch `feature/JNG-6411_Unify_data_testId_contract` adds browser-storage key
namespacing to the generated React application. This work is outside the branch's stated
scope — a data-testid contract — and it was introduced only to let two frontends of the same
actor be served from one origin, a topology that has since been withdrawn in favour of one
frontend per deployment (`judo-ng@unify-frontend-selection`).

The PR is a **pushed draft and is not merged**; `19d9281b` is contained only by the feature
branch, and `develop` has no `storage-namespace` module. So there is nothing released to
revert and no user to migrate. **The correct action is to take this off the branch, not to
ship it and follow up with a revert.**

That distinction matters here more than usual, because the change is harmful as written:

- `axiosInterceptor.ts.hbs` rewrites the OIDC storage key **unconditionally**, from
  `oidc.user:{realm}:{clientId}` to `judo:{basePath}:oidc.user:{realm}:{clientId}`, with no
  fallback and no read-through of the old key. Every already-signed-in user of every
  deployed React frontend would have been silently signed out on upgrade, with no note in
  the PR.
- `useDataStore.ts.hbs` routes the application's **entire** data-store key space through the
  same helper, for both the `sessionStorage` and `localStorage` drivers — so the blast
  radius is not one key but all of them.

The withdrawn premise is also worth stating plainly: with one frontend per deployment, two
same-origin frontends cannot collide, so the namespacing solves a problem that no longer
exists while creating one that does.

## What Changes

Remove the branch's namespacing from the generated application and return the affected keys
to their `develop` values.

- **Delete** `utilities/storage-namespace.ts.hbs` and `utilities/storage-namespace.test.ts`,
  and remove the module's registration from the generator's file list.
- **Restore all six consumers** to their `develop` form:
  `auth/axiosInterceptor.ts.hbs`, `hooks/useDataStore.ts.hbs`, `hooks/useLocalStorage.ts.hbs`,
  `components/RootErrorBoundary.tsx.hbs`,
  `layout/Header/HeaderContent/Customization/index.tsx.hbs`, and the
  `utilities/index.tsx.hbs` barrel re-export.
- **Leave no partial state.** Deleting the module while any consumer still imports it breaks
  generated-application compilation, so the deletion and all six restorations are one unit.
- **Add a requirement** that generated browser-storage keys are not derived from the path the
  application is served on. The namespacing was able to land unremarked because nothing
  stated this.

## Impact

- Affected capability: `application-structure`.
- Affected code: 8 files under `judo-ui-react/src/main/resources/actor/src/` (one module, its
  test, six consumers) plus the generator file-list registration.
- **Restores upgrade safety.** A user signed in against a deployment predating this branch
  stays signed in.
- **Restores the full data-store key space**, not just the OIDC key.
- **Sibling change:** `judo-frontend-runtime@drop-branch-storage-namespacing` removes the
  equivalent work on the runtime side. The two are independent — either can land first —
  because they are different generated applications.
- **Umbrella:** `judo-ng@unify-frontend-selection` records the withdrawn topology and the
  cross-repo ordering.

## Non-Goals

- **No new opt-in namespacing mechanism.** If two frontends must ever share an origin again,
  that is a fresh proposal with an explicit upgrade path — not a flag left behind here.
- **No change to the testid work on the same branch.** The data-testid contract is the
  branch's legitimate scope and is untouched.
- **No change to which keys the application uses**, beyond removing the prefix. Key names,
  drivers, and lifetimes return to `develop`'s values exactly.
