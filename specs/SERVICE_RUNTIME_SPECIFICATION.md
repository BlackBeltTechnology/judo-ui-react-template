# Runtime Service Layer Specification

## Overview

This specification defines a runtime service layer that dynamically generates service implementations on top of the parser layer, eliminating the need for code generation. The runtime service layer provides the same interface and functionality as the current generated services but creates implementations at runtime based on metadata from the parser.

## Architecture

### Components

1. **Service Factory** - Creates service instances dynamically based on metadata
2. **Service Registry** - Manages and caches service instances
3. **Runtime Service Proxy** - Handles method calls and routes them to appropriate HTTP operations
4. **Metadata Provider** - Provides parsed metadata from the parser layer
5. **Serialization Manager** - Manages runtime serialization/deserialization

### Layers

```
┌─────────────────────────────────────┐
│    Application Layer                │
│    (Uses Service Interfaces)        │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Service Factory & Registry       │
│    (Creates Dynamic Services)       │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Runtime Service Proxy            │
│    (Handles Method Dispatch)        │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Axios/HTTP Layer                 │
│    (JudoAxiosService)               │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│    Parser Layer                     │
│    (Metadata Provider)              │
└─────────────────────────────────────┘
```

## Core Interfaces

### ServiceFactory

```typescript
interface ServiceFactory {
  /**
   * Create a class service for a given ClassType
   */
  createClassService<T>(classType: ClassTypeMetadata): T;
  
  /**
   * Create a relation service for a given RelationType
   */
  createRelationService<T>(relation: RelationTypeMetadata): T;
  
  /**
   * Create the access service
   */
  createAccessService(): AccessService;
}
```

### ServiceRegistry

```typescript
interface ServiceRegistry {
  /**
   * Register a service instance
   */
  register(key: string, service: any): void;
  
  /**
   * Get a service instance
   */
  get<T>(key: string): T | undefined;
  
  /**
   * Check if service exists
   */
  has(key: string): boolean;
  
  /**
   * Clear all services
   */
  clear(): void;
}
```

### MetadataProvider

```typescript
interface MetadataProvider {
  /**
   * Get application metadata
   */
  getApplication(): ApplicationMetadata;
  
  /**
   * Get ClassType by FQName
   */
  getClassType(fqName: string): ClassTypeMetadata | undefined;
  
  /**
   * Get RelationType by FQName
   */
  getRelationType(fqName: string): RelationTypeMetadata | undefined;
  
  /**
   * Get all access relations
   */
  getAccessRelations(): RelationTypeMetadata[];
  
  /**
   * Get all non-access relations
   */
  getNonAccessRelations(): RelationTypeMetadata[];
}
```

## Metadata Structure

### ClassTypeMetadata

Based on the parser spec and ui.ecore, ClassType metadata includes:

```typescript
interface ClassTypeMetadata {
  fqName: string;
  name: string;
  
  // Capabilities
  isTemplateable: boolean;
  isMapped: boolean;
  isDeletable: boolean;
  isUpdatable: boolean;
  isUpdateValidatable: boolean;
  
  // Relations
  relations: RelationTypeMetadata[];
  
  // Operations
  operations: OperationTypeMetadata[];
  
  // Attributes for serialization
  attributes: AttributeTypeMetadata[];
}
```

### RelationTypeMetadata

```typescript
interface RelationTypeMetadata {
  fqName: string;
  name: string;
  owner: ClassTypeMetadata;
  target: ClassTypeMetadata;
  
  // Capabilities
  isAccess: boolean;
  isCollection: boolean;
  isListable: boolean;
  isRefreshable: boolean;
  isRangeable: boolean;
  isCreatable: boolean;
  isCreateValidatable: boolean;
  isDeletable: boolean;
  isUpdatable: boolean;
  isUpdateValidatable: boolean;
  isSetable: boolean;
  isUnsetable: boolean;
  isAddable: boolean;
  isRemovable: boolean;
  isExportable: boolean;
}
```

### OperationTypeMetadata

```typescript
interface OperationTypeMetadata {
  name: string;
  fqName: string;
  
  // Input/Output
  input?: OperationInputMetadata;
  output?: OperationOutputMetadata;
  
  // Capabilities
  isMapped: boolean;
  isStatic: boolean;
  isInputRangeable: boolean;
  
  // Validation
  isInputValidateable: boolean;
}

interface OperationInputMetadata {
  target: ClassTypeMetadata;
  behaviours: OperationTargetBehaviourType[];
}

interface OperationOutputMetadata {
  target: ClassTypeMetadata;
}
```

