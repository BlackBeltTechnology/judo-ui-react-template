# REST Request Mapping Specification

## Overview

This specification defines all URL fabrication patterns and HTTP method mappings used in the JUDO UI REST service layer. It provides a comprehensive reference for:
- Generating Vitest test suites
- Implementing parser-based request mappers
- Ensuring consistency across all service implementations

## Core Concepts

### Path Construction Components

All REST paths are constructed from these building blocks:

| Component | Description | Example |
|-----------|-------------|---------|
| `{basePath}` | Application base path | `/api` |
| `{modelName}` | Application model name | `edemokracia` |
| `{actorPath}` | Actor path from package tokens + simple name | `admin/Admin` |
| `{classPath}` | ClassType package path + simple name | `admin/Issue` |
| `{relationName}` | Relation name (converted from `::` to `/`) | `issues` |
| `{operationName}` | Operation name | `createComment` |
| `{suffix}` | Operation suffix (e.g., `~get`, `~list`, `~create`) | `~update` |

### Full URL Template

```
{basePath}/{modelName}/{actorPath}/{classPath}{suffix}
```

### Path Prefix

All paths documented below are relative to the actor path prefix:
```
{basePath}/{modelName}/{actorPath}
```

For example, if:
- `basePath` = `/api`
- `modelName` = `edemokracia`
- `actorPath` = `admin/Admin`

Then the prefix is: `/api/edemokracia/admin/Admin`

## HTTP Methods

The service layer uses only two HTTP methods:

| Method | Usage |
|--------|-------|
| `GET` | Template retrieval, file downloads, metadata, principal |
| `POST` | All other operations (CRUD, list, range, operations, validation) |

## Custom Headers

| Header | Description | Used In |
|--------|-------------|---------|
| `X-Judo-SignedIdentifier` | Entity identifier for scoped operations | POST with owner context |
| `X-Judo-Mask` | Field mask for response filtering | Commands returning data |
| `X-Judo-Mark-Selected-Range-Items` | Mark selected items in range responses | Range operations |
| `X-Token` | Upload/download token | File operations |
| `Content-Type: multipart/form-data` | File upload content type | Upload operations |

---

## Entity Operations

Operations that work directly on a ClassType entity.

### Template Retrieval

Retrieves a template (default values) for creating a new entity.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/{classPath}/~template` |
| Request Body | None |
| Response | `Entity` |
| Headers | None |

**Path Construction:**
```
restPath(classType, '/~template', '', '')
→ /{packages}/{simpleName}/~template
```

**Example:**
```
GET /admin/Issue/~template
```

---

### Entity Refresh (Get by Identifier)

Retrieves a single entity by its signed identifier.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/~get` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `EntityStored` |
| Headers | `X-Judo-SignedIdentifier: {target.__signedIdentifier}` |

**Path Construction:**
```
restPath(classType, '/~get', '', '')
→ /{packages}/{simpleName}/~get
```

**Example:**
```
POST /admin/Issue/~get
Headers: X-Judo-SignedIdentifier: abc123
Body: { "_mask": "{name,description}" }
```

---

### Entity Update

Updates an existing entity.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/~update` |
| Request Body | `EntityStored` (serialized) |
| Response | `EntityStored` |
| Headers | `X-Judo-SignedIdentifier: {target.__signedIdentifier}`, `X-Judo-Mask: {mask}` |

**Path Construction:**
```
restPath(classType, '/~update', '', '')
→ /{packages}/{simpleName}/~update
```

**Example:**
```
POST /admin/Issue/~update
Headers: 
  X-Judo-SignedIdentifier: abc123
  X-Judo-Mask: {}
Body: { "__signedIdentifier": "abc123", "name": "Updated Issue", ... }
```

---

### Entity Update Validation

Validates an entity update without persisting.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/~validate` |
| Request Body | `EntityStored` (serialized) |
| Response | `EntityStored` |
| Headers | `X-Judo-SignedIdentifier: {target.__signedIdentifier}` |

**Path Construction:**
```
restPath(classType, '/~validate', '', '')
→ /{packages}/{simpleName}/~validate
```

**Example:**
```
POST /admin/Issue/~validate
Headers: X-Judo-SignedIdentifier: abc123
Body: { "__signedIdentifier": "abc123", "name": "Updated Issue", ... }
```

---

### Entity Delete

Deletes an existing entity.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/~delete` |
| Request Body | None (`undefined`) |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {target.__signedIdentifier}` |

**Path Construction:**
```
restPath(classType, '/~delete', '', '')
→ /{packages}/{simpleName}/~delete
```

**Example:**
```
POST /admin/Issue/~delete
Headers: X-Judo-SignedIdentifier: abc123
```

---

## Relation Operations

Operations that work on relations of a ClassType.

### List Relation (Collection)

Lists entities in a collection relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/{relationName}/~list` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `Array<EntityStored>` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (if owner provided) |

**Path Construction:**
```
restPath(ownerClassType, '/', relationName, '/~list')
→ /{ownerPackages}/{ownerSimpleName}/{relationName}/~list
```

**Example:**
```
POST /admin/Issue/comments/~list
Headers: X-Judo-SignedIdentifier: abc123
Body: { "_orderBy": [{"attribute": "created", "descending": true}] }
```

---

### Get Relation (Single)

Gets a single entity from a non-collection relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/{relationName}/~get` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `EntityStored \| null` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Path Construction:**
```
restPath(ownerClassType, '/', relationName, '/~get')
→ /{ownerPackages}/{ownerSimpleName}/{relationName}/~get
```

**Example:**
```
POST /admin/Issue/owner/~get
Headers: X-Judo-SignedIdentifier: abc123
Body: {}
```

---

### Refresh Access Relation (Single)

Special case: Refreshes a non-collection access relation directly.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/{relationName}/~get` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `EntityStored` |
| Headers | None (access relation, no owner) |
| Condition | Only for access relations that are NOT collections |

**Path Construction:**
```
restPath(ownerClassType, '/', relationName, '/~get')
→ /{ownerPackages}/{ownerSimpleName}/{relationName}/~get
```

**Example:**
```
POST /admin/Admin/dashboard/~get
Body: { "_mask": "{summary,stats}" }
```

**Note:** This is exposed as `refreshFor{Relation}()` method in the service interface.

---

### Get Relation Range

Gets the available range of entities that can be set/added to a relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/{relationName}/~range` |
| Request Body | `{ owner?: EntityStored, queryCustomizer?: QueryCustomizer }` |
| Response | `Array<EntityStored>` |
| Headers | `X-Judo-Mark-Selected-Range-Items: true` |

**Path Construction:**
```
restPath(ownerClassType, '/', relationName, '/~range')
→ /{ownerPackages}/{ownerSimpleName}/{relationName}/~range
```

**Example:**
```
POST /admin/Issue/owner/~range
Headers: X-Judo-Mark-Selected-Range-Items: true
Body: { 
  "owner": { "__signedIdentifier": "abc123", ... }, 
  "queryCustomizer": { "_orderBy": [...] } 
}
```

---

### Create Relation Entity

Creates a new entity through a relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern (non-access) | `/{ownerClassPath}/~update/{relationName}/~create` |
| Path Pattern (access) | `/{ownerClassPath}/{relationName}/~create` |
| Request Body | `Entity` (serialized) |
| Response | `EntityStored` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (non-access), `X-Judo-Mask: {mask}` |

**Path Construction (non-access):**
```
restPath(ownerClassType, '/~update/', relationName, '/~create')
→ /{ownerPackages}/{ownerSimpleName}/~update/{relationName}/~create
```

**Path Construction (access):**
```
restPath(ownerClassType, '/', relationName, '/~create')
→ /{ownerPackages}/{ownerSimpleName}/{relationName}/~create
```

