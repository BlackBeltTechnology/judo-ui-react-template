# VisualElementModel Specification

**Domain:** Runtime Model  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-core-types.md`, `metamodel/02-visual-element.md`  
**Blocks:** Visual element components, element registry  

## Overview

`VisualElementModel` is the base runtime model for all UI elements including inputs, containers, tables, links, and display elements. It provides common properties for layout, conditional rendering, and data binding.

## TypeScript Interfaces

### Base VisualElementModel

```typescript
interface VisualElementModel extends BaseModel {
  // Identification
  id: string;
  name: string;
  type: VisualElementType;
  
  // Grid Layout
  col: number;                          // Column span (1-12)
  row?: number;                         // Row position
  
  // Sizing
  size?: SizeModel;
  stretch: StretchType;
  fit: FitType;
  
  // Display
  label?: string;
  icon?: IconModel;
  
  // Conditional Rendering
  hiddenBy?: string;                    // Attribute for visibility
  enabledBy?: string;                   // Attribute for enabled state
  requiredBy?: string;                  // Attribute for required state
  
  // Styling
  subTheme?: string;
  autoFocus?: boolean;
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}
```

### InputModel

```typescript
interface InputModel extends VisualElementModel {
  type: 'textInput' | 'numericInput' | 'dateInput' | 'dateTimeInput' | 
        'timeInput' | 'textArea' | 'checkbox' | 'enumerationCombo' | 
        'enumerationRadio' | 'binaryTypeInput';
  
  // Data Binding
  attributeName: string;                // Bound attribute name
  attributeType: DataType;              // Attribute data type
  attributeFqn: string;                 // Fully qualified attribute name
  
  // State
  readOnly: boolean;
  required: boolean;
  
  // Validation
  validation: ValidationRules;
  onBlur: boolean;                      // Validate on blur
  
  // Value
  defaultValue?: any;
}

interface ValidationRules {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  minValue?: number | string;
  maxValue?: number | string;
  pattern?: string;
  minValueBy?: string;
  maxValueBy?: string;
}
```

### LinkModel

```typescript
interface LinkModel extends VisualElementModel {
  type: 'link';
  
  // Relation
  relationName: string;
  relationFqn: string;
  targetType: string;                   // Target ClassType name
  cardinality: CardinalityType;
  
  // Navigation
  targetPageName?: string;
  targetPagePath?: string;
  
  // Display
  displayAttributeName?: string;        // Attribute to show as link text
}
```

### ButtonModel

(See `06-button-model.md` for full specification)

```typescript
interface ButtonModel extends VisualElementModel {
  type: 'button';
  actionName: string;
  actionType: ActionType;
  buttonStyle: ButtonStyle;
  tooltipText?: string;
}
```

### TableModel

(See `07-table-model.md` for full specification)

```typescript
interface TableModel extends VisualElementModel {
  type: 'table';
  dataElement: string;
  columns: ColumnModel[];
  rowActions: ActionModel[];
  filters: FilterModel[];
  enableFiltering: boolean;
  enableSorting: boolean;
  enablePagination: boolean;
  enableSelection: boolean;
}
```

### ContainerModel

(See `03-container-model.md` for full specification)

```typescript
interface ContainerModel extends VisualElementModel {
  type: 'flex' | 'tabController' | 'card';
  children: VisualElementModel[];
  layout?: LayoutModel;
}
```

### DisplayElementModel

```typescript
interface DisplayElementModel extends VisualElementModel {
  type: 'text' | 'label' | 'divider' | 'spacer' | 'image' | 
        'icon' | 'chip' | 'progress' | 'alert';
  // Type-specific properties
  [key: string]: any;
}
```

## Type Discriminated Unions

```typescript
type AnyVisualElementModel =
  | InputModel
  | LinkModel
  | ButtonModel
  | TableModel
  | ContainerModel
  | DisplayElementModel;

// Type guard
function isInputModel(element: VisualElementModel): element is InputModel {
  return ['textInput', 'numericInput', 'dateInput', 'dateTimeInput',
          'timeInput', 'textArea', 'checkbox', 'enumerationCombo',
          'enumerationRadio', 'binaryTypeInput'].includes(element.type);
}

function isContainerModel(element: VisualElementModel): element is ContainerModel {
  return ['flex', 'tabController', 'card'].includes(element.type);
}

