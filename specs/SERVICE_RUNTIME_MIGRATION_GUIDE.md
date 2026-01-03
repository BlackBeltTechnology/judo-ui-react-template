# Runtime Service Layer Migration Guide

## Overview

This guide provides a step-by-step migration path from the current code generation approach to the runtime service layer. It includes a detailed comparison, migration strategies, and practical examples.

## Current vs Runtime Architecture Comparison

### Current (Code Generation) Architecture

```
┌─────────────────────────────────┐
│  Application Code               │
│  Uses Generated Services        │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  Generated Service Interfaces   │
│  (data-service/*.ts)            │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  Generated Service Impls        │
│  (data-axios/*ServiceImpl.ts)   │
│  - One file per ClassType       │
│  - One file per RelationType    │
│  - AccessServiceImpl            │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  JudoAxiosService               │
└─────────────────────────────────┘
```

**Build Process:**
1. Parse UI model
2. Generate TypeScript service interfaces
3. Generate TypeScript service implementations
4. Generate serializers
5. Compile TypeScript
6. Bundle for production

### Runtime Architecture

```
┌─────────────────────────────────┐
│  Application Code               │
│  Uses Service Interfaces        │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  Service Factory                │
│  Creates services at runtime    │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  Runtime Service Proxy          │
│  Single implementation          │
│  Method interception via Proxy  │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  JudoAxiosService               │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│  Parser Layer (Metadata)        │
└─────────────────────────────────┘
```

**Build Process:**
1. Parse UI model (loaded at runtime)
2. Compile TypeScript (only runtime layer + interfaces)
3. Bundle for production (much smaller)

## Detailed Comparison

| Aspect | Code Generation | Runtime Service Layer |
|--------|----------------|----------------------|
| **Bundle Size** | Large (N service files × M methods) | Small (1 proxy implementation) |
| **Build Time** | Slow (generate + compile many files) | Fast (compile runtime layer only) |
| **Type Safety** | Excellent (generated types) | Excellent (same interfaces) |
| **Debugging** | Hard (trace through generated code) | Easier (single implementation) |
| **Maintenance** | Generate for every change | No regeneration needed |
| **Flexibility** | Static (regenerate required) | Dynamic (runtime metadata) |
| **Cold Start** | Faster (pre-compiled) | Slightly slower (metadata parse) |
| **Hot Reload** | Requires regeneration | Instant (metadata refresh) |
| **Testing** | Test generated code | Test single implementation |
| **Code Review** | Review generated code | Review runtime implementation |

## Migration Strategies

### Strategy 1: Big Bang Migration

**Pros:**
- Clean break
- No parallel maintenance
- Immediate benefits

**Cons:**
- Higher risk
- Longer migration window
- All-or-nothing

**Steps:**
1. Implement full runtime layer
2. Test thoroughly against all scenarios
3. Switch all services at once
4. Remove generator

**Timeline:** 4-6 weeks

### Strategy 2: Gradual Migration (Recommended)

**Pros:**
- Lower risk
- Validate incrementally
- Easy rollback
- Learn and adapt

**Cons:**
- Longer migration period
- Parallel maintenance
- Mixed architecture temporarily

**Steps:**
1. Implement runtime layer alongside generated services
2. Migrate one service type at a time
3. Run both in parallel for validation
4. Gradually replace generated services
5. Remove generator when all migrated

**Timeline:** 8-12 weeks

### Strategy 3: Feature Flag Migration

**Pros:**
- A/B testing possible
- Instant rollback
- Production validation
- Gradual rollout

**Cons:**
- Additional complexity
- Longer parallel maintenance
- Feature flag overhead

**Steps:**
1. Implement runtime layer
2. Add feature flags
3. Enable for subset of users
4. Monitor and compare
5. Gradual rollout
6. Remove flags and generator

**Timeline:** 10-14 weeks

## Recommended Migration Path (Strategy 2)

### Phase 1: Infrastructure (Week 1-2)

**Goals:**
- Set up runtime layer foundation
- Integrate with parser
- No breaking changes

**Tasks:**

1. **Create runtime layer package structure:**
   ```
   src/
     runtime-service/
       core/
         MetadataProvider.ts
         ServiceRegistry.ts
         SerializationManager.ts
       proxy/
         RuntimeServiceProxy.ts
         MethodPatternMatcher.ts
       factory/
         ServiceFactory.ts
       index.ts
   ```

2. **Implement MetadataProvider:**
   ```typescript
   // Connects to parser layer
   export class ParserMetadataProvider implements MetadataProvider {
     // Implementation as per guide
   }
   ```