## Runtime Service Proxy

The core of the runtime service layer is the proxy that intercepts method calls and translates them to HTTP operations.

### Method Name Patterns

The proxy analyzes method names to determine the operation type:

#### Class Service Methods

| Pattern | Operation | Example |
|---------|-----------|---------|
| `getTemplate()` | GET template | `/Class/~template` |
| `refresh(target, ...)` | POST refresh | `/Class/~get` |
| `delete(target)` | POST delete | `/Class/~delete` |
| `update(target, ...)` | POST update | `/Class/~update` |
| `validateUpdate(target)` | POST validate | `/Class/~validate` |
| `getTemplateFor{Relation}()` | GET template | `/Target/~template` |
| `create{Relation}(owner, target, ...)` | POST create | `/Class/~update/{relation}/~create` |
| `validateCreate{Relation}(owner, target)` | POST validate | `/Class/~update/{relation}/~validate` |
| `list{Relation}(target, ...)` | POST list | `/Class/{relation}/~list` |
| `get{Relation}(target, ...)` | POST get | `/Class/{relation}/~get` |
| `getRangeFor{Relation}(owner?, ...)` | POST range | `/Class/{relation}/~range` |
| `set{Relation}(owner, selected)` | POST set | `/Class/~update/{relation}/~set` |
| `unset{Relation}(owner)` | POST unset | `/Class/~update/{relation}/~unset` |
| `add{Relation}(owner, selected)` | POST add | `/Class/~update/{relation}/~add` |
| `remove{Relation}(owner, selected)` | POST remove | `/Class/~update/{relation}/~remove` |
| `delete{Relation}(target)` | POST delete | `/Target/~delete` |
| `export{Relation}(owner?, ...)` | POST export | `/Class/{relation}/~export` |
| `{operation}For{Relation}(...)` | POST operation | `/Target/{operation}` |
| `validateOn{Operation}For{Relation}(...)` | POST validate | `/Target/{operation}/~validate` |
| `getTemplateOn{Operation}For{Relation}()` | GET template | `/InputTarget/~template` |
| `getRangeOn{Operation}For{Relation}(...)` | POST range | `/Target/{operation}/~range` |
| `{operation}(...)` | POST operation | `/Class/{operation}` |
| `validateOn{Operation}(...)` | POST validate | `/Class/{operation}/~validate` |
| `getTemplateOn{Operation}()` | GET template | `/InputTarget/~template` |
| `getRangeOn{Operation}(...)` | POST range | `/Class/{operation}/~range` |
| `getRangeOn{Operation}For{Relation}(...)` | POST range | `/InputTarget/{relation}/~range` |

#### Relation Service Methods

