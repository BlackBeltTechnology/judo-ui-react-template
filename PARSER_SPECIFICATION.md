# TypeScript Model Parser Specification

## Overview

This specification describes a three-pass parser for loading EMF/Ecore-based JSON model files into TypeScript runtime objects. The parser handles model serialization with `@id` identifiers, `eClass` type information, and `$ref` cross-references.

## Architecture

The parser uses a three-pass approach:

1. **Pass 1: Index Building** - Traverse the complete model and create an ID-to-element map
2. **Pass 2: Object Instantiation** - Create implementation class instances for all indexed elements
3. **Pass 3: Reference Wiring** - Resolve all `$ref` references to their target objects

## Data Structures

### 1. Element Index Map

```typescript
type ElementIndex = Map<string, any>;
```

**Purpose**: Maps each `@id` to its raw JSON element for fast lookup.

**Key**: The `@id` string value (e.g., `"God/(esm/_l2GwYM8nEe6U3KSieLrWmg)/Application"`)

**Value**: The raw JSON object containing the element data

### 2. Instance Map

```typescript
type InstanceMap = Map<string, any>;
```

**Purpose**: Maps each `@id` to its instantiated implementation class instance.

**Key**: The `@id` string value

**Value**: An instance of the appropriate implementation class (e.g., `ApplicationImpl`, `TableImpl`)

### 3. EClass to Implementation Class Map

```typescript
type EClassMapping = Map<string, new (data: any) => any>;
```

**Purpose**: Fast lookup from eClass URI to implementation class constructor.

**Key**: The eClass URI (e.g., `"http://blackbelt.hu/judo/meta/ui#//Application"`)

**Value**: The constructor function for the implementation class

### 4. Meta-Model Schema Map

```typescript
type PropertyTypeInfo = {
  eClass: string;
  isMany: boolean;
};

type MetaModelSchema = Map<string, Map<string, PropertyTypeInfo>>;
```

**Purpose**: Maps each eClass to its property definitions, allowing type derivation for nodes without explicit `eClass` attributes.

**Key**: The eClass URI of the parent type

**Value**: A Map of property names to their type information

**Example Entries**:
```typescript
const metaModelSchema = new Map<string, Map<string, PropertyTypeInfo>>([
  ['http://blackbelt.hu/judo/meta/ui#//Application', new Map([
    ['navigationController', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//NavigationController', 
      isMany: false 
    }],
    ['pages', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//PageDefinition', 
      isMany: true 
    }],
  ])],
  ['http://blackbelt.hu/judo/meta/ui#//PageContainer', new Map([
    ['table', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//Table', 
      isMany: false 
    }],
    ['actions', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//ActionDefinition', 
      isMany: true 
    }],
  ])],
  // ... more type definitions
]);
```

**Usage**: When encountering a node without an `eClass` attribute, the parser can look up the parent's type and the property name to derive the expected type.

