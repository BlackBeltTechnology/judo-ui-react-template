## MODIFIED Requirements

### Requirement: Enumeration filter autocomplete displays a localized empty-state message

The `SingleValueFilterComponent` widget's enumeration-branch `<Autocomplete>` (bound to `filter.filterOption.enumValues`) SHALL pass a localized `noOptionsText` prop sourced from `judo.autocomplete.no-results` so that, when the user types a query matching no enum label, the dropdown paper displays the same localized message used by the server-paginated widgets. MUI's built-in English default (`"No options"`) SHALL never be surfaced.

This widget was explicitly OUT of scope for JNG-6409 (its option source is a finite local enum array, so no server-side truncation is possible), but IS in scope for this change because a locally-filtered array can still yield an empty result set for a given user query.

#### Scenario: Enumeration filter returns zero matches under en-US

- **GIVEN** an active locale of `en-US` and a table column with an enumeration filter open
- **WHEN** the user types a query into the filter's Autocomplete that matches no enum label
- **THEN** the dropdown paper renders the text `"No matching results"`

#### Scenario: Enumeration filter returns zero matches under hu-HU

- **GIVEN** an active locale of `hu-HU` and the same filter open
- **WHEN** the user types a query matching no enum label
- **THEN** the dropdown paper renders the text `"Nincs találat"`

**Templates**:
- `actor/src/components/table/SingleValueFilterComponent.tsx.hbs`

**i18n**: `judo.autocomplete.no-results` in `system_en-US.json.hbs`, `system_hu-HU.json.hbs`, `system_default.json.hbs`.
