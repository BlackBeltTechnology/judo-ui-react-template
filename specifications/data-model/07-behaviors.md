# Behaviors Specification
**Reviewed:** Not yet reviewed
**Last Updated:** 2025-11-28  
**Created:** 2025-11-28  
**Status:** ✅ Complete  

---

- `components/07-action-executor-hook.md` - Behavior checks
- `components/04-field-state-management.md` - Behavior-aware state
**Components:**

- All action specifications - Respect behaviors
**Actions:**

- `runtime-model/02-page-model.md` - Behavior evaluation
- `runtime-model/01-core-types.md` - BehaviorModel interface
**Runtime Model:**

- `04-operation-type.md` - Operation behaviors
- `02-relation-type.md` - Relation behaviors
- `01-class-type.md` - Contains behaviors
**Data Model:**

## Related Specifications

- [x] Syntax errors in conditions
- [x] Circular behavior dependencies
- [x] Complex nested conditions
- [x] Null/undefined attribute access
- [x] Always false condition
- [x] Always true condition
### Edge Cases

- [x] Actions disabled when behaviors false
- [x] UI reflects behavior state
- [x] Invalid conditions fail gracefully
- [x] Null checks handled safely
- [x] Complex conditions evaluate correctly
- [x] Simple conditions evaluate correctly
### Integration Tests

- [x] JavaScript condition generation
- [x] Referenced attribute extraction
- [x] Static behavior detection
- [x] Condition parsing
- [x] Behavior extraction from metamodel
### Unit Tests

## Testing Criteria

```
}, [data, actions.canUpdate, editMode]);
  return editMode ? 'edit' : 'view';
  if (!actions.canUpdate) return 'view';
  if (!data.__identifier) return 'create';
const formMode = useMemo(() => {
```typescript

### Form Mode

```
</Button>
  Delete
>
  onClick={handleDelete}
  disabled={!actions.canDelete || isLoading}
<Button

</Button>
  Update
>
  onClick={handleUpdate}
  disabled={!actions.canUpdate || isLoading}
<Button
```typescript

### Button Rendering

```
}, [data, model.behaviors, context]);
  };
    canRead: evaluateBehavior(behaviors.isReadable, data, context)
    canDelete: evaluateBehavior(behaviors.isDeletable, data, context),
    canUpdate: evaluateBehavior(behaviors.isUpdateable, data, context),
    canCreate: evaluateBehavior(behaviors.isCreatable, data, context),
  return {
  
  const behaviors = model.behaviors || {};
const actions = useMemo(() => {
```typescript

### Action State Management

```
}
  }
    return false;  // Fail-safe: default to disabled
    console.error('Behavior evaluation failed:', error);
  } catch (error) {
      (...Object.values(evalContext));
    return new Function(...Object.keys(evalContext), `return ${condition}`)
    // (Simplified - real implementation uses safe parser)
    // Parse and evaluate condition
    
    const evalContext = { ...data, ...context };
    // Create safe evaluation context
  try {
): boolean {
  context: any = {}
  data: any,
  condition: string,
function evaluateBehavior(
```typescript

### Behavior Evaluation

## Common Patterns

```
}
    public static String generateJavaScriptCondition(String condition);
    public static Set<String> extractReferencedAttributes(Behavior behavior);
    public static boolean isStaticBehavior(Behavior behavior);
    public static String generateEvaluator(Behavior behavior);
    public static BehaviorModel extractBehavior(Behavior behavior);
public class BehaviorGenerator {
```java
**Key Methods:**

See `generators/03-data-model-generator.md`.

## Generator Implementation

```
}
  isReadable?: string | boolean;
  isDeletable?: string | boolean;
  isUpdateable?: string | boolean;
  isCreatable?: string | boolean;   // Condition or static value
interface ClassBehaviors {
// Generated behaviors in model

type BehaviorEvaluator = (data: any, context: any) => boolean;
// Behavior evaluation function

}
  error?: string;              // If evaluation failed
  result: boolean;             // Evaluated condition result
  name: string;
interface EvaluatedBehavior {
// Evaluated behavior

}
  sourceId?: string;
  condition: string;           // condition expression
  name: string;                // name (e.g., "isUpdateable")
interface BehaviorModel {
```typescript

## Runtime Model Mapping

Custom names should follow `is{Capability}` or `can{Action}` pattern.

- `isCallable`
- `isReadable`
- `isDeletable`
- `isUpdateable`
- `isCreatable`
Must use these standard names:
### Standard Names

- Should not have side effects
- Avoid circular dependencies
- Condition should be deterministic
- Referenced attributes must exist in ClassType
- Condition must be syntactically valid
### Constraints

- `condition` - Must be valid boolean expression
- `name` - Must match standard behavior names
### Required Properties

## Validation Rules

```
</behaviors>
  </condition>
    dueDate > TODAY && status == 'ACTIVE'
  <condition>
<behaviors name="isUpdateable">
```xml

### Example 7: Date-Based Behavior

```
</behaviors>
  </condition>
    userRole == 'ADMIN' || (userRole == 'MANAGER' && status == 'DRAFT')
  <condition>
<behaviors name="isDeletable">
```xml

### Example 6: Role-Based Behavior

