## MODIFIED Requirements

### Requirement: Link autocomplete dropdown surfaces truncation hint

When a `Link` element renders as `<SingleRelationInput>` with autocomplete support (`autocompleteSetActionDefinition` present), the link container template SHALL pass `autoCompleteLimit={ calculateLinkAutocompleteRows(link) }` to the widget. The widget SHALL then render the truncation footer per the `input-widgets` capability requirement "Autocomplete dropdowns hint when the result list is truncated".

For collection-association autocomplete via the `Tags` widget (driven from the tag container template), the same hint SHALL appear in the multi-select dropdown using the widget's existing `limitOptions` prop.

#### Scenario: SingleRelationInput receives the limit from its container

- **WHEN** a `Link` element bound to a single relation is generated with an autocomplete range action
- **THEN** the generated `link/index.tsx` passes `autoCompleteLimit={ calculateLinkAutocompleteRows(link) }` to `<SingleRelationInput>`
- **AND** the same value is used both as the server-side `_seek.limit` (existing behaviour) and as the threshold for the truncation hint (new behaviour)

#### Scenario: Tags widget surfaces the hint using its existing limit

- **WHEN** a collection-aggregation relation renders via the `Tags` widget
- **AND** the autocomplete fetch returns a number of options equal to `limitOptions`
- **THEN** the dropdown paper renders the same truncation footer as `SingleRelationInput`

**Templates**:
- `actor/src/containers/components/link/index.tsx.hbs`
- `actor/src/containers/components/tag/index.tsx.hbs`
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
