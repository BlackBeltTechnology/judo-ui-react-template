## MODIFIED Requirements

### Requirement: Autocomplete dropdowns hint when the result list is truncated

Every generator-emitted MUI `<Autocomplete>` widget bound to a **server-paginated** option source — namely `SingleRelationInput`, `Tags`, and `TextWithTypeAhead` — SHALL render a non-interactive **footer** at the **bottom** of the dropdown paper, below the option listbox, whenever the returned options list has reached the limit injected by the caller. `SingleValueFilterComponent` is explicitly out of scope because its autocomplete is bound to a finite local enum array (no server pagination).

The footer SHALL be visible whether the search input is empty or non-empty — its sole gate is `options.length >= limit`. The footer SHALL be hidden when the returned options count is below the limit (i.e. the server is known to have returned every match).

The footer SHALL NOT participate in keyboard navigation and SHALL carry `pointerEvents: 'none'` so it cannot be clicked or hovered as if it were an option. It SHALL be marked `role="status"` so assistive technology is informed that the list was truncated — the message is real system status, not decoration, and SHALL NOT be hidden with `aria-hidden`.

The footer SHALL be visually distinguished from the option rows as an enclosed metadata region rather than a list item: a top divider (`borderTop: 1, borderColor: 'divider'`), a filled background, a reduced row height (`minHeight: 32`), and `caption` typography in `text.secondary`. It SHALL NOT use italic typography — the enclosing region, not the font style, carries the "this is not an option" signal.

The footer background SHALL be taken from the neutral grey ramp — `grey.300` in light mode, `grey.800` in dark mode — and SHALL NOT be any `action.*` token. The `action.*` tokens are interaction-state colours: `action.hover` resolves to the exact same fill an option receives while hovered, which makes a static footer indistinguishable from a hovered row, and its separation from `background.paper` (1.09:1 light) is too faint to read as a region at all.

The footer SHALL state the system's actual knowledge — how many results are shown — rather than issue an instruction alone. It SHALL source its label from the i18n key `judo.autocomplete.showing-first-results`, interpolating the caller-supplied limit as `{{limit}}`, with English default `"Showing the first {{limit}} results — narrow your search"`.

The caller container template SHALL pass the limit to the widget via an `autoCompleteLimit` prop (or, in the case of `Tags`, via the existing `limitOptions` prop which already drives the fetch). When the prop is absent or zero, the footer SHALL never render — preserving existing behaviour for any widget consumer that has not opted in.

#### Scenario: Footer rendered below the listbox when page is full

- **GIVEN** a `SingleRelationInput` whose caller passed `autoCompleteLimit={10}`
- **WHEN** the autocomplete search returns 10 options (regardless of whether the user has typed)
- **THEN** the dropdown paper renders the `<AutocompleteMoreResultsHint />` element **after** the listbox containing the 10 option items
- **AND** the footer reads "Showing the first 10 results — narrow your search" (or the active locale's translation)

#### Scenario: Footer hidden when fewer options than limit returned

- **GIVEN** the same widget with `autoCompleteLimit={10}`
- **WHEN** the autocomplete search returns 3 options
- **THEN** no footer is rendered; only the 3 option items are visible inside the listbox

#### Scenario: Footer visible on initial open with empty input

- **GIVEN** the same widget
- **WHEN** the user clicks into the field and the dropdown opens with the initial unfiltered batch (returns 10 options)
- **AND** the input value is still empty
- **THEN** the footer IS rendered below the 10 options, reporting that only the first 10 are shown

#### Scenario: Footer is outside the listbox role and unfocusable

- **GIVEN** the dropdown is open with the footer visible
- **WHEN** the user presses ArrowDown / ArrowUp to navigate options
- **THEN** focus moves only between `role="option"` elements; the footer is never reached
- **AND** the footer's container does not carry `role="option"` and is a sibling of the listbox element, never a child of it

#### Scenario: Footer reports the limit it was given

- **GIVEN** a widget whose caller passed `autoCompleteLimit={25}`
- **WHEN** the autocomplete search returns 25 options
- **THEN** the footer interpolates that same limit, reading "Showing the first 25 results — narrow your search"

#### Scenario: Footer is announced to assistive technology

- **GIVEN** the dropdown is open with the footer visible
- **WHEN** the footer is inspected in the DOM
- **THEN** it carries `role="status"` and does NOT carry `aria-hidden="true"`, so a screen-reader user learns that the result list was truncated

#### Scenario: Footer is visually enclosed, not styled as an option

- **GIVEN** the dropdown is open with the footer visible
- **WHEN** the footer is inspected in the DOM
- **THEN** it has a top divider and a filled background distinct from the option rows, and its computed `font-style` is `normal` (not italic)

#### Scenario: Footer fill is not confusable with a hovered option

- **GIVEN** the dropdown is open with the footer visible
- **AND** the pointer is hovering one of the option rows
- **WHEN** the footer's computed background is compared with the hovered option's computed background
- **THEN** the two differ — the footer uses the neutral grey ramp while the hovered option uses `action.hover`

**Key Helpers**: `UiWidgetHelper.calculateLinkAutocompleteRows()`, `UiWidgetHelper.calculateTextAutocompleteRows()` (new in this change)

**Templates**:
- `actor/src/components/widgets/SingleRelationInput.tsx.hbs`
- `actor/src/components/widgets/Tags.tsx.hbs`
- `actor/src/components/widgets/TextWithTypeAhead.tsx.hbs`
- `actor/src/components/widgets/AutocompleteMoreResultsHint.tsx.hbs` (new)

**i18n**: `judo.autocomplete.showing-first-results` in `system_en-US.json.hbs`, `system_hu-HU.json.hbs`, `system_default.json.hbs`.