**Example (non-access):**
```
POST /admin/Issue/~update/comments/~create
Headers: 
  X-Judo-SignedIdentifier: abc123
  X-Judo-Mask: {}
Body: { "text": "New comment", ... }
```

**Example (access):**
```
POST /admin/Admin/issues/~create
Headers: X-Judo-Mask: {}
Body: { "title": "New Issue", ... }
```

---

### Validate Create Relation Entity

Validates creation of an entity through a relation without persisting.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern (non-access) | `/{ownerClassPath}/~update/{relationName}/~validate` |
| Path Pattern (access) | `/{ownerClassPath}/{relationName}/~validate` |
| Request Body | `Entity` (serialized) |
| Response | `Entity` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (non-access only) |

**Path Construction:** Same as Create Relation Entity, but with `/~validate` suffix.

**Example:**
```
POST /admin/Issue/~update/comments/~validate
Headers: X-Judo-SignedIdentifier: abc123
Body: { "text": "New comment", ... }
```

---

### Set Relation

Sets a relation to point to specific entity/entities.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/~update/{relationName}/~set` |
| Request Body | `EntityStored` (single) or `Array<EntityStored>` (collection) |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (non-access only) |

**Path Construction:**
```
restPath(ownerClassType, '/~update/', relationName, '/~set')
→ /{ownerPackages}/{ownerSimpleName}/~update/{relationName}/~set
```

**Example:**
```
POST /admin/Issue/~update/owner/~set
Headers: X-Judo-SignedIdentifier: abc123
Body: { "__signedIdentifier": "user456", ... }
```

---

### Unset Relation

Clears a relation (sets to null/empty).

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/~update/{relationName}/~unset` |
| Request Body | None (`undefined`) |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (non-access only) |

**Path Construction:**
```
restPath(ownerClassType, '/~update/', relationName, '/~unset')
→ /{ownerPackages}/{ownerSimpleName}/~update/{relationName}/~unset
```

**Example:**
```
POST /admin/Issue/~update/owner/~unset
Headers: X-Judo-SignedIdentifier: abc123
```

---

### Add to Relation

Adds entities to a collection relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/~update/{relationName}/~add` |
| Request Body | `Array<EntityStored>` |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (non-access only) |

**Path Construction:**
```
restPath(ownerClassType, '/~update/', relationName, '/~add')
→ /{ownerPackages}/{ownerSimpleName}/~update/{relationName}/~add
```

**Example:**
```
POST /admin/Issue/~update/attachments/~add
Headers: X-Judo-SignedIdentifier: abc123
Body: [{ "__signedIdentifier": "file1", ... }, { "__signedIdentifier": "file2", ... }]
```

---

### Remove from Relation

Removes entities from a collection relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/~update/{relationName}/~remove` |
| Request Body | `Array<EntityStored>` |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (non-access only) |

**Path Construction:**
```
restPath(ownerClassType, '/~update/', relationName, '/~remove')
→ /{ownerPackages}/{ownerSimpleName}/~update/{relationName}/~remove
```

**Example:**
```
POST /admin/Issue/~update/attachments/~remove
Headers: X-Judo-SignedIdentifier: abc123
Body: [{ "__signedIdentifier": "file1", ... }]
```

---

### Export Relation

Exports relation data as a file (typically CSV/Excel).

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/{relationName}/~export` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `Blob` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (if owner provided) |
| Response Type | `blob` |

**Path Construction:**
```
restPath(ownerClassType, '/', relationName, '/~export')
→ /{ownerPackages}/{ownerSimpleName}/{relationName}/~export
```

**Example:**
```
POST /admin/Issue/comments/~export
Headers: X-Judo-SignedIdentifier: abc123
Response-Type: blob
Body: {}
```

---

## Nested Relation Operations (Target Relations)

Operations on relations of the target entity of a relation. These are accessed through relation services and operate on relations one level deeper.

### List Target Relation (Collection)

Lists entities in a collection relation of the target entity.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/{targetRelationName}/~list` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `Array<TargetRelationEntityStored>` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Path Construction:**
```
restPath(relation.target, '/', targetRelationName, '/~list')
→ /{targetPackages}/{targetSimpleName}/{targetRelationName}/~list
```

**Example:**
```
POST /admin/Comment/attachments/~list
Headers: X-Judo-SignedIdentifier: comment123
Body: {}
```

---

### Get Target Relation (Single)

Gets a single entity from a non-collection target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/{targetRelationName}/~get` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `TargetRelationEntityStored \| null` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Path Construction:**
```
restPath(relation.target, '/', targetRelationName, '/~get')
→ /{targetPackages}/{targetSimpleName}/{targetRelationName}/~get
```

**Example:**
```
POST /admin/Comment/author/~get
Headers: X-Judo-SignedIdentifier: comment123
Body: {}
```

---

### Get Target Relation Range

Gets the available range for a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/{targetRelationName}/~range` |
| Request Body | `{ owner: TargetEntityStored, queryCustomizer?: QueryCustomizer }` |
| Response | `Array<TargetRelationEntityStored>` |
| Headers | `X-Judo-Mark-Selected-Range-Items: true` |

**Path Construction:**
```
restPath(relation.target, '/', targetRelationName, '/~range')
→ /{targetPackages}/{targetSimpleName}/{targetRelationName}/~range
```

**Example:**
```
POST /admin/Comment/author/~range
Headers: X-Judo-Mark-Selected-Range-Items: true
Body: {
  "owner": { "__signedIdentifier": "comment123", ... },
  "queryCustomizer": {}
}
```

---

### Get Target Relation Template

Gets a template for creating entities in a target relation.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/{targetRelationTargetClassPath}/~template` |
| Request Body | None |
| Response | `TargetRelationEntity` |
| Headers | None |

**Path Construction:**
```
restPath(targetRelation.target, '/~template', '', '')
→ /{targetRelationTargetPackages}/{targetRelationTargetSimpleName}/~template
```

**Example:**
```
GET /admin/Attachment/~template
```

---

### Create Target Relation Entity

Creates an entity through a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~create` |
| Request Body | `TargetRelationEntity` (serialized) |
| Response | `TargetRelationEntityStored` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}`, `X-Judo-Mask: {mask}` |

**Path Construction:**
```
restPath(relation.target, '/~update/', targetRelationName, '/~create')
→ /{targetPackages}/{targetSimpleName}/~update/{targetRelationName}/~create
```

**Example:**
```
POST /admin/Comment/~update/attachments/~create
Headers: 
  X-Judo-SignedIdentifier: comment123
  X-Judo-Mask: {}
Body: { "filename": "doc.pdf", ... }
```

---

### Validate Create Target Relation Entity

Validates creation through a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~validate` |
| Request Body | `TargetRelationEntity` (serialized) |
| Response | `TargetRelationEntity` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Example:**
```
POST /admin/Comment/~update/attachments/~validate
Headers: X-Judo-SignedIdentifier: comment123
Body: { "filename": "doc.pdf", ... }
```

---

### Update Target Relation Entity

Updates an entity accessed through a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetRelationTargetClassPath}/~update` |
| Request Body | `TargetRelationEntityStored` (serialized) |
| Response | `TargetRelationEntityStored` |
| Headers | `X-Judo-SignedIdentifier: {input.__signedIdentifier}`, `X-Judo-Mask: {mask}` |

**Path Construction:**
```
restPath(targetRelation.target, '/~update', '', '')
→ /{targetRelationTargetPackages}/{targetRelationTargetSimpleName}/~update
```

**Example:**
```
POST /admin/Attachment/~update
Headers: 
  X-Judo-SignedIdentifier: attach456
  X-Judo-Mask: {}