| Pattern | Operation | Example |
|---------|-----------|---------|
| `list(owner?, ...)` | POST list | `/Owner/{relation}/~list` |
| `export(owner?, ...)` | POST export | `/Owner/{relation}/~export` |
| `refreshFor{Relation}(...)` | POST refresh | `/Owner/{relation}/~get` (access only) |
| `refresh(owner?, ...)` | POST refresh | `/Target/~get` |
| `getRange(owner?, ...)` | POST range | `/Owner/{relation}/~range` |
| `getTemplate()` | GET template | `/Target/~template` |
| `create(owner?, target, ...)` | POST create | `/Owner/~update/{relation}/~create` or `/Owner/{relation}/~create` |
| `validateCreate(owner?, target)` | POST validate | `/Owner/~update/{relation}/~validate` or `/Owner/{relation}/~validate` |
| `delete(target)` | POST delete | `/Target/~delete` |
| `update(target, ...)` | POST update | `/Target/~update` |
| `validateUpdate(owner?, target)` | POST validate | `/Target/~validate` |
| `set(owner?, selected)` | POST set | `/Owner/~update/{relation}/~set` |
| `unset(owner?)` | POST unset | `/Owner/~update/{relation}/~unset` |
| `add(owner?, selected)` | POST add | `/Owner/~update/{relation}/~add` |
| `remove(owner?, selected)` | POST remove | `/Owner/~update/{relation}/~remove` |
| `list{TargetRelation}(owner, ...)` | POST list | `/Target/{targetRelation}/~list` |
| `get{TargetRelation}(owner, ...)` | POST get | `/Target/{targetRelation}/~get` |
| `export{TargetRelation}(owner, ...)` | POST export | `/Target/{targetRelation}/~export` |
| `getRangeFor{TargetRelation}(owner, ...)` | POST range | `/Target/{targetRelation}/~range` |
| `getTemplateFor{TargetRelation}()` | GET template | `/TargetTarget/~template` |
| `create{TargetRelation}(owner, target, ...)` | POST create | `/Target/~update/{targetRelation}/~create` |
| `validateCreate{TargetRelation}(owner, target)` | POST validate | `/Target/~update/{targetRelation}/~validate` |
| `delete{TargetRelation}(target)` | POST delete | `/TargetTarget/~delete` |
| `update{TargetRelation}(owner, target, ...)` | POST update | `/TargetTarget/~update` |
| `validateUpdate{TargetRelation}(owner, target)` | POST validate | `/Target/~update/{targetRelation}/~validate` |
| `set{TargetRelation}(owner, selected)` | POST set | `/Target/~update/{targetRelation}/~set` |
| `unset{TargetRelation}(owner)` | POST unset | `/Target/~update/{targetRelation}/~unset` |
| `add{TargetRelation}(owner, selected)` | POST add | `/Target/~update/{targetRelation}/~add` |
| `remove{TargetRelation}(owner, selected)` | POST remove | `/Target/~update/{targetRelation}/~remove` |
| `{operation}For{TargetRelation}(...)` | POST operation | `/TargetTarget/{operation}` |
| `validateOn{Operation}For{TargetRelation}(...)` | POST validate | `/TargetTarget/{operation}/~validate` |
| `getTemplateOn{Operation}For{TargetRelation}()` | GET template | `/InputTarget/~template` |
| `getRangeOn{Operation}For{TargetRelation}(...)` | POST range | `/TargetTarget/{operation}/~range` |
| `{operation}(...)` | POST operation | `/Target/{operation}` |
| `validateOn{Operation}(...)` | POST validate | `/Target/{operation}/~validate` |
| `getTemplateOn{Operation}()` | GET template | `/InputTarget/~template` |
| `getRangeOn{Operation}(...)` | POST range | `/Target/{operation}/~range` |

#### Access Service Methods

| Pattern | Operation | Example |
|---------|-----------|---------|
| `getPrincipal()` | GET principal | `/~principal` |
| `getMetaData()` | GET metadata | `/~meta` |
| `uploadFile(path, file)` | POST upload | attribute path + `/~upload-token`, then upload |
| `downloadFile(token, disposition)` | GET download | `/download?disposition={disp}` |
| `findInstanceOf{Relation}(identifier, mask?)` | POST list | `/{relation}/~list` with identifier |

### Path Construction

The proxy constructs REST paths based on:

1. **ClassType operations**: `/api/{ApplicationName}/{ClassFQName}/~{operation}`
2. **Relation operations**: `/api/{ApplicationName}/{OwnerFQName}/{relationName}/~{operation}`
3. **Nested operations**: Use owner's path + relation name + operation
4. **Operation invocations**: `/api/{ApplicationName}/{ClassFQName}/{operationName}` (or with `/~validate`, `/~range`)

### Request/Response Handling

#### Headers

- `X-Judo-SignedIdentifier`: Used for entity identification
- `X-Judo-Mask`: Used for command query customization
- `X-Judo-Mark-Selected-Range-Items`: Used for range operations

#### Serialization/Deserialization

The proxy uses runtime serializers based on metadata:

1. **Serialization**: Convert typed objects to plain objects for HTTP
2. **Deserialization**: Convert HTTP responses to typed objects
3. **Array handling**: Map arrays using appropriate deserializers
4. **Null handling**: Handle empty strings and null values appropriately

#### Error Handling

Follow Axios error conventions:
- `401, 403`: Unauthorized/Forbidden with `FeedbackItem[]`
- `400`: Bad request with `FeedbackItem[]`
- `422`: Business logic errors with fault containers

## Implementation Strategy

### Phase 1: Core Infrastructure

1. **Create MetadataProvider**
   - Integrate with parser layer
   - Expose parsed metadata through clean interface
   - Cache metadata for performance

2. **Create ServiceRegistry**
   - Implement singleton pattern
   - Support lazy initialization
   - Provide cache invalidation

3. **Create SerializationManager**
   - Runtime serializer generation
   - Support for all data types
   - QueryCustomizer serialization

### Phase 2: Service Proxies

1. **Implement RuntimeServiceProxy**
   - Method interception via Proxy API
   - Pattern matching for method names
   - Path construction logic
   - Request/response transformation

