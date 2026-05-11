## 1. DateTimePicker data-testid (Bug #1)

- [x] 1.1 In `datetimeinput.hbs`, remove `data-testid="{{ getXMIID child }}"` from the top-level `<DateTimePicker>` prop (line 23)
- [x] 1.2 In `datetimeinput.hbs`, add `'data-testid': '{{ getXMIID child }}'` inside `slotProps.textField.inputProps` (after the existing `slotProps.textField` block around line 34)
- [x] 1.3 Apply the same fix to `dateinput.hbs` and `timeinput.hbs` if they have the same pattern

## 2. ButtonGroup data-testid (Bug #3)

- [x] 2.1 In `buttongroup.hbs` line 53, add `data-testid="{{ getXMIID child }}"` as a new prop on `<DropdownButton>`
- [x] 2.2 Keep the existing `id="{{ getXMIID child }}-button-group"` for accessibility anchoring

## 3. Table column header data-testid (Bug #5)

- [x] 3.1 In `table/index.tsx.hbs`, add a `renderHeader` function to the `GridColDef` definition that wraps the header label in `<span data-testid="{{ getXMIID column }}">` — the Column's XMI ID already contains the full `/TableColumn/(discriminator/...)` pattern
- [x] 3.2 Verified: `getXMIID column` returns the composite ID matching VisualElementIds pattern (e.g., `God/(esm/_8AiKcE7tEeycO-gUAWxcVg)/TableColumn/(discriminator/God/(esm/_YT0hQE7rEeycO-gUAWxcVg)/TransferObjectTableTable)`)

## 4. Row View button (Bug #6)

- [x] 4.1 In `table/index.tsx.hbs` line 244, remove the `{{# unless button.actionDefinition.isOpenPageAction }}` guard and its closing `{{/ unless }}` on line 268
- [x] 4.2 Verify the View button renders with the correct `data-testid` matching `TransferObjectTableRowViewButton` / `TabularReferenceTableRowViewButton`

## 5. Snapshot updates

- [x] 5.1 Build the ActionGroupTest itest to generate new output
- [x] 5.2 Copy updated files from `target/frontend-react/` to `src/test/resources/snapshots/frontend-react/` for all affected itests
- [x] 5.3 Run the full itest suite to verify all snapshots pass

## 6. Verification

- [ ] 6.1 Run the DataTestIdValidation.spec.ts E2E tests against the regenerated ActionGroupTest frontend to confirm all 7 bugs are resolved
