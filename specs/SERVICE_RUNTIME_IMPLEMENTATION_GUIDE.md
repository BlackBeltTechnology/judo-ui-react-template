# Runtime Service Layer Implementation Guide

## Overview

This guide provides concrete implementation examples for the runtime service layer specified in `SERVICE_RUNTIME_SPECIFICATION.md`. It includes detailed pseudocode, patterns, and implementation strategies.

## Core Implementation Components

### 1. MetadataProvider Implementation

```typescript
/**
 * Provides access to parsed metadata from the parser layer
 */
class ParserMetadataProvider implements MetadataProvider {
  private cache: Map<string, any> = new Map();
  private application: ApplicationMetadata;
  
  constructor(private parserContext: ParserContext) {
    // Initialize from parser
    this.application = this.parseApplication(parserContext);
  }
  
  getApplication(): ApplicationMetadata {
    return this.application;
  }
  
  getClassType(fqName: string): ClassTypeMetadata | undefined {
    if (this.cache.has(`class:${fqName}`)) {
      return this.cache.get(`class:${fqName}`);
    }
    
    const classType = this.application.classTypes.find(ct => ct.fqName === fqName);
    if (classType) {
      this.cache.set(`class:${fqName}`, classType);
    }
    return classType;
  }
  
  getRelationType(fqName: string): RelationTypeMetadata | undefined {
    if (this.cache.has(`relation:${fqName}`)) {
      return this.cache.get(`relation:${fqName}`);
    }
    
    for (const classType of this.application.classTypes) {
      const relation = classType.relations.find(r => r.fqName === fqName);
      if (relation) {
        this.cache.set(`relation:${fqName}`, relation);
        return relation;
      }
    }
    
    return undefined;
  }
  
  getAccessRelations(): RelationTypeMetadata[] {
    return this.application.relationTypes.filter(r => r.isAccess);
  }
  
  getNonAccessRelations(): RelationTypeMetadata[] {
    return this.application.relationTypes.filter(r => !r.isAccess);
  }
  
  private parseApplication(context: ParserContext): ApplicationMetadata {
    // Parse from parser context
    // This would use the parser layer to extract metadata
    return {
      name: context.application.name,
      principal: context.application.principal,
      classTypes: context.application.classTypes.map(ct => this.parseClassType(ct)),
      relationTypes: context.application.relationTypes.map(rt => this.parseRelationType(rt)),
    };
  }
  
  private parseClassType(classType: any): ClassTypeMetadata {
    return {
      fqName: classType.getFQName(),
      name: classType.name,
      isTemplateable: classType.isTemplateable,
      isMapped: classType.isMapped,
      isDeletable: classType.isDeletable,
      isUpdatable: classType.isUpdatable,
      isUpdateValidatable: classType.isUpdateValidatable,
      relations: classType.relations.map(r => this.parseRelationType(r)),
      operations: classType.operations.map(o => this.parseOperationType(o)),
      attributes: classType.attributes.map(a => this.parseAttributeType(a)),
      restPath: this.computeRestPath(classType),
    };
  }
  
  private parseRelationType(relation: any): RelationTypeMetadata {
    return {
      fqName: relation.getFQName(),
      name: relation.name,
      owner: this.parseClassType(relation.owner),
      target: this.parseClassType(relation.target),
      isAccess: relation.isAccess,
      isCollection: relation.isCollection,
      isListable: relation.isListable,
      isRefreshable: relation.isRefreshable,
      isRangeable: relation.isRangeable,
      isCreatable: relation.isCreatable,
      isCreateValidatable: relation.isCreateValidatable,
      isDeletable: relation.isDeletable,
      isUpdatable: relation.isUpdatable,
      isUpdateValidatable: relation.isUpdateValidatable,
      isSetable: relation.isSetable,
      isUnsetable: relation.isUnsetable,
      isAddable: relation.isAddable,
      isRemovable: relation.isRemovable,
      isExportable: relation.isExportable,
    };
  }
  
  private parseOperationType(operation: any): OperationTypeMetadata {
    return {
      name: operation.name,
      fqName: operation.getFQName(),
      input: operation.input ? {
        target: this.parseClassType(operation.input.target),
        behaviours: operation.input.behaviours,
      } : undefined,
      output: operation.output ? {
        target: this.parseClassType(operation.output.target),
      } : undefined,
      isMapped: operation.isMapped,
      isStatic: operation.isStatic,
      isInputRangeable: operation.isInputRangeable,
      isInputValidateable: operation.input?.behaviours.includes('VALIDATE_INPUT') ?? false,
    };
  }
  
  private parseAttributeType(attribute: any): AttributeTypeMetadata {
    return {
      name: attribute.name,
      dataType: attribute.dataType,
      isRequired: attribute.isRequired,
      // ... other attribute properties
    };
  }
  
  private computeRestPath(classType: any): string {
    // Compute REST path based on application and class FQName
    // Format: /api/{appName}/{classFQName}
    return `/api/${this.application.name}/${classType.getFQName()}`;
  }
}
```

