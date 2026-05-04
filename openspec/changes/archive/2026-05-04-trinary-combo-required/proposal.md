## Why

The `TrinaryLogicCombobox` widget (used for `ui:TrinaryLogicCombo` form inputs and as filter UI for boolean attributes) always renders three options: `Yes`, `No`, and `Unknown` (the empty/`undefined` placeholder). It accepts a `required` prop and forwards it to MUI's `<TextField required>`, but does not use it to gate the option set.

For required boolean attributes this is wrong: the user must pick `true` or `false`, so the `Unknown` option is misleading — it lets the user "pick" a value that the form will then reject in validation, which is bad UX.

The codebase already encodes this exact distinction for tables: `UiWidgetHelper.getCellEditType()` returns `"boolean"` for required booleans (renders MUI's `GridEditBooleanCell`, true/false only) and `"optionalBoolean"` for nullable ones (3-state select). Forms simply did not pick up this convention.

Additionally, no integration test in `judo-ui-react-itest` currently models a `ui:TrinaryLogicCombo`, so the bug is invisible to snapshot regression and would silently regress on any future template change.

## What Changes

- The `TrinaryLogicCombobox` React component SHALL render only the `Yes` and `No` options when the `required` prop is `true`. The `Unknown` option SHALL be rendered only when `required` is `false` (the default).
- When `required` is `true` and the underlying attribute value is `null`/`undefined`, the MUI `<Select>` `value` prop SHALL be `''` (empty string) so MUI does not warn about an out-of-range value. The visible field stays empty until the user picks `Yes`/`No`, and the existing required-validation handles submission.
- All three call sites (form input fragment, filter dialog, single-value column filter) keep passing `required` exactly as today. The form fragment already computes `required` from `isRequired` plus dynamic `isXxxRequired` actions and `requiredBy`. Filter call sites do not pass `required` and therefore default to `false`, preserving the 3-option behavior that filters need ("don't filter on this").
- A demo of both modes SHALL be added to the `ActionGroupTest` integration test so that:
  - the generated React app contains a visible required-boolean trinary combo and an optional-boolean trinary combo side-by-side, and
  - the snapshot regression captures `required={...}` at the call site for both modes, locking in that the form fragment passes the correct value.

## Capabilities

### Modified Capabilities
- `input-widgets`: The `Trinary Logic Combo` requirement gains an option-set rule keyed on the bound attribute's `isRequired` flag — required → 2 options, optional → 3 options.

## Impact

- `judo-ui-react/src/main/resources/actor/src/components/widgets/TrinaryLogicCombobox.tsx.hbs` — conditionally render the `Unknown` `<MenuItem>`; adjust the `Select`'s `value` resolution when required.
- `judo-ui-react-itest/ActionGroupTest/model/ActionGroupTest-ui.model` — add a required-boolean and an optional-boolean attribute (or reuse existing optional ones) on a visible entity (e.g. `Galaxy`) and bind two `ui:TrinaryLogicCombo` visual elements to them inside an existing form so the user can `pnpm dev` and inspect both side-by-side.
- `judo-ui-react-itest/ActionGroupTest/action_group_test__god/src/test/resources/snapshots/...` — refresh affected container snapshots after the model addition.
- `openspec/specs/input-widgets/spec.md` — update the `Trinary Logic Combo` requirement text and scenarios.
- No breaking changes for existing usages: all current `TrinaryLogicCombobox` call sites either pass a `required` derived from `isRequired` (form input fragment) or omit `required` (filter contexts, default `false`). Existing nullable boolean fields behave identically.
- No metamodel change. No Java helper change. No new template parameter. No new i18n keys (all three labels already exist as `judo.component.TrinaryLogic.{true,false,unknown}`).
