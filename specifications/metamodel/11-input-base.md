# Input Base Specification

**Domain:** Metamodel  
**Ecore Class:** `ui::Input` (abstract)  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `02-visual-element.md`, `03-attribute-type.md`  
**Blocks:** All input type specifications  

## Overview

`Input` is the abstract base class for all form input elements. It provides common properties for data-bound input fields including attribute binding, validation, read-only state, and change handlers. All concrete input types (TextInput, NumericInput, DateInput, etc.) extend this class.

## Metamodel Definition

### Class Hierarchy

```
NamedElement (abstract)
└─ VisualElement (abstract)
   └─ Input (abstract)
      ├─ TextInput
      ├─ NumericInput
      ├─ DateInput
      ├─ DateTimeInput
      ├─ TimeInput
      ├─ TextArea
      ├─ Checkbox
      ├─ EnumerationCombo
      ├─ EnumerationRadio
      └─ BinaryTypeInput
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Input field name (inherited) |
| `sourceId` | EString | 0..1 | - | XMIID from source (inherited) |
| `attributeType` | AttributeType | 1 | - | Bound data attribute |
| `readOnly` | EBoolean | 0..1 | false | Whether field is read-only |
| `onBlur` | EBoolean | 0..1 | false | Whether to trigger validation on blur |

### Inherited Properties

From VisualElement:
- `col`, `row` - Grid positioning
- `label` - Display label
- `icon` - Display icon
- `hiddenBy`, `enabledBy`, `requiredBy` - Conditional rendering
- `size` - Fixed dimensions
- `stretch`, `fit` - Layout behavior
- `subTheme` - Theme variant

### Relationships

**Contains:**
- Inherited from VisualElement

**Referenced By:**
- Container children
- Form fields

**References:**
- `attributeType: AttributeType` - The data attribute this input binds to

## Key Concepts

### Attribute Binding

Every input binds to an AttributeType:
```xml
<children xsi:type="ui:TextInput" 
          name="firstName" 
          attributeType="User#firstName"/>
```

This binding determines:
- **Data type:** String, number, date, etc.
- **Validation:** Required, min/max, pattern
- **Default value:** Initial input value
- **Label:** If not explicitly set

### Data Flow

**Two-way data binding:**
1. **Display:** `value = data[attributeType.name]`
2. **Change:** `data[attributeType.name] = newValue`
3. **Validation:** Apply attribute constraints
4. **Submit:** Include in form data

### Read-Only State

**ReadOnly from Attribute:**
```xml
<attributes name="createdAt" type="TIMESTAMP" isReadOnly="true"/>
```

**ReadOnly from Input:**
```xml
<children xsi:type="ui:TextInput" 
          name="email" 
          attributeType="User#email"
          readOnly="true"/>
```

**Runtime Determination:**
```typescript
const isReadOnly = input.readOnly || 
                   attribute.isReadOnly || 
                   !behaviors.canUpdate ||
                   formMode === 'view';
```

### Validation

Inputs inherit validation from AttributeType:
```xml
<attributes name="email" 
            type="STRING" 
            required="true"
            pattern="^[^@]+@[^@]+\.[^@]+$"/>
```

Input applies these rules automatically.

### On Blur Behavior

When `onBlur=true`:
- Validation triggers when field loses focus
- Immediate feedback for invalid input
- Better UX for complex validation

When `onBlur=false` (default):
- Validation on form submit only
- Less intrusive during typing

## Examples from Sample Model

### Example 1: Basic Text Input

```xml
<children xsi:type="ui:TextInput" 
          name="firstName" 
          attributeType="User#firstName"
          col="6"/>
```

**Generated:**
```typescript
<TextField
  name="firstName"
  label={t('fields.firstName')}
  value={data.firstName || ''}
  onChange={(e) => handleChange('firstName', e.target.value)}
  required={attribute.required}
  disabled={isReadOnly}
/>
```

### Example 2: Read-Only Input

```xml
<children xsi:type="ui:TextInput" 
          name="id" 
          attributeType="User#id"
          readOnly="true"
          col="6"/>
```

### Example 3: Input with On Blur Validation

```xml
<children xsi:type="ui:TextInput" 
          name="email" 
          attributeType="User#email"
          onBlur="true"
          col="12"/>
```

**Generated:**
```typescript
<TextField
  name="email"
  value={data.email}
  onChange={handleChange}
  onBlur={() => validateField('email')}
  error={!!errors.email}
  helperText={errors.email}
/>
```

### Example 4: Numeric Input

```xml
<children xsi:type="ui:NumericInput" 
          name="age" 
          attributeType="User#age"
          col="6"/>
```

**Inherited behavior from Input base:**
- Binds to User#age attribute
- Gets min/max from attribute constraints
- Integer validation automatic

### Example 5: Date Input

```xml
<children xsi:type="ui:DateInput" 
          name="birthDate" 
          attributeType="User#birthDate"
          col="6"/>
