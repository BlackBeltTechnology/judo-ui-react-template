# ClassType Specification

**Domain:** Data Model  
**Ecore Class:** `data::ClassType`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** None (base data model class)  
**Blocks:** `02-relation-type.md`, `03-attribute-type.md`, `04-operation-type.md`, all runtime models  

## Overview

`ClassType` is the core data model element representing entities/domain objects in the JUDO system. It defines the structure, attributes, relations, operations, and behaviors of business entities. ClassTypes serve as the foundation for all data-bound UI elements.

## Metamodel Definition

### Class Hierarchy

```
NamedElement
└─ DataElement (abstract)
   └─ ClassType
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Unique class identifier (e.g., "User", "Order") |
| `sourceId` | EString | 0..1 | - | XMIID from source model |
| `annotations` | Annotation | 0..* | - | Custom metadata |
| `abstract` | EBoolean | 0..1 | false | Whether class is abstract |
| `entity` | EBoolean | 0..1 | true | Whether this is a persistent entity |
| `mapped` | EBoolean | 0..1 | false | Whether mapped to external source |

### Relationships

**Contains:**
- `attributes: AttributeType[]` (0..*) - Class attributes
- `relations: RelationType[]` (0..*) - Relationships to other classes
- `operations: OperationType[]` (0..*) - Business operations
- `behaviors: Behavior[]` (0..*) - Conditional behaviors

**Referenced By:**
- `PageDefinition.dataElement` - Pages operating on this type
- `RelationType.target` - Relations pointing to this type
- `OperationType.returnType` - Operations returning this type

**References:**
- `superType: ClassType` (0..1) - Parent class for inheritance

## Key Concepts

### Entity Types

1. **Regular Entity** (`entity=true`):
   - Persistent domain object
   - Has identity (ID field)
   - CRUD operations available
   - Stored in database

2. **Transfer Object** (`entity=false`):
   - Transient data structure
   - Used for operation parameters/returns
   - Not persisted directly
   - May aggregate multiple entities

3. **Abstract Class** (`abstract=true`):
   - Cannot be instantiated
   - Used for inheritance
   - Defines common structure
   - Subclasses provide concrete implementation

### Class Inheritance

```
ClassType: "Person"
├─ attributes: [id, name, birthDate]
├─ operations: [getAge()]
│
ClassType: "Employee" (extends Person)
├─ attributes: [employeeId, salary]
├─ operations: [promote()]
└─ inherits: [id, name, birthDate, getAge()]
```

### Behaviors

ClassTypes can have conditional behaviors:
- `isCreatable` - Can create new instances
- `isUpdateable` - Can update instances
- `isDeletable` - Can delete instances
- `isReadable` - Can read instances
- Conditional on attribute values

## Examples from Sample Model

### Example 1: Simple Entity

```xml
<classTypes name="User" 
            entity="true" 
            abstract="false"
            sourceId="_class_user">
  <attributes name="id" type="STRING" identifier="true"/>
  <attributes name="firstName" type="STRING"/>
  <attributes name="lastName" type="STRING"/>
  <attributes name="email" type="STRING"/>
  <attributes name="active" type="BOOLEAN"/>
  
  <relations name="posts" 
             target="Post" 
             cardinality="MANY"
             isCreatable="true"
             isUpdateable="true"/>
  
  <operations name="activate" 
              type="INSTANCE_OPERATION"
              returnType="User"/>
</classTypes>
```

### Example 2: Abstract Base Class

```xml
<classTypes name="BaseEntity" 
            entity="true" 
            abstract="true">
  <attributes name="id" type="STRING" identifier="true"/>
  <attributes name="createdAt" type="DATE_TIME"/>
  <attributes name="updatedAt" type="DATE_TIME"/>
  <attributes name="version" type="INTEGER"/>
</classTypes>

<classTypes name="User" 
            entity="true"
            superType="BaseEntity">
  <!-- inherits id, createdAt, updatedAt, version -->
  <attributes name="username" type="STRING"/>
</classTypes>
```

### Example 3: Transfer Object

```xml
<classTypes name="UserRegistrationRequest" 
            entity="false" 
            abstract="false">
  <attributes name="username" type="STRING" required="true"/>
  <attributes name="email" type="STRING" required="true"/>
  <attributes name="password" type="STRING" required="true"/>
  <attributes name="confirmPassword" type="STRING" required="true"/>
</classTypes>
```

### Example 4: With Behaviors

```xml
<classTypes name="Invoice" 
            entity="true">
  <attributes name="status" type="InvoiceStatus"/>
  <attributes name="total" type="DECIMAL"/>
  
  <behaviors name="canUpdate">
    <condition>
      status == InvoiceStatus.DRAFT
    </condition>
  </behaviors>
  
  <behaviors name="canDelete">
    <condition>
      status == InvoiceStatus.DRAFT || status == InvoiceStatus.CANCELLED
    </condition>
  </behaviors>
</classTypes>
```

### Example 5: Mapped Entity

```xml
<classTypes name="ExternalCustomer" 
            entity="true" 
            mapped="true"
            sourceId="_external_customer">
  <!-- Mapped to external system -->
  <annotations key="externalSystem" value="CRM"/>
  <annotations key="endpoint" value="/api/customers"/>
  
  <attributes name="externalId" type="STRING" identifier="true"/>
  <attributes name="name" type="STRING"/>
