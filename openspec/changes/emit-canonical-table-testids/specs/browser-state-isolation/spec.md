## ADDED Requirements

### Requirement: Persisted browser state is namespaced by serving base path

Every `localStorage` and `sessionStorage` key the generated application persists SHALL be prefixed
with `judo:<namespace>:`, where `<namespace>` is derived from the base path the application is
actually served on: leading and trailing slashes dropped, remaining slashes replaced by dots, and the
origin root represented as `root`. The derivation SHALL match `@judo/core`'s
`deriveStorageNamespace`/`namespacedStorageKey` scheme so the two engines agree.

#### Scenario: Two engines on one origin keep independent state
- **GIVEN** a React-generated actor served on `/RelationTest/Actor/` and a runtime actor served on `/RelationTest_runtime/Actor/` from the same origin
- **WHEN** both applications persist a value under the same unprefixed key
- **THEN** the React actor writes `judo:RelationTest.Actor:<key>`
- **AND** the runtime actor writes `judo:RelationTest_runtime.Actor:<key>`
- **AND** neither application reads or overwrites the other's entry

#### Scenario: Authentication sessions do not collide
- **GIVEN** two applications of the same model served from one origin against the same realm and client
- **WHEN** each stores its OIDC user entry
- **THEN** each `oidc.user:<realm>:<clientId>` key is namespaced by its own base path
- **AND** signing in to one application does not invalidate or replace the other's session

#### Scenario: Root-served application uses the root namespace
- **GIVEN** an application served from the origin root
- **WHEN** a key is persisted
- **THEN** the namespace segment is `root`
- **AND** the key takes the form `judo:root:<key>`

#### Scenario: Namespace derivation is total
- **GIVEN** a base path with redundant leading or trailing slashes, or nested path segments
- **WHEN** the namespace is derived
- **THEN** redundant slashes are collapsed and nested segments are joined with dots
- **AND** the derivation returns a value for every input without throwing

#### Scenario: Non-browser context falls back safely
- **GIVEN** code executing where `document` is undefined
- **WHEN** the namespace is resolved
- **THEN** the root namespace is returned
- **AND** no exception propagates to the caller

### Requirement: Scoped clear leaves sibling application state intact

Where the application clears persisted state, it SHALL remove only entries carrying its own
namespace prefix. A blanket clear of a storage area SHALL NOT be performed.

#### Scenario: Error-boundary reset is scoped
- **GIVEN** two applications of one model served from one origin, each with persisted session state
- **WHEN** the user triggers the settings reset on the error boundary of one application
- **THEN** only that application's namespaced entries are removed
- **AND** the sibling application's persisted state remains readable
- **AND** unrelated third-party entries on the origin are untouched

#### Scenario: Storage-change listeners observe the namespaced key
- **GIVEN** a hook that reacts to cross-tab storage changes for one of its keys
- **WHEN** a storage event arrives
- **THEN** the hook compares the event key against the namespaced key
- **AND** a change written by a sibling application under a different namespace does not trigger an update