**Example Entries**:
```typescript
const eClassMap = new Map<string, new (data: any) => any>([
  // Core Application
  ['http://blackbelt.hu/judo/meta/ui#//Application', ApplicationImpl],
  
  // Pages
  ['http://blackbelt.hu/judo/meta/ui#//PageDefinition', PageDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//PageContainer', PageContainerImpl],
  
  // Visual Elements - Basic
  ['http://blackbelt.hu/judo/meta/ui#//Button', ButtonImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ButtonGroup', ButtonGroupImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Checkbox', CheckboxImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Flex', FlexImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Formatted', FormattedImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Link', LinkImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Table', TableImpl],
  
  // Visual Elements - Input Types
  ['http://blackbelt.hu/judo/meta/ui#//BinaryTypeInput', BinaryTypeInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//DateInput', DateInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//DateTimeInput', DateTimeInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//NumericInput', NumericInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//TextArea', TextAreaImpl],
  ['http://blackbelt.hu/judo/meta/ui#//TextInput', TextInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//TimeInput', TimeInputImpl],
  
  // Visual Elements - Enumeration
  ['http://blackbelt.hu/judo/meta/ui#//EnumerationCombo', EnumerationComboImpl],
  ['http://blackbelt.hu/judo/meta/ui#//EnumerationToggleButtonbar', EnumerationToggleButtonbarImpl],
  
  // Actions - Basic CRUD
  ['http://blackbelt.hu/judo/meta/ui#//BackActionDefinition', BackActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//CancelActionDefinition', CancelActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ClearActionDefinition', ClearActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//CreateActionDefinition', CreateActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//DeleteActionDefinition', DeleteActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//FilterActionDefinition', FilterActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//RefreshActionDefinition', RefreshActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//RemoveActionDefinition', RemoveActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//SetActionDefinition', SetActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//UnsetActionDefinition', UnsetActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//UpdateActionDefinition', UpdateActionDefinitionImpl],
  
  // Actions - Bulk Operations
  ['http://blackbelt.hu/judo/meta/ui#//BulkCallOperationActionDefinition', BulkCallOperationActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//BulkDeleteActionDefinition', BulkDeleteActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//BulkRemoveActionDefinition', BulkRemoveActionDefinitionImpl],
  
  // Actions - Open/Navigate
  ['http://blackbelt.hu/judo/meta/ui#//OpenAddSelectorActionDefinition', OpenAddSelectorActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenCreateFormActionDefinition', OpenCreateFormActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenOperationInputFormActionDefinition', OpenOperationInputFormActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenOperationInputSelectorActionDefinition', OpenOperationInputSelectorActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenPageActionDefinition', OpenPageActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenSetSelectorActionDefinition', OpenSetSelectorActionDefinitionImpl],
  
  // Actions - Operation Call
  ['http://blackbelt.hu/judo/meta/ui#//InputFormCallOperationActionDefinition', InputFormCallOperationActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//InputSelectorCallOperationActionDefinition', InputSelectorCallOperationActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ParameterlessCallOperationActionDefinition', ParameterlessCallOperationActionDefinitionImpl],
  
  // Actions - Autocomplete
  ['http://blackbelt.hu/judo/meta/ui#//AutocompleteRangeActionDefinition', AutocompleteRangeActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//AutocompleteSetActionDefinition', AutocompleteSetActionDefinitionImpl],
  
  // Actions - Other
  ['http://blackbelt.hu/judo/meta/ui#//CustomActionDefinition', CustomActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ExportActionDefinition', ExportActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//GetTemplateActionDefinition', GetTemplateActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//InlineCreateRowActionDefinition', InlineCreateRowActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//PreFetchActionDefinition', PreFetchActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//RowDeleteActionDefinition', RowDeleteActionDefinitionImpl],
  
  // Data Types - Core
  ['http://blackbelt.hu/judo/meta/ui#//data/AttributeType', AttributeTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/ClassType', ClassTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/OperationParameterType', OperationParameterTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/OperationType', OperationTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/RelationType', RelationTypeImpl],
  
  // Data Types - Primitives
  ['http://blackbelt.hu/judo/meta/ui#//data/BinaryType', BinaryTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/BooleanType', BooleanTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/DateType', DateTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/EnumerationMember', EnumerationMemberImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/EnumerationType', EnumerationTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/MimeType', MimeTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/NumericType', NumericTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/StringType', StringTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/TimeType', TimeTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/TimestampType', TimestampTypeImpl],
]);
```

## JSON Model Structure

### Element with @id

Elements that have an `@id` property are concrete model instances that can be referenced:

```json
{
  "eClass": "http://blackbelt.hu/judo/meta/ui#//Application",
  "@id": "God/(esm/_l2GwYM8nEe6U3KSieLrWmg)/Application",
  "name": "Application",
  "fqName": "God::Application",
  "navigationController": {
    "eClass": "http://blackbelt.hu/judo/meta/ui#//NavigationController",
    "@id": "God/(esm/_l2GwYM8nEe6U3KSieLrWmg)/NavigationController",
    "name": "NavigationController",
    "items": []
  }
}
```

