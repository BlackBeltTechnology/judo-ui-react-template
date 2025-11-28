# Specification Template

> **Instructions:** Copy this template when creating a new specification file. Replace all [bracketed] placeholders. Delete sections that don't apply, but keep the structure.

---

# [Element/Component Name] Specification

**Domain:** [Metamodel | Runtime Model | Visual Elements | Actions | Components | Generators | Validation | Data Model | Integration | Examples]  
**Subdomain:** [If applicable, e.g., "inputs", "containers"]  
**Status:** Draft | In Progress | Review | Complete  
**Assigned:** [Agent ID or "Unassigned"]  
**Completion Date:** [YYYY-MM-DD or "N/A"]  
**Reviewed By:** [Reviewer ID or "Pending"]

## Dependencies

**External:**
- `[domain]/[file].md` - [Why this is needed]
- [List all specification files this depends on]

**Internal:**
- [Dependencies within same domain]

**Blocks:**
- [List specifications that depend on this one]

---

## Overview

[2-3 paragraph description of what this specification covers. Include:
- What element/component/system this describes
- Its purpose in the overall architecture
- Key responsibilities
- How it fits into the larger system]

---

## Metamodel Reference

> **Note:** Include this section for Metamodel, Visual Elements, Actions, and Data Model domains

**Ecore Class:** `ui::[PackageName]::[ClassName]`  
**XMIID Pattern:** [Pattern for sourceId if applicable]

### Class Hierarchy

```
[ParentClass]
└─ [CurrentClass]
   ├─ [ChildClass1]
   └─ [ChildClass2]
```

### Properties

| Name | Type | Multiplicity | Default | Required | Description |
|------|------|--------------|---------|----------|-------------|
| [propertyName] | [EString\|EBoolean\|etc] | [0..1\|1\|0..*] | [value] | [Y/N] | [Description] |
| ... | ... | ... | ... | ... | ... |

### Operations

| Name | Return Type | Parameters | Description |
|------|-------------|------------|-------------|
| [operationName] | [Type] | [(Type) param, ...] | [Description] |
| ... | ... | ... | ... |

### Relationships

**Contains:**
- `[ClassName]` ([multiplicity]) - [Description]

**Referenced By:**
- `[ClassName].[property]` - [Description]

**References:**
- `[ClassName]` - [Description]

### Enumerations

> Include if this element uses enums

```
[EnumName]:
  [VALUE1] = 0  // [Description]
  [VALUE2] = 1  // [Description]
```

---

## Runtime Model Definition

> **Note:** Include for Runtime Model, Components, and Visual Elements specs

### TypeScript Interface

```typescript
/**
 * [Description of the model/interface]
 * 
 * @see [Link to metamodel spec]
 * @example
 * ```typescript
 * const example: [InterfaceName] = {
 *   // example usage
 * };
 * ```
 */
export interface [InterfaceName] [extends BaseInterface] {
  /** [Property description] */
  [propertyName]: [Type];
  
  /** [Property description - optional] */
  [optionalProperty]?: [Type];
  
  // ... all properties
}

// Related types
export type [TypeAlias] = [Definition];

// Type guards
export function is[InterfaceName](obj: any): obj is [InterfaceName] {
  // validation logic
}
```

### Property Details

#### [propertyName]

- **Type:** `[TypeScript type]`
- **Required:** Yes | No
- **Default:** `[value]` or "none"
- **Metamodel Source:** `[Ecore class].[property]`
- **Transformation:** [How metamodel value becomes runtime value]
- **Validation:** [Any validation rules]
- **Description:** [Detailed description]

[Repeat for each property]

---

## Metamodel to Runtime Mapping

| Runtime Property | Metamodel Property | Type Transformation | Notes |
|-----------------|-------------------|-------------------|-------|
| [runtimeProp] | [metamodelProp] | [description] | [notes] |
| ... | ... | ... | ... |

---

## Component Implementation

> **Note:** Include for Visual Elements and Components specs

### Component Interface

```typescript
/**
 * [Component description]
 * 
 * @param props - [Props description]
 * @returns [Return description]
 */
export function [ComponentName](props: [ComponentName]Props): JSX.Element {
  // Implementation notes
}

export interface [ComponentName]Props {
  /** [Prop description] */
  [propName]: [Type];
  
  // ... all props
}
```

### State Management

```typescript
// Internal state hooks
const [state, setState] = useState<[Type]>([initialValue]);

// Custom hooks used
const { ... } = useCustomHook(...);
```

### Event Handlers

```typescript
// Event handler signatures
const handleChange = (value: [Type]) => void;
const handleClick = (event: React.MouseEvent) => void;
```

### Accessibility

- **ARIA Attributes:** [List required ARIA attributes]
- **Keyboard Navigation:** [Describe keyboard interactions]
- **Screen Reader:** [Describe screen reader behavior]

---

## Generator Implementation

> **Note:** Include for Generators and elements that need generation

### Java Helper Methods

```java
package hu.blackbelt.judo.ui.generator.react;

/**
 * [Description of helper class]
 */
public class [HelperClassName] {
    
    /**
     * [Method description]
     * 
     * @param [param] - [param description]
     * @return [return description]
     */
    public static [ReturnType] [methodName]([ParamType] [param]) {
        // Implementation pseudocode
        // 1. Step one
        // 2. Step two
        // 3. Return result
    }
    
    // ... more methods
}
```

