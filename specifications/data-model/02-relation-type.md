# RelationType Specification
**Reviewed:** Not yet reviewed
**Last Updated:** 2025-11-28  
**Created:** 2025-11-28  
**Status:** ✅ Complete  

---

- `visual-elements/other/01-link-element.md` - Navigate relations
**Visual Elements:**

- `actions/11-unset-action.md` - Unset ONE relation
- `actions/10-set-action.md` - Set ONE relation
- `actions/09-remove-action.md` - Remove from MANY relation
- `actions/08-add-action.md` - Add to MANY relation
**Actions:**

- `runtime-model/01-core-types.md` - RelationTypeModel interface
**Runtime Model:**

- `07-behaviors.md` - Conditional relation behaviors
- `01-class-type.md` - Contains relations
**Data Model:**

- `14-link.md` - UI representation of relations
- `04-reference-typed-visual-element.md` - References relations
**Metamodel:**

## Related Specifications

- [x] Composition cycles (should error)
- [x] Required many-cardinality (should warn)
- [x] Opposite mismatch detection
- [x] Missing opposite reference
- [x] Self-referencing relations
- [x] Circular relations (non-composition)
### Edge Cases

- [x] Conditional behaviors evaluated
- [x] CRUD operations respect flags
- [x] Composition cascade delete works
- [x] Bidirectional sync maintained
- [x] Many-to-one navigation works
- [x] One-to-many navigation works
### Integration Tests

- [x] Service method generation
- [x] Composition vs association detection
- [x] Opposite relation resolution
- [x] CRUD capability detection
- [x] Cardinality determination
- [x] Relation FQN generation
### Unit Tests

## Testing Criteria

```
await employeeService.unsetDepartment(employeeId);
// Unset ONE relation (make null)

await employeeService.setDepartment(employeeId, departmentId);
// Set ONE relation
```typescript

### Set/Unset One Relation

```
});
  content: '...'
  title: 'New Post',
const newPost = await userService.createPost(userId, {
// Create and add new entity

await userService.addPost(userId, existingPostId);
// Add existing entity
```typescript

### Add to Many Relation

```
// user.posts is now populated
});
  _expand: ['posts']
const user = await userService.getById(userId, {
// Include in query
```typescript

### Eager Loading

```
const posts = await userService.listPosts(userId);
// Load on demand

}
  posts?: Post[];  // Lazy-loaded
  name: string;
  id: string;
interface User {
// Relation not loaded initially
```typescript

### Lazy Loading

## Common Patterns

```
}
    public static List<String> getServiceMethods(RelationType relation);
    public static boolean isBidirectional(RelationType relation);
    public static boolean isManyToMany(RelationType relation);
    public static boolean isManyToOne(RelationType relation);
    public static boolean isOneToMany(RelationType relation);
    public static String getFQN(RelationType relation);
    public static RelationTypeModel extractRelationModel(RelationType relation);
public class RelationTypeGenerator {
```java
**Key Methods:**

See `generators/03-data-model-generator.md`.

## Generator Implementation

```
}
  delete?: (ownerId: string, targetId: string) => Promise<void>;
  create?: (ownerId: string, data: Partial<Target>) => Promise<Target>;
  unset?: (ownerId: string) => Promise<void>;
  set?: (ownerId: string, targetId: string) => Promise<void>;
  remove?: (ownerId: string, targetId: string) => Promise<void>;
  add?: (ownerId: string, targetId: string) => Promise<void>;
  list: (ownerId: string, query?: QueryCustomizer) => Promise<Target[]>;
interface RelationServiceMethods {
// Service methods generated

}
  annotations?: Record<string, string>;
  sourceId?: string;
  // Metadata
  
  isReadable: boolean | string;    // isReadable
  isDeletable: boolean | string;   // isDeletable
  isUpdateable: boolean | string;  // isUpdateable
  isCreatable: boolean | string;   // isCreatable (may be conditional)
  // CRUD capabilities
  
  isRequired: boolean;             // isRequired
  isComposition: boolean;          // isComposition
  // Characteristics
  
  oppositeFqn?: string;            // opposite FQN
  opposite?: string;               // opposite.name
  // Bidirectional
  
  targetFqn: string;               // target FQN
  targetType: string;              // target.name
  // Target
  
  cardinality: 'one' | 'many';     // cardinality enum
  fqn: string;                     // ownerClass.name + "#" + name
  name: string;                    // name
interface RelationTypeModel {
```typescript

## Runtime Model Mapping

- **Opposite naming:** Related but distinct (e.g., "posts" ↔ "author")
- **MANY cardinality:** Plural noun (e.g., "posts", "employees")
- **ONE cardinality:** Singular noun (e.g., "author", "department")
### Naming Conventions

- CRUD flags must make semantic sense together
- Composition relations should typically be MANY
- If `isRequired=true`, cardinality should be ONE
- Opposite relation must reference back correctly
- Cannot have circular composition (A owns B owns A)
### Constraints

- `cardinality` - Must be ONE or MANY
- `target` - Must reference valid ClassType
- `name` - Must be unique within ClassType
### Required Properties

## Validation Rules

```
</classTypes>
  </relations>
    </behaviors>
      <condition>status == 'DRAFT'</condition>
    <behaviors name="canUpdate">
    </behaviors>
      <condition>status == 'DRAFT'</condition>
    <behaviors name="canCreate">
             isComposition="true">
             cardinality="MANY"
             target="LineItem" 
  <relations name="lineItems" 
<classTypes name="Invoice">
```xml

### Example 5: Conditional CRUD

