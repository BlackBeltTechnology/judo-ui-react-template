## MODIFIED Requirements

### Requirement: Autocomplete dropdowns hint when the result list is truncated

Every generator-emitted MUI `<Autocomplete>` widget bound to a **server-paginated** option source — namely `SingleRelationInput`, `Tags`, and `TextWithTypeAhead` — SHALL render a non-interactive **header** at the **top** of the dropdown paper, above the option listbox, whenever the returned options list has reached the limit injected by the caller. `SingleValueFilterComponent` is explicitly out of scope because its autocomplete is bound to a finite local enum array (no server pagination).

The header SHALL be visible whether the search input is empty or non-empty — its sole gate is `options.length >= limit`. The header SHALL be hidden when the returned options count is below the limit (i.e. the server is known to have returned every match).

The header SHALL NOT participate in keyboard navigation, SHALL be marked `aria-hidden="true"` with `pointerEvents: 'none'`, SHALL render its label in **italic typography** (`font-style: italic`) so the user reads it as a tip rather than a selectable option, and SHALL source its label from the i18n key `judo.autocomplete.type-for-more-results` with English default `"Type for more results…"`.

The caller container template SHALL pass the limit to the widget via an `autoCompleteLimit` prop (or, in the case of `Tags`, via the existing `limitOptions` prop which already drives the fetch). When the prop is absent or zero, the header SHALL never render — preserving existing behaviour for any widget consumer that has not opted in.

#### Scenario: Header rendered above the listbox when page is full

- **GIVEN** a `SingleRelationInput` whose caller passed `autoCompleteLimit={10}`
- **WHEN** the autocomplete search returns 10 options (regardless of whether the user has typed)
- **THEN** the dropdown paper renders the `<AutocompleteMoreResultsHint />` element **before** the listbox containing the 10 option items
- **AND** the header reads "Type for more results…" (or the active locale's translation)

#### Scenario: Header hidden when fewer options than limit returned

- **GIVEN** the same widget with `autoCompleteLimit={10}`
- **WHEN** the autocomplete search returns 3 options
- **THEN** no header is rendered; only the 3 option items are visible inside the listbox

#### Scenario: Header visible on initial open with empty input

- **GIVEN** the same widget
- **WHEN** the user clicks into the field and the dropdown opens with the initial unfiltered batch (returns 10 options)
- **AND** the input value is still empty
- **THEN** the header IS rendered above the 10 options, advising the user to type to narrow the results

#### Scenario: Header is outside the listbox role and unfocusable

- **GIVEN** the dropdown is open with the header visible
- **WHEN** the user presses ArrowDown / ArrowUp to navigate options
- **THEN** focus moves only between `role="option"` elements; the header is never reached
- **AND** the header's container does not carry `role="option"`; assistive technology announces only the option items

#### Scenario: Header text is rendered in italic

- **GIVEN** the dropdown is open with the header visible
- **WHEN** the header is inspected in the DOM
- **THEN** its computed `font-style` is `italic`, visually distinguishing the tip from the regular (non-italic) option labels

**Key Helpers**: `UiWidgetHelper.calculateLinkAutocompleteRows()`, `UiWidgetHelper.calculateTextAutocompleteRows()` (new in this change)

**Templates**:
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
- `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`
- `actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs` (new)

**i18n**: `judo.autocomplete.type-for-more-results` in `system_en-US.json.hbs`, `system_hu-HU.json.hbs`, `system_default.json.hbs`.