Body: { "__signedIdentifier": "attach456", "filename": "updated.pdf", ... }
```

---

### Validate Update Target Relation Entity

Validates update of a target relation entity.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~validate` |
| Request Body | `TargetRelationEntityStored` (serialized) |
| Response | `TargetRelationEntityStored` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Note:** This uses the owner's signedIdentifier, not the target's.

**Example:**
```
POST /admin/Comment/~update/attachments/~validate
Headers: X-Judo-SignedIdentifier: comment123
Body: { "__signedIdentifier": "attach456", "filename": "updated.pdf", ... }
```

---

### Delete Target Relation Entity

Deletes an entity accessed through a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetRelationTargetClassPath}/~delete` |
| Request Body | None (`undefined`) |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {target.__signedIdentifier}` |

**Path Construction:**
```
restPath(targetRelation.target, '/~delete', '', '')
→ /{targetRelationTargetPackages}/{targetRelationTargetSimpleName}/~delete
```

**Example:**
```
POST /admin/Attachment/~delete
Headers: X-Judo-SignedIdentifier: attach456
```

---

### Set Target Relation

Sets a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~set` |
| Request Body | `TargetRelationEntityStored` or `Array<TargetRelationEntityStored>` |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Example:**
```
POST /admin/Comment/~update/author/~set
Headers: X-Judo-SignedIdentifier: comment123
Body: { "__signedIdentifier": "user789", ... }
```

---

### Unset Target Relation

Clears a target relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~unset` |
| Request Body | None (`undefined`) |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Example:**
```
POST /admin/Comment/~update/author/~unset
Headers: X-Judo-SignedIdentifier: comment123
```

---

### Add to Target Relation

Adds entities to a target collection relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~add` |
| Request Body | `Array<TargetRelationEntityStored>` |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Example:**
```
POST /admin/Comment/~update/attachments/~add
Headers: X-Judo-SignedIdentifier: comment123
Body: [{ "__signedIdentifier": "file1", ... }]
```

---

### Remove from Target Relation

Removes entities from a target collection relation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/~update/{targetRelationName}/~remove` |
| Request Body | `Array<TargetRelationEntityStored>` |
| Response | `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |

**Example:**
```
POST /admin/Comment/~update/attachments/~remove
Headers: X-Judo-SignedIdentifier: comment123
Body: [{ "__signedIdentifier": "file1", ... }]
```

---

### Export Target Relation

Exports target relation data.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetClassPath}/{targetRelationName}/~export` |
| Request Body | `QueryCustomizer` (optional) |
| Response | `Blob` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` |
| Response Type | `blob` |

**Example:**
```
POST /admin/Comment/attachments/~export
Headers: X-Judo-SignedIdentifier: comment123
Response-Type: blob
Body: {}
```

---

### Target Relation Operation Invocation

Invokes an operation on a target relation's target entity.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetRelationTargetClassPath}/{operationName}` |
| Request Body | `InputEntity` (if operation has input) or `undefined` |
| Response | `OutputEntity` (if operation has output) or `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (if non-static) |

**Path Construction:**
```
operationRestPath(targetRelation.target, operation, '')
→ /{targetRelationTargetPackages}/{targetRelationTargetSimpleName}/{operationName}
```

**Example:**
```
POST /admin/Attachment/download
Headers: X-Judo-SignedIdentifier: attach456
Body: {}
```

---

### Validate Target Relation Operation Input

Validates input for a target relation operation.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetRelationTargetClassPath}/{operationName}/~validate` |
| Request Body | `InputEntity` |
| Response | `InputEntity` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (if non-static) |

**Example:**
```
POST /admin/Attachment/download/~validate
Headers: X-Judo-SignedIdentifier: attach456
Body: { "format": "pdf" }
```

---

### Get Target Relation Operation Input Template

Gets template for target relation operation input.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/{operationInputClassPath}/~template` |
| Request Body | None |
| Response | `InputEntity` |
| Headers | None |

**Example:**
```
GET /admin/DownloadInput/~template
```

---

### Get Target Relation Operation Input Range

Gets range for target relation operation input.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{targetRelationTargetClassPath}/{operationName}/~range` |
| Request Body | `{ owner?: TargetRelationTargetStored, queryCustomizer?: QueryCustomizer }` |
| Response | `Array<InputEntityStored>` |
| Headers | `X-Judo-Mark-Selected-Range-Items: true` |

**Example:**
```
POST /admin/Attachment/selectFormat/~range
Headers: X-Judo-Mark-Selected-Range-Items: true
Body: {
  "owner": { "__signedIdentifier": "attach456", ... },
  "queryCustomizer": {}
}
```

---

## Operation Invocations

Operations defined on ClassTypes (business logic).

### Invoke Operation

Invokes a custom operation on an entity.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/{operationName}` |
| Request Body | `InputEntity` (if operation has input) or `undefined` |
| Response | `OutputEntity` (if operation has output) or `void` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (if mapped/non-static) |

**Path Construction:**
```
operationRestPath(classType, operation, '')
→ /{packages}/{simpleName}/{operationName}
```

**Example:**
```
POST /admin/Issue/createComment
Headers: X-Judo-SignedIdentifier: abc123
Body: { "text": "My comment" }
```

---

### Validate Operation Input

Validates operation input without execution.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/{operationName}/~validate` |
| Request Body | `InputEntity` |
| Response | `InputEntity` |
| Headers | `X-Judo-SignedIdentifier: {owner.__signedIdentifier}` (if mapped/non-static) |

**Path Construction:**
```
operationRestPath(classType, operation, '/~validate')
→ /{packages}/{simpleName}/{operationName}/~validate
```

**Example:**
```
POST /admin/Issue/createComment/~validate
Headers: X-Judo-SignedIdentifier: abc123
Body: { "text": "My comment" }
```

---

### Get Operation Input Template

Gets a template for the operation's input type.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/{inputClassPath}/~template` |
| Request Body | None |
| Response | `InputEntity` |
| Headers | None |

**Path Construction:**
```
restPath(operation.input.target, '/~template', '', '')
→ /{inputPackages}/{inputSimpleName}/~template
```

**Example:**
```
GET /admin/CreateCommentInput/~template
```

---

### Get Operation Input Range

Gets the available range for the operation's input (selector operations).

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{classPath}/{operationName}/~range` |
| Request Body | `{ owner?: EntityStored, queryCustomizer?: QueryCustomizer }` |
| Response | `Array<InputEntityStored>` |
| Headers | `X-Judo-Mark-Selected-Range-Items: true` |

**Path Construction:**
```
operationRestPath(classType, operation, '/~range')
→ /{packages}/{simpleName}/{operationName}/~range
```

**Example:**
```
POST /admin/Issue/assignUser/~range
Headers: X-Judo-Mark-Selected-Range-Items: true
Body: { 
  "owner": { "__signedIdentifier": "abc123", ... },
  "queryCustomizer": {}
}
```

---

### Get Operation Input Relation Range

Gets the range for a relation on the operation's input type.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{inputClassPath}/{relationName}/~range` |
| Request Body | `{ owner?: InputEntityStored, queryCustomizer?: QueryCustomizer }` |
| Response | `Array<RelationTargetStored>` |
| Headers | `X-Judo-Mark-Selected-Range-Items: true` |

**Path Construction:**
```
restPath(operation.input.target, '/', relationName, '/~range')
→ /{inputPackages}/{inputSimpleName}/{relationName}/~range
```

**Example:**
```
POST /admin/CreateIssueInput/category/~range
Headers: X-Judo-Mark-Selected-Range-Items: true
Body: { 
  "owner": {},
  "queryCustomizer": {}
}
```

---

## Access Service Operations

Special operations for application-level access.

### Get Principal

Gets the current authenticated user's principal data.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/~principal` |
| Request Body | None |
| Response | `PrincipalStored` |
| Headers | None |

