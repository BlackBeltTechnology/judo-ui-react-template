# Runtime Service Layer - Architecture Diagrams

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   UI Views   │  │  Controllers │  │   Business   │         │
│  │              │→ │              │→ │    Logic     │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ↓ Uses Service Interfaces
┌─────────────────────────────────────────────────────────────────┐
│                    Service Interface Layer                      │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ UserService  │  │ProductService│  │AccessService │         │
│  │ (Interface)  │  │ (Interface)  │  │ (Interface)  │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ↓ Implemented by Runtime Proxies
┌─────────────────────────────────────────────────────────────────┐
│                    Runtime Service Layer                        │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              ServiceFactory                               │ │
│  │  ┌─────────────────────────────────────────────────────┐ │ │
│  │  │  createClassService(metadata)    → Proxy Instance   │ │ │
│  │  │  createRelationService(metadata) → Proxy Instance   │ │ │
│  │  │  createAccessService()           → Proxy Instance   │ │ │
│  │  └─────────────────────────────────────────────────────┘ │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────┐  ┌──────────────────┐  ┌────────────────┐  │
│  │   Service     │  │  Runtime         │  │  Method        │  │
│  │   Registry    │  │  ServiceProxy    │  │  Pattern       │  │
│  │               │  │                  │  │  Matcher       │  │
│  └───────────────┘  └──────────────────┘  └────────────────┘  │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              SerializationManager                         │ │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────────────┐ │ │
│  │  │ Serialize  │  │Deserialize │  │ QueryCustomizer    │ │ │
│  │  │            │  │            │  │ Serialization      │ │ │
│  │  └────────────┘  └────────────┘  └────────────────────┘ │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ↓ HTTP Requests
┌─────────────────────────────────────────────────────────────────┐
│                      HTTP Transport Layer                       │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              JudoAxiosService                             │ │
│  │  ┌────────────────────────────────────────────────────┐  │ │
│  │  │  axios.get(path, config)                           │  │ │
│  │  │  axios.post(path, data, config)                    │  │ │
│  │  │  getPathForActor(path) → full URL                  │  │ │
│  │  └────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
└────────────────────────────────┬────────────────────────────────┘
                                 │
                                 ↓ Uses Metadata
┌─────────────────────────────────────────────────────────────────┐
│                       Metadata Layer                            │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              MetadataProvider                             │ │
│  │  ┌────────────────────────────────────────────────────┐  │ │
│  │  │  getApplication()      → ApplicationMetadata       │  │ │
│  │  │  getClassType(fqName)  → ClassTypeMetadata         │  │ │
│  │  │  getRelationType(fqName) → RelationTypeMetadata    │  │ │
│  │  └────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌───────────────────────────────────────────────────────────┐ │
│  │              Parser Context                               │ │
│  │  ┌────────────────────────────────────────────────────┐  │ │
│  │  │  UI Model → Parsed Metadata → Cached Metadata      │  │ │
│  │  └────────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## Method Invocation Flow

### Example: userService.listFriends(user)

```
1. Application calls method
   ┌──────────────────────────────────────┐
   │ userService.listFriends(user)        │
   └──────────┬───────────────────────────┘
              │
              ↓
2. Proxy intercepts call
   ┌──────────────────────────────────────┐
   │ RuntimeServiceProxy.get()            │
   │   → createMethodHandler('listFriends')│
   └──────────┬───────────────────────────┘
              │
              ↓
3. Pattern matching
   ┌──────────────────────────────────────┐
   │ MethodPatternMatcher.match()         │
   │   'listFriends' matches:             │
   │   /^list([A-Z][a-zA-Z0-9]*)$/        │
   │   → relationName = 'friends'         │
   └──────────┬───────────────────────────┘
              │
              ↓
4. Build request
   ┌──────────────────────────────────────┐
   │ pattern.pathBuilder()                │
   │   → '/api/demo/User/friends/~list'   │
   │ pattern.requestBuilder()             │
   │   → serialize queryCustomizer        │
   │ pattern.headerBuilder()              │
   │   → { X-Judo-SignedIdentifier: ... } │
   └──────────┬───────────────────────────┘
              │
              ↓
5. Execute HTTP request
   ┌──────────────────────────────────────┐
   │ axiosService.axios.post()            │
   │   POST /api/demo/User/friends/~list  │
   │   Headers: { X-Judo-SignedIdentifier }│
   │   Body: { _mask: ..., _seek: ... }   │
   └──────────┬───────────────────────────┘
              │
              ↓
6. Receive response
   ┌──────────────────────────────────────┐
   │ { data: [...], status: 200, ... }    │
   └──────────┬───────────────────────────┘
              │
              ↓
7. Handle response
   ┌──────────────────────────────────────┐
   │ pattern.responseHandler()            │
   │   → deserialize array of friends     │
   │   → return JudoRestResponse          │
   └──────────┬───────────────────────────┘
              │
              ↓
8. Return to application
   ┌──────────────────────────────────────┐
   │ return { data: Friend[], ... }       │
   └──────────────────────────────────────┘
```

