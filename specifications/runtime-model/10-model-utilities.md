# Model Utilities Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** All other runtime model specifications (02-09)  
**Blocks:** None (utility functions)  
**Priority:** P1

## Overview

This specification defines utility functions, helper methods, and type guards for working with runtime models. These utilities simplify model manipulation, traversal, validation, and transformation.

## Model Traversal

### Find Element by ID

```typescript
function findElementById(
  container: ContainerModel,
  elementId: string
): VisualElementModel | null {
  for (const element of container.visualElements) {
    if (element.id === elementId) {
      return element;
    }
    
    // Recursively search in nested containers
    if (isContainerModel(element)) {
      const found = findElementById(element as ContainerModel, elementId);
      if (found) return found;
    }
  }
  
  return null;
}
```

### Find Element by Name

```typescript
function findElementByName(
  container: ContainerModel,
  elementName: string
): VisualElementModel | null {
  for (const element of container.visualElements) {
    if (element.name === elementName) {
      return element;
    }
    
    if (isContainerModel(element)) {
      const found = findElementByName(element as ContainerModel, elementName);
      if (found) return found;
    }
  }
  
  return null;
}
```

### Get All Elements

```typescript
function getAllElements(container: ContainerModel): VisualElementModel[] {
  const elements: VisualElementModel[] = [];
  
  for (const element of container.visualElements) {
    elements.push(element);
    
    if (isContainerModel(element)) {
      elements.push(...getAllElements(element as ContainerModel));
    }
  }
  
  return elements;
}
```

### Get All Input Elements

```typescript
function getAllInputElements(container: ContainerModel): InputModel[] {
  return getAllElements(container).filter(isInputModel) as InputModel[];
}
```

## Model Validation

### Validate Page Model

```typescript
interface ModelValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

function validatePageModel(page: PageModel): ModelValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Required fields
  if (!page.name) errors.push('Page name is required');
  if (!page.type) errors.push('Page type is required');
  if (!page.container) errors.push('Page container is required');
  
  // Type consistency
  if (page.type !== page.container.type) {
    errors.push('Page type must match container type');
  }
  
  // Dialog configuration
  if (page.openInDialog && !page.dialogSize) {
    warnings.push('Dialog size should be specified for dialog pages');
  }
  
  // Data element
  if (!page.isDashboard && !page.dataElement) {
    errors.push('Non-dashboard pages must have dataElement');
  }
  
  // Container validation
  const containerValidation = validateContainerModel(page.container);
  errors.push(...containerValidation.errors);
  warnings.push(...containerValidation.warnings);
  
  return {
    valid: errors.length === 0,
    errors,
    warnings
  };
}
```

### Validate Container Model

```typescript
function validateContainerModel(container: ContainerModel): ModelValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Required fields
  if (!container.name) errors.push('Container name is required');
  if (!container.type) errors.push('Container type is required');
  
  // Children validation
  if (container.visualElements.length === 0) {
    warnings.push(`Container ${container.name} has no children`);
  }
  
  // Validate each child
  for (const element of container.visualElements) {
    const elementValidation = validateVisualElement(element);
    errors.push(...elementValidation.errors);
    warnings.push(...elementValidation.warnings);
  }
  
  return { valid: errors.length === 0, errors, warnings };
}
```

### Validate Visual Element

```typescript
function validateVisualElement(element: VisualElementModel): ModelValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];
  
  // Required fields
  if (!element.id) errors.push(`Element missing id`);
  if (!element.name) errors.push(`Element ${element.id} missing name`);
  if (!element.type) errors.push(`Element ${element.id} missing type`);
  
  // Column span
  if (element.col < 1 || element.col > 12) {
    errors.push(`Element ${element.name} col must be 1-12, got ${element.col}`);
  }
  
  // Input-specific validation
  if (isInputModel(element)) {
    const input = element as InputModel;
    if (!input.attributeName) {
      errors.push(`Input ${input.name} missing attributeName`);
    }
  }
  
  return { valid: errors.length === 0, errors, warnings };
}
```

## Model Transformation

### Convert to JSON

```typescript
function modelToJSON(model: BaseModel): string {
  return JSON.stringify(model, null, 2);
}
```

### Parse from JSON

```typescript
function parseModelFromJSON<T extends BaseModel>(json: string): T {
  return JSON.parse(json) as T;
}
```

### Deep Clone Model

```typescript
function cloneModel<T extends BaseModel>(model: T): T {
  return JSON.parse(JSON.stringify(model));
}
```

### Merge Models

```typescript
function mergeModels<T extends BaseModel>(base: T, override: Partial<T>): T {
  return {
    ...base,
    ...override,
    annotations: {
      ...base.annotations,
      ...override.annotations
    }
  };
}
```

## Model Queries

### Get Actions by Type

```typescript
function getActionsByType(
  page: PageModel,
  actionType: ActionType
): ActionModel[] {
  return page.actions.filter(action => action.type === actionType);
}
```

### Get Required Fields

```typescript
function getRequiredFields(container: ContainerModel): InputModel[] {
  return getAllInputElements(container).filter(input => input.required);
}
```

### Get Conditional Elements

```typescript
function getConditionalElements(container: ContainerModel): VisualElementModel[] {
  return getAllElements(container).filter(element => 
    element.hiddenBy || element.enabledBy || element.requiredBy
  );
}
```

## Model Statistics

### Count Elements by Type

```typescript
function countElementsByType(container: ContainerModel): Record<string, number> {
  const counts: Record<string, number> = {};
  
  for (const element of getAllElements(container)) {
    counts[element.type] = (counts[element.type] || 0) + 1;
  }
  
  return counts;
}
```

