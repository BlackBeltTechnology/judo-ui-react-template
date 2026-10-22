# Doubt review — round 1

Fresh-context adversarial review of this change. The reviewers got only the artifact and the contract. The author re-checked every finding against git before classifying it.

| Role | Model | Result |
|---|---|---|
| `@propose-review-1` | claude-fable-5-1 | **skipped**: same family as the author (Claude) |
| `@propose-review-2` | glm-5.3 | 5 findings |
| `@propose-review-3` | deepseek-v4.1-flash | 6 findings |
| `@propose-review-4` | gpt-6.1-sol | 5 findings (delivered separately; part of the text was truncated) |

Classes: **A** = valid, actionable · **T** = valid trade-off · **N** = noise · **C** = contract misread.

## Findings

| # | Sev | Found by | Finding | Author's check | Class | Resolution |
|---|---|---|---|---|---|---|
| 1 | high | GLM, GPT | The in-flight `emit-canonical-table-testids` change still ADDs `browser-state-isolation` (every key namespaced as `judo:<ns>:`). Deleting `drop-branch-storage-namespacing` leaves the opposite requirement standing. | `ls emit-canonical-table-testids/specs` → `browser-state-isolation`; its proposal:88 and tasks 1.1–1.6 | A | Task 1.3: remove the `browser-state-isolation` delta from `emit-canonical-table-testids`, along with its proposal lines and tasks 1.1–1.6/7.3 namespacing parts (marked "withdrawn — JNG-6411 scope"). Only after that is `drop-branch-storage-namespacing` deleted. |
| 2 | high | DS, GLM | The proposal says "Modified Capabilities" but the deltas are `## ADDED`. `input-widgets` overlaps the baseline "Date/Time/DateTime Input" requirement. | `openspec/specs/input-widgets/spec.md` has that requirement | A | The new requirements are labelled as added to existing capabilities. The single-input date structure moves out (see #3), so it does not shadow "Date/Time/DateTime Input". |
| 3 | high | GPT | The spec requires single-input dates (`enableAccessibleFieldDOMStructure={false}`, 5 templates). That is a UI/a11y change, the same one the runtime moved to PR B. | `f4c258c0` touches 3 date/time widgets and 2 filter templates | A | The single-input part of `f4c258c0` is dropped; the follow-up PR carries `sectioned-date-fields` instead, mirroring runtime PR B. #604 keeps only the id on the value input and on the checkbox's inner input. Same scope question to the reviewer as on the runtime side. |
| 4 | high | DS | The storage pair does not net to zero: `ui-react.yaml` +5 (registers the `transfer-id.test.ts` copy) and `NoStorageNamespacingTest.java` +41. | `git diff --stat 19d9281b^ 4ebc5b35 -- <files>` → 2 files, +46 | A | Keep the `ui-react.yaml` hunk (transfer-id test registration, JNG-6411). Drop `NoStorageNamespacingTest.java` along with the feature. |
| 5 | medium | DS | `d188686f` (adds the `drop-branch-storage-namespacing` folder) is not on the drop list. | `git log -- openspec/changes/drop-branch-storage-namespacing` → `d188686f`, `4ebc5b35` | A | Drop set: `19d9281b`, `d188686f`, `4ebc5b35` (minus #4's yaml hunk). |
| 6 | medium | GPT | `861f9002` has no operator-vocabulary behaviour change; it only adds ids and threads the table identity through. Moving it to the follow-up would remove JNG-6411 plumbing. | `git show 861f9002`: `getOperatorsByFilter(filter)` unchanged; only `data-testid` / `buildFilterPanel…TestId` | A | `861f9002` **stays in #604 whole**. The proposal is corrected. |
| 7 | medium | DS | `0c23309c`: the confirmation id and the gating of bulk delete/clear sit in one commit and one file, so splitting is required, not optional. | `ConfirmationDialog.tsx.hbs` id plus the `BulkDeleteAction`/`ClearAction` empty-table gating | A | Task 1.1 states the split precisely: the `ConfirmationDialog`/`useCRUDDialog` id hunks stay; the empty-table gating in `BulkDeleteAction`/`ClearAction` and the eager confirm go to the follow-up. |
| 8 | medium | GLM, DS | "About 712 changed `.hbs` lines without a test id" cannot be reproduced. | `git diff <mb> <branch> -- '*.hbs'` → 863 added lines, 323 mention test-id/data-id, so **540** do not | A | Corrected: "540 of 863 added `.hbs` lines", with the command. |
| 9 | medium | GPT | `mvn -pl judo-ui-react -am verify` does not run `judo-ui-react-itest`, so the snapshots are neither regenerated nor checked. | root `pom.xml:81-82` separate modules; `emit-canonical-table-testids` 6.1/6.2 `-DforceSnapshotOverwrite=true` | A | Task 2.4: `mvn clean install -DskipPrepareNodeJS -DforceSnapshotOverwrite=true` (regenerate), then without the overwrite flag (check), across the whole reactor including the itest. |
| 10 | medium | GLM, DS | Cross-repo: the runtime `testid-contract` and the specs here restate the same grammar, and nothing checks parity. | the React template does not import `@judo/test-ids`; it mirrors it in `.hbs` | A | New task 3.3: after runtime PR A's rebase, compare the test-id helper output of `transfer-id.ts.hbs` / `test-id` builders with `@judo/test-ids` on a common fixture. The specs here reference the runtime capability. |
| 11 | low | DS | "exactly as the runtime": the React template sets `testId` on `MdiIcon`, the runtime sets `data-testid` on a MUI icon. | `MdiIcon.tsx.hbs:88` → `<span … data-testid={testId}>` | N | `MdiIcon` forwards it to `data-testid`, so it is the same attribute. The spec makes this explicit anyway ("an element with `data-testid`"). |

## Tally

11 findings: 10 A, 0 T, 1 N, 0 C. One round, as requested. A second round is recommended because of #1 and #3.
