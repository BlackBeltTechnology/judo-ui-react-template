# Runtime Service Specification

## Overview

This specification defines a **single runtime service** that can replace all generated service implementations (`*ServiceImpl.ts`) in the JUDO UI React template. The runtime service will dynamically construct API endpoints and handle data serialization/deserialization based on metadata, eliminating the need for code generation of service classes.

## Goals

1. **Replace generated services** with a single configurable runtime service
2. **Maintain API compatibility** with the same endpoints, headers, and data formats
3. **Support all operation types** currently handled by generated services
4. **Leverage model metadata** for dynamic endpoint construction

## Architecture

### Current Generated Service Architecture

```
┌─────────────────────────┐     ┌────────────────────────┐
│   Generated Service     │────▶│    JudoAxiosService    │
│   (e.g., GodService     │     │    (Base Class)        │
│    ForGalaxiesImpl)     │     └────────────────────────┘
└─────────────────────────┘                │
           │                               ▼
           │                    ┌────────────────────────┐
           └───────────────────▶│    AxiosProvider       │
                                │    (JudoAxiosProvider) │
                                └────────────────────────┘
```

### Proposed Runtime Service Architecture

```
┌─────────────────────────┐     ┌────────────────────────┐
│   JudoRuntimeService    │────▶│    AxiosProvider       │
│   (Single Instance)     │     │    (JudoAxiosProvider) │
└─────────────────────────┘     └────────────────────────┘
           │
           ▼
┌─────────────────────────┐
│   Model Metadata        │
│   (ClassType, Relation, │
│    Operation info)      │
└─────────────────────────┘
```

## Endpoint Mapping Patterns

Based on analysis of generated service implementations, endpoints follow these patterns:

### 1. Base Path Construction

```typescript
// Application-level base path
const appPath = `/{application.modelName}`;

// Actor base path (for class-level operations)
// actorBase = {actor.name}/{actor.packageNameTokens.join('/')}
const actorBasePath = `/{application.modelName}/{actorBase}`;

// Actor relation path (for access relation operations - repeated segments)
// actorPath = {actor.name}/{packageTokens}/{actor.name}/{packageTokens}
const actorRelationPath = `/{application.modelName}/{actorPath}`;
```

**Example:**
- Application modelName: `ActionGroupTest`
- Actor name: `God`
- Actor packageNameTokens: `['God']`
- Actor Base (for class operations): `God/God` → `/ActionGroupTest/God/God`
- Actor Path (for access relations): `God/God/God/God` → `/ActionGroupTest/God/God/God/God`

### 2. Path Usage

**Actor Relation Operations** (access relations on the actor) use the full repeated path:
- `getMetadata`, `getPrincipal`
- `listActorRelation`, `createOnActorRelation`, `validateCreateOnActorRelation`
- Example: `/ActionGroupTest/God/God/God/God/galaxies/~list`

**Class-Level Operations** (operations on entity classes) use the base path:
- `getTemplate`, `refresh`, `delete`, `update`, `validateUpdate`
- `listRelation`, `getRelation`, `createRelation`, etc.
- Example: `/ActionGroupTest/God/God/View/Galaxy/~get`

### 2. Class-Level Operations

These operate on transfer objects/class types directly:

| Operation | HTTP Method | Path Pattern | Body | Headers |
|-----------|-------------|--------------|------|---------|
| `getTemplate` | GET | `/{ClassPath}/~template` | - | - |
| `refresh` / `get` | POST | `/{ClassPath}/~get` | QueryCustomizer | `X-Judo-SignedIdentifier` |
| `delete` | POST | `/{ClassPath}/~delete` | - | `X-Judo-SignedIdentifier` |
| `update` | POST | `/{ClassPath}/~update` | Serialized Entity | `X-Judo-SignedIdentifier`, `X-Judo-Mask` |
| `validateUpdate` | POST | `/{ClassPath}/~validate` | Serialized Entity | `X-Judo-SignedIdentifier` |
| `getMetaData` | GET | `/{ActorPath}/~meta` | - | - |

**ClassPath Examples:**
- `View/Galaxy` → `/View/Galaxy/~get`
- `BinaryTypeTest/GalaxyDocumentTransfer` → `/BinaryTypeTest/GalaxyDocumentTransfer/~template`

### 3. Relation-Level Operations (Access Relations from Actor)

For relations directly on the actor (dashboard/access relations):