## Service Creation Flow

### Creating a Class Service

```
Application Request
        │
        ↓
┌───────────────────────────────────────┐
│ ServiceProvider.getClassService()     │
│   fqName = 'demo.User'                │
└───────┬───────────────────────────────┘
        │
        ↓
┌───────────────────────────────────────┐
│ Check ServiceRegistry                 │
│   key = 'class:demo.User'             │
│   cached? → return cached             │
└───────┬───────────────────────────────┘
        │ Not cached
        ↓
┌───────────────────────────────────────┐
│ MetadataProvider.getClassType()       │
│   → ClassTypeMetadata                 │
│     - fqName: 'demo.User'             │
│     - relations: [...]                │
│     - operations: [...]               │
│     - attributes: [...]               │
└───────┬───────────────────────────────┘
        │
        ↓
┌───────────────────────────────────────┐
│ ServiceFactory.createClassService()   │
│   metadata = ClassTypeMetadata        │
└───────┬───────────────────────────────┘
        │
        ↓
┌───────────────────────────────────────┐
│ new RuntimeServiceProxy()             │
│   - metadata                          │
│   - axiosService                      │
│   - serializationManager              │
│   - serviceType: 'class'              │
└───────┬───────────────────────────────┘
        │
        ↓
┌───────────────────────────────────────┐
│ proxy.createProxy()                   │
│   → new Proxy(target, handler)        │
│   → intercepts all method calls       │
└───────┬───────────────────────────────┘
        │
        ↓
┌───────────────────────────────────────┐
│ ServiceRegistry.register()            │
│   key = 'class:demo.User'             │
│   service = proxy instance            │
└───────┬───────────────────────────────┘
        │
        ↓
┌───────────────────────────────────────┐
│ Return service to application         │
│   typed as UserService                │
└───────────────────────────────────────┘
```

## Pattern Matching Architecture

### Pattern Matcher Components

