# Core Types Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** All metamodel and data model specifications  
**Blocks:** All other runtime model specifications  

## Overview

This specification defines the foundational TypeScript types and interfaces used across all runtime models. These core types provide the base structure for pages, containers, visual elements, actions, and validation, ensuring consistency and type safety throughout the runtime model system.

## Core Base Interfaces

### BaseModel

Base interface inherited by all runtime models:

```typescript
interface BaseModel {
  id: string;                           // Unique identifier (from name or sourceId)
  name: string;                         // Element name
  sourceId?: string;                    // Original XMIID for traceability
  fqn?: string;                         // Fully qualified name
  annotations?: Record<string, string>; // Custom metadata
}
```

### IconModel

Icon representation:

```typescript
interface IconModel {
  name: string;                         // Icon identifier (e.g., 'person', 'edit')
  color?: string;                       // Color override ('primary', 'error', '#FF0000')
}
```

### SizeModel

Fixed dimensions:

```typescript
interface SizeModel {
  width?: number;                       // Width in pixels
  height?: number;                      // Height in pixels
}
```

## Layout & Positioning Types

### GridPosition

Grid layout positioning:

```typescript
interface GridPosition {
  col: number;                          // Column span (1-12)
  row?: number;                         // Row position
}
```

### StretchType

Element stretch behavior:

```typescript
type StretchType = 'none' | 'horizontal' | 'vertical' | 'both';
```

### FitType

Space filling strategy:

```typescript
type FitType = 'none' | 'loose' | 'tight';
```

### FlexDirection

Flexbox direction:

```typescript
type FlexDirection = 'horizontal' | 'vertical';
```

### AlignmentType

Alignment options:

```typescript
type MainAxisAlignment = 'start' | 'center' | 'end' | 'space-between' | 'space-around' | 'space-evenly';
type CrossAxisAlignment = 'start' | 'center' | 'end' | 'stretch' | 'baseline';
```

## Data Types

### DataType

Primitive and enum types:

```typescript
type DataType = 
  | 'string'
  | 'text'
  | 'integer'
  | 'long'
  | 'decimal'
  | 'double'
  | 'date'
  | 'time'
  | 'dateTime'
  | 'timestamp'
  | 'boolean'
  | 'binary'
  | string;  // For enum type names
```

### TypeScriptType

Mapped TypeScript types:

```typescript
type TypeScriptType = 
  | 'string'
  | 'number'
  | 'boolean'
  | 'Date'
  | 'Blob'
  | 'File'
  | string;  // For enum names
```

## Conditional Rendering

### ConditionalProperties

Properties for conditional visibility/state:

```typescript
interface ConditionalProperties {
  hiddenBy?: string;                    // Attribute controlling visibility
  enabledBy?: string;                   // Attribute controlling enabled state
  requiredBy?: string;                  // Attribute controlling required state
}
```

## Cardinality

### CardinalityType

Relation cardinality:

```typescript
type CardinalityType = 'one' | 'many';
```

## Container Types

### PageContainerType

Page container types:

```typescript
type PageContainerType = 'form' | 'view' | 'table';
```

### DialogSize

Dialog size options:

```typescript
type DialogSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';
```

## Visual Element Types

### VisualElementType

All visual element type identifiers:

```typescript
type VisualElementType =
  // Inputs
  | 'textInput'
  | 'numericInput'
  | 'dateInput'
  | 'dateTimeInput'
  | 'timeInput'
  | 'textArea'
  | 'checkbox'
  | 'enumerationCombo'
  | 'enumerationRadio'
  | 'binaryTypeInput'
  // Containers
  | 'flex'
  | 'tabController'
  | 'card'
  // Tables
  | 'table'
  // Navigation
  | 'link'
  | 'button'
  // Display
  | 'text'
  | 'label'
  | 'divider'
  | 'spacer'
  | 'image'
  | 'icon'
  | 'chip'
  | 'progress'
  | 'alert';
```

## Action Types

### ActionType

All action type identifiers:

```typescript
type ActionType =
  // CRUD
  | 'refresh'
  | 'create'
  | 'update'
  | 'delete'
  // Relations
  | 'add'
  | 'remove'
  | 'set'
  | 'unset'
  // Navigation
  | 'openPage'
  | 'openForm'
  | 'openSelector'
  | 'back'
  | 'close'
  // Operations
  | 'callOperation'
  // Export
  | 'export'
  // Custom
  | string;
```

### OperationKind

Operation execution context:

```typescript
type OperationKind = 'static' | 'instance';
```

## Button Types

### ButtonStyle

Button visual styles:

```typescript
type ButtonStyle = 'text' | 'outlined' | 'contained' | 'icon' | 'fab';
```

## Validation Types

### ValidationRuleType

Validation rule types:

```typescript
type ValidationRuleType =
  | 'required'
  | 'minValue'
  | 'maxValue'
  | 'minLength'
  | 'maxLength'
  | 'pattern'
  | 'minValueBy'
  | 'maxValueBy'
  | 'custom';
```

### SeverityType

Message severity levels:

```typescript
type SeverityType = 'error' | 'warning' | 'info' | 'success';
```

## Query & Filtering

### QueryCustomizer

Query parameters for data fetching:

```typescript
interface QueryCustomizer {
  _seek?: {
    page?: number;
    limit?: number;
  };
  _orderBy?: Array<{
    attribute: string;
    descending?: boolean;
  }>;
  _expand?: string[];                   // Relations to eager load
  _mask?: string[];                     // Attributes to include
  [filterKey: string]: any;             // Filter values
}
```

### FilterType

Filter UI types:

```typescript
type FilterType = 'text' | 'select' | 'dateRange' | 'numericRange' | 'boolean';
```

### ColumnAlignment