| Operation | HTTP Method | Path Pattern | Body | Headers |
|-----------|-------------|--------------|------|---------|
| `list` | POST | `/{ActorPath}/{relationName}/~list` | QueryCustomizer | `X-Judo-SignedIdentifier` (optional) |
| `create` | POST | `/{ActorPath}/{relationName}/~create` | Serialized Entity | `X-Judo-Mask` |
| `validateCreate` | POST | `/{ActorPath}/{relationName}/~validate` | Serialized Entity | - |

**Example:**
- Actor relation `galaxies` → `/God/God/galaxies/~list`

### 4. Nested Relation Operations (Relations on Entities)

For relations on entities (not directly on actor):

| Operation | HTTP Method | Path Pattern | Body | Headers |
|-----------|-------------|--------------|------|---------|
| `list{Relation}` | POST | `/{ClassPath}/{relationName}/~list` | QueryCustomizer | `X-Judo-SignedIdentifier` |
| `get{Relation}` | POST | `/{ClassPath}/{relationName}/~get` | QueryCustomizer | `X-Judo-SignedIdentifier` |
| `create{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~create` | Serialized Entity | `X-Judo-SignedIdentifier`, `X-Judo-Mask` |
| `validateCreate{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~validate` | Serialized Entity | `X-Judo-SignedIdentifier` |
| `update{Relation}` | POST | `/{TargetClassPath}/~update` | Serialized Entity | `X-Judo-SignedIdentifier`, `X-Judo-Mask` |
| `validateUpdate{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~validate` | Serialized Entity | `X-Judo-SignedIdentifier` |
| `delete{Relation}` | POST | `/{TargetClassPath}/~delete` | - | `X-Judo-SignedIdentifier` |
| `set{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~set` | Serialized Entity(s) | `X-Judo-SignedIdentifier` |
| `unset{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~unset` | - | `X-Judo-SignedIdentifier` |
| `add{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~add` | Serialized Entity[] | `X-Judo-SignedIdentifier` |
| `remove{Relation}` | POST | `/{ClassPath}/~update/{relationName}/~remove` | Serialized Entity[] | `X-Judo-SignedIdentifier` |
| `getRange` | POST | `/{ClassPath}/{relationName}/~range` | `{ owner, queryCustomizer }` | `X-Judo-MarkSelectedRangeItems` |
| `getTemplate` | GET | `/{TargetClassPath}/~template` | - | - |
| `export` | POST | `/{ClassPath}/{relationName}/~export` | QueryCustomizer | `X-Judo-SignedIdentifier` (blob response) |

**Example:**
- `View/Galaxy` has relation `stars`
- List stars: `/View/Galaxy/stars/~list`
- Create star: `/View/Galaxy/~update/stars/~create`
- Delete star: `/View/Star/~delete`

### 5. Operation Calls (Bound Operations on Entities)

| Operation Type | HTTP Method | Path Pattern | Body | Headers |
|----------------|-------------|--------------|------|---------|
| With Input | POST | `/{ClassPath}/{operationName}` | Serialized Input | `X-Judo-SignedIdentifier` |
| Parameterless | POST | `/{ClassPath}/{operationName}` | undefined | `X-Judo-SignedIdentifier` (if bound) |
| Validate Input | POST | `/{ClassPath}/{operationName}/~validate` | Serialized Input | `X-Judo-SignedIdentifier` |
| Get Input Template | GET | `/{InputClassPath}/~template` | - | - |

**Example:**
- Galaxy has operation `createDarkMatter` with input `ViewMatterCreator`
- Call: `/View/Galaxy/createDarkMatter` (POST with input)
- Validate: `/View/Galaxy/createDarkMatter/~validate`
- Template: `/View/MatterCreator/~template`

### 6. Unbound Operations (Static Operations)

| Operation Type | HTTP Method | Path Pattern | Body | Headers |
|----------------|-------------|--------------|------|---------|
| With Input | POST | `/{ClassPath}/{operationName}` | Serialized Input | - |
| Parameterless | POST | `/{ClassPath}/{operationName}` | undefined | - |

**Example:**
- Static `init` operation on Galaxy: `/View/Galaxy/init` (no signedIdentifier)

### 7. File Upload/Download

| Operation | HTTP Method | Path Pattern | Body | Headers |
|-----------|-------------|--------------|------|---------|
| Get Upload Token | POST | `/{attributePath}/~upload-token` | - | - |
| Upload File | POST | `{fileBasePath}/upload` | FormData | `Content-Type: multipart/form-data`, `X-Token` |
| Download File | GET | `{fileBasePath}/download?disposition={disposition}` | - | `X-Token` |

## HTTP Headers