3. **Implement ServiceRegistry:**
   ```typescript
   // Service caching and management
   export class ServiceRegistryImpl implements ServiceRegistry {
     // Implementation as per guide
   }
   ```

4. **Implement SerializationManager:**
   ```typescript
   // Runtime serialization
   export class SerializationManager {
     // Implementation as per guide
   }
   ```

5. **Write unit tests:**
   - Test metadata parsing
   - Test registry operations
   - Test serialization

**Validation:**
- All tests pass
- No impact on existing code
- Metadata correctly parsed

### Phase 2: Access Service Migration (Week 3-4)

**Goals:**
- Migrate simplest service first
- Validate approach
- Build confidence

**Tasks:**

1. **Implement AccessService proxy:**
   ```typescript
   // Create patterns for access service methods
   const accessServicePatterns = [
     // getPrincipal, getMetaData, uploadFile, downloadFile, findInstanceOf*
   ];
   ```

2. **Create factory method:**
   ```typescript
   class ServiceFactoryImpl {
     createAccessService(): AccessService {
       // Implementation
     }
   }
   ```

3. **Add feature flag:**
   ```typescript
   const USE_RUNTIME_ACCESS_SERVICE = process.env.RUNTIME_ACCESS_SERVICE === 'true';
   
   export function getAccessService(): AccessService {
     if (USE_RUNTIME_ACCESS_SERVICE) {
       return serviceFactory.createAccessService();
     }
     return new AccessServiceImpl();
   }
   ```

4. **Write integration tests:**
   - Test all access service methods
   - Compare generated vs runtime outputs
   - Verify identical behavior

5. **Enable in development:**
   ```bash
   RUNTIME_ACCESS_SERVICE=true npm run dev
   ```

**Validation:**
- All access service tests pass
- Identical behavior to generated service
- No performance regression

### Phase 3: Simple Class Service Migration (Week 5-6)

**Goals:**
- Migrate a simple ClassType service
- Handle CRUD operations
- Validate pattern matching

**Tasks:**

1. **Choose simple ClassType:**
   - No complex relations
   - Basic CRUD operations
   - Good test coverage

2. **Implement ClassService proxy:**
   ```typescript
   const classServicePatterns = [
     // getTemplate, refresh, delete, update, validateUpdate
     // Basic relation operations
   ];
   ```

3. **Create factory method:**
   ```typescript
   class ServiceFactoryImpl {
     createClassService<T>(classType: ClassTypeMetadata): T {
       // Implementation
     }
   }
   ```

4. **Update service provider:**
   ```typescript
   export function getUserService(): UserService {
     if (USE_RUNTIME_SERVICES) {
       const metadata = metadataProvider.getClassType('demo.User');
       return serviceFactory.createClassService<UserService>(metadata);
     }
     return new UserServiceImpl();
   }
   ```

5. **Write comprehensive tests:**
   - Test all CRUD operations
   - Test relation operations
   - Compare outputs

**Validation:**
- All class service tests pass
- Correct method routing
- Proper serialization/deserialization

### Phase 4: Complex Class Service Migration (Week 7-8)

**Goals:**
- Handle complex relations
- Support operations
- Handle nested operations

**Tasks:**

1. **Choose complex ClassType:**
   - Multiple relations
   - Operations with input/output
   - Nested operations

2. **Extend pattern matcher:**
   ```typescript
   const complexPatterns = [
     // Operation invocations
     // Nested relation operations
     // Operation input range
     // Validate operations
   ];
   ```

3. **Handle edge cases:**
   - Operations on relations
   - Operations on operation inputs
   - Complex nesting

4. **Migrate service:**
   - Update service provider
   - Enable feature flag
   - Run tests

**Validation:**
- All complex operations work
- Nested operations correct
- No edge case failures

### Phase 5: Relation Service Migration (Week 9-10)

**Goals:**
- Migrate RelationType services
- Handle access vs non-access
- Support target operations

**Tasks:**

1. **Implement RelationService proxy:**
   ```typescript
   const relationServicePatterns = [
     // list, refresh, getRange, create, delete, update
     // set, unset, add, remove
     // Target relation operations
     // Target operations
   ];
   ```

2. **Handle access relations:**
   ```typescript
   // Special handling for isAccess relations
   if (relation.isAccess) {
     // Different path construction
     // No owner parameter in some cases
   }
   ```

3. **Migrate relation services:**
   - Start with simple relations
   - Move to complex relations
   - Test thoroughly

**Validation:**
- Relation CRUD works
- Target operations work
- Access relations correct

### Phase 6: Full Migration (Week 11-12)

**Goals:**
- Migrate all remaining services
- Remove feature flags
- Remove generator

