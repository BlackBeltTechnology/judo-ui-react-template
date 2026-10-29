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

### Requirement: Create from the set selector adopts the persisted target

When the set selector of such a link (eager, not a composition, on a view page) creates a target on a saved owner, the relation's `~create` endpoint already links the target. The selector SHALL report the result as created (`openCreated`). The page SHALL reload, and SHALL NOT stage the target into the owner. This mirrors the runtime's `LinkRenderer` `handleSelectorCreate`. A target picked from the selector SHALL still be staged.

#### Scenario: Selector create after an unset

- **WHEN** a saved Galaxy view unsets its astronomer, opens the set selector and creates a new astronomer there
- **THEN** the form calls `~update/astronomer/~create`
- **AND** the page reloads, so the field shows the new astronomer, the owner is not dirty and the refresh button is enabled
