## ADDED Requirements

### Requirement: Browser-storage keys are independent of the served path

The generated application SHALL NOT derive any browser-storage key from the path or origin it
is served from. Keys SHALL be stable across deployments of the same application, so that
upgrading a deployment does not orphan state written by the previous version.

This applies to every storage surface the generated application writes: the OIDC session
store, the application data store in both its `sessionStorage` and `localStorage` drivers,
and any UI preference key.

#### Scenario: The same application served from two different base paths

- **GIVEN** a generated application deployed at one base path
- **AND** the same application deployed at a different base path
- **WHEN** each writes a browser-storage key
- **THEN** both use the same key

#### Scenario: An upgrade preserves an authenticated session

- **GIVEN** a user signed in to a deployed generated application
- **WHEN** the deployment is upgraded to a build produced after this change
- **THEN** the user remains signed in
- **AND** no re-authentication is required

#### Scenario: An upgrade preserves stored application data

- **GIVEN** a generated application that has written data-store entries
- **WHEN** the deployment is upgraded to a build produced after this change
- **THEN** the previously written entries are still readable
- **AND** this holds for both the `sessionStorage` and the `localStorage` driver

### Requirement: No key-namespacing indirection is generated

The generated application SHALL NOT contain a storage-key namespacing helper, and no
generated module SHALL wrap a storage key before use.

Storage keys SHALL appear at their point of use in the form the application reads and writes
them, so that a change to a key is visible in the diff of the file that owns it.

#### Scenario: The generated source contains no namespacing helper

- **WHEN** an application is generated
- **THEN** no storage-key namespacing module is present in the generated sources
- **AND** no generated module imports such a helper

#### Scenario: A partial removal does not produce a generated application

- **GIVEN** a generator state in which a namespacing module has been removed
- **AND** at least one generated module still imports it
- **WHEN** an application is generated and compiled
- **THEN** the compilation fails
- **AND** the failure is not masked or downgraded to a warning
