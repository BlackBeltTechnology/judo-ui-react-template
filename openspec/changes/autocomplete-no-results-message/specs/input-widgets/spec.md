## MODIFIED Requirements

### Requirement: Autocomplete dropdowns display a localized empty-state message

Every generator-emitted MUI `<Autocomplete>` widget — `SingleRelationInput`, `Tags`, and `TextWithTypeAhead` — SHALL render a localized empty-state message inside the dropdown's Paper whenever the current search has settled with zero options. The message SHALL be sourced from the i18n key `judo.autocomplete.no-results`. The English default value SHALL be `"No matching results"`; the Hungarian value SHALL be `"Nincs találat"`. MUI's built-in English default (`"No options"`) SHALL never be surfaced to the end user.

Because all three widgets set `freeSolo={true}` (search-with-suggestions pattern), MUI's built-in `noOptionsText` prop is silently suppressed by MUI. The widgets therefore inject the message through the shared `slots.paper` component supplied by the `useAutocompleteHintPaper` hook. The exact injection mechanism is an implementation concern; the observable contract is the presence of the localized text in the dropdown DOM under the conditions above.

The empty-state message SHALL NOT participate in keyboard navigation. The message SHALL NOT be shown while the widget's `loading` state is `true`. The message SHALL coexist with — but never overlap — the JNG-6409 "more results" hint: the no-results message requires `optionsLength === 0`, and the more-results hint requires `optionsLength >= limit > 0`, which are mutually exclusive states.

No caller-side (container-template) plumbing is required; the localized behaviour SHALL be automatic on regeneration.

#### Scenario: Empty search result under en-US

- **GIVEN** an active locale of `en-US` and a `SingleRelationInput` bound to a server-paginated relation
- **WHEN** the user types a query that returns zero options and the fetch settles (`loading === false`)
- **THEN** the dropdown paper renders the text `"No matching results"`
- **AND** the text `"No options"` (MUI's untranslated default) is NOT present anywhere in the dropdown DOM

#### Scenario: Empty search result under hu-HU

- **GIVEN** an active locale of `hu-HU` and the same widget
- **WHEN** the user types a query that returns zero options
- **THEN** the dropdown paper renders the text `"Nincs találat"`

#### Scenario: Empty message suppressed while loading

- **GIVEN** the same widget with an in-flight fetch (`loading === true`)
- **WHEN** the user has just typed a query and the server has not yet responded
- **THEN** the dropdown paper renders the localized `loadingText` (MUI default acceptable for this change), NOT the `judo.autocomplete.no-results` message
- **AND** once the fetch settles with zero results, the paper transitions to the `judo.autocomplete.no-results` message

#### Scenario: Empty message does not appear alongside the more-results hint

- **GIVEN** the same widget configured with `autoCompleteLimit={10}`
- **WHEN** the server returns exactly 10 options
- **THEN** the JNG-6409 more-results hint is rendered
- **AND** the `judo.autocomplete.no-results` message is NOT rendered (because `options.length` is 10, not 0)

#### Scenario: Tags and TextWithTypeAhead follow the same contract

- **WHEN** the same zero-result condition holds for `Tags` or `TextWithTypeAhead`
- **THEN** both widgets render the same localized message via the same i18n key with the same fallback value

**Templates**:
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
- `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`

**i18n**: `judo.autocomplete.no-results` in `system_en-US.json.hbs`, `system_hu-HU.json.hbs`, `system_default.json.hbs`.
