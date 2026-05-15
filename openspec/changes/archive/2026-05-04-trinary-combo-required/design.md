## Context

`TrinaryLogicCombobox.tsx.hbs` is a single React component used in three contexts:

| Call site | Path | `required` passed? | Intended option count |
|---|---|---|---|
| Form / page input | `actor/src/containers/widget-fragments/trinarylogiccombo.hbs` | yes — derived from `isRequired` + dynamic `isXxxRequired` action + `requiredBy` | required → 2; optional → 3 |
| Filter dialog | `actor/src/components/dialog/FilterDialog.tsx.hbs` | no (default `false`) | always 3 (Unknown = "no filter") |
| Single-value column filter | `actor/src/components/table/SingleValueFilterComponent.tsx.hbs` | no (default `false`) | always 3 |

The component currently accepts `required`, forwards it to `<TextField required={required}>`, but always renders the `Unknown` `<MenuItem>` regardless. The fix is purely inside the component file. No call site changes.

The codebase already has the required/optional distinction baked into the table cell editor path:

```java
// UiWidgetHelper.java
if (dataType instanceof BooleanType) {
    if (!column.getAttributeType().isIsRequired()) {
        return "optionalBoolean";   // -> BooleanSelectEditInputCell (3 options)
    }
    return "boolean";               // -> GridEditBooleanCell (2 options)
}
```

So the form/page widget is the last remaining inconsistency.

## Goals / Non-Goals

**Goals:**
- For required boolean trinary combos, hide the `Unknown` option and keep the user in true/false land.
- Preserve the existing 3-option behavior for filter contexts (where "Unknown" semantically means "do not filter on this attribute").
- Add an integration-test demo so the change is verifiable both visually (run the generated app) and by snapshot diff (the `required={...}` call-site value is locked in).
- Keep the change reactive: a runtime-required field (`isXxxRequired(...)` action returning true based on data) must update its option set when `required` flips.

**Non-Goals:**
- Add a new metamodel attribute or a new widget type. The model already distinguishes `TrinaryLogicCombo` (nullable boolean intent) from `Checkbox` (non-nullable boolean intent). Modelers who want a "no Unknown ever" widget can already pick `Checkbox`.
- Touch `judo-meta-esm` or `judo-meta-ui`. All needed concepts already exist upstream: `BooleanType` and `required` are defined in `judo-meta-esm` (`esm.ecore`); `TrinaryLogicCombo` is defined in `judo-meta-ui` (`ui.ecore`); the UI XMI in `judo-ui-react-itest/ActionGroupTest/model/ActionGroupTest-ui.model` is hand-maintained and consumed directly by `judo-ui-generator-maven-plugin`, with no ESM-to-UI transform step in the itest build that would overwrite hand-edits.
- Change filter UX. Filters intentionally show `Unknown` to express "no filter on this attribute"; making filters honor a backend `isRequired` flag would broaden scope and is not the user-reported bug.
- Add an automated render-time unit test inside the component. The generated app does not ship Vitest/Jest harnesses, so behavior is verified by (a) snapshot of the call site in containers, and (b) manual `pnpm dev` inspection of the demo screen in `ActionGroupTest`.
- Default-select `false` for required booleans. Submitting a value the user did not consciously pick is worse than letting required-field validation surface the missing pick.

## Decisions

### Decision 1: Conditionally render the `Unknown` `<MenuItem>` rather than splitting into two components

```tsx
<MenuItem ... value={'Yes'}> ... </MenuItem>
<MenuItem ... value={'No'}> ... </MenuItem>
{!required && (
  <MenuItem ... data-testid={`${id}-undefined`} value={'Unknown'}> ... </MenuItem>
)}
```

**Rationale.** A second component (`BinaryLogicCombobox`?) would force every call site to choose, would break the existing 3 call sites, and would force `widget-fragments/trinarylogiccombo.hbs` to branch on `child.attributeType.isRequired`. The conditional `MenuItem` keeps the change to one file and one prop, leverages the existing reactive `required` flow, and matches MUI idioms (Select children are commonly conditional).

**Alternative rejected.** Build the menu item array imperatively inside the component body. Equivalent at runtime, less idiomatic in the codebase (other widgets render MenuItems inline).

### Decision 2: Empty-string fallback for the Select value when required + null/undefined

`TRINARY_LOGIC.get(undefined)` and `TRINARY_LOGIC.get(null)` return `'Unknown'`. If we hide the `Unknown` `<MenuItem>` while leaving the resolved Select value at `'Unknown'`, MUI logs a "value is out of range" warning and the rendered field shows nothing useful.

Resolution:
```tsx
value={(required && (value === null || value === undefined))
  ? ''
  : (TRINARY_LOGIC.get(value) ?? '')}
```

**Rationale.** This makes "required + no choice yet" visually equivalent to a fresh Select, which is what the user expects. Required-field validation surfaces the unfilled state at submit time.

**Alternative rejected.** Force `value` to `false` when required + null. This silently submits `false` if the user never opens the dropdown — actively wrong. Rejected.

### Decision 3: Demo placement in `ActionGroupTest`

Add two `ui:TrinaryLogicCombo` visual elements to an existing visible form in `ActionGroupTest` (concretely: a form on the `Galaxy` view, alongside the existing checkboxes). Add the supporting boolean attributes (one `isRequired="true"`, one without) as plain mapped attributes.

**Rationale.**
- `ActionGroupTest` already has a rich Galaxy form with multiple booleans, so adding two more next to them is a minimal, contextually appropriate change and is what the user asked for ("`adjun hozzá majd /…/ActionGroupTest részben pl. egy részt ahol megtudom nézni`").
- It activates the previously dead code path: no other itest models a `TrinaryLogicCombo`, so the snapshot machinery has never witnessed this widget. After the change, any future regression to either the form fragment or the component shows up as either a snapshot diff or a visible UI break.
- Snapshots only cover container files (`src/containers/...`, `src/dialogs/...`), not `src/components/widgets/TrinaryLogicCombobox.tsx`. So the snapshot diff verifies the *call site* (`required={...}` is the right value) but not the option-set logic. The option-set logic is verified by visual check on `pnpm dev`. That is the verification triangle: Java unit/integration build → snapshot diff at call site → visual run-time check on the demo widgets.

**Alternative rejected.** Add a brand-new itest module just for the trinary combo. Higher cost, no extra coverage value over reusing `ActionGroupTest`.

## Risks

- **Snapshot churn.** Adding model attributes triggers diffs in any container that renders the affected entity (Galaxy). We mitigate by scoping the new attributes to `Galaxy` only and updating snapshots in one task.
- **Pro itest parity.** `ActionGroupTestPro` shares model conventions but lives in a separate module. The proposal targets `ActionGroupTest` only (community MUI). If the user wants the same demo for the Pro module, that's a follow-up — flagged in tasks.
- **Backward-compat with stored data.** Existing required-boolean-bound `ui:TrinaryLogicCombo` instances in customer models would have always shown `Unknown` until now. After this change, when a stored value is `null` for a required boolean, the field renders empty rather than as `Unknown`. The customer experience strictly improves (validation now matches model intent), but it's worth flagging in the migration note in the changelog.
