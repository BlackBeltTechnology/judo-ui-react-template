# Runtime Model Domain Specifications

**Purpose:** Define TypeScript interfaces for runtime model structures that represent metamodel elements in a JSON-serializable format.

**Status:** 0/10 Complete

**Processing:** Sequential after Metamodel domain complete

## Files in this Domain

1. `01-core-types.md` - Shared types, enums, utility types
2. `02-page-model.md` - PageModel interface
3. `03-container-model.md` - ContainerModel interface
4. `04-visual-element-model.md` - VisualElementModel and type-specific configs
5. `05-action-model.md` - ActionModel and subtypes
6. `06-button-model.md` - ButtonModel and ButtonGroupModel
7. `07-table-model.md` - TableModel, ColumnModel, FilterModel
8. `08-validation-model.md` - ValidationRuleModel structures
9. `09-i18n-model.md` - I18nModel structure
10. `10-model-utilities.md` - Helper types and utilities

## Dependencies

**External:**
- `metamodel/*` - Must understand metamodel first

**Internal:**
- File 01 must be done first (defines shared types)
- Files 02-09 can be done in parallel after 01
- File 10 done last (uses all others)

## Assignment Recommendations

### Sequential
Process 01 → 02-09 (parallel) → 10

### Parallel (3 agents)
- **Agent 2A:** Files 01, 10
- **Agent 2B:** Files 02, 03, 04
- **Agent 2C:** Files 05, 06, 07, 08, 09

Agent 2B and 2C wait for Agent 2A to complete file 01

## Key Concepts

### Model vs Implementation

**Model (what we specify here):**
```typescript
interface VisualElementModel {
  id: string;
  type: string;
  name: string;
  // ... data only
}
```

**Component (specified in components domain):**
```typescript
function ModelDrivenInput({ model, data, ... }) {
  // ... behavior
}
```

### Serialization

All model types must be:
- JSON-serializable
- No functions
- No React elements
- References by string ID only

## Completion Criteria

For each file:
- [ ] TypeScript interfaces complete
- [ ] All metamodel properties mapped
- [ ] Default values specified
- [ ] Optional vs required clearly marked
- [ ] Type guards provided (if needed)
- [ ] Validation schema defined
- [ ] Example model instances provided
- [ ] Cross-references to metamodel specs

## Output Format

```markdown
# [Model Name] Specification

**Domain:** Runtime Model
**Status:** [Draft|Complete]
**Dependencies:** [List spec files]

## Purpose
[What this model represents]

## TypeScript Definition

\`\`\`typescript
export interface [ModelName] {
  // ... complete interface
}
\`\`\`

## Property Details

### [propertyName]
- **Type:** ...
- **Required:** Yes/No
- **Default:** ...
- **Metamodel Source:** [Ecore property]
- **Description:** ...

## Metamodel Mapping

| Runtime Property | Metamodel Property | Transformation |
|-----------------|-------------------|----------------|
| ... | ... | ... |

## Example Instance

\`\`\`typescript
const example: [ModelName] = {
  // ... complete example
};
\`\`\`

## Validation

\`\`\`typescript
function validate[ModelName](model: [ModelName]): ValidationError[] {
  // ... validation logic
}
\`\`\`

## Related Specifications
- [Links]
```

## Processing Time

- **Estimated time per file:** 45-60 minutes
- **Sequential:** 7.5-10 hours
- **Parallel (3 agents):** 3-4 hours