### 2. ServiceRegistry Implementation

```typescript
/**
 * Singleton registry for service instances
 */
class ServiceRegistryImpl implements ServiceRegistry {
  private static instance: ServiceRegistryImpl;
  private services: Map<string, any> = new Map();
  
  private constructor() {}
  
  static getInstance(): ServiceRegistryImpl {
    if (!ServiceRegistryImpl.instance) {
      ServiceRegistryImpl.instance = new ServiceRegistryImpl();
    }
    return ServiceRegistryImpl.instance;
  }
  
  register(key: string, service: any): void {
    this.services.set(key, service);
  }
  
  get<T>(key: string): T | undefined {
    return this.services.get(key) as T | undefined;
  }
  
  has(key: string): boolean {
    return this.services.has(key);
  }
  
  clear(): void {
    this.services.clear();
  }
  
  // Helper to generate keys
  static classServiceKey(fqName: string): string {
    return `class:${fqName}`;
  }
  
  static relationServiceKey(fqName: string): string {
    return `relation:${fqName}`;
  }
  
  static accessServiceKey(): string {
    return 'access';
  }
}
```

### 3. RuntimeServiceProxy Implementation

```typescript
/**
 * Core proxy that handles method interception and routing
 */
class RuntimeServiceProxy<T extends object> {
  private methodPatternMatcher: MethodPatternMatcher;
  
  constructor(
    private metadata: ClassTypeMetadata | RelationTypeMetadata,
    private axiosService: JudoAxiosService,
    private serializationManager: SerializationManager,
    private serviceType: 'class' | 'relation' | 'access'
  ) {
    this.methodPatternMatcher = new MethodPatternMatcher(serviceType);
  }
  
  createProxy(): T {
    const handler: ProxyHandler<any> = {
      get: (target, prop, receiver) => {
        if (typeof prop === 'string') {
          return this.createMethodHandler(prop);
        }
        return Reflect.get(target, prop, receiver);
      },
    };
    
    return new Proxy({}, handler) as T;
  }
  
  private createMethodHandler(methodName: string): Function {
    return async (...args: any[]) => {
      const pattern = this.methodPatternMatcher.match(methodName);
      
      if (!pattern) {
        throw new Error(`Unknown method: ${methodName}`);
      }
      
      try {
        // Build request
        const request = this.buildRequest(pattern, methodName, args);
        
        // Execute request
        const response = await this.executeRequest(request);
        
        // Handle response
        return this.handleResponse(response, pattern, args);
      } catch (error) {
        throw this.enhanceError(error, methodName, args);
      }
    };
  }
  
  private buildRequest(
    pattern: MethodPattern,
    methodName: string,
    args: any[]
  ): ServiceRequest {
    const matches = methodName.match(pattern.regex)!;
    
    return {
      method: pattern.httpMethod,
      path: pattern.pathBuilder(matches, this.metadata, args),
      body: pattern.requestBuilder(args, this.metadata, this.serializationManager),
      headers: pattern.headerBuilder(args, this.metadata),
    };
  }
  
  private async executeRequest(request: ServiceRequest): Promise<any> {
    const fullPath = this.axiosService.getPathForActor(request.path);
    
    switch (request.method) {
      case 'GET':
        return await this.axiosService.axios.get(fullPath, {
          headers: request.headers,
        });
      case 'POST':
        return await this.axiosService.axios.post(
          fullPath,
          request.body,
          { headers: request.headers }
        );
      default:
        throw new Error(`Unsupported HTTP method: ${request.method}`);
    }
  }
  
  private handleResponse(
    response: any,
    pattern: MethodPattern,
    args: any[]
  ): any {
    return pattern.responseHandler(
      response,
      this.metadata,
      this.serializationManager
    );
  }
  
  private enhanceError(error: any, methodName: string, args: any[]): Error {
    if (error.response) {
      // Axios error with response
      const enhanced = new Error(
        `Service method '${methodName}' failed: ${error.message}`
      );
      (enhanced as any).originalError = error;
      (enhanced as any).statusCode = error.response.status;
      (enhanced as any).data = error.response.data;
      return enhanced;
    }
    return error;
  }
}
```