**Tasks:**

1. **Migrate all services:**
   - Migrate remaining ClassTypes
   - Migrate remaining RelationTypes
   - Verify all work

2. **Remove feature flags:**
   ```typescript
   // Remove conditional logic
   export function getUserService(): UserService {
     const metadata = metadataProvider.getClassType('demo.User');
     return serviceFactory.createClassService<UserService>(metadata);
   }
   ```

3. **Remove generated code:**
   ```bash
   # Delete generated service implementations
   rm -rf src/generated/data-axios/*ServiceImpl.ts
   
   # Keep interfaces (still needed for types)
   # Or generate minimal interface definitions
   ```

4. **Update build process:**
   ```json
   // package.json
   {
     "scripts": {
       "build": "tsc && vite build",
       // Remove: "generate": "java -jar generator.jar"
     }
   }
   ```

5. **Update documentation:**
   - Update README
   - Update architecture docs
   - Update developer guides

**Validation:**
- All tests pass with runtime services only
- No references to generated implementations
- Build process works
- Production deployment successful

## Code Examples

### Before: Using Generated Services

```typescript
// Import generated service implementation
import { UserServiceImpl } from '../generated/data-axios/UserServiceImpl';
import type { UserService } from '../generated/data-service/UserService';

// Create instance
const userService: UserService = new UserServiceImpl(axiosService);

// Use service
const user = await userService.refresh(storedUser);
const friends = await userService.listFriends(user);
```

### After: Using Runtime Services

```typescript
// Import factory and metadata provider
import { serviceFactory, metadataProvider } from '../runtime-service';
import type { UserService } from '../generated/data-service/UserService';

// Create service from metadata
const userMetadata = metadataProvider.getClassType('demo.User');
const userService: UserService = serviceFactory.createClassService<UserService>(userMetadata);

// Use service (identical API)
const user = await userService.refresh(storedUser);
const friends = await userService.listFriends(user);
```

### Abstraction Layer

Create a service provider abstraction for easier migration:

```typescript
// services/ServiceProvider.ts

class ServiceProvider {
  private static factory: ServiceFactory;
  private static metadataProvider: MetadataProvider;
  
  static initialize(factory: ServiceFactory, provider: MetadataProvider) {
    ServiceProvider.factory = factory;
    ServiceProvider.metadataProvider = provider;
  }
  
  static getClassService<T>(fqName: string): T {
    const metadata = ServiceProvider.metadataProvider.getClassType(fqName);
    if (!metadata) {
      throw new Error(`ClassType not found: ${fqName}`);
    }
    return ServiceProvider.factory.createClassService<T>(metadata);
  }
  
  static getRelationService<T>(fqName: string): T {
    const metadata = ServiceProvider.metadataProvider.getRelationType(fqName);
    if (!metadata) {
      throw new Error(`RelationType not found: ${fqName}`);
    }
    return ServiceProvider.factory.createRelationService<T>(metadata);
  }
  
  static getAccessService(): AccessService {
    return ServiceProvider.factory.createAccessService();
  }
}

// Usage - much simpler!
const userService = ServiceProvider.getClassService<UserService>('demo.User');
const accessService = ServiceProvider.getAccessService();
```

## Testing Strategy

### 1. Parallel Testing

Run tests against both generated and runtime services:

```typescript
describe('UserService', () => {
  const testCases = [
    { name: 'Generated', service: new UserServiceImpl() },
    { name: 'Runtime', service: serviceFactory.createClassService(userMetadata) },
  ];
  
  testCases.forEach(({ name, service }) => {
    describe(`${name} implementation`, () => {
      it('should refresh user', async () => {
        const result = await service.refresh(testUser);
        expect(result.data).toBeDefined();
        expect(result.data.__signedIdentifier).toBe(testUser.__signedIdentifier);
      });
      
      // ... more tests
    });
  });
});
```

### 2. Snapshot Testing

Compare outputs between generated and runtime:

```typescript
it('should produce identical output', async () => {
  const generatedService = new UserServiceImpl();
  const runtimeService = serviceFactory.createClassService<UserService>(userMetadata);
  
  const generatedResult = await generatedService.refresh(testUser);
  const runtimeResult = await runtimeService.refresh(testUser);
  
  expect(runtimeResult).toEqual(generatedResult);
});
```

### 3. Performance Testing

Ensure runtime services aren't slower:

```typescript
it('should have acceptable performance', async () => {
  const service = serviceFactory.createClassService<UserService>(userMetadata);
  
  const start = performance.now();
  for (let i = 0; i < 1000; i++) {
    await service.getTemplate();
  }
  const duration = performance.now() - start;
  
  expect(duration).toBeLessThan(5000); // 5ms per call on average
});
```