### Handlebars Template

```handlebars
{{! templates/[template-name].hbs }}
{{! Description of what this template generates }}

{{! Example usage: }}
{{> [template-name] element=element context=context }}

{{! Template structure: }}
[Template content with annotations]
```

### Template Context

```java
// Context object for template
Map<String, Object> context = new HashMap<>();
context.put("[key]", [value]);
```

---

## Behavior Specification

> **Note:** Particularly important for Actions and Components

### User Interactions

1. **[Interaction Name]:**
   - **Trigger:** [What causes this]
   - **Action:** [What happens]
   - **Result:** [End state]

### State Transitions

```
[Initial State] 
  ↓ [trigger]
[Intermediate State]
  ↓ [trigger]
[Final State]
```

### Business Rules

1. **[Rule Name]:** [Description]
2. **[Rule Name]:** [Description]

---

## Validation Rules

> **Note:** Include for elements with validation

### Field-Level Validation

```typescript
function validate[Field](value: [Type], context: [Context]): ValidationError | null {
  // Validation logic
  if ([condition]) {
    return { message: "[error]", code: "[code]" };
  }
  return null;
}
```

### Cross-Field Validation

```typescript
function validateCrossField(data: [DataType]): ValidationError[] {
  const errors: ValidationError[] = [];
  
  // Check relationships between fields
  if ([condition]) {
    errors.push({ field: "[field]", message: "[error]" });
  }
  
  return errors;
}
```

---

## Examples

### Example 1: [Basic Usage Scenario]

**Metamodel:**
```xml
[XML snippet from ui.model showing this element]
```

**Generated Runtime Model:**
```typescript
export const exampleModel: [ModelType] = {
  // Generated model structure
};
```

**Generated Component:**
```typescript
// Generated component usage
export default function ExamplePage() {
  return <ModelDrivenComponent model={exampleModel} />;
}
```

**Runtime Behavior:**
[Description of what happens at runtime]

### Example 2: [Edge Case or Advanced Usage]

[Similar structure to Example 1]

### Example 3: [Error Scenario]

**Scenario:** [Description of error case]

**Handling:**
```typescript
try {
  // operation
} catch (error) {
  // error handling
}
```

---

## Testing Criteria

### Unit Tests

**Test Suite:** `[TestFileName].spec.ts`

- [ ] **Test:** [Test name]
  - **Given:** [Initial conditions]
  - **When:** [Action]
  - **Then:** [Expected result]

- [ ] **Test:** [Test name]
  - **Given:** [Initial conditions]
  - **When:** [Action]
  - **Then:** [Expected result]

[3-5 unit tests minimum]

### Integration Tests

**Test Suite:** `[IntegrationTestFileName].integration.spec.ts`

- [ ] **Scenario:** [Scenario name]
  - **Steps:** [1. step, 2. step, 3. step]
  - **Expected:** [Final state]

[2-3 integration tests]

### Edge Cases

- [ ] **Edge Case:** [Description]
  - **Handling:** [How it's handled]
  - **Test:** [Test description]

[3-5 edge cases]

### Performance Tests

> Include for performance-critical components

- [ ] **Metric:** [What to measure]
  - **Target:** [Acceptable threshold]
  - **Test Method:** [How to test]

---

## Design Decisions

### Decision 1: [Decision Title]

**Context:** [Why this decision was needed]

**Options Considered:**
1. **Option A:** [Description]
   - Pros: [List]
   - Cons: [List]
2. **Option B:** [Description]
   - Pros: [List]
   - Cons: [List]

**Chosen:** [Option X]

**Rationale:** [Why this was chosen]

### Decision 2: [Another Decision]

[Same structure]

---

## Open Questions

1. **[Question]**
   - **Context:** [Why this is unclear]
   - **Impact:** [What depends on this]
   - **Action:** [Who needs to decide]

2. **[Question]**
   - [Same structure]

---

## Future Enhancements

1. **[Enhancement Name]**
   - **Description:** [What could be added]
   - **Benefit:** [Why it would be useful]
   - **Effort:** [Estimated complexity]
   - **Priority:** Low | Medium | High

---

## Related Specifications

### Depends On
- `[domain]/[file].md` - [Why this is referenced]

### Referenced By
- `[domain]/[file].md` - [How this is used]

### Related
- `[domain]/[file].md` - [Related concept]

---

## References

### External Documentation
- **Ecore Definition:** `ui.ecore` lines [X-Y]
- **Original Proposal:** `[PROPOSAL_FILE].md` section [X]
- **Mapping Reference:** `METAMODEL_TO_RUNTIME_MAPPING.md` section "[Section]"

### Sample Code
- **Sample Model:** `judo-ui-react-itest/[TestName]/model/[ModelFile]` lines [X-Y]
- **Generated Example:** `[path to generated example]`

---

## Revision History

| Date | Version | Author | Changes |
|------|---------|--------|---------|
| [YYYY-MM-DD] | 0.1 | [Agent ID] | Initial draft |
| [YYYY-MM-DD] | 0.2 | [Agent ID] | Added examples |
| [YYYY-MM-DD] | 1.0 | [Reviewer] | Approved |

---

## Appendix

### Glossary

- **[Term]:** [Definition]
- **[Term]:** [Definition]

### Code Snippets

> Additional code examples that don't fit in main examples

```typescript
// [Description]
[Code]
```

---

**End of Specification**