### Standard Headers

| Header Name | Purpose | When Used |
|-------------|---------|-----------|
| `X-Judo-SignedIdentifier` | Identifies the entity being operated on | All operations on stored entities |
| `X-Judo-Mask` | Specifies which fields to return | Create, Update operations |
| `X-Judo-CountRecords` | Request total record count | List operations |
| `X-Judo-MarkSelectedRangeItems` | Mark already selected items | Range operations |
| `Content-Type` | Request content type | All POST (default: `application/json`) |

### Response Headers

| Header Name | Purpose |
|-------------|---------|
| `x-judo-count` | Total record count (when requested) |

## Data Structures

### QueryCustomizer

```typescript
interface QueryCustomizer<T> {
  _mask?: string;           // Field mask for response
  _seek?: Seek<T>;          // Pagination
  _orderBy?: OrderingType[];// Sorting
  _identifier?: string;     // Find by identifier
  // Dynamic filter properties based on entity attributes
  [attributeName: string]?: FilterBy[];
}

interface Seek<T> {
  lastItem?: T;
  limit?: number;
  reverse?: boolean;
}

interface OrderingType {
  attribute: string;
  descending?: boolean;
}

interface FilterBy {
  operator: FilterOperator;
  value: any;
}

type FilterOperator = 
  | 'equal' | 'notEqual' 
  | 'less' | 'lessOrEqual' 
  | 'greater' | 'greaterOrEqual'
  | 'like' | 'ilike'
  | 'isEmpty' | 'isNotEmpty';
```

### CommandQueryCustomizer

```typescript
interface CommandQueryCustomizer {
  _mask?: string;  // Field mask for response after command
}
```

### JudoStored (Entity Metadata)

```typescript
interface JudoStored<T> {
  __signedIdentifier: string;
  __identifier?: string;
  __entityType?: string;
  __updateable?: boolean;
  __deleteable?: boolean;
  __selected?: boolean;
  __version?: number;
}
```

## Runtime Service API

### Core Interface

```typescript
interface JudoRuntimeService {
  // Configuration
  init(config: RuntimeServiceConfig): void;
  
  // Class-level operations
  getTemplate<T>(classPath: string): Promise<JudoRestResponse<T>>;
  refresh<T>(classPath: string, target: JudoStored<T>, queryCustomizer?: QueryCustomizer<T>): Promise<JudoRestResponse<T>>;
  delete(classPath: string, target: JudoStored<any>): Promise<JudoRestResponse<void>>;
  update<T>(classPath: string, target: T & JudoStored<T>, queryCustomizer?: CommandQueryCustomizer): Promise<JudoRestResponse<T>>;
  validateUpdate<T>(classPath: string, target: T & JudoStored<T>): Promise<JudoRestResponse<T>>;
  
  // Relation operations (from owner context)
  list<T>(ownerClassPath: string, relationName: string, owner?: JudoStored<any>, queryCustomizer?: QueryCustomizer<T>): Promise<JudoRestResponse<T[]>>;
  create<T, R>(ownerClassPath: string, relationName: string, owner: JudoStored<any> | undefined, target: T, queryCustomizer?: CommandQueryCustomizer): Promise<JudoRestResponse<R>>;
  validateCreate<T>(ownerClassPath: string, relationName: string, owner: JudoStored<any> | undefined, target: T): Promise<JudoRestResponse<T>>;
  
  // Relation CRUD on nested entities
  getRelation<T>(ownerClassPath: string, relationName: string, owner: JudoStored<any>, queryCustomizer?: QueryCustomizer<T>): Promise<JudoRestResponse<T | null>>;
  listRelation<T>(ownerClassPath: string, relationName: string, owner: JudoStored<any>, queryCustomizer?: QueryCustomizer<T>): Promise<JudoRestResponse<T[]>>;
  createRelation<T, R>(ownerClassPath: string, relationName: string, owner: JudoStored<any>, target: T, queryCustomizer?: CommandQueryCustomizer): Promise<JudoRestResponse<R>>;
  updateRelation<T>(targetClassPath: string, target: T & JudoStored<T>, queryCustomizer?: CommandQueryCustomizer): Promise<JudoRestResponse<T>>;
  deleteRelation(targetClassPath: string, target: JudoStored<any>): Promise<JudoRestResponse<void>>;
  
  // Relation link management
  setRelation(ownerClassPath: string, relationName: string, owner: JudoStored<any>, selected: JudoStored<any> | JudoStored<any>[]): Promise<JudoRestResponse<void>>;
  unsetRelation(ownerClassPath: string, relationName: string, owner: JudoStored<any>): Promise<JudoRestResponse<void>>;
  addRelation(ownerClassPath: string, relationName: string, owner: JudoStored<any>, selected: JudoStored<any>[]): Promise<JudoRestResponse<void>>;
  removeRelation(ownerClassPath: string, relationName: string, owner: JudoStored<any>, selected: JudoStored<any>[]): Promise<JudoRestResponse<void>>;
  
  // Range queries
  getRange<T>(ownerClassPath: string, relationName: string, owner?: JudoStored<any>, queryCustomizer?: QueryCustomizer<T>): Promise<JudoRestResponse<T[]>>;
  
  // Operations
  callOperation<I, O>(classPath: string, operationName: string, owner?: JudoStored<any>, input?: I): Promise<JudoRestResponse<O>>;
  validateOperationInput<I>(classPath: string, operationName: string, owner: JudoStored<any>, input: I): Promise<JudoRestResponse<I>>;
  getOperationInputTemplate<T>(inputClassPath: string): Promise<JudoRestResponse<T>>;
  
  // Export
  exportRelation(ownerClassPath: string, relationName: string, owner: JudoStored<any>, queryCustomizer?: QueryCustomizer<any>): Promise<Blob>;
  
  // File operations
  uploadFile(attributePath: string, file: File): Promise<any>;
  downloadFile(token: string, disposition: 'inline' | 'attachment'): Promise<Blob>;
  
  // Metadata
  getMetadata(): Promise<JudoRestResponse<JudoMetaData>>;
  
  // Instance lookup (for AccessService pattern)
  findInstance<T>(relationPath: string, identifier: string, mask?: string): Promise<T | undefined>;
}
```

