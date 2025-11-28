# AttributeType Specification

**Domain:** Data Model  
**Ecore Class:** `data::AttributeType`  
**Status:** Complete  
**Assigned:** AI Agent  
**Dependencies:** `01-class-type.md`, `06-data-types.md`  
**Blocks:** Input visual elements, validation, runtime models  

## Overview

`AttributeType` defines properties/fields of ClassTypes. It specifies data types, constraints, defaults, and behaviors for entity attributes. AttributeTypes are the foundation for input fields, validation rules, and data binding in the UI.

## Metamodel Definition

### Class Hierarchy

```
NamedElement
└─ DataElement (abstract)
   └─ AttributeType
```

### Properties

| Name | Type | Multiplicity | Default | Description |
|------|------|--------------|---------|-------------|
| `name` | EString | 1 | - | Attribute name (e.g., "firstName", "email") |
| `sourceId` | EString | 0..1 | - | XMIID from source |
| `annotations` | Annotation | 0..* | - | Custom metadata |
| `type` | DataType | 1 | - | Primitive or enum type |
| `identifier` | EBoolean | 0..1 | false | Whether this is the ID field |
| `required` | EBoolean | 0..1 | false | Whether value is mandatory |
| `isReadOnly` | EBoolean | 0..1 | false | Whether field is read-only |
| `defaultValue` | EString | 0..1 | - | Default value expression |

### Constraints

| Name | Type | Description |
|------|------|-------------|
| `minValue` | EString | Minimum numeric value |
| `maxValue` | EString | Maximum numeric value |
| `minLength` | EInt | Minimum string length |
| `maxLength` | EInt | Maximum string length |
| `pattern` | EString | Regex validation pattern |
| `minValueBy` | AttributeType | Dynamic min from another field |
| `maxValueBy` | AttributeType | Dynamic max from another field |

### Relationships

**Contains:**
- Inherited from NamedElement

**Referenced By:**
- `ClassType.attributes` - Owning class
- `Input.attributeType` - Input fields bound to attribute
- `Validation rules` - Field validation
- `VisualElement.hiddenBy/enabledBy/requiredBy` - Conditional rendering

**References:**
- `type: DataType` - The attribute's data type
- `minValueBy: AttributeType` - Dynamic constraint source
- `maxValueBy: AttributeType` - Dynamic constraint source

## Key Concepts

### Data Types

See `06-data-types.md` for complete list:
- **String types:** STRING, TEXT
- **Numeric types:** INTEGER, LONG, DECIMAL, DOUBLE
- **Temporal types:** DATE, TIME, DATE_TIME, TIMESTAMP
- **Boolean:** BOOLEAN
- **Binary:** BINARY
- **Enum:** EnumerationType reference

### Identifier Attributes

Every entity must have exactly one identifier:
```xml
<attributes name="id" type="STRING" identifier="true"/>
```

Properties:
- Unique within entity type
- Typically auto-generated
- Read-only in most cases
- Used for entity lookup

### Required vs Optional

```typescript
// Required attribute
interface User {
  email: string;  // Not null
}

// Optional attribute
interface User {
  middleName?: string;  // Nullable
}
```

### Read-Only Attributes

```xml
<attributes name="createdAt" type="TIMESTAMP" isReadOnly="true"/>
<attributes name="version" type="INTEGER" isReadOnly="true"/>
```

Generated fields are disabled in forms.

### Validation Constraints

**Static Constraints:**
```xml
<attributes name="age" type="INTEGER" minValue="0" maxValue="150"/>
<attributes name="email" type="STRING" pattern="^[^@]+@[^@]+\.[^@]+$"/>
<attributes name="username" type="STRING" minLength="3" maxLength="20"/>
```

**Dynamic Constraints:**
```xml
<attributes name="startDate" type="DATE"/>
<attributes name="endDate" type="DATE" minValueBy="startDate"/>
```

## Examples from Sample Model

### Example 1: Basic String Attribute

```xml
<attributes name="firstName" 
            type="STRING" 
            required="true"
            maxLength="50"/>
```

**Generated TypeScript:**
```typescript
interface User {
  firstName: string;  // Required
}

// Validation
{
  firstName: {
    required: true,
    maxLength: 50
  }
}
```

### Example 2: Identifier Attribute

```xml
<attributes name="id" 
            type="STRING" 
            identifier="true"
            isReadOnly="true"/>
```

### Example 3: Numeric with Constraints

```xml
<attributes name="salary" 
            type="DECIMAL" 
            required="false"
            minValue="0"
            maxValue="999999.99"/>
```

**Generated Input:**
```typescript
<NumericInput
  name="salary"
  label={t('fields.salary')}
  value={data.salary}
  onChange={handleChange}
  min={0}
  max={999999.99}
  decimalPlaces={2}
/>
```

### Example 4: Date Range

```xml
<attributes name="birthDate" 
            type="DATE" 
            required="true"
            maxValue="TODAY"/>

<attributes name="hireDate" 
            type="DATE" 
            minValueBy="birthDate"/>
```

### Example 5: Enum Attribute

```xml
<attributes name="status" 
            type="UserStatus"
            required="true"
            defaultValue="ACTIVE"/>
```

**Generated Input:**
```typescript
<EnumerationCombo
  name="status"
  label={t('fields.status')}
  value={data.status}
  onChange={handleChange}
  options={UserStatusEnum}
  required={true}
/>
```

### Example 6: Boolean with Default

```xml
<attributes name="active" 
            type="BOOLEAN" 
            required="true"
            defaultValue="true"/>
```