Table column alignment:

```typescript
type ColumnAlignment = 'left' | 'center' | 'right';
```

## Behavior Types

### BehaviorConfig

Behavior configuration:

```typescript
interface BehaviorConfig {
  isCreatable?: boolean | string;       // Boolean or condition expression
  isUpdateable?: boolean | string;
  isDeletable?: boolean | string;
  isReadable?: boolean | string;
  isCallable?: boolean | string;
}
```

## Internationalization

### I18nKey

Internationalization key pattern:

```typescript
type I18nKey = string;  // Pattern: 'judo.{context}.{name}'
```

### LocaleModel

Locale configuration:

```typescript
interface LocaleModel {
  code: string;                         // e.g., 'en-US', 'hu-HU'
  label: string;                        // Display name
  translations: Record<string, string>; // Key-value translations
}
```

## Type Guards

Helper functions for runtime type checking:

```typescript
// Check if type is numeric
function isNumericType(type: DataType): boolean {
  return ['integer', 'long', 'decimal', 'double'].includes(type);
}

// Check if type is temporal
function isTemporalType(type: DataType): boolean {
  return ['date', 'time', 'dateTime', 'timestamp'].includes(type);
}

// Check if type is string-like
function isStringType(type: DataType): boolean {
  return ['string', 'text'].includes(type);
}

// Check if element is input
function isInputElement(type: VisualElementType): boolean {
  return [
    'textInput', 'numericInput', 'dateInput', 'dateTimeInput',
    'timeInput', 'textArea', 'checkbox', 'enumerationCombo',
    'enumerationRadio', 'binaryTypeInput'
  ].includes(type);
}

// Check if element is container
function isContainerElement(type: VisualElementType): boolean {
  return ['flex', 'tabController', 'card'].includes(type);
}
```

## Type Mapping Utilities

### Type Conversion

```typescript
// Map DataType to TypeScript type
function getTypeScriptType(dataType: DataType): TypeScriptType {
  const mapping: Record<string, TypeScriptType> = {
    'string': 'string',
    'text': 'string',
    'integer': 'number',
    'long': 'number',
    'decimal': 'number',
    'double': 'number',
    'date': 'Date',
    'time': 'string',
    'dateTime': 'Date',
    'timestamp': 'number',
    'boolean': 'boolean',
    'binary': 'Blob'
  };
  
  return mapping[dataType] || dataType;  // Default to enum name
}

// Map DataType to input component
function getInputComponent(dataType: DataType): VisualElementType {
  const mapping: Record<string, VisualElementType> = {
    'string': 'textInput',
    'text': 'textArea',
    'integer': 'numericInput',
    'long': 'numericInput',
    'decimal': 'numericInput',
    'double': 'numericInput',
    'date': 'dateInput',
    'time': 'timeInput',
    'dateTime': 'dateTimeInput',
    'timestamp': 'dateTimeInput',
    'boolean': 'checkbox',
    'binary': 'binaryTypeInput'
  };
  
  return mapping[dataType] || 'enumerationCombo';  // Default to enum combo
}
```

## Constants

### Default Values

```typescript
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZE_OPTIONS = [10, 25, 50, 100];
export const DEFAULT_COL_SPAN = 12;
export const MAX_COL_SPAN = 12;
export const DEFAULT_DIALOG_SIZE: DialogSize = 'md';
export const DEFAULT_BUTTON_STYLE: ButtonStyle = 'text';
export const DEFAULT_FEATURED_ACTIONS = 3;
```

## Utility Types

### Generic Model Collection

```typescript
type ModelCollection<T extends BaseModel> = T[];

interface ModelMap<T extends BaseModel> {
  [key: string]: T;
}
```

### Partial Updates

```typescript
type PartialModel<T> = {
  [P in keyof T]?: T[P];
};
```

### Deep Partial

```typescript
type DeepPartial<T> = {
  [P in keyof T]?: T[P] extends object ? DeepPartial<T[P]> : T[P];
};
```

## Error Types

### ModelError

Errors during model processing:

```typescript
interface ModelError {
  code: string;                         // Error code
  message: string;                      // Human-readable message
  field?: string;                       // Field name if field-specific
  severity: SeverityType;               // Error severity
  context?: any;                        // Additional context
}

type ModelErrors = ModelError[];
```

## Examples

### Complete Type Usage

```typescript
// Page model using core types
const pageModel: BaseModel & {
  type: PageContainerType;
  dialogSize: DialogSize;
  icon?: IconModel;
} = {
  id: 'page-user-form',
  name: 'UserForm',
  fqn: 'app.pages.UserForm',
  sourceId: '_xmiid_12345',
  type: 'form',
  dialogSize: 'md',
  icon: {
    name: 'person',
    color: 'primary'
  },
  annotations: {
    customTheme: 'light'
  }
};

// Visual element using core types
const inputModel: BaseModel & GridPosition & ConditionalProperties = {
  id: 've-firstName',
  name: 'firstName',
  col: 6,
  row: 0,
  hiddenBy: 'isArchived',
  enabledBy: 'isEditable'
};
```

## Related Specifications

All runtime model specifications depend on these core types:
- `02-page-model.md` - Uses BaseModel, IconModel, DialogSize
- `03-container-model.md` - Uses BaseModel, PageContainerType
- `04-visual-element-model.md` - Uses BaseModel, VisualElementType, GridPosition
- `05-action-model.md` - Uses BaseModel, ActionType
- `06-button-model.md` - Uses BaseModel, ButtonStyle
- `07-table-model.md` - Uses BaseModel, FilterType, ColumnAlignment
- `08-validation-model.md` - Uses ValidationRuleType, SeverityType
- `09-i18n-model.md` - Uses I18nKey, LocaleModel

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

