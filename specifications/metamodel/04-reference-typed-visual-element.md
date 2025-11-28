# ReferenceTypedVisualElement Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::ReferenceTypedVisualElement`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-named-element.md`, `02-visual-element.md`, `03-labeled-element.md`  
**Blocks:** `05-page-definition.md`, `14-link.md`, data-bound visual elements  

## Overview

`ReferenceTypedVisualElement` is an abstract class that represents UI elements bound to data model elements. It extends both `VisualElement` and `LabeledElement` to provide data binding capabilities for pages, links, and other elements that reference data types (ClassType or RelationType).

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
├─ VisualElement (abstract)
├─ LabeledElement (abstract)
└─ ReferenceTypedVisualElement (abstract)
   ├─ PageDefinition
   └─ Link
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| **From NamedElement:** ||||
| `name` | EString | 1 | - | Unique identifier |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `annotations` | Annotation | 0..* | - | Custom annotations |
| **From LabeledElement:** ||||
| `label` | EString | 0..1 | - | Display label |
| `icon` | Icon | 0..1 | - | Display icon |
| **From VisualElement:** ||||
| `col` | EInt | 0..1 | 12 | Grid column span |
| `row` | EInt | 0..1 | - | Grid row position |
| `size` | Size | 0..1 | - | Fixed dimensions |
| `stretch` | Stretch | 0..1 | NONE | Stretch behavior |
| `fit` | Fit | 0..1 | LOOSE | Fit behavior |
| `hiddenBy` | AttributeType | 0..1 | - | Conditional visibility |
| `enabledBy` | AttributeType | 0..1 | - | Conditional enablement |
| `requiredBy` | AttributeType | 0..1 | - | Conditional requirement |
| `subTheme` | EString | 0..1 | - | Theme variant |
| `autoFocus` | EBoolean | 0..1 | false | Auto focus on mount |
| **Own Properties:** ||||
| `dataElement` | DataElement | 0..1 | - | Referenced data type (ClassType or RelationType) |

### Operations

All operations inherited from base classes.

### Relationships

**Contains:**
- Inherited from base classes

**Referenced By:**
- Pages reference ClassType or RelationType
- Links reference RelationType
- Actions reference these elements

**References:**
- `dataElement: DataElement` - The data type this element operates on

## Key Concepts

### Data Binding

The `dataElement` property establishes the connection between UI and data model:

1. **ClassType Reference**: For standalone entities
   ```xml
   <pages name="UserPage" dataElement="User">
   ```

2. **RelationType Reference**: For related entities
   ```xml
   <link name="posts" dataElement="User#posts">
   ```

### Type Resolution

Data element resolution follows FQN pattern:
- Simple name: `"User"` (ClassType)
- Relation path: `"User#posts"` (RelationType)
- Nested path: `"Company#departments#employees"` (chain)

### Inheritance Combination

This class combines three inheritance paths:
```
        NamedElement
       /     |      \
      /      |       \
Visual  Labeled   (direct)
Element  Element     |
     \      |       /
      \     |      /
   ReferenceTypedVisualElement
```

Provides:
- Identification (NamedElement)
- Display properties (LabeledElement)
- Layout capabilities (VisualElement)
- Data binding (own property)

## Examples from Sample Model

### Example 1: Page Bound to ClassType

```xml
<pages name="UserView" 
       label="User Details"
       dataElement="User">
  <icon name="person"/>
  <container xsi:type="ui:PageContainer" 
             type="VIEW"
             dataElement="User">
    <!-- children reference User attributes -->
  </container>
</pages>
```

**Data Binding:**
```typescript
// Page operates on User entities
interface UserViewProps {
  data: User;
  actions: UserActions;
}
```

### Example 2: Link to RelationType

```xml
<children xsi:type="ui:Link" 
          name="posts" 
          label="User Posts"
          col="12"
          dataElement="User#posts">
  <icon name="article"/>
</children>
```

**Data Binding:**
```typescript
// Link navigates to related Post entities
interface LinkProps {
  relation: 'posts';
  owner: User;
  target: Post[];
}
```

### Example 3: Selector Page with RelationType

```xml
<pages name="PostSelector" 
       label="Select Posts"
       isSelector="true"
       isRelationSelector="true"
       dataElement="User#posts">
  <container xsi:type="ui:PageContainer" 
             type="TABLE"
             dataElement="Post">
    <!-- table of Post entities -->
  </container>
</pages>
```

### Example 4: Dashboard Without DataElement

```xml
<pages name="Dashboard" 
       label="Dashboard"
       dashboard="true">
  <!-- dataElement is null for dashboards -->
  <container xsi:type="ui:PageContainer" type="VIEW">
    <!-- custom widgets -->
  </container>
</pages>
```

### Example 5: Link with Conditional Visibility