### Example 7: Pattern Validation

```xml
<attributes name="phoneNumber" 
            type="STRING" 
            pattern="^\+?[1-9]\d{1,14}$"
            maxLength="15"/>
```

### Example 8: Read-Only Calculated Field

```xml
<attributes name="fullName" 
            type="STRING" 
            isReadOnly="true">
  <annotations key="calculated" value="firstName + ' ' + lastName"/>
</attributes>
```

## Validation Rules

### Required Properties
- `name` - Must be unique within ClassType
- `type` - Must specify valid DataType

### Constraints
- Only one identifier per ClassType
- Identifier should be read-only
- MinValue < MaxValue for numeric types
- MinLength < MaxLength for string types
- Pattern must be valid regex
- DefaultValue must match type
- MinValueBy/MaxValueBy must reference compatible types

### Naming Conventions
- Use camelCase (e.g., `firstName`, `isActive`)
- Boolean attributes: prefix with `is`, `has`, `can`
- Dates: suffix with `Date`, `Time`, or `At`
- IDs: typically `id` or meaningful name with `Id` suffix

## Runtime Model Mapping

```typescript
interface AttributeTypeModel {
  name: string;                    // name
  fqn: string;                     // ClassName.attributeName
  type: DataType;                  // type (mapped to TS type)
  
  // Characteristics
  identifier: boolean;             // identifier
  required: boolean;               // required
  readOnly: boolean;               // isReadOnly
  defaultValue?: string | number | boolean;  // defaultValue
  
  // Constraints
  minValue?: number | string;      // minValue
  maxValue?: number | string;      // maxValue
  minLength?: number;              // minLength
  maxLength?: number;              // maxLength
  pattern?: string;                // pattern
  minValueBy?: string;             // minValueBy.name
  maxValueBy?: string;             // maxValueBy.name
  
  // UI hints
  inputType?: string;              // Generated from type
  
  // Metadata
  sourceId?: string;
  annotations?: Record<string, string>;
}

// TypeScript interface property
interface User {
  firstName: string;               // String, required
  middleName?: string;             // String, optional
  age: number;                     // Integer, required
  active?: boolean;                // Boolean, optional
  birthDate: Date;                 // Date, required
  status: UserStatus;              // Enum, required
}
```

## Generator Implementation

See `generators/04-visual-element-extractor.md`.

**Key Methods:**
```java
public class AttributeTypeGenerator {
    public static AttributeTypeModel extractAttributeModel(AttributeType attr);
    public static String getTypeScriptType(AttributeType attr);
    public static String getInputType(AttributeType attr);
    public static boolean isNumericType(AttributeType attr);
    public static boolean isTemporalType(AttributeType attr);
    public static boolean isEnumType(AttributeType attr);
    public static ValidationRules extractValidationRules(AttributeType attr);
}
```

## Common Patterns

### Type Mapping

```typescript
// Data type to TypeScript
STRING → string
INTEGER → number
DECIMAL → number
BOOLEAN → boolean
DATE → Date
DATE_TIME → Date
EnumerationType → enum

// Data type to input component
STRING → TextInput
INTEGER → NumericInput
DECIMAL → NumericInput
BOOLEAN → Checkbox
DATE → DateInput
DATE_TIME → DateTimeInput
EnumerationType → EnumerationCombo
```

### Validation Generation

```typescript
const validationSchema = yup.object({
  firstName: yup.string()
    .required('First name is required')
    .max(50, 'Maximum 50 characters'),
  
  age: yup.number()
    .required('Age is required')
    .min(0, 'Age must be positive')
    .max(150, 'Invalid age'),
  
  email: yup.string()
    .required('Email is required')
    .matches(/^[^@]+@[^@]+\.[^@]+$/, 'Invalid email format'),
  
  endDate: yup.date()
    .min(yup.ref('startDate'), 'End date must be after start date')
});
```

### Conditional Fields

```typescript
// Attribute used for conditional rendering
<TextInput
  name="ssn"
  hiddenBy="isForeign"  // References AttributeType
  disabled={data.isForeign === true}
/>
```

## Testing Criteria

### Unit Tests
- [x] Attribute extraction from metamodel
- [x] Type mapping to TypeScript
- [x] Input type determination
- [x] Validation rule generation
- [x] Default value handling
- [x] Identifier detection

### Integration Tests
- [x] Input fields generated correctly
- [x] Validation rules applied
- [x] Required fields enforced
- [x] Read-only fields disabled
- [x] Dynamic constraints work
- [x] Default values set

### Edge Cases
- [x] Multiple identifiers (should error)
- [x] Invalid default value type
- [x] Circular dynamic constraints
- [x] Pattern syntax errors
- [x] MinValue > MaxValue (should error)
- [x] Unsupported data type

## Related Specifications

**Metamodel:**
- `02-visual-element.md` - Conditional rendering attributes
- `11-input-base.md` - Input fields bound to attributes

**Data Model:**
- `01-class-type.md` - Contains attributes
- `06-data-types.md` - Attribute types
- `05-enumeration-type.md` - Enum attributes

**Runtime Model:**
- `runtime-model/01-core-types.md` - AttributeTypeModel
- `runtime-model/08-validation-model.md` - Validation rules

**Visual Elements:**
- `visual-elements/inputs/*` - All input types

**Validation:**
- `validation/01-validation-types.md` - Validation system
- `validation/02-field-validators.md` - Field-level validation

---

**Status:** ✅ Complete  
**Created:** 2025-11-28  
**Last Updated:** 2025-11-28  
**Reviewed:** Not yet reviewed

