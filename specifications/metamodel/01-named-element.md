# NamedElement Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::NamedElement`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** None (base abstract class)  
**Blocks:** All other metamodel elements  

## Overview

`NamedElement` is the root abstract base class for all named elements in the UI metamodel. It provides core identification and annotation capabilities that are inherited by all UI elements including pages, containers, visual elements, actions, and data elements.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
├─ LabeledElement (abstract)
├─ VisualElement (abstract)
├─ ActionDefinition (abstract)
├─ DataElement (abstract)
├─ Annotation
└─ ... (all named elements)
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Unique identifier for the element within its context |
| `sourceId` | EString | 0..1 | - | Original XMIID from source model for traceability |
| `annotations` | Annotation | 0..* | - | Custom metadata annotations |

### Operations

| Name | Return Type | Parameters | Description |
|------|-------------|------------|-------------|
| `getSeparator` | EString | - | Returns the separator character used in FQN (default: ".") |
| `getFQName` | EString | - | Returns fully qualified name (e.g., "app.User.firstName") |
| `getOwner` | NamedElement | - | Returns the parent/owner element in the hierarchy |

### Relationships

**Contains:**
- `Annotation[]` (0..*) - Custom annotations for extensibility

**Referenced By:**
- All metamodel elements inherit from NamedElement

## Key Concepts

### Unique Identification

The `name` property serves as the primary identifier:
- Must be unique within the element's container/scope
- Used for code generation (variable names, function names)
- Forms the basis of FQN (Fully Qualified Name)
- Should follow naming conventions of target language

### Fully Qualified Names (FQN)

FQN construction traverses the ownership hierarchy:
```
getOwner().getFQName() + getSeparator() + name
```

Examples:
- Page: `"UserPage"`
- Container field: `"UserPage.container.firstName"`
- Action: `"UserPage.actions.createUser"`

### Source Traceability

`sourceId` maintains link to original model:
- Stores XMIID from source XMI/UML model
- Enables round-trip engineering
- Used for model diffing and updates
- Essential for code regeneration without losing changes

### Annotations

Support for custom metadata:
- Key-value pairs for extensibility
- Used by generators for custom behavior
- Can specify framework-specific configurations
- Examples: `@CustomValidator`, `@UIHint`, `@Performance`

## Examples from Sample Model

### Example 1: Simple Named Element

```xml
<pages name="UserList" 
       sourceId="_xmiid_12345">
  <!-- content -->
</pages>
```

**Generated FQN:** `"UserList"`

### Example 2: Nested Named Element

```xml
<pages name="UserForm" sourceId="_page_001">
  <container name="container" sourceId="_container_001">
    <children xsi:type="ui:TextInput" 
              name="firstName" 
              sourceId="_input_001"
              attributeType="User#firstName"/>
  </container>
</pages>
```

**Generated FQNs:**
- Page: `"UserForm"`
- Container: `"UserForm.container"`
- Input: `"UserForm.container.firstName"`

### Example 3: With Annotations

```xml
<pages name="Dashboard" sourceId="_dashboard_001">
  <annotations key="customComponent" value="true"/>
  <annotations key="theme" value="dark"/>
  <annotations key="performance.lazyLoad" value="true"/>
</pages>
```

### Example 4: Action Definition

```xml
<actions name="createUser" 
         sourceId="_action_create_001"
         actionDefinition="CreateAction">
  <annotations key="confirmation" value="always"/>
</actions>
```

**Generated FQN:** `"UserForm.actions.createUser"`

## Validation Rules

### Required Properties
- `name` - Must be present and non-empty
- `name` - Must be unique within parent container

### Constraints
- Name must be valid identifier in target language (TypeScript/Java)
- Name should not contain special characters except underscore
- Name should not start with number
- Name should be descriptive and follow conventions
- SourceId should be preserved across regenerations

### Naming Conventions

**TypeScript/JavaScript:**
- Classes/Types: PascalCase (e.g., `UserForm`, `TextInput`)
- Variables/Fields: camelCase (e.g., `firstName`, `isEnabled`)
- Actions: camelCase verbs (e.g., `createUser`, `refreshData`)

**Java:**
- Classes: PascalCase
- Methods: camelCase
- Constants: UPPER_SNAKE_CASE

## Runtime Model Mapping

`NamedElement` properties are inherited by all runtime model interfaces:

```typescript
interface BaseModel {
  id: string;           // Maps to: name or sourceId
  name: string;         // Maps to: name
  sourceId?: string;    // Maps to: sourceId
  annotations?: Record<string, string>;  // Maps to: annotations
  fqn?: string;         // Generated from: getFQName()
}
```

### Key Mappings
- `name` → `name: string`
- `sourceId` → `sourceId?: string`
- `annotations` → `annotations?: Record<string, string>`
- `getFQName()` → `fqn?: string` (computed)

## Generator Implementation

See `generators/01-generator-helpers.md` for Java implementation.

**Key Helper Methods:**
```java
public class NamedElementHelper {
    public static String getName(NamedElement element);
    public static String getFQName(NamedElement element);
    public static String getSourceId(NamedElement element);
    public static String getSafeName(NamedElement element); // Sanitized for code gen
    public static Map<String, String> getAnnotations(NamedElement element);
    public static String getAnnotation(NamedElement element, String key);
    public static boolean hasAnnotation(NamedElement element, String key);
}
```

## Common Patterns

### Name Resolution

```typescript
// Finding element by name in generated code
const element = container.elements.find(e => e.name === 'firstName');
```

### FQN Usage in Navigation

```typescript
// Navigate using FQN
navigate(`/elements/${element.fqn.replace(/\./g, '/')}`);
```

### Annotation-Based Customization

```typescript
// Check for custom implementation
if (element.annotations?.['customComponent'] === 'true') {
  return <CustomComponent model={element} />;
}
```

### Source Tracking

```typescript
// Map generated elements back to source
const sourceMapping = {
  [element.sourceId]: element.fqn
};
```

## Testing Criteria

### Unit Tests
- [x] Name validation rules enforced
- [x] FQN generation correct for nested elements
- [x] SourceId preserved across model transformations
- [x] Annotation key-value storage and retrieval
- [x] Name uniqueness validation within scope

### Integration Tests
- [x] FQN used correctly in navigation
- [x] Names generate valid TypeScript identifiers
- [x] Annotations accessible in runtime models
- [x] Source traceability maintained through generation
- [x] Owner hierarchy traversal works correctly

### Edge Cases
- [x] Special characters in names (sanitization)
- [x] Very long names (truncation strategy)
- [x] Name conflicts (error handling)
- [x] Missing sourceId (graceful degradation)
- [x] Circular ownership references (should not occur)
- [x] Empty or null names (validation error)

## Related Specifications

**Metamodel:**
- `02-visual-element.md` - Extends NamedElement
- `03-labeled-element.md` - Extends NamedElement
- All other metamodel specs inherit from this

**Runtime Model:**
- `runtime-model/01-core-types.md` - Base TypeScript interfaces

**Generators:**
- `generators/01-generator-helpers.md` - Java helper methods

**Data Model:**
- `data-model/01-class-type.md` - Uses NamedElement

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