function isTableModel(element: VisualElementModel): element is TableModel {
  return element.type === 'table';
}
```

## Examples

### Example 1: Text Input

```typescript
const firstNameInput: InputModel = {
  id: 've-firstName',
  name: 'firstName',
  type: 'textInput',
  
  // Layout
  col: 6,
  row: 0,
  stretch: 'horizontal',
  fit: 'loose',
  
  // Display
  label: 'First Name',
  
  // Data Binding
  attributeName: 'firstName',
  attributeType: 'string',
  attributeFqn: 'User.firstName',
  
  // State
  readOnly: false,
  required: true,
  autoFocus: true,
  
  // Validation
  validation: {
    required: true,
    maxLength: 50
  },
  onBlur: false
};
```

### Example 2: Numeric Input with Constraints

```typescript
const ageInput: InputModel = {
  id: 've-age',
  name: 'age',
  type: 'numericInput',
  
  col: 6,
  
  label: 'Age',
  
  attributeName: 'age',
  attributeType: 'integer',
  attributeFqn: 'User.age',
  
  readOnly: false,
  required: false,
  
  validation: {
    minValue: 0,
    maxValue: 150
  }
};
```

### Example 3: Conditional Input

```typescript
const ssnInput: InputModel = {
  id: 've-ssn',
  name: 'ssn',
  type: 'textInput',
  
  col: 12,
  
  label: 'SSN',
  
  attributeName: 'ssn',
  attributeType: 'string',
  attributeFqn: 'User.ssn',
  
  readOnly: false,
  required: false,
  
  // Conditional Rendering
  hiddenBy: 'isForeign',                // Hidden when user is foreign
  requiredBy: 'needsSSN',               // Required when needsSSN is true
  
  validation: {
    pattern: '^\\d{3}-\\d{2}-\\d{4}$',
    maxLength: 11
  }
};
```

### Example 4: Enum Combo

```typescript
const statusCombo: InputModel = {
  id: 've-status',
  name: 'status',
  type: 'enumerationCombo',
  
  col: 6,
  
  label: 'Status',
  icon: { name: 'info' },
  
  attributeName: 'status',
  attributeType: 'UserStatus',          // Enum type
  attributeFqn: 'User.status',
  
  readOnly: false,
  required: true,
  
  validation: {
    required: true
  },
  
  defaultValue: 'ACTIVE'
};
```

### Example 5: Link Element

```typescript
const postsLink: LinkModel = {
  id: 've-posts',
  name: 'posts',
  type: 'link',
  
  col: 12,
  
  label: 'View Posts',
  icon: { name: 'article' },
  
  // Relation
  relationName: 'posts',
  relationFqn: 'User.posts',
  targetType: 'Post',
  cardinality: 'many',
  
  // Navigation
  targetPageName: 'PostList',
  targetPagePath: '/pages/User/:id/posts',
  
  // Display
  displayAttributeName: undefined       // Use label
};
```

### Example 6: Read-Only Display

```typescript
const idDisplay: InputModel = {
  id: 've-id',
  name: 'id',
  type: 'textInput',
  
  col: 6,
  
  label: 'ID',
  
  attributeName: 'id',
  attributeType: 'string',
  attributeFqn: 'User.id',
  
  readOnly: true,                       // Always read-only
  required: false,
  
  validation: {}
};
```

### Example 7: Date Range

```typescript
const startDateInput: InputModel = {
  id: 've-startDate',
  name: 'startDate',
  type: 'dateInput',
  
  col: 6,
  
  label: 'Start Date',
  
  attributeName: 'startDate',
  attributeType: 'date',
  attributeFqn: 'Event.startDate',
  
  readOnly: false,
  required: true,
  
  validation: {
    required: true
  }
};

const endDateInput: InputModel = {
  id: 've-endDate',
  name: 'endDate',
  type: 'dateInput',
  
  col: 6,
  
  label: 'End Date',
  
  attributeName: 'endDate',
  attributeType: 'date',
  attributeFqn: 'Event.endDate',
  
  readOnly: false,
  required: true,
  
  validation: {
    required: true,
    minValueBy: 'startDate'             // Must be >= startDate
  }
};
```

## Element Registry

Visual elements are rendered through a registry pattern:

```typescript
const elementRegistry: Record<VisualElementType, React.ComponentType<any>> = {
  textInput: TextInputComponent,
  numericInput: NumericInputComponent,
  dateInput: DateInputComponent,
  // ... all element types
};

function VisualElementRenderer({ element, data }: Props) {
  const Component = elementRegistry[element.type];
  if (!Component) {
    console.error(`Unknown element type: ${element.type}`);
    return null;
  }
  return <Component model={element} data={data} />;
}
```

## Validation Rules

- `id` must be unique within container
- `name` must be unique within container
- `col` must be 1-12
- `type` must be valid VisualElementType
- Input elements must have `attributeName`
- Link elements must have `relationName`
- Conditional attributes must reference boolean attributes

## Related Specifications

**Metamodel:**
- `metamodel/02-visual-element.md` - Source specification
- `metamodel/11-input-base.md` - Input elements
- `metamodel/14-link.md` - Link elements

**Runtime Model:**
- `01-core-types.md` - Base types
- `06-button-model.md` - Button model
- `07-table-model.md` - Table model

**Components:**
- `components/03-visual-element-registry.md` - Element registry

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

