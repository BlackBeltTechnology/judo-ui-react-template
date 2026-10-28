## ADDED Requirements

### Requirement: Create through an eager link persists on a saved owner

On a view page, a create through an eager link (not a table) whose relation is not a `COMPOSITION` SHALL be staged into the owner only while the owner is unsaved. On a saved owner it SHALL be created through the relation's own `~create` endpoint, and the page SHALL then reload even in edit mode. This mirrors the runtime's `LinkRenderer` `stagesCreateLocally`. Tables, composition links, lazy links and form pages SHALL keep their create flow.

#### Scenario: Eager aggregation link on a saved owner

- **WHEN** a saved Galaxy view creates an astronomer through its eager `AGGREGATION` link
- **THEN** the form calls `~update/astronomer/~create` instead of staging the astronomer into the owner
- **AND** the page reloads, so the field shows the new astronomer and the owner is not dirty

#### Scenario: Create after an unset

- **WHEN** the astronomer is unset (the owner is in edit mode) and a new one is created through the link
- **THEN** the page still reloads after the create and the field shows the new astronomer

#### Scenario: Eager composition link stays a draft

- **WHEN** the link's relation is a `COMPOSITION`
- **THEN** the create is staged into the owner (`isDraft: true`) and persisted by the owner's save