```
┌─────────────────────────────────────────────────────────────┐
│                  MethodPatternMatcher                       │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ Class Service Patterns                                │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │ Pattern 1: /^getTemplate$/                      │  │ │
│  │  │  → GET /Class/~template                         │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 2: /^refresh$/                          │  │ │
│  │  │  → POST /Class/~get                             │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 3: /^delete$/                           │  │ │
│  │  │  → POST /Class/~delete                          │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 4: /^update$/                           │  │ │
│  │  │  → POST /Class/~update                          │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 5: /^create([A-Z][a-zA-Z0-9]*)$/        │  │ │
│  │  │  → POST /Class/~update/{relation}/~create       │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 6: /^(list|get)([A-Z][a-zA-Z0-9]*)$/    │  │ │
│  │  │  → POST /Class/{relation}/~{list|get}           │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 7: /^getRangeFor([A-Z][a-zA-Z0-9]*)$/   │  │ │
│  │  │  → POST /Class/{relation}/~range                │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ ... 20+ more patterns                           │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ Relation Service Patterns                             │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │ Pattern 1: /^list$/                             │  │ │
│  │  │  → POST /Owner/{relation}/~list                 │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 2: /^refresh$/                          │  │ │
│  │  │  → POST /Target/~get                            │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ ... 30+ more patterns                           │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ Access Service Patterns                               │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │ Pattern 1: /^getPrincipal$/                     │  │ │
│  │  │  → GET /~principal                              │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ Pattern 2: /^getMetaData$/                      │  │ │
│  │  │  → GET /~meta                                   │  │ │
│  │  ├─────────────────────────────────────────────────┤  │ │
│  │  │ ... 5+ more patterns                            │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│                    MethodPattern                        │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ regex: /^methodNamePattern$/                   │    │
│  │   - Matches method name                        │    │
│  │   - Captures groups for relation/operation     │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ httpMethod: 'GET' | 'POST'                     │    │
│  │   - HTTP verb to use                           │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ pathBuilder(matches, metadata, args) → string  │    │
│  │   - Constructs REST path                       │    │
│  │   - Uses metadata to find relations/operations │    │
│  │   - Example: '/api/demo/User/friends/~list'    │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ requestBuilder(args, metadata, serMgr) → any   │    │
│  │   - Builds request body                        │    │
│  │   - Serializes arguments                       │    │
│  │   - Example: { _mask: '{}', _seek: {...} }     │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ headerBuilder(args, metadata) → headers        │    │
│  │   - Builds request headers                     │    │
│  │   - Example: { X-Judo-SignedIdentifier: ... }  │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ responseHandler(response, metadata, serMgr)    │    │
│  │   - Transforms response                        │    │
│  │   - Deserializes data                          │    │
│  │   - Example: { data: User[], ... }             │    │
│  └────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## Serialization Architecture

### SerializationManager Flow

```
┌─────────────────────────────────────────────────────────────┐
│                  SerializationManager                       │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ Serializer Cache                                      │ │
│  │  Map<string, RuntimeSerializer>                       │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │ 'demo.User' → RuntimeSerializer<User>           │  │ │
│  │  │ 'demo.Product' → RuntimeSerializer<Product>     │  │ │
│  │  │ 'demo.Order' → RuntimeSerializer<Order>         │  │ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐ │
│  │ StoredSerializer Cache                                │ │
│  │  Map<string, RuntimeSerializer>                       │ │
│  │  ┌─────────────────────────────────────────────────┐  │ │
│  │  │ 'demo.User' → RuntimeSerializer<UserStored>     │  │ │
│  │  │ 'demo.Product' → RuntimeSerializer<ProductStored>│ │
│  │  └─────────────────────────────────────────────────┘  │ │
│  └───────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### RuntimeSerializer Flow

```
Serialize Flow:
┌─────────────────┐
│ TypeScript Obj  │
│ { name: 'John', │
│   age: 30,      │
│   birthDate:    │
│   Date(...) }   │
└────────┬────────┘
         │
         ↓
┌─────────────────────────────────┐
│ RuntimeSerializer.serialize()   │
│   - Iterate attributes          │
│   - Apply type conversions      │
└────────┬────────────────────────┘
         │
         ↓
┌─────────────────┐
│ Plain JS Object │
│ { name: 'John', │
│   age: 30,      │
│   birthDate:    │
│   '2024-01-01'} │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ HTTP Request    │
└─────────────────┘

Deserialize Flow:
┌─────────────────┐
│ HTTP Response   │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│ Plain JS Object │
│ { name: 'John', │
│   age: 30,      │
│   birthDate:    │
│   '2024-01-01'} │
└────────┬────────┘
         │
         ↓
┌───────────────────────────────────┐
│ RuntimeSerializer.deserialize()   │
│   - Iterate attributes            │
│   - Apply type conversions        │
│   - new Date('2024-01-01')        │
└────────┬──────────────────────────┘
         │
         ↓
┌─────────────────┐
│ TypeScript Obj  │
│ { name: 'John', │
│   age: 30,      │
│   birthDate:    │
│   Date(...) }   │
└─────────────────┘
```

## Caching Strategy

### Multi-Level Caching

```
┌─────────────────────────────────────────────────────────────┐
│                      Level 1: Metadata Cache                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ MetadataProvider                                      │  │
│  │   Map<string, ClassTypeMetadata>                      │  │
│  │   Map<string, RelationTypeMetadata>                   │  │
│  │   Cached on first access, never expires               │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Level 2: Service Cache                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ ServiceRegistry                                       │  │
│  │   Map<string, Service>                                │  │
│  │   Cached on first creation, never expires             │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Level 3: Serializer Cache                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ SerializationManager                                  │  │
│  │   Map<string, RuntimeSerializer>                      │  │
│  │   Cached on first use, never expires                  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Level 4: Pattern Cache                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │ MethodPatternMatcher                                  │  │
│  │   Array<CompiledPattern>                              │  │
│  │   Compiled at initialization, immutable               │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Error Handling Flow

```
Application Call
        │
        ↓