```
</behaviors>
  <condition>true</condition>
<behaviors name="isReadable">
```xml

### Example 5: Always Readable

```
</operations>
  </behaviors>
    </condition>
      approvedBy == null
      submittedDate != null && 
      status == 'PENDING' && 
    <condition>
  <behaviors name="isCallable">
  
  <parameters name="comments" type="STRING"/>
<operations name="approve" operationKind="INSTANCE">
```xml

### Example 4: Operation Callable Condition

```
</Button>
  Add Item
<Button disabled={!canAddItems} onClick={handleAddItem}>

const canRemoveItems = invoice.status === 'DRAFT';
const canAddItems = invoice.status === 'DRAFT';
```typescript
**Runtime Effect:**

```
</classTypes>
  </relations>
    </behaviors>
      <condition>status == 'DRAFT'</condition>
    <behaviors name="isDeletable">
    
    </behaviors>
      <condition>status == 'DRAFT'</condition>
    <behaviors name="isCreatable">
  <relations name="lineItems" target="LineItem" cardinality="MANY">
<classTypes name="Invoice">
```xml

### Example 3: Relation CRUD Behavior

```
</behaviors>
  </condition>
    (status == 'DRAFT' || status == 'CANCELLED') && createdBy == currentUser
  <condition>
<behaviors name="isDeletable">
```xml

### Example 2: Multi-Condition Delete

```
</Button>
  Update
<Button disabled={!canUpdate} onClick={handleUpdate}>

const canUpdate = order.status === 'DRAFT';
```typescript
**Runtime Effect:**

```
</classTypes>
  </behaviors>
    <condition>status == 'DRAFT'</condition>
  <behaviors name="isUpdateable">
  
  <attributes name="status" type="OrderStatus"/>
<classTypes name="Order">
```xml

### Example 1: Draft-Based Update

## Examples from Sample Model

Controls when operation can be called.

```
</operations>
  </behaviors>
    <condition>status == 'PENDING' && approvedBy == null</condition>
  <behaviors name="isCallable">
<operations name="approve" operationKind="INSTANCE">
```xml
**Operation-Level Behaviors:**

Affects operations on the specific relation.

```
</relations>
  </behaviors>
    <condition>status == 'DRAFT'</condition>
  <behaviors name="isCreatable">
<relations name="items" target="Item" cardinality="MANY">
```xml
**Relation-Level Behaviors:**

Affects all CRUD operations on the entity.

```
</classTypes>
  </behaviors>
    <condition>status == 'DRAFT' || status == 'CANCELLED'</condition>
  <behaviors name="isDeletable">
  
  </behaviors>
    <condition>status == 'DRAFT'</condition>
  <behaviors name="isUpdateable">
  
  <attributes name="status" type="InvoiceStatus"/>
<classTypes name="Invoice">
```xml
**Class-Level Behaviors:**

### Behavior Scope

   - Fields disabled based on behaviors
   - Read-only form if `!isUpdateable`
3. **Form Mode:**

   - Operation button disabled if `!isCallable`
   - Create action hidden if `!isCreatable`
2. **Action Availability:**

   - Delete button disabled if `!isDeletable`
   - Update button disabled if `!isUpdateable`
1. **Button Enabled/Disabled:**

Behaviors are evaluated at runtime to determine UI state:

### Behavior Evaluation

```
approvedBy != null
```
**Null Checks:**

```
quantity > 0 && quantity <= maxQuantity
```
**Comparison:**

```
status == 'DRAFT' && !isArchived
```
**Multiple Conditions:**

```
status == 'DRAFT'
```
**Simple Attribute Check:**

Conditions are boolean expressions evaluated against entity data:

### Conditional Expressions

## Key Concepts

- `OperationType.behaviors` - Operation-level behaviors
- `RelationType.behaviors` - Relation-level behaviors
- `ClassType.behaviors` - Class-level behaviors
**Referenced By:**

- None
**Contains:**

### Relationships

| `isCallable` | OperationType | Can call operation |
| `isReadable` | ClassType, RelationType | Can read/view instances |
| `isDeletable` | ClassType, RelationType | Can delete instances |
| `isUpdateable` | ClassType, RelationType | Can update instances |
| `isCreatable` | ClassType, RelationType | Can create new instances |
|----------|------------|-------------|
| Behavior | Applies To | Description |

### Standard Behavior Names

| `condition` | EString | 1 | - | Boolean expression to evaluate |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `name` | EString | 1 | - | Behavior name (e.g., "canUpdate", "isVisible") |
|------|------|--------------|---------|-------------|
| Name | Type | Multiplicity | Default | Description |

### Properties

```
└─ Behavior
Named Element
```

### Class Hierarchy

## Metamodel Definition

`Behavior` defines conditional capabilities on ClassTypes, RelationTypes, and OperationTypes. Behaviors control when entities can be created, updated, deleted, or read based on the entity's current state. This enables dynamic business rules and state-based UI controls.

## Overview

**Blocks:** Conditional CRUD, runtime behavior evaluation  
**Dependencies:** `01-class-type.md`, `02-relation-type.md`, `04-operation-type.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Ecore Class:** `data::Behavior`  
**Domain:** Data Model  