### 4. MethodPatternMatcher Implementation

```typescript
/**
 * Matches method names to operation patterns
 */
class MethodPatternMatcher {
  private patterns: MethodPattern[];
  
  constructor(serviceType: 'class' | 'relation' | 'access') {
    this.patterns = this.initializePatterns(serviceType);
  }
  
  match(methodName: string): MethodPattern | undefined {
    for (const pattern of this.patterns) {
      if (pattern.regex.test(methodName)) {
        return pattern;
      }
    }
    return undefined;
  }
  
  private initializePatterns(serviceType: string): MethodPattern[] {
    switch (serviceType) {
      case 'class':
        return this.createClassServicePatterns();
      case 'relation':
        return this.createRelationServicePatterns();
      case 'access':
        return this.createAccessServicePatterns();
      default:
        return [];
    }
  }
  
  private createClassServicePatterns(): MethodPattern[] {
    return [
      // getTemplate()
      {
        regex: /^getTemplate$/,
        httpMethod: 'GET',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => 
          `${metadata.restPath}/~template`,
        requestBuilder: () => undefined,
        headerBuilder: () => ({}),
        responseHandler: (response, metadata, serMgr) => ({
          ...response,
          data: serMgr.deserialize(response.data, metadata),
        }),
      },
      
      // refresh(target, queryCustomizer?, headers?)
      {
        regex: /^refresh$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => 
          `${metadata.restPath}/~get`,
        requestBuilder: (args, metadata, serMgr) => 
          serMgr.serializeQueryCustomizer(args[1]),
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
          ...(args[2] ?? {}),
        }),
        responseHandler: (response, metadata, serMgr) => ({
          ...response,
          data: serMgr.deserializeStored(response.data, metadata),
        }),
      },
      
      // delete(target)
      {
        regex: /^delete$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => 
          `${metadata.restPath}/~delete`,
        requestBuilder: () => undefined,
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response) => response,
      },
      
      // update(target, queryCustomizer?)
      {
        regex: /^update$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => 
          `${metadata.restPath}/~update`,
        requestBuilder: (args, metadata, serMgr) => 
          serMgr.serializeStored(args[0], metadata),
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
          [X_JUDO_MASK]: args[1]?._mask ?? '{}',
        }),
        responseHandler: (response, metadata, serMgr) => ({
          ...response,
          data: serMgr.deserializeStored(response.data, metadata),
        }),
      },
      
      // validateUpdate(target)
      {
        regex: /^validateUpdate$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => 
          `${metadata.restPath}/~validate`,
        requestBuilder: (args, metadata, serMgr) => 
          serMgr.serializeStored(args[0], metadata),
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response, metadata, serMgr) => ({
          ...response,
          data: serMgr.deserializeStored(response.data, metadata),
        }),
      },
      
      // getTemplateFor{Relation}()
      {
        regex: /^getTemplateFor([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'GET',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          if (!relation) throw new Error(`Relation not found: ${relationName}`);
          return `${relation.target.restPath}/~template`;
        },
        requestBuilder: () => undefined,
        headerBuilder: () => ({}),
        responseHandler: (response, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(response._matchedMethod.match(/getTemplateFor([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return {
            ...response,
            data: serMgr.deserialize(response.data, relation!.target),
          };
        },
      },
      
      // create{Relation}(owner, target, queryCustomizer?)
      {
        regex: /^create([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/~update/${relationName}/~create`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(args._matchedMethod.match(/create([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return serMgr.serialize(args[1], relation!.target);
        },
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
          [X_JUDO_MASK]: args[2]?._mask ?? '{}',
        }),
        responseHandler: (response, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(response._matchedMethod.match(/create([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return {
            ...response,
            data: serMgr.deserializeStored(response.data, relation!.target),
          };
        },
      },
      
      // list{Relation}(target, queryCustomizer?, headers?) or get{Relation}(...)
      {
        regex: /^(list|get)([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const operation = matches[1]; // 'list' or 'get'
          const relationName = lowerFirst(matches[2]);
          return `${metadata.restPath}/${relationName}/~${operation}`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => 
          serMgr.serializeQueryCustomizer(args[1]) ?? {},
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
          ...(args[2] ?? {}),
        }),
        responseHandler: (response, metadata: ClassTypeMetadata, serMgr) => {
          const matches = response._matchedMethod.match(/^(list|get)([A-Z][a-zA-Z0-9]*)$/);
          const isCollection = matches[1] === 'list';
          const relationName = lowerFirst(matches[2]);
          const relation = metadata.relations.find(r => r.name === relationName);
          
          if (isCollection) {
            return {
              ...response,
              data: Array.isArray(response.data) 
                ? response.data.map(d => serMgr.deserializeStored(d, relation!.target))
                : [],
            };
          } else {
            return {
              ...response,
              data: (typeof response.data === 'string' && !response.data.length)
                ? null
                : serMgr.deserializeStored(response.data, relation!.target),
            };
          }
        },
      },
      
      // getRangeFor{Relation}(owner?, queryCustomizer?, headers?)
      {
        regex: /^getRangeFor([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/${relationName}/~range`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => ({
          owner: args[0] ? serMgr.serializeStored(args[0], metadata) : {},
          queryCustomizer: serMgr.serializeQueryCustomizer(args[1]) ?? {},
        }),
        headerBuilder: (args) => ({
          ...(args[2] ?? {}),
          [X_JUDO_MARK_SELECTED_RANGE_ITEMS]: 'true',
        }),
        responseHandler: (response, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(response._matchedMethod.match(/getRangeFor([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return {
            ...response,
            data: Array.isArray(response.data)
              ? response.data.map(d => serMgr.deserializeStored(d, relation!.target))
              : [],
          };
        },
      },
      
      // set{Relation}(owner, selected)
      {
        regex: /^set([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/~update/${relationName}/~set`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(args._matchedMethod.match(/set([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          
          if (relation!.isCollection) {
            return args[1].map(s => serMgr.serializeStored(s, relation!.target));
          } else {
            return serMgr.serializeStored(args[1], relation!.target);
          }
        },
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response) => response,
      },
      
      // unset{Relation}(owner)
      {
        regex: /^unset([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/~update/${relationName}/~unset`;
        },
        requestBuilder: () => undefined,
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response) => response,
      },
      
      // add{Relation}(owner, selected)
      {
        regex: /^add([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/~update/${relationName}/~add`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(args._matchedMethod.match(/add([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return args[1].map(s => serMgr.serializeStored(s, relation!.target));
        },
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response) => response,
      },
      
      // remove{Relation}(owner, selected)
      {
        regex: /^remove([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/~update/${relationName}/~remove`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => {
          const relationName = lowerFirst(args._matchedMethod.match(/remove([A-Z][a-zA-Z0-9]*)$/)[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return args[1].map(s => serMgr.serializeStored(s, relation!.target));
        },
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response) => response,
      },
      
      // delete{Relation}(target)
      {
        regex: /^delete([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          const relation = metadata.relations.find(r => r.name === relationName);
          return `${relation!.target.restPath}/~delete`;
        },
        requestBuilder: () => undefined,
        headerBuilder: (args) => ({
          [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier,
        }),
        responseHandler: (response) => response,
      },
      
      // export{Relation}(owner?, queryCustomizer?)
      {
        regex: /^export([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const relationName = lowerFirst(matches[1]);
          return `${metadata.restPath}/${relationName}/~export`;
        },
        requestBuilder: (args, metadata, serMgr) => 
          serMgr.serializeQueryCustomizer(args[1]) ?? {},
        headerBuilder: (args) => 
          args[0] ? { [X_JUDO_SIGNED_IDENTIFIER]: args[0].__signedIdentifier } : {},
        responseHandler: (response) => response, // Blob response
      },
      
      // {operation}For{Relation}(...) - operation invocations on relation targets
      {
        regex: /^([a-z][a-zA-Z0-9]*)For([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const operationName = matches[1];
          const relationName = lowerFirst(matches[2]);
          const relation = metadata.relations.find(r => r.name === relationName);
          const operation = relation!.target.operations.find(o => o.name === operationName);
          return `${relation!.target.restPath}/${operation!.name}`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => {
          // Complex logic based on operation.isMapped and operation.input
          // args[0] might be owner if isMapped, then target
          // or just target if not mapped
          const matches = args._matchedMethod.match(/^([a-z][a-zA-Z0-9]*)For([A-Z][a-zA-Z0-9]*)$/);
          const operationName = matches[1];
          const relationName = lowerFirst(matches[2]);
          const relation = metadata.relations.find(r => r.name === relationName);
          const operation = relation!.target.operations.find(o => o.name === operationName);
          
          if (operation!.input) {
            const targetArg = operation!.isMapped ? args[1] : args[0];
            return targetArg ? serMgr.serialize(targetArg, operation!.input.target) : null;
          }
          return undefined;
        },
        headerBuilder: (args, metadata: ClassTypeMetadata) => {
          const matches = args._matchedMethod.match(/^([a-z][a-zA-Z0-9]*)For([A-Z][a-zA-Z0-9]*)$/);
          const operationName = matches[1];
          const relationName = lowerFirst(matches[2]);
          const relation = metadata.relations.find(r => r.name === relationName);
          const operation = relation!.target.operations.find(o => o.name === operationName);
          
          if (operation!.isMapped) {
            return { [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier };
          }
          return {};
        },
        responseHandler: (response, metadata: ClassTypeMetadata, serMgr) => {
          const matches = response._matchedMethod.match(/^([a-z][a-zA-Z0-9]*)For([A-Z][a-zA-Z0-9]*)$/);
          const operationName = matches[1];
          const relationName = lowerFirst(matches[2]);
          const relation = metadata.relations.find(r => r.name === relationName);
          const operation = relation!.target.operations.find(o => o.name === operationName);
          
          if (operation!.output) {
            return {
              ...response,
              data: serMgr.deserializeStored(response.data, operation!.output.target),
            };
          }
          return response;
        },
      },
      
      // {operation}(...) - operation invocations on class
      {
        regex: /^([a-z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ClassTypeMetadata) => {
          const operationName = matches[1];
          const operation = metadata.operations.find(o => o.name === operationName);
          if (!operation) throw new Error(`Operation not found: ${operationName}`);
          return `${metadata.restPath}/${operation.name}`;
        },
        requestBuilder: (args, metadata: ClassTypeMetadata, serMgr) => {
          const operationName = args._matchedMethod;
          const operation = metadata.operations.find(o => o.name === operationName);
          
          if (operation!.input) {
            const targetArg = operation!.isMapped ? args[1] : args[0];
            return targetArg ? serMgr.serialize(targetArg, operation!.input.target) : null;
          }
          return undefined;
        },
        headerBuilder: (args, metadata: ClassTypeMetadata) => {
          const operationName = args._matchedMethod;
          const operation = metadata.operations.find(o => o.name === operationName);
          
          if (operation!.isMapped) {
            return { [X_JUDO_SIGNED_IDENTIFIER]: args[0]?.__signedIdentifier };
          }
          return {};
        },
        responseHandler: (response, metadata: ClassTypeMetadata, serMgr) => {
          const operationName = response._matchedMethod;
          const operation = metadata.operations.find(o => o.name === operationName);
          
          if (operation!.output) {
            return {
              ...response,
              data: response.data ? serMgr.deserializeStored(response.data, operation!.output.target) : response.data,
            };
          }
          return response;
        },
      },
      
      // More patterns for validateCreate*, validateOn*, getTemplateOn*, getRangeOn*, etc.
      // ... (similar patterns with appropriate logic)
    ];
  }
  
  private createRelationServicePatterns(): MethodPattern[] {
    // Similar patterns for RelationType-based services
    // Differences: owner parameter handling, access vs non-access paths
    return [
      // Similar to class service but adjusted for relation context
      // ... patterns
    ];
  }
  
  private createAccessServicePatterns(): MethodPattern[] {
    return [
      // getPrincipal()
      {
        regex: /^getPrincipal$/,
        httpMethod: 'GET',
        pathBuilder: (matches, metadata: ApplicationMetadata) => 
          `/api/${metadata.name}/~principal`,
        requestBuilder: () => undefined,
        headerBuilder: () => ({}),
        responseHandler: (response, metadata: ApplicationMetadata, serMgr) => ({
          ...response,
          data: serMgr.deserializeStored(response.data, metadata.principal!),
        }),
      },
      
      // getMetaData()
      {
        regex: /^getMetaData$/,
        httpMethod: 'GET',
        pathBuilder: (matches, metadata: ApplicationMetadata) => 
          `/api/${metadata.name}/~meta`,
        requestBuilder: () => undefined,
        headerBuilder: () => ({}),
        responseHandler: (response) => response,
      },
      
      // uploadFile(attributePath, file)
      {
        regex: /^uploadFile$/,
        httpMethod: 'POST',
        pathBuilder: () => '', // Special handling needed
        requestBuilder: () => undefined, // Special handling needed
        headerBuilder: () => ({}),
        responseHandler: (response) => response,
        // This needs custom logic for two-step upload process
      },
      
      // downloadFile(downloadToken, disposition)
      {
        regex: /^downloadFile$/,
        httpMethod: 'GET',
        pathBuilder: (matches, metadata, args) => 
          `/download?disposition=${args[1]}`,
        requestBuilder: () => undefined,
        headerBuilder: (args) => ({
          'X-Token': args[0],
        }),
        responseHandler: (response) => response,
      },
      
      // findInstanceOf{Relation}(identifier, mask?)
      {
        regex: /^findInstanceOf([A-Z][a-zA-Z0-9]*)$/,
        httpMethod: 'POST',
        pathBuilder: (matches, metadata: ApplicationMetadata) => {
          const relationName = lowerFirst(matches[1]);
          const relation = metadata.relationTypes.find(r => r.name === relationName && r.isAccess);
          return `/api/${metadata.name}/${relation!.owner.fqName}/${relationName}/~list`;
        },
        requestBuilder: (args) => ({
          _identifier: args[0],
          _mask: args[1],
          _seek: { limit: 1 },
        }),
        headerBuilder: () => ({}),
        responseHandler: (response, metadata: ApplicationMetadata, serMgr) => {
          if (Array.isArray(response.data) && response.data.length === 1) {
            const matches = response._matchedMethod.match(/findInstanceOf([A-Z][a-zA-Z0-9]*)$/);
            const relationName = lowerFirst(matches[1]);
            const relation = metadata.relationTypes.find(r => r.name === relationName && r.isAccess);
            return serMgr.deserializeStored(response.data[0], relation!.target);
          }
          return undefined;
        },
      },
    ];
  }
}
```

### 5. SerializationManager Implementation

```typescript
/**
 * Manages runtime serialization and deserialization
 */
class SerializationManager {
  private serializers: Map<string, RuntimeSerializer<any>> = new Map();
  private storedSerializers: Map<string, RuntimeSerializer<any>> = new Map();
  
  serialize<T>(obj: T, metadata: ClassTypeMetadata): any {
    const serializer = this.getSerializer(metadata);
    return serializer.serialize(obj);
  }
  
  deserialize<T>(data: any, metadata: ClassTypeMetadata): T {
    const serializer = this.getSerializer(metadata);
    return serializer.deserialize(data);
  }
  
  serializeStored<T>(obj: T, metadata: ClassTypeMetadata): any {
    const serializer = this.getStoredSerializer(metadata);
    return serializer.serialize(obj);
  }
  
  deserializeStored<T>(data: any, metadata: ClassTypeMetadata): T {
    const serializer = this.getStoredSerializer(metadata);
    return serializer.deserialize(data);
  }
  
  serializeQueryCustomizer(customizer: any): any {
    if (!customizer) return {};
    
    return {
      _mask: customizer._mask,
      _seek: customizer._seek,
      _orderBy: customizer._orderBy,
      // ... other fields
    };
  }
  
  private getSerializer(metadata: ClassTypeMetadata): RuntimeSerializer<any> {
    const key = metadata.fqName;
    if (!this.serializers.has(key)) {
      this.serializers.set(key, new RuntimeSerializer(metadata, false));
    }
    return this.serializers.get(key)!;
  }
  
  private getStoredSerializer(metadata: ClassTypeMetadata): RuntimeSerializer<any> {
    const key = metadata.fqName;
    if (!this.storedSerializers.has(key)) {
      this.storedSerializers.set(key, new RuntimeSerializer(metadata, true));
    }
    return this.storedSerializers.get(key)!;
  }
}

/**
 * Runtime serializer for a specific type
 */
class RuntimeSerializer<T> {
  constructor(
    private metadata: ClassTypeMetadata,
    private isStored: boolean
  ) {}
  
  serialize(obj: T): any {
    if (!obj) return obj;
    
    const result: any = {};
    
    for (const attr of this.metadata.attributes) {
      const value = (obj as any)[attr.name];
      if (value !== undefined) {
        result[attr.name] = this.serializeAttribute(value, attr);
      }
    }
    
    // Preserve system fields for stored types
    if (this.isStored && (obj as any).__signedIdentifier) {
      result.__signedIdentifier = (obj as any).__signedIdentifier;
    }
    
    return result;
  }
  
  deserialize(data: any): T {
    if (!data) return data;
    
    const result: any = {};
    
    for (const attr of this.metadata.attributes) {
      const value = data[attr.name];
      if (value !== undefined) {
        result[attr.name] = this.deserializeAttribute(value, attr);
      }
    }
    
    // Preserve system fields for stored types
    if (this.isStored && data.__signedIdentifier) {
      result.__signedIdentifier = data.__signedIdentifier;
    }
    
    return result as T;
  }
  
  private serializeAttribute(value: any, attr: AttributeTypeMetadata): any {
    switch (attr.dataType) {
      case 'Date':
      case 'Timestamp':
        return value instanceof Date ? value.toISOString() : value;
      case 'Boolean':
        return Boolean(value);
      case 'Integer':
      case 'Decimal':
        return Number(value);
      case 'String':
        return String(value);
      case 'Enum':
        return value; // Enums are typically strings
      default:
        return value;
    }
  }
  
  private deserializeAttribute(value: any, attr: AttributeTypeMetadata): any {
    switch (attr.dataType) {
      case 'Date':
      case 'Timestamp':
        return value ? new Date(value) : null;
      case 'Boolean':
        return Boolean(value);
      case 'Integer':
      case 'Decimal':
        return Number(value);
      case 'String':
        return value !== null && value !== undefined ? String(value) : null;
      case 'Enum':
        return value; // Enums are typically strings
      default:
        return value;
    }
  }
}
```

### 6. ServiceFactory Implementation

```typescript
/**
 * Factory for creating runtime services
 */
class ServiceFactoryImpl implements ServiceFactory {
  private registry: ServiceRegistry;
  private metadataProvider: MetadataProvider;
  private axiosService: JudoAxiosService;
  private serializationManager: SerializationManager;
  
  constructor(config: ServiceLayerConfig) {
    this.metadataProvider = config.metadataProvider;
    this.axiosService = config.axiosService;
    this.registry = ServiceRegistryImpl.getInstance();
    this.serializationManager = new SerializationManager();
  }
  
  createClassService<T>(classType: ClassTypeMetadata): T {
    const key = ServiceRegistryImpl.classServiceKey(classType.fqName);
    
    if (this.registry.has(key)) {
      return this.registry.get<T>(key)!;
    }
    
    const proxy = new RuntimeServiceProxy<T>(
      classType,
      this.axiosService,
      this.serializationManager,
      'class'
    );
    
    const service = proxy.createProxy();
    this.registry.register(key, service);
    
    return service;
  }
  
  createRelationService<T>(relation: RelationTypeMetadata): T {
    const key = ServiceRegistryImpl.relationServiceKey(relation.fqName);
    
    if (this.registry.has(key)) {
      return this.registry.get<T>(key)!;
    }
    
    const proxy = new RuntimeServiceProxy<T>(
      relation,
      this.axiosService,
      this.serializationManager,
      'relation'
    );
    
    const service = proxy.createProxy();
    this.registry.register(key, service);
    
    return service;
  }
  
  createAccessService(): AccessService {
    const key = ServiceRegistryImpl.accessServiceKey();
    
    if (this.registry.has(key)) {
      return this.registry.get<AccessService>(key)!;
    }
    
    const application = this.metadataProvider.getApplication();
    const proxy = new RuntimeServiceProxy<AccessService>(
      application as any,
      this.axiosService,
      this.serializationManager,
      'access'
    );
    
    const service = proxy.createProxy();
    this.registry.register(key, service);
    
    return service;
  }
}
```

## Usage Examples

### Initialization

```typescript
// Step 1: Parse metadata
const parserContext = await loadParserContext();
const metadataProvider = new ParserMetadataProvider(parserContext);

// Step 2: Create axios service
const axiosService = new JudoAxiosService(axiosInstance, axiosProvider);

// Step 3: Create factory
const serviceFactory = new ServiceFactoryImpl({
  metadataProvider,
  axiosService,
  enableCache: true,
  cacheStrategy: 'memory',
  debug: false,
  logRequests: false,
});

// Step 4: Create services
const userMetadata = metadataProvider.getClassType('demo.User');
const userService = serviceFactory.createClassService<UserService>(userMetadata);

const accessService = serviceFactory.createAccessService();
```

### Service Usage

```typescript
// All service methods work exactly as before
const principal = await accessService.getPrincipal();

const users = await userService.listFriends(currentUser, {
  _mask: '{name,email}',
  _seek: { limit: 10, offset: 0 },
});

await userService.addFriends(currentUser, [newFriend1, newFriend2]);

const updatedUser = await userService.update({
  ...currentUser,
  name: 'New Name',
});
```

## Testing Examples

### Unit Test

```typescript
describe('RuntimeServiceProxy', () => {
  it('should handle refresh method', async () => {
    const metadata = createMockClassMetadata();
    const axiosService = createMockAxiosService();
    const serializationManager = new SerializationManager();
    
    const proxy = new RuntimeServiceProxy(
      metadata,
      axiosService,
      serializationManager,
      'class'
    );
    
    const service = proxy.createProxy();
    
    const target = { __signedIdentifier: 'abc123' };
    const result = await service.refresh(target);
    
    expect(axiosService.axios.post).toHaveBeenCalledWith(
      expect.stringContaining('/~get'),
      {},
      expect.objectContaining({
        headers: expect.objectContaining({
          'X-Judo-SignedIdentifier': 'abc123',
        }),
      })
    );
  });
});
```

### Integration Test

```typescript
describe('ServiceFactory Integration', () => {
  it('should create and use a class service', async () => {
    const factory = createTestFactory();
    const metadata = factory.metadataProvider.getClassType('test.User');
    const service = factory.createClassService(metadata);
    
    const result = await service.getTemplate();
    
    expect(result.data).toBeDefined();
    expect(result.data.name).toBe('');
  });
});
```

## Performance Optimizations

### 1. Pattern Compilation

Compile regex patterns once at initialization:

```typescript
class OptimizedMethodPatternMatcher {
  private compiledPatterns: Array<{
    regex: RegExp;
    pattern: MethodPattern;
  }>;
  
  constructor(serviceType: string) {
    const patterns = this.initializePatterns(serviceType);
    this.compiledPatterns = patterns.map(p => ({
      regex: p.regex,
      pattern: p,
    }));
  }
  
  match(methodName: string): MethodPattern | undefined {
    // Use cached compiled patterns
    for (const { regex, pattern } of this.compiledPatterns) {
      if (regex.test(methodName)) {
        return pattern;
      }
    }
    return undefined;
  }
}
```

### 2. Metadata Caching

Cache expensive metadata lookups:

```typescript
class CachedMetadataProvider implements MetadataProvider {
  private classTypeCache = new Map<string, ClassTypeMetadata>();
  private relationTypeCache = new Map<string, RelationTypeMetadata>();
  
  // ... implement with caching
}
```

### 3. Serializer Pooling

Reuse serializers across invocations:

```typescript
class PooledSerializationManager extends SerializationManager {
  // Serializers are already cached in base implementation
  // Additional optimizations could include:
  // - Warmup: pre-create serializers for common types
  // - Lazy loading: create only when needed
}
```

## Conclusion

This implementation guide provides the detailed pseudocode and patterns needed to build the runtime service layer. The key concepts are:

1. **Metadata-driven**: All behavior is determined by metadata from the parser
2. **Proxy-based**: JavaScript Proxy API handles method interception
3. **Pattern matching**: Method names are matched to operation patterns
4. **Caching**: Aggressive caching for performance
5. **Type safety**: Maintains TypeScript type safety through interfaces

The implementation can be built incrementally, starting with the core infrastructure and gradually adding service types and patterns.