**Path Construction:**
```
rootPathForApp(application) + '/~principal'
```

**Example:**
```
GET /~principal
```

---

### Get Metadata

Gets application metadata.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/~meta` |
| Request Body | None |
| Response | `JudoMetaData` |
| Headers | None |

**Path Construction:**
```
rootPathForApp(application) + '/~meta'
```

**Example:**
```
GET /~meta
```

---

### Upload File

Uploads a file to a binary attribute.

| Property | Value |
|----------|-------|
| Method | `POST` (2 requests) |
| Path Pattern 1 | `{attributePath}/~upload-token` |
| Path Pattern 2 | `/upload` (file service) |
| Request Body 1 | None |
| Request Body 2 | `FormData` |
| Response | Upload result |
| Headers 2 | `Content-Type: multipart/form-data`, `X-Token: {token}` |

**Flow:**
1. POST to `{attributePath}/~upload-token` to get upload token
2. POST to file service `/upload` with token and file

**Example:**
```
POST /admin/Issue/avatar/~upload-token
→ Response: { "token": "xyz789" }

POST /upload
Headers: 
  Content-Type: multipart/form-data
  X-Token: xyz789
Body: FormData with file
```

---

### Download File

Downloads a file from a binary attribute.

| Property | Value |
|----------|-------|
| Method | `GET` |
| Path Pattern | `/download?disposition={disposition}` (file service) |
| Request Body | None |
| Response | `Blob` |
| Headers | `X-Token: {downloadToken}` |
| Response Type | `blob` |

**Example:**
```
GET /download?disposition=attachment
Headers: X-Token: abc123
Response-Type: blob
```

---

### Find Instance by Identifier

Finds a specific entity instance from an access relation by identifier.

| Property | Value |
|----------|-------|
| Method | `POST` |
| Path Pattern | `/{ownerClassPath}/{relationName}/~list` |
| Request Body | `{ _identifier: string, _mask?: string, _seek: { limit: 1 } }` |
| Response | `Array<EntityStored>` (expect single item or empty) |
| Headers | None |

**Example:**
```
POST /admin/Admin/issues/~list
Body: {
  "_identifier": "abc123",
  "_mask": "{name,description}",
  "_seek": { "limit": 1 }
}
```

---

## Path Construction Rules

### ClassType Path

```java
restPath(ClassType classType, String first, String name, String second) {
    String suffix = first + (name == null ? "" : name.replace("::", "/")) + (second == null ? "" : second);
    String packages = !classType.packageNameTokens.isEmpty() 
        ? String.join("/", classType.packageNameTokens) + "/" 
        : "";
    return "/" + packages + classType.simpleName
        .split("::")
        .filter(i -> !i.contains("_default_transferobjecttypes"))
        .join("/") + suffix;
}
```

### Operation Path

```java
operationRestPath(ClassType classType, OperationType operation, String suffix) {
    return classTypeRestPath(classType, "") + "/" + operation.name + suffix;
}

