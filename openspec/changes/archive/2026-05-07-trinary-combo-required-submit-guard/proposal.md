## Why

The previous fix (`JNG-6398`, archive `2026-05-04-trinary-combo-required`) corrected only the visual surface of `TrinaryLogicCombobox`: it hides the `Unknown` option and falls back to an empty Select value when `required = true`. The submit pathway was not touched, so a required boolean left untouched is still serialized as `undefined` and shipped to the backend, which has to reject it with `MISSING_REQUIRED_ATTRIBUTE`. That contradicts the project's own client-side validation pattern (`passesLocalValidation` in `actor/src/utilities/form-utils.ts.hbs`) and is what the user described as "frontend verification missing — if it's not filled out, it shouldn't be sent to the backend".

The pattern is also already half-implemented in the project: `UiPageHelper.getRequiredByWidgetsForPage()` only collects widgets whose `requiredBy` reference is set, and `hasPageRequiredBy()` gates whether `passesLocalValidation` is generated into the form action at all. Statically-required attributes (`attributeType.isRequired = true`) — including required booleans bound to `TrinaryLogicCombo` — are therefore never validated client-side. This change closes that gap for trinary combos specifically, using the existing pattern, without inventing a parallel mechanism.

## What Changes

- `UiPageHelper.getRequiredByWidgetsForPage()` SHALL also collect `TrinaryLogicCombo` widgets whose bound `BooleanType` attribute has `isRequired = true`, even when the widget has no `requiredBy` reference. Static required booleans enter `requiredByRecord` exactly the same way conditionally-required widgets do today.
- `hasPageRequiredBy()` becomes true for any page that contains either (a) a `requiredBy` widget (today's behavior) or (b) a statically-required `TrinaryLogicCombo` (new). This ensures `passesLocalValidation` is imported and called in the generated `Create*Action`/`Update*Action`/`InputForm*` fragments.
- `local-validate.fragment.hbs` SHALL emit, for each statically-required trinary combo, a `requiredByRecord[attrName]: true` entry (no dependency on a `requiredBy` field's runtime value). The literal `true` makes intent explicit in the generated code and keeps the existing `passesLocalValidation` helper unchanged.
- `TrinaryLogicCombobox.tsx.hbs` SHALL preserve the `data-testid={`${id}-undefined`}` testid surface even when `required = true`. The `Unknown` `<MenuItem>` SHALL still be omitted from the option list, but the testid SHALL be re-attached to a non-rendered or hidden anchor (e.g. `display: 'none'` MenuItem) so existing standardized-test-data-ID consumers (JNG-6381) keep working. **No behavior change for end users.**
- A new integration-test scenario in `ActionGroupTest` SHALL cover the submit-guard contract:
  - The generated `ViewGalaxyForm.tsx` action snapshots SHALL include `discoveryConfirmed: !!data.{{ requiredBy.name }}`-style entries for `requiredBy` cases AND `discoveryConfirmed: true` (the new static-required entry) for the statically-required boolean.
  - The page action (`Create*` / `Update*`) SHALL import `passesLocalValidation`.
- The `input-widgets` capability spec SHALL gain submit-guard scenarios for required trinary combos and explicitly state filter-context exemption.

No metamodel change. No new template parameter. No new i18n keys. No widget API change beyond an internal testid preservation refactor.

## Capabilities

### Modified Capabilities
- `input-widgets`: The `Trinary Logic Combo` requirement gains a submit-guard rule (statically-required → blocked from submission when null/undefined) and a testid-preservation rule.

## Impact

- `judo-ui-react/src/main/java/hu/blackbelt/judo/ui/generator/react/UiPageHelper.java` — extend `getRequiredByWidgetsForPage()` to also collect statically-required `TrinaryLogicCombo` widgets; keep ordering deterministic.
- `judo-ui-react/src/main/resources/actor/src/fragments/page/local-validate.fragment.hbs` — emit a literal `true` entry in `requiredByRecord` when the widget is statically-required and has no `requiredBy`; keep existing `requiredBy`-driven entries unchanged.
- `judo-ui-react/src/main/resources/actor/src/components/widgets/TrinaryLogicCombobox.tsx.hbs` — re-attach the `${id}-undefined` testid when `required = true`, e.g. on a `<MenuItem sx={ { display: 'none' } } value={'__hidden__'} data-testid={`${id}-undefined`} />` so query-by-testid keeps working but the option is unselectable.
- `judo-ui-react-itest/ActionGroupTest/...` — refresh `ViewGalaxyForm.tsx.snapshot` and the corresponding page-action snapshot (`*ViewGalaxyPage*` `Create`/`Update` action file) to capture (a) the new `passesLocalValidation` import and call, (b) the `requiredByRecord` entry for `discoveryConfirmed`, (c) the preserved testid in the widget. Verify Pro itest is not affected (it is not exercised here; mirrored demo remains a separate follow-up as in the previous archive).
- `openspec/specs/input-widgets/spec.md` — augment the `Trinary Logic Combo` requirement with the submit-guard rule, testid-preservation rule, and corresponding scenarios.
- No breaking change for end users. No breaking change for filter contexts (no `required` is passed there → no submit-guard entries added → 3-option behavior preserved).
- Migration note for downstream apps: any custom action that previously shipped a `discoveryConfirmed: undefined` payload will now be blocked at the form level with the standard `MISSING_REQUIRED_ATTRIBUTE` validation message; downstream clients should not depend on the backend rejection roundtrip for this specific shape.