### Configuration

```typescript
interface RuntimeServiceConfig {
  axiosProvider: AxiosProvider;
  applicationName: string;
  actorName: string;
}
```

## Serialization Requirements

The runtime service must handle:

### 1. Date/Time Serialization

```typescript
// Date: "yyyy-MM-dd"
// Time: "HH:mm:ss" or "HH:mm:ss.SSS"  
// Timestamp: ISO 8601 (toISOString())
```

### 2. QueryCustomizer Serialization

- Filter values must be serialized according to their data type
- Seek lastItem must be serialized
- Preserve _mask, _orderBy, _seek, _identifier

### 3. Entity Serialization

- Serialize all defined attributes
- Preserve `__signedIdentifier` and other JudoStored properties
- Handle nested relations (serialize their stored identifiers)
- Handle null values explicitly

### 4. Response Deserialization

- Parse dates from strings to Date objects
- Reconstruct JudoStored metadata
- Handle arrays and nested objects

## ClassPath Derivation

The `classPath` is derived from the model metadata:

```typescript
// From ClassType fqName: "God::View::Galaxy"
// ClassPath: "View/Galaxy"

function deriveClassPath(classType: ClassType): string {
  // Remove actor prefix, replace :: with /
  const parts = classType.fqName.split('::');
  // Skip the first part (actor name) and join with /
  return parts.slice(1).join('/');
}

// From RelationType on ClassType
function deriveRelationPath(ownerClass: ClassType, relation: RelationType): string {
  return `${deriveClassPath(ownerClass)}/${relation.name}`;
}
```

## Migration Strategy

### Phase 1: Create Runtime Service
1. Implement `JudoRuntimeService` class
2. Implement all endpoint patterns
3. Implement serialization/deserialization utilities

### Phase 2: Create Service Facades
1. For each existing service interface, create a facade that delegates to runtime service
2. Facades compute classPath/relationName from their context
3. Maintain backward compatibility

### Phase 3: Update Consumers
1. Gradually migrate pages to use runtime service directly
2. Pass model metadata (classPath, relationName) from model-runtime

### Phase 4: Remove Generated Services
1. Remove service generation templates
2. Remove service interfaces (or keep as type definitions)
3. Remove Impl classes

## Example Usage

### Current (Generated Service)

```typescript
const godServiceForGalaxies = new GodServiceForGalaxiesImpl(judoAxiosProvider);
const result = await godServiceForGalaxies.list(undefined, queryCustomizer);
const created = await godServiceForGalaxies.create(newGalaxy);
const refreshed = await godServiceForGalaxies.refresh(owner, queryCustomizer);
```

### Proposed (Runtime Service)