2. **Create ClassServiceProxy**
   - Handle all class service operations
   - Support nested relation operations
   - Support nested operation invocations

3. **Create RelationServiceProxy**
   - Handle relation operations
   - Support nested target operations
   - Handle access vs non-access differences

4. **Create AccessServiceProxy**
   - Handle application-level operations
   - Principal and metadata access
   - File upload/download

### Phase 3: Service Factory

1. **Implement ServiceFactory**
   - Create services based on metadata
   - Return Proxy-wrapped services
   - Configure serializers and HTTP client

2. **Type generation support**
   - Generate TypeScript interfaces
   - Maintain type safety
   - Support IDE autocomplete

### Phase 4: Integration

1. **Migration utilities**
   - Helper to convert from generated to runtime services
   - Compatibility layer

2. **Testing infrastructure**
   - Unit tests for each proxy type
   - Integration tests with mock HTTP
   - E2E tests with real backend

## API Design

### Creating Services

```typescript
// Initialize the runtime service layer
const serviceFactory = new ServiceFactory({
  metadataProvider: parserMetadataProvider,
  axiosService: judoAxiosService,
});

// Create class service
const userService = serviceFactory.createClassService<UserService>(
  metadata.getClassType('demo.User')
);

// Create relation service
const friendsService = serviceFactory.createRelationService<FriendsRelationService>(
  metadata.getRelationType('demo.User.friends')
);

// Create access service
const accessService = serviceFactory.createAccessService();
```

### Using Services

The API remains identical to generated services:

```typescript
// Same as before - transparent to consumers
const user = await userService.refresh(storedUser);
const friends = await userService.listFriends(user);
await userService.addFriends(user, [newFriend]);
```

## Method Pattern Recognition Algorithm

```typescript
interface MethodPattern {
  regex: RegExp;
  operationType: OperationType;
  pathBuilder: (matches: string[], metadata: any) => string;
  requestBuilder: (args: any[], metadata: any) => any;
  responseHandler: (response: any, metadata: any) => any;
}

class MethodPatternMatcher {
  patterns: MethodPattern[] = [
    // Class patterns
    {
      regex: /^getTemplate$/,
      operationType: 'GET_TEMPLATE',
      pathBuilder: (_, meta) => `${meta.classType.restPath}/~template`,
      // ...
    },
    {
      regex: /^refresh$/,
      operationType: 'REFRESH',
      pathBuilder: (_, meta) => `${meta.classType.restPath}/~get`,
      // ...
    },
    {
      regex: /^getTemplateFor([A-Z]\w+)$/,
      operationType: 'GET_TEMPLATE_FOR_RELATION',
      pathBuilder: ([_, relationName], meta) => {
        const relation = meta.classType.relations.find(r => 
          r.name === lowerFirst(relationName)
        );
        return `${relation.target.restPath}/~template`;
      },
      // ...
    },
    {
      regex: /^create([A-Z]\w+)$/,
      operationType: 'CREATE_RELATION',
      pathBuilder: ([_, relationName], meta) => {
        const relation = meta.classType.relations.find(r => 
          r.name === lowerFirst(relationName)
        );
        return `${meta.classType.restPath}/~update/${relation.name}/~create`;
      },
      // ...
    },
    // ... more patterns
  ];
  
  match(methodName: string): MethodPattern | undefined {
    for (const pattern of this.patterns) {
      const matches = methodName.match(pattern.regex);
      if (matches) {
        return pattern;
      }
    }
    return undefined;
  }
}
```

## Serialization Strategy

### Runtime Serializer

```typescript
class RuntimeSerializer<T> {
  constructor(
    private metadata: ClassTypeMetadata,
    private serializerManager: SerializationManager
  ) {}
  
  serialize(obj: T): any {
    const result: any = {};
    
    for (const attr of this.metadata.attributes) {
      if (obj[attr.name] !== undefined) {
        result[attr.name] = this.serializeAttribute(
          obj[attr.name],
          attr
        );
      }
    }
    
    // Handle __signedIdentifier and other special fields
    if (obj['__signedIdentifier']) {
      result.__signedIdentifier = obj['__signedIdentifier'];
    }
    
    return result;
  }
  
  deserialize(data: any): T {
    const result: any = {};
    
    for (const attr of this.metadata.attributes) {
      if (data[attr.name] !== undefined) {
        result[attr.name] = this.deserializeAttribute(
          data[attr.name],
          attr
        );
      }
    }
    
    // Preserve system fields
    if (data.__signedIdentifier) {
      result.__signedIdentifier = data.__signedIdentifier;
    }
    
    return result as T;
  }
  
  private serializeAttribute(value: any, attr: AttributeTypeMetadata): any {
    // Handle dates, enums, primitives, etc.
    // Based on attribute data type from metadata
  }
  
  private deserializeAttribute(value: any, attr: AttributeTypeMetadata): any {
    // Handle dates, enums, primitives, etc.
    // Based on attribute data type from metadata
  }
}
```

