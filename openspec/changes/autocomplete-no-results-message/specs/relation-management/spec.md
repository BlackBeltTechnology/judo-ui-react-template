## MODIFIED Requirements

### Requirement: Link and tag autocomplete dropdowns show a localized empty-state message

When a `Link` element renders as `<SingleRelationInput>` with autocomplete support, and when a collection-association renders as `<Tags>`, the widgets SHALL surface the localized empty-state message defined by the `input-widgets` capability requirement "Autocomplete dropdowns display a localized empty-state message". No new caller-side prop is required; the widget owns the message end-to-end.

This requirement is purely a wiring assertion from the relation-management perspective — it guarantees that the localized empty message applies to every relation-driven autocomplete without any container template change.

#### Scenario: SingleRelationInput inside a link container shows the localized empty message

- **WHEN** the user opens a link-container autocomplete and types a query returning zero options
- **THEN** the generated `link/index.tsx`'s `<SingleRelationInput>` displays the `judo.autocomplete.no-results` message in the active locale
- **AND** no change to `link/index.tsx.hbs` was necessary to achieve this

#### Scenario: Tags widget inside a tag container shows the localized empty message

- **WHEN** the user opens a tag-container autocomplete and types a query returning zero options
- **THEN** the generated `tag/index.tsx`'s `<Tags>` displays the same localized message
- **AND** no change to `tag/index.tsx.hbs` was necessary to achieve this

**Templates**:
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