┌───────────────────────┐
│ RuntimeServiceProxy   │
└───────┬───────────────┘
        │
        ↓
┌───────────────────────┐
│ HTTP Request          │
└───────┬───────────────┘
        │
        ↓ Error?
┌───────────────────────────────────┐
│ Axios Error                       │
│                                   │
│ If response exists:               │
│   ┌─────────────────────────────┐ │
│   │ 401 Unauthorized            │ │
│   │ 403 Forbidden               │ │
│   │ 400 Bad Request             │ │
│   │   → FeedbackItem[]          │ │
│   │ 422 Business Error          │ │
│   │   → FaultContainer          │ │
│   └─────────────────────────────┘ │
│                                   │
│ If no response:                   │
│   ┌─────────────────────────────┐ │
│   │ Network Error               │ │
│   │ Timeout                     │ │
│   │ Connection Refused          │ │
│   └─────────────────────────────┘ │
└───────┬───────────────────────────┘
        │
        ↓
┌───────────────────────────────────┐
│ enhanceError()                    │
│   - Add context                   │
│   - Add method name               │
│   - Add arguments                 │
│   - Preserve original error       │
└───────┬───────────────────────────┘
        │
        ↓
┌───────────────────────────────────┐
│ Enhanced Error                    │
│   message: "Service method        │
│   'listFriends' failed: ..."      │
│   originalError: AxiosError       │
│   statusCode: 401                 │
│   data: FeedbackItem[]            │
└───────┬───────────────────────────┘
        │
        ↓
┌───────────────────────────────────┐
│ Thrown to Application             │
└───────────────────────────────────┘
```

## Deployment Architecture

### Development Environment

```
┌───────────────────────────────────┐
│ Developer Machine                 │
│                                   │
│ ┌───────────────────────────────┐ │
│ │ npm run dev                   │ │
│ │   - Hot reload enabled        │ │
│ │   - Source maps enabled       │ │
│ │   - Debug logging enabled     │ │
│ └───────────────────────────────┘ │
│                                   │
│ ┌───────────────────────────────┐ │
│ │ Runtime Service Layer         │ │
│ │   - Unminified code           │ │
│ │   - Debug mode ON             │ │
│ │   - Metadata from local file  │ │
│ └───────────────────────────────┘ │
└───────────────────────────────────┘
```

### Production Environment

```
┌───────────────────────────────────┐
│ Build Process                     │
│                                   │
│ ┌───────────────────────────────┐ │
│ │ npm run build                 │ │
│ │   1. Compile TypeScript       │ │
│ │   2. Bundle with Vite         │ │
│ │   3. Minify                   │ │
│ │   4. Tree shake               │ │
│ │   5. Code split               │ │
│ └───────────────────────────────┘ │
└───────────────────────────────────┘
                │
                ↓
┌───────────────────────────────────┐
│ Production Bundle                 │
│                                   │
│ ┌───────────────────────────────┐ │
│ │ main.js (minified)            │ │
│ │   - Runtime service layer     │ │
│ │   - Application code          │ │
│ │   - Dependencies              │ │
│ │                               │ │
│ │ metadata.json                 │ │
│ │   - Parsed UI model           │ │
│ │   - Loaded at runtime         │ │
│ └───────────────────────────────┘ │
└───────────────────────────────────┘
                │
                ↓
┌───────────────────────────────────┐
│ CDN / Web Server                  │
│   - Serves static assets          │
│   - Caching enabled               │
│   - Compression enabled           │
└───────────────────────────────────┘
```

## Summary

These diagrams illustrate the complete architecture of the runtime service layer:

1. **System Architecture:** Shows all layers and their relationships
2. **Method Invocation Flow:** Traces a single method call through the system
3. **Service Creation Flow:** Shows how services are instantiated
4. **Pattern Matching Architecture:** Details how method names are matched
5. **Serialization Architecture:** Shows serialization/deserialization process
6. **Caching Strategy:** Illustrates multi-level caching
7. **Error Handling Flow:** Shows how errors are processed and enhanced
8. **Deployment Architecture:** Shows dev and prod configurations

The runtime service layer provides a clean, maintainable architecture that eliminates code generation while preserving type safety and performance.

