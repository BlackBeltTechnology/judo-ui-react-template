# AI Agent Processing Guide

This guide explains how AI agents should process the specification files to implement the runtime model generation system.

## Overview

The specifications are organized into **10 domains** with **109 total files** that can be processed **in parallel** using **up to 13 agents** to complete in **6-9 hours** instead of 20-30 hours sequentially.

## Processing Phases

### Phase 1: Foundation (2-3 hours)

**Parallel Execution:**

```yaml
Agent1A:
  domain: metamodel
  files: [01-04]  # Base classes
  depends_on: []
  
Agent1B:
  domain: data-model
  files: [01-08]  # All data model specs
  depends_on: []

Agent1C:
  domain: metamodel
  files: [05-08]  # Container elements
  depends_on: [Agent1A]

Agent1D:
  domain: metamodel
  files: [09-15]  # Visual components
  depends_on: [Agent1A]
```

**Completion Criteria:**
- All metamodel elements documented
- All data model elements documented
- Foundation for runtime model established

### Phase 2: Runtime Model (1-1.5 hours)

**Sequential Start, Then Parallel:**

```yaml
Agent2A:
  domain: runtime-model
  files: [01]  # Core types first
  depends_on: [Agent1A, Agent1B, Agent1C, Agent1D]

Agent2B:
  domain: runtime-model
  files: [02, 03, 04]
  depends_on: [Agent2A]

Agent2C:
  domain: runtime-model
  files: [05, 06, 07, 08, 09]
  depends_on: [Agent2A]

Agent2D:
  domain: runtime-model
  files: [10]  # Utilities last
  depends_on: [Agent2B, Agent2C]
```

**Completion Criteria:**
- All TypeScript interfaces defined
- Model validation logic specified
- Examples provided

### Phase 3: Visual Elements & Actions (3.5-5 hours)

**Highly Parallel:**

```yaml
Agent4:
  domain: visual-elements/inputs
  files: [01-10]
  depends_on: [Agent2D]

Agent5:
  domain: visual-elements/containers
  files: [01-05]
  depends_on: [Agent2D]

Agent6:
  domain: visual-elements/tables
  files: [01-05]
  depends_on: [Agent2D]

Agent7:
  domain: visual-elements/other
  files: [01-05]
  depends_on: [Agent2D]

Agent8A:
  domain: actions
  files: [01-03]  # Core first
  depends_on: [Agent2D]

Agent8B:
  domain: actions
  files: [04-07]  # CRUD actions
  depends_on: [Agent8A]

Agent8C:
  domain: actions
  files: [08-11]  # Relation actions
  depends_on: [Agent8A]

Agent8D:
  domain: actions
  files: [12-14]  # Navigation actions
  depends_on: [Agent8A]

Agent8E:
  domain: actions
  files: [15]  # Operation actions
  depends_on: [Agent8A]

Agent9:
  domain: validation
  files: [01-05]
  depends_on: [Agent2D]
```

**Completion Criteria:**
- All visual element specs complete
- All action specs complete
- Validation system specified

### Phase 4: Components (6-8 hours, sequential)

```yaml
Agent10:
  domain: components
  files: [01-12]  # Sequential processing
  depends_on: [Agent4, Agent5, Agent6, Agent7, Agent8E, Agent9]
```

**Completion Criteria:**
- All React components specified
- Hook interfaces defined
- Integration patterns clear

### Phase 5: Generators (5-7 hours, sequential)

```yaml
Agent11:
  domain: generators
  files: [01-10]
  depends_on: [Agent10]
```

**Completion Criteria:**
- Java helper methods specified
- Handlebars templates documented
- Generation logic complete

### Phase 6: Integration & Examples (2-3 hours)

**Parallel:**

```yaml
Agent12:
  domain: integration
  files: [01-05]
  depends_on: [Agent10]

Agent13:
  domain: examples
  files: [01-04]
  depends_on: [Agent10, Agent11]
```

**Completion Criteria:**
- Integration points documented
- Complete examples provided
- System validated end-to-end

## Processing a Specification File

### Step 1: Read Dependencies

```bash
# Check dependencies section
grep "^**Dependencies:**" specifications/domain/file.md

# Ensure all dependencies are complete
check_dependencies(dependencies)
```

### Step 2: Parse Metamodel

```bash
# For metamodel specs, extract from ui.ecore
parse_ecore_element(className)

# Document all properties, operations, relationships
```

### Step 3: Define Runtime Model

```typescript
// Generate TypeScript interface
export interface [Name]Model {
  // Map all metamodel properties
  // Add computed properties
  // Define relationships as string references
}
```

### Step 4: Specify Implementation

```markdown
## Generator Implementation

### Java Helper Methods
```java
public static [Type] extract[Name]([MetamodelClass] element) {
  // extraction logic
}
```

### Handlebars Template
```handlebars
{{! template structure }}
```

### React Component
```typescript
export function [Name]Component({ model, ...props }) {
  // component logic
}
```
```

### Step 5: Provide Examples