```typescript
const runtimeService = new JudoRuntimeService(config);

// Using relation info from model
const galaxiesRelation = application.actor.relations.find(r => r.name === 'galaxies');
const classPath = deriveClassPath(galaxiesRelation.target); // "View/Galaxy"

// List via actor relation
const result = await runtimeService.list('God/God', 'galaxies', undefined, queryCustomizer);

// Create on actor relation
const created = await runtimeService.create('God/God', 'galaxies', undefined, newGalaxy);

// Refresh existing entity
const refreshed = await runtimeService.refresh('View/Galaxy', storedGalaxy, queryCustomizer);
```

## Appendix A: Complete Endpoint Reference

**Path Notation:**
- `{App}` = `application.modelName`
- `{ActorBase}` = `{actor.name}/{actor.packageNameTokens.join('/')}` (for class operations)
- `{ActorPath}` = `{actor.name}/{packageTokens}/{actor.name}/{packageTokens}` (for actor relations)

### Actor-Level Endpoints (use ActorPath - repeated segments)

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Metadata | GET | `/{App}/{ActorPath}/~meta` |
| Get Principal | GET | `/{App}/{ActorPath}/~principal` |
| List Actor Relation | POST | `/{App}/{ActorPath}/{relationName}/~list` |
| Create on Actor Relation | POST | `/{App}/{ActorPath}/{relationName}/~create` |
| Validate Create on Actor | POST | `/{App}/{ActorPath}/{relationName}/~validate` |

### Class-Level Endpoints (use ActorBase - single segment)

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Template | GET | `/{App}/{ActorBase}/{ClassPath}/~template` |
| Get/Refresh | POST | `/{App}/{ActorBase}/{ClassPath}/~get` |
| Delete | POST | `/{App}/{ActorBase}/{ClassPath}/~delete` |
| Update | POST | `/{App}/{ActorBase}/{ClassPath}/~update` |
| Validate Update | POST | `/{App}/{ActorBase}/{ClassPath}/~validate` |

### Relation Endpoints on Entities (use ActorBase - single segment)

| Operation | Method | Endpoint |
|-----------|--------|----------|
| List | POST | `/{App}/{ActorBase}/{ClassPath}/{relationName}/~list` |
| Get (single) | POST | `/{App}/{ActorBase}/{ClassPath}/{relationName}/~get` |
| Range | POST | `/{App}/{ActorBase}/{ClassPath}/{relationName}/~range` |
| Create | POST | `/{App}/{ActorBase}/{ClassPath}/~update/{relationName}/~create` |
| Validate Create | POST | `/{App}/{ActorBase}/{ClassPath}/~update/{relationName}/~validate` |
| Set | POST | `/{App}/{ActorBase}/{ClassPath}/~update/{relationName}/~set` |
| Unset | POST | `/{App}/{ActorBase}/{ClassPath}/~update/{relationName}/~unset` |
| Add | POST | `/{App}/{ActorBase}/{ClassPath}/~update/{relationName}/~add` |
| Remove | POST | `/{App}/{ActorBase}/{ClassPath}/~update/{relationName}/~remove` |
| Export | POST | `/{App}/{ActorBase}/{ClassPath}/{relationName}/~export` |

### Operation Endpoints (use ActorBase - single segment)

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Call (with/without input) | POST | `/{App}/{ActorBase}/{ClassPath}/{operationName}` |
| Validate Input | POST | `/{App}/{ActorBase}/{ClassPath}/{operationName}/~validate` |

### File Endpoints (use ActorBase - single segment)

| Operation | Method | Endpoint |
|-----------|--------|----------|
| Get Upload Token | POST | `/{App}/{ActorBase}/{attributePath}/~upload-token` |
| Upload | POST | `{fileBasePath}/upload` |
| Download | GET | `{fileBasePath}/download?disposition={disposition}` |

## Appendix B: Header Constants

```typescript
export const X_JUDO_SIGNED_IDENTIFIER = 'X-Judo-SignedIdentifier';
export const X_JUDO_COUNT_RECORDS = 'X-Judo-CountRecords';
export const X_JUDO_COUNT = 'x-judo-count';
export const X_JUDO_MASK = 'X-Judo-Mask';
export const X_JUDO_MARK_SELECTED_RANGE_ITEMS = 'X-Judo-MarkSelectedRangeItems';
```

## Appendix C: Error Handling

All operations should handle:
- **401**: Unauthorized
- **403**: Forbidden  
- **400**: Validation errors (returns `Array<FeedbackItem>`)

```typescript
interface FeedbackItem {
  code: string;
  level: 'error' | 'warning' | 'info';
  message: string;
  location?: string;
}
```