classTypeRestPath(ClassType classType, String suffix) {
    return "/" + classType.name
        .split("::")
        .filter(i -> !i.contains("_default_transferobjecttypes"))
        .join("/") + suffix;
}
```

### Relation Path

```java
relationRestPath(RelationType relation, String suffix) {
    return String.join("/", relation.ownerPackageNameTokens)
        + "/" + relation.ownerSimpleName
        + "/" + relation.name
        + suffix;
}
```

### Actor Path

```java
rootPathForApp(Application app) {
    return String.join("/", app.actor.packageNameTokens)
        + "/" + app.actor.simpleName;
}
```

---

## Request Mapping Summary Table

### Entity Operations

| Operation | HTTP Method | Path Pattern | Has Owner Header | Has Body | Returns Data |
|-----------|-------------|--------------|------------------|----------|--------------|
| getTemplate | GET | `/{classPath}/~template` | No | No | Yes |
| refresh | POST | `/{classPath}/~get` | Yes | Optional | Yes |
| update | POST | `/{classPath}/~update` | Yes | Yes | Yes |
| validateUpdate | POST | `/{classPath}/~validate` | Yes | Yes | Yes |
| delete | POST | `/{classPath}/~delete` | Yes | No | No |

### Relation Operations

| Operation | HTTP Method | Path Pattern | Has Owner Header | Has Body | Returns Data |
|-----------|-------------|--------------|------------------|----------|--------------|
| list{Relation} | POST | `/{classPath}/{relation}/~list` | Yes (optional) | Optional | Yes (array) |
| get{Relation} | POST | `/{classPath}/{relation}/~get` | Yes | Optional | Yes/Null |
| refreshFor{Relation} (access) | POST | `/{classPath}/{relation}/~get` | No | Optional | Yes |
| getRangeFor{Relation} | POST | `/{classPath}/{relation}/~range` | Optional (body) | Yes | Yes (array) |
| getTemplateFor{Relation} | GET | `/{targetClassPath}/~template` | No | No | Yes |
| create{Relation} | POST | `/{classPath}/~update/{relation}/~create` | Yes | Yes | Yes |
| create{Relation} (access) | POST | `/{classPath}/{relation}/~create` | No | Yes | Yes |
| validateCreate{Relation} | POST | `/{classPath}/~update/{relation}/~validate` | Yes | Yes | Yes |
| validateCreate{Relation} (access) | POST | `/{classPath}/{relation}/~validate` | No | Yes | Yes |
| set{Relation} | POST | `/{classPath}/~update/{relation}/~set` | Yes (non-access) | Yes | No |
| unset{Relation} | POST | `/{classPath}/~update/{relation}/~unset` | Yes (non-access) | No | No |
| add{Relation} | POST | `/{classPath}/~update/{relation}/~add` | Yes (non-access) | Yes (array) | No |
| remove{Relation} | POST | `/{classPath}/~update/{relation}/~remove` | Yes (non-access) | Yes (array) | No |
| delete{Relation} | POST | `/{targetClassPath}/~delete` | Yes | No | No |
| export{Relation} | POST | `/{classPath}/{relation}/~export` | Optional | Optional | Yes (blob) |

### Nested Relation Operations (via Relation Services)

| Operation | HTTP Method | Path Pattern | Has Owner Header | Has Body | Returns Data |
|-----------|-------------|--------------|------------------|----------|--------------|
| list{TargetRelation} | POST | `/{targetPath}/{targetRel}/~list` | Yes | Optional | Yes (array) |
| get{TargetRelation} | POST | `/{targetPath}/{targetRel}/~get` | Yes | Optional | Yes/Null |
| getRangeFor{TargetRelation} | POST | `/{targetPath}/{targetRel}/~range` | In body | Yes | Yes (array) |
| getTemplateFor{TargetRelation} | GET | `/{targetRelTargetPath}/~template` | No | No | Yes |
| create{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~create` | Yes | Yes | Yes |
| validateCreate{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~validate` | Yes | Yes | Yes |
| update{TargetRelation} | POST | `/{targetRelTargetPath}/~update` | Yes (target's) | Yes | Yes |
| validateUpdate{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~validate` | Yes (owner's) | Yes | Yes |
| delete{TargetRelation} | POST | `/{targetRelTargetPath}/~delete` | Yes | No | No |
| set{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~set` | Yes | Yes | No |
| unset{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~unset` | Yes | No | No |
| add{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~add` | Yes | Yes (array) | No |
| remove{TargetRelation} | POST | `/{targetPath}/~update/{targetRel}/~remove` | Yes | Yes (array) | No |
| export{TargetRelation} | POST | `/{targetPath}/{targetRel}/~export` | Yes | Optional | Yes (blob) |

### Operation Invocations

| Operation | HTTP Method | Path Pattern | Has Owner Header | Has Body | Returns Data |
|-----------|-------------|--------------|------------------|----------|--------------|
| {operation} | POST | `/{classPath}/{operation}` | Mapped only | If input | If output |
| {operation}For{Relation} | POST | `/{targetPath}/{operation}` | Mapped only | If input | If output |
| {operation}For{TargetRelation} | POST | `/{targetRelTargetPath}/{operation}` | Non-static only | If input | If output |
| validateOn{Operation} | POST | `/{classPath}/{operation}/~validate` | Mapped only | Yes | Yes |
| validateOn{Op}For{Rel} | POST | `/{targetPath}/{operation}/~validate` | Mapped only | Yes | Yes |
| validateOn{Op}For{TargetRel} | POST | `/{targetRelTargetPath}/{op}/~validate` | Non-static only | Yes | Yes |
| getTemplateOn{Operation} | GET | `/{inputClassPath}/~template` | No | No | Yes |
| getRangeOn{Operation} | POST | `/{classPath}/{operation}/~range` | Optional (body) | Yes | Yes (array) |
| getRangeOn{Op}For{Rel} | POST | `/{targetPath}/{operation}/~range` | Optional (body) | Yes | Yes (array) |
| getRangeOn{Op}For{TargetRel} | POST | `/{targetRelTargetPath}/{op}/~range` | Non-static (body) | Yes | Yes (array) |
| getRangeOn{Op}For{InputRel} | POST | `/{inputPath}/{inputRel}/~range` | Optional (body) | Yes | Yes (array) |

### Access Service Operations

| Operation | HTTP Method | Path Pattern | Has Owner Header | Has Body | Returns Data |
|-----------|-------------|--------------|------------------|----------|--------------|
| getPrincipal | GET | `/~principal` | No | No | Yes |
| getMetaData | GET | `/~meta` | No | No | Yes |
| uploadFile (token) | POST | `{attrPath}/~upload-token` | No | No | Yes (token) |
| uploadFile (file) | POST | `/upload` (file service) | X-Token | Yes (FormData) | Yes |
| downloadFile | GET | `/download?disposition=...` | X-Token | No | Yes (blob) |
| findInstanceOf{Relation} | POST | `/{classPath}/{relation}/~list` | No | Yes (special) | Yes (array) |

---

## Test Case Categories

For generating Vitest test suites, implement tests for each category:

### 1. Path Construction Tests

```typescript
describe('Path Construction', () => {
  describe('restPath', () => {
    it('should construct path for ClassType without package', () => {
      // Given: classType with empty packageNameTokens, simpleName = "Issue"
      // When: restPath(classType, '/~template', '', '')
      // Then: "/Issue/~template"
    });
    
    it('should construct path for ClassType with single package', () => {
      // Given: classType with packageNameTokens = ["admin"], simpleName = "Issue"
      // When: restPath(classType, '/~template', '', '')
      // Then: "/admin/Issue/~template"
    });
    
    it('should construct path for ClassType with nested packages', () => {
      // Given: classType with packageNameTokens = ["org", "example", "admin"], simpleName = "Issue"
      // When: restPath(classType, '/~template', '', '')
      // Then: "/org/example/admin/Issue/~template"
    });
    
    it('should handle relation name conversion', () => {
      // Given: relationName with "::" separator like "sub::relation"
      // When: restPath(classType, '/', 'sub::relation', '/~list')
      // Then: path contains "sub/relation" (converted)
    });
    
    it('should filter out _default_transferobjecttypes segments', () => {
      // Given: classType.simpleName contains "_default_transferobjecttypes"
      // When: restPath(classType, '/~template', '', '')
      // Then: segment is filtered out of path
    });
    
    it('should concatenate first, name, and second correctly', () => {
      // Given: first = '/~update/', name = 'comments', second = '/~create'
      // When: restPath(classType, first, name, second)
      // Then: "/{classPath}/~update/comments/~create"
    });
    
    it('should handle null name parameter', () => {
      // Given: name = null
      // When: restPath(classType, '/~get', null, '')
      // Then: "/{classPath}/~get"
    });
    
    it('should handle null second parameter', () => {
      // Given: second = null
      // When: restPath(classType, '/', 'comments', null)
      // Then: "/{classPath}/comments"
    });
  });

  describe('operationRestPath', () => {
    it('should append operation name to class path', () => {
      // Given: classType, operation.name = "createComment"
      // When: operationRestPath(classType, operation, '')
      // Then: "/{classPath}/createComment"
    });
    
    it('should append suffix after operation name', () => {
      // Given: suffix = "/~validate"
      // When: operationRestPath(classType, operation, '/~validate')
      // Then: "/{classPath}/{operationName}/~validate"
    });
    
    it('should append range suffix', () => {
      // Given: suffix = "/~range"
      // When: operationRestPath(classType, operation, '/~range')
      // Then: "/{classPath}/{operationName}/~range"
    });
  });

  describe('relationRestPath', () => {
    it('should construct path from owner package tokens', () => {
      // Given: relation with ownerPackageNameTokens = ["admin"]
      // When: relationRestPath(relation, '')
      // Then: "admin/{ownerSimpleName}/{relationName}"
    });
    
    it('should include owner simple name and relation name', () => {
      // Given: ownerSimpleName = "Issue", relationName = "comments"
      // When: relationRestPath(relation, '/~list')
      // Then: contains "Issue/comments/~list"
    });
  });

  describe('rootPathForApp', () => {
    it('should construct actor path from package tokens', () => {
      // Given: app.actor.packageNameTokens = ["admin"]
      // When: rootPathForApp(app)
      // Then: "admin/{actorSimpleName}"
    });
    
    it('should include actor simple name', () => {
      // Given: app.actor.simpleName = "Admin"
      // When: rootPathForApp(app)
      // Then: ends with "/Admin"
    });
  });
});
```

### 2. HTTP Method Tests

```typescript
describe('HTTP Methods', () => {
  describe('GET operations', () => {
    it('should use GET for getTemplate', () => {
      // Entity template
    });
    
    it('should use GET for getTemplateFor{Relation}', () => {
      // Relation target template
    });
    
    it('should use GET for getTemplateOn{Operation}', () => {
      // Operation input template
    });
    
    it('should use GET for getTemplateOn{Op}For{Rel}', () => {
      // Nested operation input template
    });
    
    it('should use GET for getPrincipal', () => {
      // Access service principal
    });
    
    it('should use GET for getMetaData', () => {
      // Access service metadata
    });
    
    it('should use GET for downloadFile', () => {
      // File download
    });
  });

  describe('POST operations', () => {
    const postOperations = [
      'refresh', 'update', 'validateUpdate', 'delete',
      'list', 'get (relation)', 'getRange', 
      'create', 'validateCreate', 'set', 'unset', 'add', 'remove',
      'export', 'operation invocation', 'validateOn{Operation}',
      'getRangeOn{Operation}', 'uploadFile (token)', 'uploadFile (file)',
      'findInstanceOf{Relation}'
    ];
    
    postOperations.forEach(op => {
      it(`should use POST for ${op}`, () => {
        // Verify POST method
      });
    });
  });
});
```

### 3. Header Tests

```typescript
describe('Request Headers', () => {
  describe('X-Judo-SignedIdentifier', () => {
    it('should include for entity refresh', () => {
      // target.__signedIdentifier
    });
    
    it('should include for entity update', () => {
      // target.__signedIdentifier
    });
    
    it('should include for entity delete', () => {
      // target.__signedIdentifier
    });
    
    it('should include for list relation when owner provided', () => {
      // owner.__signedIdentifier (optional)
    });
    
    it('should NOT include for list relation without owner (access)', () => {
      // No header when owner is undefined
    });
    
    it('should include for get relation', () => {
      // owner.__signedIdentifier
    });
    
    it('should include for create relation (non-access)', () => {
      // owner.__signedIdentifier
    });
    
    it('should NOT include for create relation (access)', () => {
      // Access relations don't require owner header for create
    });
    
    it('should include for set/unset/add/remove (non-access)', () => {
      // owner.__signedIdentifier
    });
    
    it('should NOT include for set/unset/add/remove (access)', () => {
      // Access relations may not require owner header
    });
    
    it('should include for mapped operations', () => {
      // owner.__signedIdentifier when operation.isMapped
    });
    
    it('should NOT include for static operations', () => {
      // No header when operation.isStatic
    });
    
    it('should include for target relation operations', () => {
      // owner.__signedIdentifier for nested relations
    });
  });

  describe('X-Judo-Mask', () => {
    it('should include for update', () => {
      // queryCustomizer?._mask ?? '{}'
    });
    
    it('should include for create', () => {
      // queryCustomizer?._mask ?? '{}'
    });
    
    it('should default to {} if not provided', () => {
      // DEFAULT_COMMAND_MASK = '{}'
    });
    
    it('should NOT include for delete', () => {
      // No mask for void operations
    });
    
    it('should NOT include for set/unset/add/remove', () => {
      // No mask for void operations
    });
  });

  describe('X-Judo-Mark-Selected-Range-Items', () => {
    it('should be true for relation range', () => {
      // getRangeFor{Relation}
    });
    
    it('should be true for operation input range', () => {
      // getRangeOn{Operation}
    });
    
    it('should be true for target relation range', () => {
      // getRangeFor{TargetRelation}
    });
    
    it('should be true for nested operation range', () => {
      // getRangeOn{Op}For{Rel}
    });
  });

  describe('X-Token', () => {
    it('should include for file upload', () => {
      // responseToken.data.token
    });
    
    it('should include for file download', () => {
      // downloadToken parameter
    });
  });

  describe('Content-Type', () => {
    it('should be multipart/form-data for file upload', () => {
      // FormData upload
    });
  });
});
```

### 4. Request Body Tests

```typescript
describe('Request Body', () => {
  describe('Entity operations', () => {
    it('should serialize entity for update', () => {
      // entitySerializer.serialize(target)
    });
    
    it('should serialize entity for create', () => {
      // entitySerializer.serialize(target)
    });
    
    it('should send undefined for delete', () => {
      // body = undefined
    });
    
    it('should send undefined for unset', () => {
      // body = undefined
    });
  });
  
  describe('Collection operations', () => {
    it('should serialize array for add', () => {
      // selected.map(s => serializer.serialize(s))
    });
    
    it('should serialize array for remove', () => {
      // selected.map(s => serializer.serialize(s))
    });
    
    it('should serialize single or array for set based on relation', () => {
      // Array for collection, single for non-collection
    });
  });
  
  describe('Query operations', () => {
    it('should serialize queryCustomizer for list', () => {
      // serializeQueryCustomizer(queryCustomizer) ?? {}
    });
    
    it('should include owner and queryCustomizer for range', () => {
      // { owner: serialized, queryCustomizer: serialized }
    });
    
    it('should send empty object for range without owner', () => {
      // { owner: {}, queryCustomizer: ... } or just { queryCustomizer: ... }
    });
  });
  
  describe('Operation invocations', () => {
    it('should serialize input for operations with input', () => {
      // inputSerializer.serialize(target)
    });
    
    it('should send undefined for operations without input', () => {
      // body = undefined
    });
    
    it('should send null when target is falsy', () => {
      // target ? serialize(target) : null
    });
  });
  
  describe('Find instance operation', () => {
    it('should include _identifier in body', () => {
      // { _identifier: identifier }
    });
    
    it('should include _mask in body when provided', () => {
      // { _mask: mask }
    });
    
    it('should include _seek with limit 1', () => {
      // { _seek: { limit: 1 } }
    });
  });
});
```

### 5. Response Handling Tests

```typescript
describe('Response Handling', () => {
  describe('Single entity deserialization', () => {
    it('should deserialize refresh response', () => {
      // storedSerializer.deserialize(data)
    });
    
    it('should deserialize update response', () => {
      // storedSerializer.deserialize(data)
    });
    
    it('should deserialize create response', () => {
      // storedSerializer.deserialize(data)
    });
    
    it('should deserialize template response', () => {
      // serializer.deserialize(data) (non-stored)
    });
    
    it('should handle null output for operations', () => {
      // data ? deserialize(data) : data
    });
  });
  
  describe('Nullable entity deserialization', () => {
    it('should return null for empty string response', () => {
      // (typeof data === 'string' && !data.length) ? null : deserialize(data)
    });
    
    it('should return null for non-existent single relation', () => {
      // get{Relation} can return null
    });
  });
  
  describe('Array deserialization', () => {
    it('should map array response through deserializer', () => {
      // data.map(d => deserializer.deserialize(d))
    });
    
    it('should return empty array for non-array response', () => {
      // Array.isArray(data) ? map(...) : []
    });
    
    it('should return empty array for list', () => {
      // Graceful handling
    });
  });
  
  describe('Blob responses', () => {
    it('should return blob for export', () => {
      // responseType: 'blob'
    });
    
    it('should return blob for download', () => {
      // responseType: 'blob'
    });
  });
  
  describe('Void responses', () => {
    it('should return void for delete', () => {
      // No data transformation
    });
    
    it('should return void for set/unset/add/remove', () => {
      // No data transformation
    });
  });
});
```

### 6. Access vs Non-Access Relation Tests

```typescript
describe('Access vs Non-Access Relations', () => {
  describe('Access relations', () => {
    it('should NOT include /~update/ in create path', () => {
      // /{classPath}/{relation}/~create (not /~update/{relation}/~create)
    });
    
    it('should NOT require X-Judo-SignedIdentifier for create', () => {
      // No owner header
    });
    
    it('should NOT require owner for list', () => {
      // owner parameter is optional
    });
    
    it('should use JudoIdentifiable type for optional owner', () => {
      // JudoIdentifiable<any> instead of specific Stored type
    });
    
    it('should support refreshFor{Relation} for non-collection access', () => {
      // Special method for access relation refresh
    });
  });

  describe('Non-access relations', () => {
    it('should include /~update/ in create path', () => {
      // /{classPath}/~update/{relation}/~create
    });
    
    it('should require X-Judo-SignedIdentifier for create', () => {
      // owner.__signedIdentifier required
    });
    
    it('should require owner for list (when provided)', () => {
      // owner header included when owner parameter given
    });
    
    it('should use specific Stored type for owner', () => {
      // {ClassType}Stored type
    });
  });
});
```

### 7. Operation Variant Tests

```typescript
describe('Operation Variants', () => {
  describe('Mapped operations (isMapped = true)', () => {
    it('should include X-Judo-SignedIdentifier header', () => {
      // owner.__signedIdentifier
    });
    
    it('should require owner parameter', () => {
      // First parameter is owner
    });
  });

  describe('Static operations (isStatic = true)', () => {
    it('should NOT include X-Judo-SignedIdentifier header', () => {
      // No owner header
    });
    
    it('should NOT require owner parameter', () => {
      // No owner in signature
    });
  });

  describe('Operations with input', () => {
    it('should serialize input to request body', () => {
      // inputSerializer.serialize(target)
    });
    
    it('should support validation endpoint', () => {
      // validateOn{Operation} with /~validate suffix
    });
    
    it('should support template retrieval when input.target.isTemplateable', () => {
      // getTemplateOn{Operation}
    });
  });

  describe('Operations with output', () => {
    it('should deserialize response with output serializer', () => {
      // outputStoredSerializer.deserialize(data)
    });
  });

  describe('Operations without input', () => {
    it('should send undefined as body', () => {
      // body = undefined
    });
  });

  describe('Operations without output', () => {
    it('should return void', () => {
      // Promise<JudoRestResponse<void>>
    });
  });

  describe('Input rangeable operations (isInputRangeable = true)', () => {
    it('should support range endpoint', () => {
      // getRangeOn{Operation} with /~range suffix
    });
    
    it('should include owner in body for non-static', () => {
      // { owner: serialized, queryCustomizer: ... }
    });
    
    it('should NOT include owner in body for static', () => {
      // { queryCustomizer: ... } only
    });
  });

  describe('Input validatable operations', () => {
    it('should check OperationTargetBehaviourType.VALIDATE_INPUT', () => {
      // operation.input.behaviours.includes(VALIDATE_INPUT)
    });
  });
});
```

### 8. Nested Relation (Target Relation) Tests

```typescript
describe('Nested Relations (Target Relations)', () => {
  describe('Path construction', () => {
    it('should use target class path for list/get/range', () => {
      // /{relation.target.path}/{targetRelation}/~list
    });
    
    it('should use target class path with /~update/ for mutations', () => {
      // /{relation.target.path}/~update/{targetRelation}/~create
    });
    
    it('should use targetRelation.target path for template/delete', () => {
      // /{targetRelation.target.path}/~template
    });
  });
  
  describe('Headers', () => {
    it('should use owner (target entity) signedIdentifier', () => {
      // The "owner" in nested context is the target of the parent relation
    });
  });
  
  describe('Operations on target relation targets', () => {
    it('should construct operation path from targetRelation.target', () => {
      // /{targetRelation.target.path}/{operation}
    });
    
    it('should handle static vs non-static correctly', () => {
      // Check operation.isStatic for header inclusion
    });
  });
});
```

### 9. File Operations Tests

```typescript
describe('File Operations', () => {
  describe('Upload flow', () => {
    it('should first request upload token', () => {
      // POST {attributePath}/~upload-token
    });
    
    it('should then upload with token in X-Token header', () => {
      // POST /upload with X-Token header
    });
    
    it('should use multipart/form-data content type', () => {
      // Content-Type: multipart/form-data
    });
    
    it('should throw if token request fails', () => {
      // Error handling for token step
    });
    
    it('should throw if upload returns non-200', () => {
      // Error handling for upload step
    });
  });
  
  describe('Download', () => {
    it('should use GET method', () => {
      // GET request
    });
    
    it('should include disposition query parameter', () => {
      // ?disposition=inline or ?disposition=attachment
    });
    
    it('should include X-Token header', () => {
      // downloadToken in header
    });
    
    it('should use blob responseType', () => {
      // responseType: 'blob'
    });
  });
});
```

---

## Parser Implementation Interface

### Core Types

```typescript
/**
 * HTTP method types used in the REST layer
 */
type HttpMethod = 'GET' | 'POST';

/**
 * Response type indicating how to handle the response
 */
type ResponseType = 'json' | 'blob';

/**
 * How to deserialize the response
 */
type DeserializationType = 'single' | 'array' | 'void' | 'nullable';

/**
 * Service type categories
 */
type ServiceType = 'class' | 'relation' | 'access';
```

### Request Configuration

```typescript
interface RequestConfiguration {
  /**
   * HTTP method to use
   */
  method: HttpMethod;
  
  /**
   * Constructed path relative to the actor prefix
   * e.g., "/admin/Issue/~get"
   */
  path: string;
  
  /**
   * Request body (undefined means no body)
   */
  body?: unknown;
  
  /**
   * Request headers to include
   */
  headers: Record<string, string>;
  
  /**
   * Response type (default: 'json')
   */
  responseType: ResponseType;
  
  /**
   * Information for deserializing the response
   */
  responseDeserializer: ResponseDeserializerConfig;
}

interface ResponseDeserializerConfig {
  /**
   * Type of deserialization to apply
   */
  type: DeserializationType;
  
  /**
   * Target class FQName for deserialization (if applicable)
   */
  targetClassFqName?: string;
  
  /**
   * Whether the target is a stored (persisted) entity
   */
  isStored: boolean;
}
```

### Request Context

```typescript
interface RequestContext {
  /**
   * The type of service making the request
   */
  serviceType: ServiceType;
  
  /**
   * ClassType metadata (for class services)
   */
  classType?: ClassTypeMetadata;
  
  /**
   * Relation metadata (for relation services)
   */
  relation?: RelationTypeMetadata;
  
  /**
   * Method arguments in order
   */
  args: unknown[];
  
  /**
   * Application metadata
   */
  application: ApplicationMetadata;
}
```

### Request Mapper Interface

```typescript
interface RequestMapper {
  /**
   * Map a service method call to an HTTP request configuration
   * 
   * @param methodName - The name of the method being called
   * @param context - Context information about the service and arguments
   * @returns Configuration for the HTTP request
   */
  mapRequest(methodName: string, context: RequestContext): RequestConfiguration;
}
```

### Method Pattern Matching

The request mapper should implement pattern matching for method names:

```typescript
interface MethodPattern {
  /**
   * Regex pattern to match method name
   */
  pattern: RegExp;
  
  /**
   * Extractor for dynamic parts (relation name, operation name, etc.)
   */
  extract: (match: RegExpMatchArray) => ExtractedParts;
  
  /**
   * Configuration builder
   */
  buildConfig: (parts: ExtractedParts, context: RequestContext) => RequestConfiguration;
}

interface ExtractedParts {
  relationName?: string;
  operationName?: string;
  targetRelationName?: string;
  isValidation?: boolean;
  isTemplate?: boolean;
  isRange?: boolean;
}
```

### Pattern Definitions

```typescript
const CLASS_SERVICE_PATTERNS: MethodPattern[] = [
  // Entity operations
  { pattern: /^getTemplate$/, ... },
  { pattern: /^refresh$/, ... },
  { pattern: /^update$/, ... },
  { pattern: /^validateUpdate$/, ... },
  { pattern: /^delete$/, ... },
  
  // Relation operations
  { pattern: /^getTemplateFor(\w+)$/, ... },
  { pattern: /^create(\w+)$/, ... },
  { pattern: /^validateCreate(\w+)$/, ... },
  { pattern: /^list(\w+)$/, ... },
  { pattern: /^get(\w+)$/, ... },  // Non-collection relation
  { pattern: /^getRangeFor(\w+)$/, ... },
  { pattern: /^set(\w+)$/, ... },
  { pattern: /^unset(\w+)$/, ... },
  { pattern: /^add(\w+)$/, ... },
  { pattern: /^remove(\w+)$/, ... },
  { pattern: /^delete(\w+)$/, ... },
  { pattern: /^export(\w+)$/, ... },
  
  // Operations for relations
  { pattern: /^(\w+)For(\w+)$/, ... },
  { pattern: /^validateOn(\w+)For(\w+)$/, ... },
  { pattern: /^getTemplateOn(\w+)For(\w+)$/, ... },
  { pattern: /^getRangeOn(\w+)For(\w+)$/, ... },
  
  // Direct operations
  { pattern: /^validateOn(\w+)$/, ... },
  { pattern: /^getTemplateOn(\w+)$/, ... },
  { pattern: /^getRangeOn(\w+)$/, ... },
  // ... and the operation itself matches by checking metadata
];

const RELATION_SERVICE_PATTERNS: MethodPattern[] = [
  // Basic operations
  { pattern: /^list$/, ... },
  { pattern: /^export$/, ... },
  { pattern: /^refresh$/, ... },
  { pattern: /^refreshFor(\w+)$/, ... },  // Access relation only
  { pattern: /^getRange$/, ... },
  { pattern: /^getTemplate$/, ... },
  { pattern: /^create$/, ... },
  { pattern: /^validateCreate$/, ... },
  { pattern: /^delete$/, ... },
  { pattern: /^update$/, ... },
  { pattern: /^validateUpdate$/, ... },
  { pattern: /^set$/, ... },
  { pattern: /^unset$/, ... },
  { pattern: /^add$/, ... },
  { pattern: /^remove$/, ... },
  
  // Target relation operations
  { pattern: /^list(\w+)$/, ... },
  { pattern: /^get(\w+)$/, ... },
  { pattern: /^export(\w+)$/, ... },
  { pattern: /^getRangeFor(\w+)$/, ... },
  { pattern: /^getTemplateFor(\w+)$/, ... },
  { pattern: /^create(\w+)$/, ... },
  { pattern: /^validateCreate(\w+)$/, ... },
  { pattern: /^delete(\w+)$/, ... },
  { pattern: /^update(\w+)$/, ... },
  { pattern: /^validateUpdate(\w+)$/, ... },
  { pattern: /^set(\w+)$/, ... },
  { pattern: /^unset(\w+)$/, ... },
  { pattern: /^add(\w+)$/, ... },
  { pattern: /^remove(\w+)$/, ... },
  
  // Target relation operations
  { pattern: /^(\w+)For(\w+)$/, ... },
  { pattern: /^validateOn(\w+)For(\w+)$/, ... },
  { pattern: /^getTemplateOn(\w+)For(\w+)$/, ... },
  { pattern: /^getRangeOn(\w+)For(\w+)$/, ... },
  
  // Direct operations on target
  { pattern: /^validateOn(\w+)$/, ... },
  { pattern: /^getTemplateOn(\w+)$/, ... },
  { pattern: /^getRangeOn(\w+)$/, ... },
  // ... operation matching via metadata
];

const ACCESS_SERVICE_PATTERNS: MethodPattern[] = [
  { pattern: /^getPrincipal$/, ... },
  { pattern: /^getMetaData$/, ... },
  { pattern: /^uploadFile$/, ... },
  { pattern: /^downloadFile$/, ... },
  { pattern: /^findInstanceOf(\w+)$/, ... },
];
```

### Path Builder Utilities

```typescript
interface PathBuilder {
  /**
   * Build path for a ClassType
   */
  buildClassPath(classType: ClassTypeMetadata): string;
  
  /**
   * Build path for a relation operation
   */
  buildRelationPath(
    ownerClass: ClassTypeMetadata,
    relationName: string,
    suffix: string
  ): string;
  
  /**
   * Build path for a mutation relation operation (with /~update/)
   */
  buildMutationRelationPath(
    ownerClass: ClassTypeMetadata,
    relationName: string,
    suffix: string
  ): string;
  
  /**
   * Build path for an operation
   */
  buildOperationPath(
    classType: ClassTypeMetadata,
    operationName: string,
    suffix: string
  ): string;
  
  /**
   * Build actor root path
   */
  buildActorPath(application: ApplicationMetadata): string;
}

// Implementation
const pathBuilder: PathBuilder = {
  buildClassPath(classType) {
    const packages = classType.packageNameTokens.join('/');
    const prefix = packages ? packages + '/' : '';
    const name = classType.simpleName
      .split('::')
      .filter(s => !s.includes('_default_transferobjecttypes'))
      .join('/');
    return '/' + prefix + name;
  },
  
  buildRelationPath(ownerClass, relationName, suffix) {
    const classPath = this.buildClassPath(ownerClass);
    const relPath = relationName.replace(/::/g, '/');
    return classPath + '/' + relPath + suffix;
  },
  
  buildMutationRelationPath(ownerClass, relationName, suffix) {
    const classPath = this.buildClassPath(ownerClass);
    const relPath = relationName.replace(/::/g, '/');
    return classPath + '/~update/' + relPath + suffix;
  },
  
  buildOperationPath(classType, operationName, suffix) {
    const classPath = this.buildClassPath(classType);
    return classPath + '/' + operationName + suffix;
  },
  
  buildActorPath(application) {
    const packages = application.actor.packageNameTokens.join('/');
    return packages + '/' + application.actor.simpleName;
  }
};
```

### Header Builder Utilities

```typescript
interface HeaderBuilder {
  /**
   * Build headers for a request
   */
  buildHeaders(config: HeaderConfig): Record<string, string>;
}

interface HeaderConfig {
  /**
   * Signed identifier for the target entity
   */
  signedIdentifier?: string;
  
  /**
   * Field mask for response
   */
  mask?: string;
  
  /**
   * Whether to mark selected range items
   */
  markSelectedRangeItems?: boolean;
  
  /**
   * Token for file operations
   */
  token?: string;
  
  /**
   * Additional custom headers
   */
  custom?: Record<string, string>;
}

// Header constants
const HEADERS = {
  X_JUDO_SIGNED_IDENTIFIER: 'X-Judo-SignedIdentifier',
  X_JUDO_MASK: 'X-Judo-Mask',
  X_JUDO_MARK_SELECTED_RANGE_ITEMS: 'X-Judo-Mark-Selected-Range-Items',
  X_TOKEN: 'X-Token',
} as const;

// Default mask
const DEFAULT_COMMAND_MASK = '{}';
```

### Example Implementation Skeleton

```typescript
class RuntimeRequestMapper implements RequestMapper {
  private readonly pathBuilder: PathBuilder;
  private readonly headerBuilder: HeaderBuilder;
  
  constructor(pathBuilder: PathBuilder, headerBuilder: HeaderBuilder) {
    this.pathBuilder = pathBuilder;
    this.headerBuilder = headerBuilder;
  }
  
  mapRequest(methodName: string, context: RequestContext): RequestConfiguration {
    switch (context.serviceType) {
      case 'class':
        return this.mapClassServiceRequest(methodName, context);
      case 'relation':
        return this.mapRelationServiceRequest(methodName, context);
      case 'access':
        return this.mapAccessServiceRequest(methodName, context);
      default:
        throw new Error(`Unknown service type: ${context.serviceType}`);
    }
  }
  
  private mapClassServiceRequest(
    methodName: string,
    context: RequestContext
  ): RequestConfiguration {
    const classType = context.classType!;
    
    // Template
    if (methodName === 'getTemplate') {
      return {
        method: 'GET',
        path: this.pathBuilder.buildClassPath(classType) + '/~template',
        headers: {},
        responseType: 'json',
        responseDeserializer: {
          type: 'single',
          targetClassFqName: classType.fqName,
          isStored: false,
        },
      };
    }
    
    // Refresh
    if (methodName === 'refresh') {
      const [target, queryCustomizer] = context.args as [any, any?];
      return {
        method: 'POST',
        path: this.pathBuilder.buildClassPath(classType) + '/~get',
        body: this.serializeQueryCustomizer(queryCustomizer),
        headers: this.headerBuilder.buildHeaders({
          signedIdentifier: target.__signedIdentifier,
        }),
        responseType: 'json',
        responseDeserializer: {
          type: 'single',
          targetClassFqName: classType.fqName,
          isStored: true,
        },
      };
    }
    
    // ... implement other patterns
    
    throw new Error(`Unknown method: ${methodName}`);
  }
  
  private mapRelationServiceRequest(
    methodName: string,
    context: RequestContext
  ): RequestConfiguration {
    // ... implement relation service patterns
    throw new Error(`Not implemented: ${methodName}`);
  }
  
  private mapAccessServiceRequest(
    methodName: string,
    context: RequestContext
  ): RequestConfiguration {
    // ... implement access service patterns
    throw new Error(`Not implemented: ${methodName}`);
  }
  
  private serializeQueryCustomizer(qc?: any): any {
    // Implement query customizer serialization
    return qc ?? {};
  }
}
```

---

## Changelog

| Version | Date | Description |
|---------|------|-------------|
| 1.0.0 | 2026-01-03 | Initial specification |