</classTypes>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within model
- `entity` - Should be explicitly set

### Constraints
- Abstract classes cannot be instantiated in UI
- Abstract classes should have at least one concrete subclass
- Entity classes must have identifier attribute
- Mapped classes must have mapping annotations
- SuperType must not create circular inheritance

### Naming Conventions
- Use PascalCase (e.g., `User`, `OrderItem`, `InvoiceStatus`)
- Should be singular noun
- Should be descriptive of domain concept
- Avoid technical prefixes/suffixes (DTO, Entity, etc.)

### Identifier Rules
- Every entity must have exactly one identifier attribute
- Identifier should be named `id` or have meaningful name
- Identifier must be of type STRING, INTEGER, or UUID
- Composite identifiers via annotations if needed

## Runtime Model Mapping

```typescript
interface ClassTypeModel {
  name: string;                      // name
  fqn: string;                       // Fully qualified name
  abstract: boolean;                 // abstract
  entity: boolean;                   // entity
  mapped: boolean;                   // mapped
  
  // Structure
  attributes: AttributeTypeModel[];  // attributes
  relations: RelationTypeModel[];    // relations
  operations: OperationTypeModel[];  // operations
  
  // Inheritance
  superType?: string;                // superType.name
  
  // Behaviors
  behaviors?: {
    isCreatable?: boolean | string;  // Behavior condition
    isUpdateable?: boolean | string;
    isDeletable?: boolean | string;
    isReadable?: boolean | string;
  };
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}

// Generated TypeScript interface for entity
interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
}
```

### Key Mappings
- `ClassType` → TypeScript interface for data structure
- `ClassType` → `ClassTypeModel` for runtime metadata
- `attributes` → interface properties
- `relations` → navigation properties
- `operations` → service methods

## Generator Implementation

See `generators/03-data-model-generator.md` for Java implementation.

**Key Methods:**
```java
public class ClassTypeGenerator {
    public static ClassTypeModel extractClassModel(ClassType classType);
    public static String generateTypeScriptInterface(ClassType classType);
    public static String generateServiceInterface(ClassType classType);
    public static List<AttributeType> getAllAttributes(ClassType classType); // Including inherited
    public static List<RelationType> getAllRelations(ClassType classType);
    public static boolean hasIdentifier(ClassType classType);
    public static AttributeType getIdentifier(ClassType classType);
}
```

## Common Patterns

### Service Interface Generation

```typescript
// Generated from ClassType
export interface UserService {
  // CRUD operations
  getById(id: string): Promise<User>;
  list(queryCustomizer?: QueryCustomizer): Promise<User[]>;
  create(data: Partial<User>): Promise<User>;
  update(id: string, data: Partial<User>): Promise<User>;
  delete(id: string): Promise<void>;
  
  // Custom operations
  activate(id: string): Promise<User>;
  
  // Relation operations
  listPosts(id: string, queryCustomizer?: QueryCustomizer): Promise<Post[]>;
}
```

### Behavior Evaluation

```typescript
// Runtime behavior check
function canUpdate(user: User, classModel: ClassTypeModel): boolean {
  const behavior = classModel.behaviors?.isUpdateable;
  if (typeof behavior === 'boolean') return behavior;
  if (typeof behavior === 'string') {
    // Evaluate condition
    return evaluateCondition(behavior, user);
  }
  return true; // Default: updateable
}
```

### Inheritance Resolution

```typescript
// Get all attributes including inherited
function getAllAttributes(classType: ClassTypeModel): AttributeTypeModel[] {
  const attributes = [...classType.attributes];
  if (classType.superType) {
    const superClass = resolveClass(classType.superType);
    attributes.unshift(...getAllAttributes(superClass));
  }
  return attributes;
}
```

## Testing Criteria

### Unit Tests
- [x] Class extraction from metamodel
- [x] Attribute collection including inherited
- [x] Relation collection including inherited
- [x] Operation collection including inherited
- [x] Identifier detection
- [x] Abstract class validation
- [x] Behavior evaluation

### Integration Tests
- [x] TypeScript interface generation
- [x] Service interface generation
- [x] CRUD operations available for entities
- [x] Operations callable on instances
- [x] Inheritance chain resolved correctly
- [x] Behaviors control UI state

### Edge Cases
- [x] Abstract class instantiation (should fail)
- [x] Missing identifier on entity (validation error)
- [x] Circular inheritance (detection)
- [x] Multiple inheritance (not supported)
- [x] Transfer object CRUD attempts (should fail)
- [x] Mapped entity sync issues

## Related Specifications

**Metamodel:**
- `04-reference-typed-visual-element.md` - References ClassType
- `05-page-definition.md` - Uses ClassType as dataElement

**Data Model:**
- `02-relation-type.md` - Relations between ClassTypes
- `03-attribute-type.md` - Class attributes
- `04-operation-type.md` - Class operations
- `07-behaviors.md` - Conditional behaviors

**Runtime Model:**
- `runtime-model/01-core-types.md` - ClassTypeModel interface

**Generators:**
- `generators/03-data-model-generator.md` - ClassType generation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