```markdown
## Examples

### Example 1: [Scenario]
[Complete example with metamodel → model → component]

### Example 2: [Edge Case]
[Handle special scenario]
```

### Step 6: Define Tests

```markdown
## Testing Criteria

### Unit Tests
- [ ] Test case 1
- [ ] Test case 2

### Integration Tests
- [ ] Test scenario 1
- [ ] Test scenario 2

### Edge Cases
- [ ] Edge case 1
- [ ] Edge case 2
```

### Step 7: Mark Complete

```markdown
**Status:** Complete
**Reviewed By:** [Agent ID or Human]
**Completion Date:** [Date]
```

## Quality Gates

### Before Moving to Next Phase

**Phase 1 → Phase 2:**
- [ ] All metamodel base classes documented
- [ ] All data model elements documented
- [ ] Peer review complete

**Phase 2 → Phase 3:**
- [ ] All runtime model interfaces defined
- [ ] Type validation logic specified
- [ ] Cross-references added

**Phase 3 → Phase 4:**
- [ ] All visual element models defined
- [ ] All action models defined
- [ ] Validation system specified

**Phase 4 → Phase 5:**
- [ ] All React components specified
- [ ] Hook interfaces complete
- [ ] State management clear

**Phase 5 → Phase 6:**
- [ ] All generators specified
- [ ] Template structure defined
- [ ] Java helpers documented

**Phase 6 → Complete:**
- [ ] Integration points documented
- [ ] Examples validated
- [ ] Full system review complete

## Communication Between Agents

### Shared Status File

```yaml
# specifications/STATUS.yaml
phase: 3
domains:
  metamodel:
    status: complete
    files_complete: 15/15
    assigned_agents: [Agent1A, Agent1C, Agent1D]
  runtime-model:
    status: complete
    files_complete: 10/10
    assigned_agents: [Agent2A, Agent2B, Agent2C, Agent2D]
  visual-elements:
    status: in-progress
    files_complete: 12/25
    assigned_agents: [Agent4, Agent5, Agent6, Agent7]
  # ...
```

### Dependency Resolution

```python
def can_start(spec_file):
    dependencies = parse_dependencies(spec_file)
    return all(is_complete(dep) for dep in dependencies)

def next_available_work(agent_id):
    for spec_file in unassigned_specs:
        if can_start(spec_file):
            assign(spec_file, agent_id)
            return spec_file
    return None
```

## Output Validation

### Automated Checks

```bash
# Run on each completed file
./validate-spec.sh specifications/domain/file.md

# Checks:
# - All required sections present
# - TypeScript syntax valid
# - Examples compile
# - Cross-references valid
# - Metamodel coverage complete
```

### Peer Review

Each spec should be reviewed by:
1. **Another agent** (for technical accuracy)
2. **Human reviewer** (for design decisions)

### Integration Tests

After each phase:
```bash
# Generate code from specs
./generate-from-specs.sh phase-3

# Run tests
npm test

# Validate output
./validate-output.sh
```

## Troubleshooting

### Circular Dependencies

If Agent X is waiting for Agent Y, and Agent Y is waiting for Agent X:

1. Identify the cycle
2. Refactor one spec to remove dependency
3. Add clarifying comment
4. Update dependency graph

### Missing Information

If metamodel information is unclear:

1. Check ui.ecore directly
2. Check sample model (ActionGroupTest-ui.model)
3. Check original proposals
4. Document assumption
5. Flag for human review

### Inconsistencies

If specifications conflict:

1. Document the conflict
2. Propose resolution
3. Update affected specs
4. Mark for review

## Progress Tracking

### Daily Updates

```markdown
# specifications/PROGRESS.md

## 2025-11-28

### Completed
- metamodel/01-named-element.md (Agent1A)
- metamodel/02-visual-element.md (Agent1A)
- data-model/01-class-type.md (Agent1B)

### In Progress
- metamodel/03-labeled-element.md (Agent1A) - 70%
- data-model/02-relation-type.md (Agent1B) - 50%

### Blocked
- runtime-model/02-page-model.md (Agent2B) - waiting for Agent1C

### Issues
- Circular dependency between Link and Table - flagged for review
```

## Final Checklist

Before considering the specifications complete:

- [ ] All 109 files created
- [ ] All files marked "Complete"
- [ ] All cross-references valid
- [ ] All examples work
- [ ] All tests pass
- [ ] Human review complete
- [ ] Documentation reviewed
- [ ] Integration validated
- [ ] Performance acceptable
- [ ] Ready for implementation

## Estimated Timeline

| Phase | Parallel | Sequential |
|-------|----------|------------|
| Phase 1 | 2-3 hours | 10-13 hours |
| Phase 2 | 1-1.5 hours | 4-5 hours |
| Phase 3 | 3.5-5 hours | 12-18 hours |
| Phase 4 | 6-8 hours | 6-8 hours |
| Phase 5 | 5-7 hours | 5-7 hours |
| Phase 6 | 2-3 hours | 4-5 hours |
| **Total** | **20-28.5 hours** | **41-56 hours** |

With 13 agents working in parallel, wall-clock time: **6-9 hours**