```

### Example 6: Conditional Input

```xml
<children xsi:type="ui:TextInput" 
          name="ssn" 
          attributeType="User#ssn"
          hiddenBy="User#isForeign"
          requiredBy="User#needsSSN"
          col="12"/>
```

**Runtime behavior:**
```typescript
const hidden = data.isForeign === true;
const required = data.needsSSN === true;
```

## Validation Rules

### Required Properties
- `name` - Must be unique within container
- `attributeType` - Must reference valid AttributeType

### Constraints
- AttributeType must exist in dataElement's ClassType
- Input type must match attribute data type
- Read-only inputs should not be required
- Cannot bind to relation (use Link instead)

### Type Compatibility

| AttributeType | Compatible Inputs |
|---------------|-------------------|
| STRING | TextInput, TextArea |
| INTEGER, LONG, DECIMAL, DOUBLE | NumericInput |
| DATE | DateInput |
| DATE_TIME, TIMESTAMP | DateTimeInput |
| TIME | TimeInput |
| BOOLEAN | Checkbox |
| EnumerationType | EnumerationCombo, EnumerationRadio |
| BINARY | BinaryTypeInput |

## Runtime Model Mapping

```typescript
interface InputModel extends VisualElementModel {
  // Input-specific
  attributeName: string;           // attributeType.name
  attributeType: string;           // attributeType.type
  attributeFqn: string;            // attributeType FQN
  
  // State
  readOnly: boolean;               // readOnly || attribute.isReadOnly
  required: boolean;               // attribute.required || requiredBy evaluation
  
  // Validation
  validation: ValidationRules;     // From attribute
  onBlur: boolean;                 // onBlur
  
  // Value
  defaultValue?: any;              // attribute.defaultValue
  
  // Inherited from VisualElement
  col: number;
  label?: string;
  icon?: IconModel;
  hiddenBy?: string;
  enabledBy?: string;
  requiredBy?: string;
}

interface ValidationRules {
  required?: boolean;
  minLength?: number;
  maxLength?: number;
  minValue?: number;
  maxValue?: number;
  pattern?: string;
  minValueBy?: string;
  maxValueBy?: string;
}
```

## Generator Implementation

See `generators/04-visual-element-extractor.md`.

**Key Methods:**
```java
public class InputGenerator {
    public static InputModel extractInput(Input input);
    public static String getAttributeName(Input input);
    public static AttributeType getAttribute(Input input);
    public static String getInputType(Input input);
    public static ValidationRules extractValidation(Input input);
    public static boolean isReadOnly(Input input, FormMode mode);
}
```

## Common Patterns

### Input Rendering

```typescript
function renderInput(input: InputModel, data: any, onChange: Function) {
  const Component = getInputComponent(input.attributeType);
  
  return (
    <Component
      name={input.attributeName}
      label={input.label}
      value={data[input.attributeName]}
      onChange={(value) => onChange(input.attributeName, value)}
      disabled={input.readOnly}
      required={input.required}
      error={errors[input.attributeName]}
      {...input.validation}
    />
  );
}
```

### Two-Way Binding

```typescript
const [data, setData] = useState<Entity>({});

const handleChange = (field: string, value: any) => {
  setData(prev => ({
    ...prev,
    [field]: value
  }));
};
```

### Validation Integration

```typescript
const validateInput = (input: InputModel, value: any): string | null => {
  const rules = input.validation;
  
  if (rules.required && !value) {
    return t('validation.required');
  }
  
  if (rules.pattern && !new RegExp(rules.pattern).test(value)) {
    return t('validation.pattern');
  }
  
  // ... more validation
  
  return null;
};
```

## Testing Criteria

### Unit Tests
- [x] Input extraction from metamodel
- [x] Attribute binding resolution
- [x] Validation rule extraction
- [x] Read-only determination
- [x] Default value handling

### Integration Tests
- [x] Input displays correct value
- [x] Changes update data
- [x] Validation triggered correctly
- [x] Read-only inputs disabled
- [x] Required inputs enforce validation
- [x] Conditional inputs show/hide

### Edge Cases
- [x] Missing attribute reference
- [x] Type mismatch (wrong input for attribute)
- [x] Null/undefined values
- [x] Very long input values
- [x] Special characters in input
- [x] Rapid value changes

## Related Specifications

**Metamodel:**
- `02-visual-element.md` - Base class
- `03-labeled-element.md` - Label support

**Data Model:**
- `03-attribute-type.md` - Attribute binding

**Runtime Model:**
- `runtime-model/04-visual-element-model.md` - InputModel interface

**Visual Elements:**
- `visual-elements/inputs/01-text-input.md` - Text inputs
- `visual-elements/inputs/02-numeric-input.md` - Numeric inputs
- `visual-elements/inputs/03-date-input.md` - Date inputs
- `visual-elements/inputs/07-checkbox.md` - Checkbox
- `visual-elements/inputs/08-enumeration-combo.md` - Enum combo

**Validation:**
- `validation/02-field-validators.md` - Field validation

**Components:**
- `components/04-field-state-management.md` - Input state

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