```
</classTypes>
             opposite="Project#members"/>
             cardinality="MANY"
             target="Project" 
  <relations name="projects" 
<classTypes name="User">

</classTypes>
  </relations>
    <annotations key="joinTable" value="project_members"/>
             isReadable="true">
             isUpdateable="true"
             cardinality="MANY"
             target="User" 
  <relations name="members" 
<classTypes name="Project">
```xml

### Example 4: Many-to-Many with Join Table

```
</classTypes>
             isRequired="true"/>
             opposite="Order#items"
             cardinality="ONE"
             target="Order" 
  <relations name="order" 
<classTypes name="OrderItem">

</classTypes>
             isDeletable="true"/>
             isCreatable="true"
             isComposition="true"
             cardinality="MANY"
             target="OrderItem" 
  <relations name="items" 
<classTypes name="Order">
```xml

### Example 3: Bidirectional Relation

```
}
  department: Department;  // Required, not optional
  id: string;
interface Employee {
```typescript
**Generated TypeScript:**

```
</classTypes>
             isReadable="true"/>
             isUpdateable="true"
             isRequired="true"
             cardinality="ONE"
             target="Department" 
  <relations name="department" 
<classTypes name="Employee">
```xml

### Example 2: Required Many-to-One

```
}
  deletePost(userId: string, postId: string): Promise<void>;
  removePost(userId: string, postId: string): Promise<void>;
  addPost(userId: string, post: Partial<Post>): Promise<Post>;
  listPosts(userId: string, query?: QueryCustomizer): Promise<Post[]>;
interface UserService {

}
  posts?: Post[];  // Optional, lazy-loaded
  // ... other fields
  id: string;
interface User {
```typescript
**Generated TypeScript:**

```
</classTypes>
  </relations>
    <annotations key="eager" value="false"/>
             isReadable="true">
             isDeletable="true"
             isUpdateable="true"
             isCreatable="true"
             cardinality="MANY"
             target="Post" 
  <relations name="posts" 
<classTypes name="User">
```xml

### Example 1: Simple One-to-Many

## Examples from Sample Model

```
           isUpdateable="true" isReadable="true"/>
<relations name="tags" target="Tag" cardinality="MANY"
```xml
**Add/Remove only:**

```
           isDeletable="true" isReadable="true"/>
           isCreatable="true" isUpdateable="true" 
<relations name="posts" target="Post" cardinality="MANY"
```xml
**Full CRUD:**

```
           isReadable="true"/>
<relations name="createdBy" target="User" cardinality="ONE" 
```xml
**Read-only:**

Relations support different operation sets:

### CRUD Operations

- Example: User → Department
- Deleting source doesn't affect target
- Independent lifecycle
**Association** (`isComposition=false`):

- Example: Order → OrderItems
- Deleting owner deletes children
- Strong ownership (lifecycle dependency)
**Composition** (`isComposition=true`):

### Composition vs Association

   ```
   </classTypes>
     <relations name="students" target="Student" cardinality="MANY" opposite="Student#courses"/>
   <classTypes name="Course">
   </classTypes>
     <relations name="courses" target="Course" cardinality="MANY"/>
   <classTypes name="Student">
   ```xml
3. **Many-to-Many**

   ```
   <relations name="department" target="Department" cardinality="ONE"/>
   ```xml
2. **Many-to-One**

   ```
   </classTypes>
     <relations name="author" target="User" cardinality="ONE" opposite="User#posts"/>
   <classTypes name="Post">
   </classTypes>
     <relations name="posts" target="Post" cardinality="MANY"/>
   <classTypes name="User">
   ```xml
1. **One-to-Many**

### Cardinality Patterns

## Key Concepts

```
MANY = 1  // Relation references multiple entities
ONE  = 0  // Relation references single entity
```
#### Cardinality

### Enumerations

- `opposite: RelationType` - Bidirectional partner
- `target: ClassType` - The related entity type
**References:**

- `Actions` - Add, remove, set, unset operations
- `Link.dataElement` - UI links to this relation
- `ClassType.relations` - Owning class
**Referenced By:**

- Inherited from NamedElement
**Contains:**

### Relationships

| `isReadable` | EBoolean | true | Can read related entities |
| `isDeletable` | EBoolean | false | Can delete related entities |
| `isUpdateable` | EBoolean | false | Can update relation (add/remove) |
| `isCreatable` | EBoolean | false | Can create new related entities |
|------|------|---------|-------------|
| Name | Type | Default | Description |

### CRUD Capabilities

| `isRequired` | EBoolean | 0..1 | false | Whether relation must have value |
| `isComposition` | EBoolean | 0..1 | false | Whether this is composition (ownership) |
| `opposite` | RelationType | 0..1 | - | Bidirectional opposite relation |
| `target` | ClassType | 1 | - | Target entity type |
| `cardinality` | Cardinality | 1 | MANY | ONE or MANY |
| `annotations` | Annotation | 0..* | - | Custom metadata |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `name` | EString | 1 | - | Relation name (e.g., "posts", "author") |
|------|------|--------------|---------|-------------|
| Name | Type | Multiplicity | Default | Description |

### Properties

```
   └─ RelationType
└─ DataElement (abstract)
NamedElement
```

### Class Hierarchy

## Metamodel Definition

`RelationType` defines relationships between ClassTypes, enabling navigation and management of related entities. It specifies cardinality, ownership, CRUD capabilities, and behaviors for relations like one-to-many, many-to-one, and many-to-many associations.

## Overview

**Blocks:** Runtime models, visual elements (links), actions  
**Dependencies:** `01-class-type.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Ecore Class:** `data::RelationType`  
**Domain:** Data Model  


