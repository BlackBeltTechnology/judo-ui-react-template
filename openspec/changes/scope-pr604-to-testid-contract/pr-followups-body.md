# JNG-6411 React template: behaviour aligned with the runtime

The goal of JNG-6411 is that one Tatami suite runs unchanged against both frontends: the app this template generates and `judo-frontend-runtime`. #604 makes the two emit the same test ids. This PR covers the places where they still **behaved** differently, so a shared spec could pass on only one engine. **Both engines now do the same thing, as the shared Tatami spec expects.** It is the template counterpart of runtime [#80](https://github.com/BlackBeltTechnology/judo-frontend-runtime/pull/80) (PR B), which aligns the runtime in the other direction. The test-id work is in #604 here and in [#78](https://github.com/BlackBeltTechnology/judo-frontend-runtime/pull/78) (PR A) on the runtime.

**Draft until #604 merges**, because it is stacked on it. After that, retarget it to `develop`.

**Merge together with:**
- `judo-frontend-runtime` [#80](https://github.com/BlackBeltTechnology/judo-frontend-runtime/pull/80): `sectioned-date-fields` and the confirmation policy (`b4c211c`)
- `judo-tatami-tests`: `keyboard-temporal-entry`

If only one side lands, the date and confirmation specs break on at least one engine.

## Behaviour changes

These are visible to users:

- **Dates:** date, time and date-time fields keep MUI X's sectioned field (day, month and year as separate sections) instead of being forced into a single plain input. MUI X v9 removed the single-input option, so the runtime cannot offer it, and the template would lose it at its own v9 upgrade anyway. Typing a date works the same on both engines: click the field, select all, type the digits. The date pickers in the filter dialog and in the fixed table filters follow suit.
- **Confirmations:** clearing a collection now asks first, and so does bulk-deleting rows from an eager table (the lazy table already asked through its bulk dialog). Unsetting a single relation still does not ask. This is the same rule as on the runtime: delete and clear always ask; remove and unset ask only when the model sets `confirmationType`.
- **Single relation field:**
  - The **Create** button stays available while the owner is being edited. Before, it was disabled in edit mode unless the relation was inline-creatable on a saved owner, so after an **Unset** the field could not be filled again without saving first. The create is staged into the owner and saved with it. The runtime's link never disables its buttons for edit mode, only for `enabledBy`.
  - The field shows **every modeled display part** (for example "Andromeda 2024-05-12"), not only the autocomplete attribute. Dates are formatted. This is how the runtime's link labels its value.

  No dedicated Tatami spec covers these two yet. The stored-containment spec in `CrudActionsOnSingleRelationsTest` that exercises create on a link is still skipped because of a runtime gap. They are here because the two engines disagreed on the same model.

## Test ids that come with these

- `field::<id>` sits on the date field's root and `field::<id>::input` on its value input, where the runtime has them.
- Date-like fixed filters carry `table::<id>::fixed-filter::<attribute>` on the root and `::input` on the value input. The other fixed-filter ids are in #604.

## Commits

| Commit | Change | Runtime counterpart |
|---|---|---|
| `fix(react): confirm before clearing a collection or bulk-deleting rows` | Confirm before clear and before an eager bulk delete | `b4c211c` |
| `feat(react): keep the sectioned date field and give it runtime ids` | Sectioned date fields, including the date filter pickers | `sectioned-date-fields` |
| `docs(openspec): add the sectioned-date-fields change` | OpenSpec change `sectioned-date-fields` | |
| `fix(react): offer create on a link while editing, and label it by every part` | Link create in edit mode, label from every part | `LinkRenderer` (`4b2bf2d`; `fa3bd88` for the date formatting) |
| `test(react): refresh itest snapshots for the follow-ups` | Snapshots | |

## Verification

- `mvn clean install -DskipPrepareNodeJS -DforceSnapshotOverwrite=true`, then `mvn clean install -DskipPrepareNodeJS`: BUILD_RESULT_PLACEHOLDER.
- `FieldTemplateTest` asserts the sectioned structure (no opt-out flag, no legacy text-field props) and the `htmlInput` id. `DialogTemplateTest` asserts the two confirmations, and `LinkTemplateTest` asserts the link changes.
- Acceptance is the Tatami run on the React stack built from #604 plus this branch, compared with the runtime A+B run (`react-template-e2e-parity`, `keyboard-temporal-entry` group 3).