### Calculate Model Depth

```typescript
function calculateModelDepth(container: ContainerModel, currentDepth = 0): number {
  let maxDepth = currentDepth;
  
  for (const element of container.visualElements) {
    if (isContainerModel(element)) {
      const depth = calculateModelDepth(element as ContainerModel, currentDepth + 1);
      maxDepth = Math.max(maxDepth, depth);
    }
  }
  
  return maxDepth;
}
```

### Get Model Summary

```typescript
interface ModelSummary {
  totalElements: number;
  elementsByType: Record<string, number>;
  inputCount: number;
  containerCount: number;
  tableCount: number;
  actionCount: number;
  maxDepth: number;
  hasConditionalElements: boolean;
  hasValidation: boolean;
}

function getModelSummary(page: PageModel): ModelSummary {
  const allElements = getAllElements(page.container);
  
  return {
    totalElements: allElements.length,
    elementsByType: countElementsByType(page.container),
    inputCount: allElements.filter(isInputModel).length,
    containerCount: allElements.filter(isContainerModel).length,
    tableCount: allElements.filter(e => e.type === 'table').length,
    actionCount: page.actions.length,
    maxDepth: calculateModelDepth(page.container),
    hasConditionalElements: getConditionalElements(page.container).length > 0,
    hasValidation: getAllInputElements(page.container).some(
      input => Object.keys(input.validation).length > 0
    )
  };
}
```

## Type Guards

Already defined in core-types, but for reference:

```typescript
// Element type guards
function isInputModel(element: VisualElementModel): element is InputModel;
function isContainerModel(element: VisualElementModel): element is ContainerModel;
function isTableModel(element: VisualElementModel): element is TableModel;
function isLinkModel(element: VisualElementModel): element is LinkModel;
function isButtonModel(element: VisualElementModel): element is ButtonModel;

// Data type guards
function isNumericType(type: DataType): boolean;
function isTemporalType(type: DataType): boolean;
function isStringType(type: DataType): boolean;
function isBooleanType(type: DataType): boolean;

// Action type guards
function isCRUDAction(action: ActionModel): boolean;
function isNavigationAction(action: ActionModel): boolean;
function isRelationAction(action: ActionModel): boolean;
function isOperationAction(action: ActionModel): boolean;
```

## Path Utilities

### Get Element Path

```typescript
function getElementPath(
  container: ContainerModel,
  elementId: string
): string | null {
  function search(
    current: ContainerModel,
    path: string[]
  ): string[] | null {
    for (const element of current.visualElements) {
      const currentPath = [...path, element.name];
      
      if (element.id === elementId) {
        return currentPath;
      }
      
      if (isContainerModel(element)) {
        const found = search(element as ContainerModel, currentPath);
        if (found) return found;
      }
    }
    
    return null;
  }
  
  const path = search(container, []);
  return path ? path.join('.') : null;
}
```

### Resolve Element by Path

```typescript
function resolveElementByPath(
  container: ContainerModel,
  path: string
): VisualElementModel | null {
  const parts = path.split('.');
  let current: ContainerModel | VisualElementModel = container;
  
  for (const part of parts) {
    if (!isContainerModel(current)) {
      return null;
    }
    
    const element = (current as ContainerModel).visualElements.find(
      e => e.name === part
    );
    
    if (!element) {
      return null;
    }
    
    current = element;
  }
  
  return current as VisualElementModel;
}
```

## Model Comparison

### Compare Models

```typescript
function compareModels<T extends BaseModel>(
  model1: T,
  model2: T
): { equal: boolean; differences: string[] } {
  const differences: string[] = [];
  
  function compareObjects(obj1: any, obj2: any, path: string = '') {
    const keys = new Set([...Object.keys(obj1), ...Object.keys(obj2)]);
    
    for (const key of keys) {
      const fullPath = path ? `${path}.${key}` : key;
      
      if (!(key in obj1)) {
        differences.push(`Missing in model1: ${fullPath}`);
      } else if (!(key in obj2)) {
        differences.push(`Missing in model2: ${fullPath}`);
      } else if (typeof obj1[key] !== typeof obj2[key]) {
        differences.push(`Type mismatch at ${fullPath}`);
      } else if (typeof obj1[key] === 'object' && obj1[key] !== null) {
        compareObjects(obj1[key], obj2[key], fullPath);
      } else if (obj1[key] !== obj2[key]) {
        differences.push(`Value mismatch at ${fullPath}: ${obj1[key]} !== ${obj2[key]}`);
      }
    }
  }
  
  compareObjects(model1, model2);
  
  return {
    equal: differences.length === 0,
    differences
  };
}
```

## Export All Utilities

```typescript
export const ModelUtils = {
  // Traversal
  findElementById,
  findElementByName,
  getAllElements,
  getAllInputElements,
  
  // Validation
  validatePageModel,
  validateContainerModel,
  validateVisualElement,
  
  // Transformation
  modelToJSON,
  parseModelFromJSON,
  cloneModel,
  mergeModels,
  
  // Queries
  getActionsByType,
  getRequiredFields,
  getConditionalElements,
  
  // Statistics
  countElementsByType,
  calculateModelDepth,
  getModelSummary,
  
  // Paths
  getElementPath,
  resolveElementByPath,
  
  // Comparison
  compareModels
};
```

## Related Specifications

**Runtime Model:**
- `01-core-types.md` - Type guards and base types
- `02-page-model.md` - Page model structure
- `03-container-model.md` - Container traversal
- `04-visual-element-model.md` - Element queries

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