### QueryCustomizer Serialization

```typescript
class RuntimeQueryCustomizerSerializer {
  serialize(customizer: any): any {
    if (!customizer) return {};
    
    return {
      _mask: customizer._mask,
      _seek: customizer._seek,
      _orderBy: customizer._orderBy,
      // ... other fields
    };
  }
}
```

## Configuration

### Runtime Configuration

```typescript
interface ServiceLayerConfig {
  // Metadata source
  metadataProvider: MetadataProvider;
  
  // HTTP client
  axiosService: JudoAxiosService;
  
  // Caching
  enableCache: boolean;
  cacheStrategy: 'memory' | 'none';
  
  // Serialization
  dateFormat?: string;
  
  // Debugging
  debug: boolean;
  logRequests: boolean;
}
```

## Performance Considerations

1. **Metadata caching**: Parse metadata once, reuse across all services
2. **Service instance caching**: Create service proxies once per type
3. **Serializer caching**: Reuse serializers for same types
4. **Pattern matching optimization**: Pre-compile regex patterns
5. **Lazy initialization**: Create services only when needed

## Migration Path

### Step 1: Parallel Implementation

Keep generated services while implementing runtime layer:
- Runtime services under different namespace
- Allow gradual migration
- Compare outputs for validation

### Step 2: Feature Parity

Ensure runtime layer supports all features:
- All operation types
- All metadata capabilities
- Error handling
- Type safety

### Step 3: Deprecation

Mark generated services as deprecated:
- Update documentation
- Provide migration guide
- Support both for transition period

### Step 4: Removal

Remove code generation:
- Delete generator templates
- Delete generator Java code
- Update build process

## Testing Strategy

### Unit Tests

1. **Pattern matching**: Verify all method patterns are recognized
2. **Path construction**: Validate REST path generation
3. **Serialization**: Test all data type conversions
4. **Error handling**: Verify error scenarios

### Integration Tests

1. **Service creation**: Test factory creates correct services
2. **Method invocation**: Test all service methods work
3. **Request/response**: Validate HTTP interactions
4. **Caching**: Test cache behavior

### E2E Tests

1. **Real backend**: Test against actual REST API
2. **All operations**: Exercise every operation type
3. **Complex scenarios**: Test nested operations, etc.

## Benefits

1. **No code generation**: Eliminates build step and generated code
2. **Smaller bundle size**: No duplicate service implementations
3. **Dynamic metadata**: Can handle runtime metadata changes
4. **Easier maintenance**: Single implementation instead of many generated files
5. **Better debugging**: Runtime logic is visible and traceable
6. **Type safety preserved**: Through TypeScript interfaces
7. **Flexible**: Can add features without regenerating

## Risks and Mitigations

### Risk: Performance overhead
**Mitigation**: Aggressive caching, benchmark against generated code

### Risk: Debugging difficulty
**Mitigation**: Excellent logging, clear error messages, debug mode

### Risk: Type safety loss
**Mitigation**: Generate TypeScript interfaces, strict types

### Risk: Missing edge cases
**Mitigation**: Comprehensive test suite, gradual migration

### Risk: Runtime errors
**Mitigation**: Validation, fallbacks, clear error messages

## Future Enhancements

1. **Code splitting**: Load metadata on demand
2. **Offline support**: Cache metadata and requests
3. **Mock mode**: Generate mock responses for testing
4. **Analytics**: Track service usage patterns
5. **Custom operations**: Allow runtime operation registration
6. **Plugin system**: Extend with custom behavior
7. **GraphQL support**: Alternative to REST

## Conclusion

This runtime service layer specification provides a comprehensive approach to replacing code generation with runtime metadata-driven services. The design maintains API compatibility while providing flexibility and reducing code generation overhead.

The implementation should be incremental, starting with core infrastructure and gradually adding service types. Thorough testing and a careful migration path will ensure a smooth transition from generated to runtime services.

