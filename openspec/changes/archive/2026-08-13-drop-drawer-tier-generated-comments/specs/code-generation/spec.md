## ADDED Requirements

### Requirement: Generated code SHALL NOT carry design rationale as comments

Templates SHALL keep design rationale — why an approach was chosen, what defect it avoids, what alternatives were rejected — out of the generated output. Such rationale SHALL live in the `openspec/changes/**` documents for the change that introduced it.

Templates MAY emit a short comment where it explains something a reader of the generated file cannot otherwise recover, such as a non-obvious external constraint at the point of use. Rationale that explains a *decision* SHALL NOT be emitted.

Where a rationale comment would otherwise be needed to name a set of states, the generated code SHOULD make those states self-describing instead — for example a named union type whose members are the states.

This applies because generated output is duplicated into every consuming application, for every actor, and cannot be revised there: a comment that drifts out of step with the requirement it implements keeps contradicting it in every generated app.

#### Scenario: Rationale accompanies a non-obvious template construct

- **GIVEN** a template construct whose reasoning needs recording (for example a breakpoint-tier rule that exists to prevent a state latch)
- **WHEN** the template is written
- **THEN** the generated output contains the construct without the explanatory prose
- **AND** the reasoning is recorded in the `proposal.md` or `design.md` of the change that introduced it

#### Scenario: State names carried by the type system

- **GIVEN** a construct that would need a comment to enumerate its states
- **WHEN** the states can be expressed as a named union type
- **THEN** the generated code declares that type so the states are visible at the point of use
- **AND** no comment enumerating them is emitted

#### Scenario: Rationale comment removed from a shipped template

- **GIVEN** a template that already emits a rationale comment block into generated apps
- **WHEN** the comment is removed and its content moved into the change documents
- **THEN** regenerating a consuming application yields a file whose only differences are the removed comment lines
- **AND** no executable line changes