```xml
<children xsi:type="ui:Link" 
          name="profile" 
          label="View Profile"
          col="6"
          dataElement="User#profile"
          hiddenBy="User#hasProfile">
  <icon name="account_circle"/>
</children>
```

## Validation Rules

### Required Properties
- `name` - Must be present
- For pages: Usually requires `dataElement` (except dashboards)
- For links: Must have `dataElement` (RelationType)

### Constraints
- `dataElement` must reference valid ClassType or RelationType
- If dataElement is RelationType, must follow `ClassName#relationName` pattern
- ClassType reference should be simple name
- Dashboard pages should have null dataElement
- Selector pages typically have dataElement

### Data Element Resolution
- Element must exist in data model
- Must be accessible (not private/internal)
- Type must match expected usage:
  - Pages: ClassType or RelationType
  - Links: RelationType only

## Runtime Model Mapping

```typescript
interface ReferenceTypedElementModel extends VisualElementModel, LabeledElementModel {
  // From NamedElement
  id: string;
  name: string;
  sourceId?: string;
  
  // From LabeledElement
  label?: string;
  icon?: { name: string; color?: string };
  
  // From VisualElement
  col: number;
  row?: number;
  stretch: StretchType;
  fit: FitType;
  hiddenBy?: string;
  enabledBy?: string;
  requiredBy?: string;
  
  // Own property
  dataElement?: string;     // FQN of ClassType or RelationType
  dataElementType?: 'class' | 'relation';  // Resolved type
}
```

### Key Mappings
- `dataElement` → `dataElement?: string` (FQN)
- Type resolution → `dataElementType: 'class' | 'relation'`

## Generator Implementation

See `generators/01-generator-helpers.md` for Java implementation.

**Key Helper Methods:**
```java
public class ReferenceTypedElementHelper {
    public static String getDataElementFQN(ReferenceTypedVisualElement element);
    public static boolean hasDataElement(ReferenceTypedVisualElement element);
    public static boolean isClassTypeReference(ReferenceTypedVisualElement element);
    public static boolean isRelationTypeReference(ReferenceTypedVisualElement element);
    public static ClassType getReferencedClass(ReferenceTypedVisualElement element);
    public static RelationType getReferencedRelation(ReferenceTypedVisualElement element);
    public static String getServiceName(ReferenceTypedVisualElement element);
}
```

## Common Patterns

### Service Resolution

```typescript
// Resolve service for data element
const serviceName = `${dataElement}Service`;
const service = serviceMap[serviceName];

// Example: "User" -> UserService
// Example: "User#posts" -> UserService.posts
```

### Type Guards

```typescript
function isClassReference(element: ReferenceTypedElementModel): boolean {
  return element.dataElementType === 'class';
}

function isRelationReference(element: ReferenceTypedElementModel): boolean {
  return element.dataElementType === 'relation';
}
```

### Navigation Pattern

```typescript
// Page navigation
if (isClassReference(page)) {
  navigate(`/pages/${page.name}/${entityId}`);
} else if (isRelationReference(page)) {
  navigate(`/pages/${page.name}/${ownerId}/${relationName}`);
}
```

### Data Fetching

```typescript
// Fetch data for element
async function fetchData(element: ReferenceTypedElementModel, id: string) {
  if (element.dataElement) {
    const service = resolveService(element.dataElement);
    return await service.getById(id);
  }
  return null;
}
```

## Testing Criteria

### Unit Tests
- [x] Data element FQN resolution
- [x] ClassType vs RelationType detection
- [x] Service name generation
- [x] Null dataElement handling (dashboards)
- [x] Invalid dataElement reference detection
- [x] Inheritance property access

### Integration Tests
- [x] Page with ClassType loads data correctly
- [x] Link with RelationType navigates correctly
- [x] Selector returns correct entity type
- [x] Dashboard without dataElement renders
- [x] Conditional properties work on references
- [x] Service resolution works correctly

### Edge Cases
- [x] Missing dataElement (dashboard case)
- [x] Invalid dataElement reference (error handling)
- [x] Circular references (detection)
- [x] Deeply nested relation paths
- [x] Polymorphic type references
- [x] Abstract class references

## Related Specifications

**Metamodel:**
- `01-named-element.md` - Base class
- `02-visual-element.md` - Inherited properties
- `03-labeled-element.md` - Inherited properties
- `05-page-definition.md` - Primary implementer
- `14-link.md` - Secondary implementer

**Data Model:**
- `data-model/01-class-type.md` - Referenced by dataElement
- `data-model/02-relation-type.md` - Referenced by dataElement

**Runtime Model:**
- `runtime-model/01-core-types.md` - Base interfaces
- `runtime-model/02-page-model.md` - Uses data binding
- `runtime-model/04-visual-element-model.md` - Link models

**Generators:**
- `generators/01-generator-helpers.md` - Helper methods
- `generators/02-page-model-generator.md` - Page generation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