## Rollback Plan

If issues are encountered, rollback is straightforward:

### Option 1: Feature Flag Rollback

```typescript
// Immediately disable runtime services
USE_RUNTIME_SERVICES=false npm run build
```

### Option 2: Git Revert

```bash
# Revert to previous working state
git revert <commit-hash>

# Rebuild with generator
npm run generate
npm run build
```

### Option 3: Partial Rollback

```typescript
// Rollback specific services only
export function getUserService(): UserService {
  // Temporarily go back to generated
  return new UserServiceImpl();
  
  // Runtime (commented out during issue investigation)
  // const metadata = metadataProvider.getClassType('demo.User');
  // return serviceFactory.createClassService<UserService>(metadata);
}
```

## Monitoring and Validation

### Metrics to Track

1. **Bundle Size:**
   - Before migration: X MB
   - After migration: Y MB
   - Target: 30-50% reduction

2. **Build Time:**
   - Before: X seconds
   - After: Y seconds
   - Target: 50% reduction

3. **Runtime Performance:**
   - Service call latency (should be similar)
   - Memory usage (should be similar or better)
   - Cold start time (may be slightly higher)

4. **Error Rates:**
   - Compare error rates before/after
   - Monitor for new error patterns
   - Track user impact

### Validation Checklist

Before considering migration complete:

- [ ] All services migrated to runtime
- [ ] All tests passing (unit, integration, e2e)
- [ ] Performance acceptable (< 5% regression)
- [ ] Bundle size reduced (target: 30%+)
- [ ] Build time reduced (target: 50%+)
- [ ] No increase in error rates
- [ ] Documentation updated
- [ ] Team trained on new approach
- [ ] Monitoring in place
- [ ] Rollback plan tested

## Common Issues and Solutions

### Issue 1: Method Pattern Not Matched

**Symptom:** Error "Unknown method: methodName"

**Solution:**
```typescript
// Add pattern to MethodPatternMatcher
{
  regex: /^methodNamePattern$/,
  // ... pattern definition
}
```

### Issue 2: Incorrect Serialization

**Symptom:** Data types wrong in request/response

**Solution:**
```typescript
// Check attribute data type in metadata
// Update SerializationManager logic
private serializeAttribute(value: any, attr: AttributeTypeMetadata): any {
  // Add special handling for this data type
}
```

### Issue 3: Wrong REST Path

**Symptom:** 404 errors on service calls

**Solution:**
```typescript
// Verify path construction in pathBuilder
pathBuilder: (matches, metadata) => {
  console.log('Building path:', metadata.restPath);
  return `${metadata.restPath}/~operation`;
}
```

### Issue 4: Missing Headers

**Symptom:** 401/403 errors

**Solution:**
```typescript
// Check headerBuilder in pattern
headerBuilder: (args) => {
  console.log('Headers:', {
    [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
  });
  return { /* ... */ };
}
```

### Issue 5: Performance Issues

**Symptom:** Slow service calls

**Solution:**
```typescript
// Enable caching
const serviceFactory = new ServiceFactoryImpl({
  enableCache: true,
  cacheStrategy: 'memory',
});

// Or pre-warm services
async function prewarmServices() {
  await Promise.all([
    serviceFactory.createClassService(userMetadata),
    serviceFactory.createClassService(productMetadata),
    // ...
  ]);
}
```

## Benefits Realized

After successful migration, you should see:

### Development Benefits
- **Faster iteration:** No regeneration step
- **Easier debugging:** Single implementation to trace
- **Better testing:** Test implementation once, not N generated files
- **Simpler maintenance:** Update one place, not N generated files

### Build Benefits
- **Smaller bundles:** 30-50% reduction typical
- **Faster builds:** 50%+ reduction in build time
- **Simpler pipeline:** Remove generation step

### Runtime Benefits
- **Flexible:** Can handle runtime metadata changes
- **Dynamic:** New entities don't require rebuild
- **Maintainable:** Clear, traceable code

## Conclusion

The migration from code generation to runtime service layer is a significant but manageable undertaking. By following a gradual, phased approach with thorough testing and validation at each step, you can successfully migrate while minimizing risk.

Key success factors:
1. **Incremental approach:** Migrate one service type at a time
2. **Thorough testing:** Test each phase before moving to next
3. **Parallel running:** Keep both approaches working during transition
4. **Clear rollback plan:** Be ready to revert if needed
5. **Team buy-in:** Ensure team understands benefits and approach

The result is a more maintainable, flexible, and efficient service layer that eliminates code generation while maintaining full type safety and API compatibility.