### Reference with $ref

References use `eClass` and `$ref` to point to elements defined elsewhere:

```json
{
  "eClass": "http://blackbelt.hu/judo/meta/ui#//PageDefinition",
  "$ref": "God/(esm/_4pyPkM_cEe6fibzd7gNETg)/AccessTablePageDefinition"
}
```

### Inline Elements

Some elements are defined inline without `@id` (usually simple value objects or contained elements that aren't referenced):

```json
{
  "eClass": "http://blackbelt.hu/judo/meta/ui#//Icon",
  "iconName": "table"
}
```

### Elements Without eClass (Type Derivation)

Some elements may not have an explicit `eClass` attribute. The parser can derive the type from the parent's meta-model definition:

```json
{
  "eClass": "http://blackbelt.hu/judo/meta/ui#//PageContainer",
  "@id": "God/(esm/_XYZ)/MyPageContainer",
  "table": {
    // No eClass specified, but parser derives it from parent's "table" property definition
    "@id": "God/(esm/_ABC)/MyTable",
    "columns": []
  }
}
```

In this example:
1. The parent is a `PageContainer` with eClass `http://blackbelt.hu/judo/meta/ui#//PageContainer`
2. The child is in the `table` property
3. The parser looks up `PageContainer` in the meta-model schema
4. It finds that the `table` property has type `http://blackbelt.hu/judo/meta/ui#//Table`
5. The parser automatically adds this eClass to the child object before indexing

## Parser Implementation

### Pass 1: Index Building with Type Derivation

**Purpose**: Build a complete index of all elements that have `@id` properties, and derive missing `eClass` attributes from parent context.

**Algorithm**:

```typescript
function buildIndex(
  jsonModel: any, 
  metaModelSchema: MetaModelSchema
): ElementIndex {
  const index = new Map<string, any>();
  
  function traverse(
    obj: any, 
    parentEClass?: string, 
    propertyName?: string
  ): void {
    if (obj === null || typeof obj !== 'object') {
      return;
    }
    
    // Derive eClass if missing but parent context is available
    if (!obj['eClass'] && parentEClass && propertyName) {
      const parentSchema = metaModelSchema.get(parentEClass);
      if (parentSchema) {
        const propertyInfo = parentSchema.get(propertyName);
        if (propertyInfo) {
          // Add derived eClass to the object
          obj['eClass'] = propertyInfo.eClass;
        }
      }
    }
    
    // If this object has an @id, add it to the index
    if (obj['@id']) {
      index.set(obj['@id'], obj);
    }
    
    // Get current eClass for recursive traversal
    const currentEClass = obj['eClass'];
    
    // Recursively traverse all properties
    if (Array.isArray(obj)) {
      for (const item of obj) {
        traverse(item, parentEClass, propertyName);
      }
    } else {
      for (const key in obj) {
        if (key !== '$ref') { // Don't traverse into references
          traverse(obj[key], currentEClass, key);
        }
      }
    }
  }
  
  traverse(jsonModel);
  return index;
}
```

**Output**: A map of all `@id` values to their JSON objects, with missing `eClass` attributes filled in through derivation.

### Pass 2: Object Instantiation

**Purpose**: Create implementation class instances for all indexed elements.

**Algorithm**:

```typescript
function instantiateObjects(
  index: ElementIndex,
  eClassMap: EClassMapping
): InstanceMap {
  const instances = new Map<string, any>();
  
  for (const [id, element] of index.entries()) {
    const eClass = element.eClass;
    
    if (!eClass) {
      console.warn(`Element ${id} has no eClass property (derivation failed in Pass 1)`);
      continue;
    }
    
    const Constructor = eClassMap.get(eClass);
    
    if (!Constructor) {
      console.warn(`No implementation class found for eClass: ${eClass}`);
      continue;
    }
    
    // Create instance with raw data (references not yet resolved)
    const instance = new Constructor(element);
    instances.set(id, instance);
  }
  
  return instances;
}
```

**Details**:
- Each element in the index is instantiated using its corresponding implementation class
- The constructor receives the raw JSON data
- The `eClass` attribute should have been derived in Pass 1 if it was missing
- At this stage, `$ref` properties are not yet resolved - they contain the reference strings
- All primitive properties (strings, numbers, booleans) are set
- Array and object properties that don't have references are set

### Pass 3: Reference Wiring

**Purpose**: Resolve all `$ref` references to their actual object instances.

**Algorithm**:

```typescript
function wireReferences(
  jsonModel: any,
  instanceMap: InstanceMap
): any {
  function resolveReferences(obj: any, parent?: any, key?: string): any {
    if (obj === null || typeof obj !== 'object') {
      return obj;
    }
    
    // If this is a reference object, resolve it
    if (obj['$ref'] && obj['eClass']) {
      const refId = obj['$ref'];
      const instance = instanceMap.get(refId);
      
      if (!instance) {
        console.warn(`Reference not found: ${refId}`);
        return obj; // Keep original if reference can't be resolved
      }
      
      return instance;
    }
    
    // If this object has an @id, return its instance
    if (obj['@id']) {
      const instance = instanceMap.get(obj['@id']);
      return instance || obj;
    }
    
    // For arrays, resolve each element
    if (Array.isArray(obj)) {
      return obj.map((item, index) => resolveReferences(item, obj, String(index)));
    }
    
    // For objects, resolve each property
    const resolved: any = {};
    for (const propKey in obj) {
      resolved[propKey] = resolveReferences(obj[propKey], obj, propKey);
    }
    
    return resolved;
  }
  
  return resolveReferences(jsonModel);
}
```

**Details**:
- Traverses the entire model structure
- When encountering a `$ref`, looks up the target in the instance map
- When encountering an `@id`, returns the instance from the instance map
- Handles nested structures and arrays
- After this pass, all references point to actual object instances

### Main Parser Function

**Complete parser integration**:

```typescript
class ModelParser {
  private eClassMap: EClassMapping;
  private metaModelSchema: MetaModelSchema;
  private elementIndex: ElementIndex;
  private instanceMap: InstanceMap;
  
  constructor(eClassMap: EClassMapping, metaModelSchema: MetaModelSchema) {
    this.eClassMap = eClassMap;
    this.metaModelSchema = metaModelSchema;
    this.elementIndex = new Map();
    this.instanceMap = new Map();
  }
  
  parse(jsonModel: any): any {
    // Pass 1: Build index with type derivation
    console.log('Pass 1: Building element index with type derivation...');
    this.elementIndex = this.buildIndex(jsonModel, this.metaModelSchema);
    console.log(`Indexed ${this.elementIndex.size} elements`);
    
    // Pass 2: Instantiate objects
    console.log('Pass 2: Instantiating objects...');
    this.instanceMap = this.instantiateObjects(this.elementIndex, this.eClassMap);
    console.log(`Instantiated ${this.instanceMap.size} objects`);
    
    // Pass 3: Wire references
    console.log('Pass 3: Wiring references...');
    const rootModel = this.wireReferences(jsonModel, this.instanceMap);
    console.log('Model parsing complete');
    
    return rootModel;
  }
  
  // ... implementation methods as described above
  
  /**
   * Get an instance by its @id
   */
  getInstance(id: string): any | undefined {
    return this.instanceMap.get(id);
  }
  
  /**
   * Get all instances of a specific type
   */
  getInstancesByType(eClass: string): any[] {
    const instances: any[] = [];
    for (const [id, instance] of this.instanceMap.entries()) {
      const element = this.elementIndex.get(id);
      if (element && element.eClass === eClass) {
        instances.push(instance);
      }
    }
    return instances;
  }
}
```

## Usage Example

```typescript
import { ModelParser } from './model-parser';
import { eClassMap } from './eclass-mapping';
import { metaModelSchema } from './meta-model-schema';
import modelJson from './model.json';

// Create parser with eClass mapping and meta-model schema
const parser = new ModelParser(eClassMap, metaModelSchema);

// Parse the model
const model = parser.parse(modelJson);

// Access the root application
console.log('Application name:', model.name);

// Get specific instance by ID
const pageContainer = parser.getInstance('God/(esm/_Dqda7M8xEe6U3KSieLrWmg)/TransferObjectTablePageContainer');

// Get all instances of a type
const allTables = parser.getInstancesByType('http://blackbelt.hu/judo/meta/ui#//Table');
console.log(`Found ${allTables.length} tables in the model`);
```

## EClass Mapping and Meta-Model Schema Generation

The eClass mapping and meta-model schema should be generated from the meta-model or maintained manually.

### EClass Mapping

Here's a suggested structure for the eClass mapping:

```typescript
// eclass-mapping.ts
import {
  // Core
  ApplicationImpl,
  
  // Pages
  PageDefinitionImpl,
  PageContainerImpl,
  
  // Visual Elements - Basic
  ButtonImpl,
  ButtonGroupImpl,
  CheckboxImpl,
  FlexImpl,
  FormattedImpl,
  LinkImpl,
  TableImpl,
  
  // Visual Elements - Input Types
  BinaryTypeInputImpl,
  DateInputImpl,
  DateTimeInputImpl,
  NumericInputImpl,
  TextAreaImpl,
  TextInputImpl,
  TimeInputImpl,
  
  // Visual Elements - Enumeration
  EnumerationComboImpl,
  EnumerationToggleButtonbarImpl,
  
  // Actions - Basic CRUD
  BackActionDefinitionImpl,
  CancelActionDefinitionImpl,
  ClearActionDefinitionImpl,
  CreateActionDefinitionImpl,
  DeleteActionDefinitionImpl,
  FilterActionDefinitionImpl,
  RefreshActionDefinitionImpl,
  RemoveActionDefinitionImpl,
  SetActionDefinitionImpl,
  UnsetActionDefinitionImpl,
  UpdateActionDefinitionImpl,
  
  // Actions - Bulk Operations
  BulkCallOperationActionDefinitionImpl,
  BulkDeleteActionDefinitionImpl,
  BulkRemoveActionDefinitionImpl,
  
  // Actions - Open/Navigate
  OpenAddSelectorActionDefinitionImpl,
  OpenCreateFormActionDefinitionImpl,
  OpenOperationInputFormActionDefinitionImpl,
  OpenOperationInputSelectorActionDefinitionImpl,
  OpenPageActionDefinitionImpl,
  OpenSetSelectorActionDefinitionImpl,
  
  // Actions - Operation Call
  InputFormCallOperationActionDefinitionImpl,
  InputSelectorCallOperationActionDefinitionImpl,
  ParameterlessCallOperationActionDefinitionImpl,
  
  // Actions - Autocomplete
  AutocompleteRangeActionDefinitionImpl,
  AutocompleteSetActionDefinitionImpl,
  
  // Actions - Other
  CustomActionDefinitionImpl,
  ExportActionDefinitionImpl,
  GetTemplateActionDefinitionImpl,
  InlineCreateRowActionDefinitionImpl,
  PreFetchActionDefinitionImpl,
  RowDeleteActionDefinitionImpl,
  
  // Data Types - Core
  AttributeTypeImpl,
  ClassTypeImpl,
  OperationParameterTypeImpl,
  OperationTypeImpl,
  RelationTypeImpl,
  
  // Data Types - Primitives
  BinaryTypeImpl,
  BooleanTypeImpl,
  DateTypeImpl,
  EnumerationMemberImpl,
  EnumerationTypeImpl,
  MimeTypeImpl,
  NumericTypeImpl,
  StringTypeImpl,
  TimeTypeImpl,
  TimestampTypeImpl,
} from './model-runtime/implementations';

export const eClassMap = new Map<string, new (data: any) => any>([
  // Core Application
  ['http://blackbelt.hu/judo/meta/ui#//Application', ApplicationImpl],
  
  // Pages
  ['http://blackbelt.hu/judo/meta/ui#//PageDefinition', PageDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//PageContainer', PageContainerImpl],
  
  // Visual Elements - Basic
  ['http://blackbelt.hu/judo/meta/ui#//Button', ButtonImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ButtonGroup', ButtonGroupImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Checkbox', CheckboxImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Flex', FlexImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Formatted', FormattedImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Link', LinkImpl],
  ['http://blackbelt.hu/judo/meta/ui#//Table', TableImpl],
  
  // Visual Elements - Input Types
  ['http://blackbelt.hu/judo/meta/ui#//BinaryTypeInput', BinaryTypeInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//DateInput', DateInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//DateTimeInput', DateTimeInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//NumericInput', NumericInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//TextArea', TextAreaImpl],
  ['http://blackbelt.hu/judo/meta/ui#//TextInput', TextInputImpl],
  ['http://blackbelt.hu/judo/meta/ui#//TimeInput', TimeInputImpl],
  
  // Visual Elements - Enumeration
  ['http://blackbelt.hu/judo/meta/ui#//EnumerationCombo', EnumerationComboImpl],
  ['http://blackbelt.hu/judo/meta/ui#//EnumerationToggleButtonbar', EnumerationToggleButtonbarImpl],
  
  // Actions - Basic CRUD
  ['http://blackbelt.hu/judo/meta/ui#//BackActionDefinition', BackActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//CancelActionDefinition', CancelActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ClearActionDefinition', ClearActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//CreateActionDefinition', CreateActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//DeleteActionDefinition', DeleteActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//FilterActionDefinition', FilterActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//RefreshActionDefinition', RefreshActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//RemoveActionDefinition', RemoveActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//SetActionDefinition', SetActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//UnsetActionDefinition', UnsetActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//UpdateActionDefinition', UpdateActionDefinitionImpl],
  
  // Actions - Bulk Operations
  ['http://blackbelt.hu/judo/meta/ui#//BulkCallOperationActionDefinition', BulkCallOperationActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//BulkDeleteActionDefinition', BulkDeleteActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//BulkRemoveActionDefinition', BulkRemoveActionDefinitionImpl],
  
  // Actions - Open/Navigate
  ['http://blackbelt.hu/judo/meta/ui#//OpenAddSelectorActionDefinition', OpenAddSelectorActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenCreateFormActionDefinition', OpenCreateFormActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenOperationInputFormActionDefinition', OpenOperationInputFormActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenOperationInputSelectorActionDefinition', OpenOperationInputSelectorActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenPageActionDefinition', OpenPageActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//OpenSetSelectorActionDefinition', OpenSetSelectorActionDefinitionImpl],
  
  // Actions - Operation Call
  ['http://blackbelt.hu/judo/meta/ui#//InputFormCallOperationActionDefinition', InputFormCallOperationActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//InputSelectorCallOperationActionDefinition', InputSelectorCallOperationActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ParameterlessCallOperationActionDefinition', ParameterlessCallOperationActionDefinitionImpl],
  
  // Actions - Autocomplete
  ['http://blackbelt.hu/judo/meta/ui#//AutocompleteRangeActionDefinition', AutocompleteRangeActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//AutocompleteSetActionDefinition', AutocompleteSetActionDefinitionImpl],
  
  // Actions - Other
  ['http://blackbelt.hu/judo/meta/ui#//CustomActionDefinition', CustomActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//ExportActionDefinition', ExportActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//GetTemplateActionDefinition', GetTemplateActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//InlineCreateRowActionDefinition', InlineCreateRowActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//PreFetchActionDefinition', PreFetchActionDefinitionImpl],
  ['http://blackbelt.hu/judo/meta/ui#//RowDeleteActionDefinition', RowDeleteActionDefinitionImpl],
  
  // Data Types - Core
  ['http://blackbelt.hu/judo/meta/ui#//data/AttributeType', AttributeTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/ClassType', ClassTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/OperationParameterType', OperationParameterTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/OperationType', OperationTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/RelationType', RelationTypeImpl],
  
  // Data Types - Primitives
  ['http://blackbelt.hu/judo/meta/ui#//data/BinaryType', BinaryTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/BooleanType', BooleanTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/DateType', DateTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/EnumerationMember', EnumerationMemberImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/EnumerationType', EnumerationTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/MimeType', MimeTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/NumericType', NumericTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/StringType', StringTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/TimeType', TimeTypeImpl],
  ['http://blackbelt.hu/judo/meta/ui#//data/TimestampType', TimestampTypeImpl],
]);
```

### Meta-Model Schema

The meta-model schema defines the property types for each eClass, enabling type derivation for nodes without explicit `eClass` attributes:

```typescript
// meta-model-schema.ts
import { MetaModelSchema, PropertyTypeInfo } from './model-parser';

export const metaModelSchema: MetaModelSchema = new Map([
  // Application
  ['http://blackbelt.hu/judo/meta/ui#//Application', new Map<string, PropertyTypeInfo>([
    ['navigationController', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//NavigationController', 
      isMany: false 
    }],
    ['pages', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//PageDefinition', 
      isMany: true 
    }],
  ])],
  
  // PageDefinition
  ['http://blackbelt.hu/judo/meta/ui#//PageDefinition', new Map<string, PropertyTypeInfo>([
    ['container', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//PageContainer', 
      isMany: false 
    }],
    ['dataElement', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//data/ClassType', 
      isMany: false 
    }],
  ])],
  
  // PageContainer
  ['http://blackbelt.hu/judo/meta/ui#//PageContainer', new Map<string, PropertyTypeInfo>([
    ['table', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//Table', 
      isMany: false 
    }],
    ['actions', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//ActionDefinition', 
      isMany: true 
    }],
    ['children', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//VisualElement', 
      isMany: true 
    }],
  ])],
  
  // Table
  ['http://blackbelt.hu/judo/meta/ui#//Table', new Map<string, PropertyTypeInfo>([
    ['columns', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//VisualElement', 
      isMany: true 
    }],
    ['rowActions', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//ActionDefinition', 
      isMany: true 
    }],
    ['tableActions', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//ActionDefinition', 
      isMany: true 
    }],
    ['dataElement', { 
      eClass: 'http://blackbelt.hu/judo/meta/ui#//data/RelationType', 
      isMany: false 
    }],
  ])],
  
  // Add more type definitions as needed...
  // This should cover all eClasses that have containment references
]);
```

**Notes on Schema Generation**:
- Only include properties that are **containment references** (not attributes or cross-references)
- The `isMany` flag indicates if the property is a collection (array)
- Abstract types (like `VisualElement`, `ActionDefinition`) can be used when multiple concrete types are possible
- The schema should be generated from the Ecore meta-model to ensure accuracy and completeness

## Performance Considerations

1. **Pass 1 (Indexing)**: O(n) where n is the total number of objects in the model
2. **Pass 2 (Instantiation)**: O(m) where m is the number of indexed elements
3. **Pass 3 (Wiring)**: O(n) with O(1) lookups for each reference

**Total Complexity**: O(n) where n is the total model size

**Memory Usage**: 
- Element Index: References to original JSON objects
- Instance Map: Implementation class instances
- Peak usage is approximately 2-3x the JSON size during parsing

## Error Handling

The parser should handle the following error cases:

1. **Missing @id**: Elements without `@id` that are referenced by `$ref`
2. **Missing eClass**: Elements without type information (after derivation attempt fails)
   - The parser first attempts to derive the `eClass` from parent context
   - If derivation fails (no parent context or property not in schema), a warning is issued
3. **Unknown eClass**: eClass values not in the mapping
4. **Broken References**: `$ref` values that don't exist in the index
5. **Circular References**: Handled naturally by the three-pass approach

## Extensions

### Validation Pass

An optional fourth pass could validate the model:

```typescript
function validateModel(rootModel: any, instanceMap: InstanceMap): ValidationResult {
  const errors: ValidationError[] = [];
  
  // Check for required properties
  // Validate types
  // Check cardinality constraints
  // Validate enum values
  
  return { valid: errors.length === 0, errors };
}
```

### Lazy Loading

For very large models, implement lazy loading:

```typescript
class LazyReference {
  constructor(private id: string, private instanceMap: InstanceMap) {}
  
  get(): any {
    return this.instanceMap.get(this.id);
  }
}
```

### Model Serialization

The reverse process - serializing instances back to JSON:

```typescript
function serializeModel(instance: any, instanceMap: InstanceMap): any {
  // Create JSON with @id for indexed objects
  // Create $ref for referenced objects
  // Serialize primitive properties
}
```

## Testing Strategy

1. **Unit Tests**: Test each pass independently
2. **Integration Tests**: Test complete parsing workflow
3. **Fixture Models**: Use sample models from itest directory
4. **Performance Tests**: Test with large models (10,000+ elements)
5. **Error Cases**: Test malformed models and edge cases

## Dependencies

- TypeScript 4.5+
- No external dependencies required (pure TypeScript)
- Optional: `zod` or `io-ts` for runtime validation

## Implementation Checklist

- [ ] Define TypeScript interfaces for parser
- [ ] Implement Pass 1: Index building with type derivation
- [ ] Implement Pass 2: Object instantiation
- [ ] Implement Pass 3: Reference wiring
- [ ] Create eClass mapping from meta-model
- [ ] Create meta-model schema for type derivation
- [ ] Add error handling and logging
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Add performance benchmarks
- [ ] Document API
- [ ] Add validation pass (optional)
- [ ] Add serialization support (optional)

## Meta-Model Integration

Based on the actual model files, the parser should handle these **65 element types**:

### Core Structure (1)
- Application (root)

### Pages (2)
- PageDefinition
- PageContainer

### Visual Elements - Basic (7)
- Button
- ButtonGroup
- Checkbox
- Flex (layout)
- Formatted
- Link
- Table

### Visual Elements - Input Types (7)
- BinaryTypeInput
- DateInput
- DateTimeInput
- NumericInput
- TextArea
- TextInput
- TimeInput

### Visual Elements - Enumeration (2)
- EnumerationCombo
- EnumerationToggleButtonbar

### Actions - Basic CRUD (11)
- BackActionDefinition
- CancelActionDefinition
- ClearActionDefinition
- CreateActionDefinition
- DeleteActionDefinition
- FilterActionDefinition
- RefreshActionDefinition
- RemoveActionDefinition
- SetActionDefinition
- UnsetActionDefinition
- UpdateActionDefinition

### Actions - Bulk Operations (3)
- BulkCallOperationActionDefinition
- BulkDeleteActionDefinition
- BulkRemoveActionDefinition

### Actions - Open/Navigate (6)
- OpenAddSelectorActionDefinition
- OpenCreateFormActionDefinition
- OpenOperationInputFormActionDefinition
- OpenOperationInputSelectorActionDefinition
- OpenPageActionDefinition
- OpenSetSelectorActionDefinition

### Actions - Operation Call (3)
- InputFormCallOperationActionDefinition
- InputSelectorCallOperationActionDefinition
- ParameterlessCallOperationActionDefinition

### Actions - Autocomplete (2)
- AutocompleteRangeActionDefinition
- AutocompleteSetActionDefinition

### Actions - Other (6)
- CustomActionDefinition
- ExportActionDefinition
- GetTemplateActionDefinition
- InlineCreateRowActionDefinition
- PreFetchActionDefinition
- RowDeleteActionDefinition

### Data Types - Core (5)
- AttributeType
- ClassType
- OperationParameterType
- OperationType
- RelationType

### Data Types - Primitives (10)
- BinaryType
- BooleanType
- DateType
- EnumerationMember
- EnumerationType
- MimeType
- NumericType
- StringType
- TimeType
- TimestampType

**Total: 65 eClasses**

Each of these should have a corresponding implementation class in the eClass mapping.

## Conclusion

This three-pass parser provides an efficient and maintainable approach to loading EMF-based JSON models into TypeScript. The separation of concerns between indexing, instantiation, and reference resolution makes the code easy to understand, test, and extend.

